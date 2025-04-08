package org.example.bot.handlers.commands;

import org.example.bot.utils.MessageSender;
import org.example.bot.utils.UserState;
import org.example.models.User;
import org.example.repositories.TaskRepository;
import org.example.repositories.UserRepository;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SetTimeZoneCommand implements Command{

    @Override
    public String getName() {
        return "/settimezone";
    }

    @Override
    public void execute(User user, long chatId,
                        MessageSender messageSender,
                        UserRepository userRepository,
                        TaskRepository taskRepository) throws TelegramApiException {
        messageSender.sendMessage(chatId,
                        "⏰ Введите ваш часовой пояс в формате:\n" +
                        "• GMT+3 (для Москвы)\n" +
                        "• Или число от -12 до +14 (например, +3)");

        user.setState(UserState.AWAITING_TIME_ZONE);
        user.clearTempData();
        userRepository.update(user);

    }
}
