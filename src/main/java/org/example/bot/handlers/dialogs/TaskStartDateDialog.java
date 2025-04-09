package org.example.bot.handlers.dialogs;

import org.example.bot.utils.DateTimeParser;
import org.example.bot.utils.KeyboardBuilder;
import org.example.bot.utils.MessageSender;
import org.example.models.User;
import org.example.repositories.TaskRepository;
import org.example.repositories.UserRepository;
import org.example.bot.utils.UserState;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class TaskStartDateDialog implements Dialog {
    @Override
    public UserState getSupportedState() {
        return UserState.AWAITING_TASK_START_DATE;
    }

    @Override
    public void execute(User user, long chatId, String input,
                        MessageSender messageSender,
                        UserRepository userRepository,
                        TaskRepository taskRepository) throws TelegramApiException {
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
        } catch (DateTimeParseException e) {
            messageSender.sendMessage(chatId, "Неверный формат даты. Примеры:\n" +
                    "• 2025-05-01 14:30\n" +
                    "• 01.05.2025 14:30");
        }
    }
}