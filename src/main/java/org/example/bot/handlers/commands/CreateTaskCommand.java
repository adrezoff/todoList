package org.example.bot.handlers.commands;

import org.example.models.User;
import org.example.repositories.TaskRepository;
import org.example.repositories.UserRepository;
import org.example.bot.utils.UserState;
import org.example.bot.utils.MessageSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class CreateTaskCommand implements Command {
    @Override
    public String getName() {
        return "/createtask";
    }

    @Override
    public void execute(User user, long chatId,
                        MessageSender messageSender,
                        UserRepository userRepository,
                        TaskRepository taskRepository) throws TelegramApiException {

        messageSender.sendMessage(chatId, "📝 Введите название задачи:");
        user.setState(UserState.AWAITING_TASK_TITLE);
        user.clearTempData();
        userRepository.update(user);
    }
}