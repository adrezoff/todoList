package org.example.bot.handlers;

import org.example.bot.utils.DateTimeParser;
import org.example.bot.utils.KeyboardBuilder;
import org.example.bot.utils.MessageSender;
import org.example.bot.utils.UserState;
import org.example.models.Task;
import org.example.models.User;
import org.example.repositories.TaskRepository;
import org.example.repositories.UserRepository;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;

public class CallbackHandler {
    private final MessageSender messageSender;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public CallbackHandler(MessageSender messageSender,
                           UserRepository userRepository,
                           TaskRepository taskRepository) {
        this.messageSender = messageSender;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    public void handle(CallbackQuery callbackQuery) throws TelegramApiException {
        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        long userId = callbackQuery.getFrom().getId();

        User user = userRepository.findById(userId).orElseThrow();

        if (data.startsWith("complete:")) {
            handleCompleteTask(data, chatId, messageId, user);
//        } else if (data.startsWith("edit:")) {
//            handleEditTask(data, chatId, messageId, user);
        } else if (data.startsWith("delete:")) {
            handleDeleteTask(data, chatId, messageId, user);
        } else if (data.startsWith("confirm_task:yes")){
            handleConfirmYes(data, chatId, messageId, user);
        } else if (data.startsWith("confirm_task:no")){
            handleConfirmNo(data, chatId, messageId, user);
        } else if (data.startsWith("time:")){
            handleTime(data, chatId, messageId, user);
        }
    }

    private void handleTime(String data, long chatId, int messageId, User user) throws TelegramApiException {
        String input = data.split(":")[1];

        if (user.getState().equals(UserState.AWAITING_TASK_START_DATE)) {
            try {
                LocalDateTime startDate = DateTimeParser.parse(input);

                ZonedDateTime zonedStartDate = startDate.atZone(user.getTimeZone());
                LocalDateTime utcStartDate = zonedStartDate.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();

                user.putTempData("newTaskStartDate", utcStartDate);
                user.putTempData("originalStartDate", startDate);
                user.setState(UserState.AWAITING_TASK_END_DATE);
                userRepository.update(user);

                messageSender.sendMessage(chatId, "Дата начала: " +
                                DateTimeParser.format(startDate) + " (ваш часовой пояс)\n" +
                                "Введите дату окончания (в том же формате):",
                        KeyboardBuilder.createTimeKeyboard());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } else if (user.getState().equals(UserState.AWAITING_TASK_END_DATE)){
            LocalDateTime endDate = DateTimeParser.parse(input);
            LocalDateTime startDate = (LocalDateTime) user.getTempData("originalStartDate");

            ZonedDateTime zonedEndDate = endDate.atZone(user.getTimeZone());
            LocalDateTime utcEndDate = zonedEndDate.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();

            if (endDate.isBefore(startDate)) {
                messageSender.sendMessage(chatId, "Дата окончания не может быть раньше даты начала. Попробуйте еще раз:");
                return;
            }

            user.putTempData("newTaskEndDate", utcEndDate);
            userRepository.update(user);

            String taskInfo = String.format(
                    "Подтвердите создание задачи:\n\nНазвание: %s\nОписание: %s\nНачало: %s\nОкончание: %s",
                    user.getTempData("newTaskTitle"),
                    user.getTempData("newTaskDescription"),
                    startDate.format(ofPattern("d MMMM yyyy HH:mm").withLocale(new Locale("ru"))),
                    endDate.format(ofPattern("d MMMM yyyy HH:mm").withLocale(new Locale("ru")))
            );

            messageSender.sendMessage(chatId, taskInfo, KeyboardBuilder.createConfirmKeyboard());
        }
    }

    private void handleConfirmYes(String data, long chatId, int messageId, User user) throws TelegramApiException {
        try {
            Task task = new Task();
            task.setUserId(user.getId());
            task.setTitle((String) user.getTempData("newTaskTitle"));
            task.setDescription((String) user.getTempData("newTaskDescription"));
            task.setStartDate((LocalDateTime) user.getTempData("originalStartDate"));
            task.setEndDate((LocalDateTime) user.getTempData("newTaskEndDate"));
            task.setCompleted(false);

            taskRepository.save(task);

            user.clearTempData();
            user.setState(UserState.IDLE);
            userRepository.update(user);

            messageSender.deleteMessage(chatId, messageId);
            messageSender.sendMessage(chatId, "✅ Задача успешно создана!");

        } catch (Exception e) {
            messageSender.sendMessage(chatId, "⚠️ Произошла ошибка при создании задачи. Попробуйте еще раз.");
            throw new TelegramApiException("Failed to confirm task creation", e);
        }
    }

    private void handleConfirmNo(String data, long chatId, int messageId, User user) throws TelegramApiException {
        try {
            user.clearTempData();
            user.setState(UserState.IDLE);
            userRepository.update(user);

            messageSender.deleteMessage(chatId, messageId);
            messageSender.sendMessage(chatId, "❌ Создание задачи отменено.");

        } catch (Exception e) {
            messageSender.sendMessage(chatId, "⚠️ Произошла ошибка при отмене создания задачи.");
            throw new TelegramApiException("Failed to cancel task creation", e);
        }
    }

    private void handleCompleteTask(String data, long chatId, int messageId, User user) throws TelegramApiException {
        long taskId = Long.parseLong(data.split(":")[1]);
        taskRepository.markAsCompleted(taskId);
        messageSender.deleteMessage(chatId, messageId);
        messageSender.sendMessage(chatId, "Задача отмечена как выполненная!");
    }

//    private void handleEditTask(String data, long chatId, int messageId, User user) throws TelegramApiException {
//        long taskId = Long.parseLong(data.split(":")[1]);
//
//        // Получаем задачу из репозитория
//        Optional<Task> taskOpt = taskRepository.findById(taskId);
//        if (taskOpt.isEmpty()) {
//            messageSender.sendMessage(chatId, "Задача не найдена!");
//            return;
//        }
//
//        Task task = taskOpt.get();
//
//        if (task.getUserId() != user.getId()) {
//            messageSender.sendMessage(chatId, "Вы не можете редактировать эту задачу!");
//            return;
//        }
//
//        user.setState(UserState.EDITING_TASK);

//        user.setTempDataJson("{\"editingTaskId\":" + taskId + "}");
//        userRepository.update(user);
//
//        String message = "Редактирование задачи:\n" +
//                "Текущий заголовок: " + task.getTitle() + "\n" +
//                "Текущее описание: " + (task.getDescription() != null ? task.getDescription() : "отсутствует") + "\n\n" +
//                "Отправьте новое название задачи или /cancel для отмены";
//
//        messageSender.deleteMessage(chatId, messageId);
//        messageSender.sendMessage(chatId, message);
//    }

    private void handleDeleteTask(String data, long chatId, int messageId, User user) throws TelegramApiException {
        long taskId = Long.parseLong(data.split(":")[1]);
        taskRepository.delete(taskId);
        messageSender.deleteMessage(chatId, messageId);
        messageSender.sendMessage(chatId, "Задача удалена!");
    }
}