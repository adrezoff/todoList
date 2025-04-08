package org.example.bot.handlers.dialogs;

import org.example.bot.utils.MessageSender;
import org.example.models.User;
import org.example.repositories.TaskRepository;
import org.example.repositories.UserRepository;
import org.example.bot.utils.UserState;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.security.InvalidParameterException;

public class TimeZoneDialog implements Dialog {
    @Override
    public UserState getSupportedState() {
        return UserState.AWAITING_TIME_ZONE;
    }

    @Override
    public void execute(User user, long chatId, String input,
                        MessageSender messageSender,
                        UserRepository userRepository,
                        TaskRepository taskRepository) throws TelegramApiException {
        try {
            String normalizedInput = normalizeTimeZoneInput(input);
            ZoneId zoneId = ZoneId.of(normalizedInput);

            user.setTimeZone(zoneId);
            user.setState(UserState.IDLE);
            userRepository.update(user);

            messageSender.sendMessage(chatId, String.format(
                    "Часовой пояс установлен: %s (%s)\n\nТеперь вы можете создавать задачи.",
                    zoneId.getId(),
                    getTimeZoneExample(zoneId)
            ));
        } catch (InvalidParameterException e) {
            messageSender.sendMessage(chatId, getErrorMsg());
        } catch (DateTimeException e) {
            messageSender.sendMessage(chatId, getErrorMsg());
        }
    }
    private String getErrorMsg(){
        return "Неверный формат часового пояса. Пожалуйста, введите один из вариантов:\n" +
                "• Число от -12 до +14 (например, +3 или -5)\n" +
                "• Название зоны (например, Europe/Moscow или America/New_York)\n" +
                "• Сокращение (например, GMT+3 или UTC-5)";
    }

    private String normalizeTimeZoneInput(String input) throws InvalidParameterException {
        if (input.matches("^[+-]?\\d{1,2}$")) {
            int offset = Integer.parseInt(input);
            if (offset >= -12 && offset <= 14) {
                return "GMT" + (offset >= 0 ? "+" + offset : offset);
            }
        }
        return input;
    }

    private String getTimeZoneExample(ZoneId zoneId) {
        LocalDateTime now = LocalDateTime.now(zoneId);
        return "Текущее время: " + now.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}