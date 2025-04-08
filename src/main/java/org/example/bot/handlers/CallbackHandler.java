package org.example.bot.handlers;

import org.example.bot.utils.MessageSender;
import org.example.bot.utils.UserState;
import org.example.models.Task;
import org.example.models.User;
import org.example.repositories.TaskRepository;
import org.example.repositories.UserRepository;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.Optional;

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
        }
    }
    private void handleConfirmYes(String data, long chatId, int messageId, User user) throws TelegramApiException {
        try {
            // Создаем новую задачу на основе временных данных
            Task task = new Task();
            task.setUserId(user.getId());
            task.setTitle((String) user.getTempData("newTaskTitle"));
            task.setDescription((String) user.getTempData("newTaskDescription"));
            task.setStartDate((LocalDateTime) user.getTempData("originalStartDate"));
            task.setEndDate((LocalDateTime) user.getTempData("newTaskEndDate"));
            task.setCompleted(false);

            // Сохраняем задачу в базу данных
            taskRepository.save(task);

            // Очищаем временные данные пользователя
            user.clearTempData();
            user.setState(UserState.IDLE);
            userRepository.update(user);

            // Удаляем сообщение с кнопками и отправляем подтверждение
            messageSender.deleteMessage(chatId, messageId);
            messageSender.sendMessage(chatId, "✅ Задача успешно создана!");

        } catch (Exception e) {
            // В случае ошибки отправляем сообщение об ошибке
            messageSender.sendMessage(chatId, "⚠️ Произошла ошибка при создании задачи. Попробуйте еще раз.");
            throw new TelegramApiException("Failed to confirm task creation", e);
        }
    }

    private void handleConfirmNo(String data, long chatId, int messageId, User user) throws TelegramApiException {
        try {
            // Очищаем временные данные пользователя
            user.clearTempData();
            user.setState(UserState.IDLE);
            userRepository.update(user);

            // Удаляем сообщение с кнопками и отправляем уведомление об отмене
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
//        // Проверяем, что задача принадлежит пользователю
//        if (task.getUserId() != user.getId()) {
//            messageSender.sendMessage(chatId, "Вы не можете редактировать эту задачу!");
//            return;
//        }
//
//        // Переводим пользователя в состояние редактирования
//        user.setState(UserState.EDITING_TASK);
//
//        // Сохраняем ID редактируемой задачи во временных данных
//        user.setTempDataJson("{\"editingTaskId\":" + taskId + "}");
//        userRepository.update(user);
//
//        // Отправляем сообщение с запросом новых данных
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