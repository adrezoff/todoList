package org.example.bot.handlers.dialogs;

import org.example.bot.utils.MessageSender;
import org.example.models.User;
import org.example.repositories.TaskRepository;
import org.example.repositories.UserRepository;
import org.example.bot.utils.UserState;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TaskTitleDialog implements Dialog {
    @Override
    public UserState getSupportedState() {
        return UserState.AWAITING_TASK_TITLE;
    }

    @Override
    public void execute(User user, long chatId, String input,
                        MessageSender messageSender,
                        UserRepository userRepository,
                        TaskRepository taskRepository) throws TelegramApiException {
        user.putTempData("newTaskTitle", input);
        user.setState(UserState.AWAITING_TASK_DESCRIPTION);
        userRepository.update(user);
        messageSender.sendMessage(chatId, "Введите описание задачи:");
    }
}