package org.example.bot.handlers.commands;

import org.example.bot.utils.MessageSender;
import org.example.models.User;
import org.example.repositories.TaskRepository;
import org.example.repositories.UserRepository;
import org.example.bot.utils.UserState;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class HelpCommand implements Command{

    @Override
    public String getName() {
        return "/help";
    }

    @Override
    public void execute(User user, long chatId,
                        MessageSender messageSender,
                        UserRepository userRepository,
                        TaskRepository taskRepository) throws TelegramApiException {
        messageSender.sendMessage(chatId, "💡Основные команды:\n\n" +
                        "/createtask - Создать новую задачу\n" +
                        "/tasks - Показать мои задачи\n" +
                        "/settimezone - Установить часовой пояс:");
        user.setState(UserState.IDLE);
        user.clearTempData();
        userRepository.update(user);
    }
}
