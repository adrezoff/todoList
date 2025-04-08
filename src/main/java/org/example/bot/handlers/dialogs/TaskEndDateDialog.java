package org.example.bot.handlers.dialogs;

import org.example.bot.utils.DateTimeParser;
import org.example.bot.utils.KeyboardBuilder;
import org.example.bot.utils.MessageSender;
import org.example.models.User;
import org.example.repositories.TaskRepository;
import org.example.repositories.UserRepository;
import org.example.bot.utils.UserState;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.time.format.DateTimeFormatter.ofPattern;

public class TaskEndDateDialog implements Dialog {
    @Override
    public UserState getSupportedState() {
        return UserState.AWAITING_TASK_END_DATE;
    }

    @Override
    public void execute(User user, long chatId, String input,
                        MessageSender messageSender,
                        UserRepository userRepository,
                        TaskRepository taskRepository) throws TelegramApiException {
        try {
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
        } catch (DateTimeParseException e) {
            messageSender.sendMessage(chatId, "Неверный формат даты. Примеры:\n" +
                    "• 2025-05-01 14:30\n" +
                    "• 01.05.2025 14:30");
        }
    }
}