package org.example.bot.handlers.dialogs;

import org.example.bot.utils.KeyboardBuilder;
import org.example.bot.utils.MessageSender;
import org.example.models.User;
import org.example.repositories.TaskRepository;
import org.example.repositories.UserRepository;
import org.example.bot.utils.UserState;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TaskDescriptionDialog implements Dialog {
    @Override
    public UserState getSupportedState() {
        return UserState.AWAITING_TASK_DESCRIPTION;
    }

    @Override
    public void execute(User user, long chatId, String input,
                        MessageSender messageSender,
                        UserRepository userRepository,
                        TaskRepository taskRepository) throws TelegramApiException {
        user.putTempData("newTaskDescription", input);
        user.setState(UserState.AWAITING_TASK_START_DATE);
        userRepository.update(user);
        messageSender.sendMessage(chatId, "Введите дату начала \n\n" +
                        "Поддерживаемые форматы: \n" +
                        "dd.MM.yyyy HH:mm\n" +
                        "d MMMM yyyy\n" +
                        "завтра/через 6 дней\n" +
                        "29 мая",
                KeyboardBuilder.createTimeKeyboard());
    }
}