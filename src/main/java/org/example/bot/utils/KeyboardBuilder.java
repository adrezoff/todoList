package org.example.bot.utils;

import org.example.models.Task;
import org.example.models.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class KeyboardBuilder {
    public static InlineKeyboardMarkup createTaskKeyboard(Task task) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> actionRow = new ArrayList<>();

        InlineKeyboardButton completeButton = new InlineKeyboardButton();
        completeButton.setText("✅ Выполнено");
        completeButton.setCallbackData("complete:" + task.getId());
        actionRow.add(completeButton);

//        InlineKeyboardButton editButton = new InlineKeyboardButton();
//        editButton.setText("⚙️Редактировать");
//        editButton.setCallbackData("edit:" + task.getId());
//        actionRow.add(editButton);

        InlineKeyboardButton deleteButton = new InlineKeyboardButton();
        deleteButton.setText("❌Удалить");
        deleteButton.setCallbackData("delete:" + task.getId());
        actionRow.add(deleteButton);

        keyboard.add(actionRow);

        markup.setKeyboard(keyboard);
        return markup;
    }
    public static InlineKeyboardMarkup createConfirmKeyboard(){
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("📩 Да")
                .callbackData("confirm_task:yes")
                .build());
        row.add(InlineKeyboardButton.builder()
                .text("🗑 Нет")
                .callbackData("confirm_task:no")
                .build());

        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
    public static InlineKeyboardMarkup createSkipKeyboard(){
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("📩 Да")
                .callbackData("confirm_task:yes")
                .build());
        row.add(InlineKeyboardButton.builder()
                .text("🗑 Нет")
                .callbackData("confirm_task:no")
                .build());

        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
}