package org.example.bot.handlers.commands;

import org.example.bot.utils.KeyboardBuilder;
import org.example.bot.utils.MessageSender;
import org.example.models.Task;
import org.example.models.User;
import org.example.repositories.TaskRepository;
import org.example.repositories.UserRepository;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TasksCommand implements Command {
    @Override
    public String getName() {
        return "/tasks";
    }

    @Override
    public void execute(User user, long chatId,
                        MessageSender messageSender,
                        UserRepository userRepository,
                        TaskRepository taskRepository) throws TelegramApiException {

        List<Task> tasks = taskRepository.findByUserId(user.getId()).stream()
                .sorted(Comparator.comparing(Task::getEndDate))
                .collect(Collectors.toList());

        if (tasks.isEmpty()) {
            messageSender.sendMessage(chatId, "У вас пока нет задач.");
            return;
        }

        messageSender.sendMessage(chatId, "📋 Ваши задачи (отсортировано по ближайшему дедлайну):");

        for (Task task : tasks) {
            if (!task.isCompleted()) {
                String taskMessage = formatTaskMessage(task);
                InlineKeyboardMarkup keyboard = KeyboardBuilder.createTaskKeyboard(task);
                messageSender.sendMessage(chatId, taskMessage, keyboard);
            }
        }
    }

    private String formatTaskMessage(Task task) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        return String.format(
                "<b>%s</b>\n" +
                        "<i>%s</i>\n\n" +
                        "⏳ Начало: %s\n" +
                        "⌛️ Конец: %s",
                escapeHtml(task.getTitle()),
                escapeHtml(task.getDescription()),
                task.getStartDate().format(formatter),
                task.getEndDate().format(formatter)
        );
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}