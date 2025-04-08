package org.example.bot.utils;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public final class MessageSender {
    private static volatile MessageSender instance;
    private final TelegramLongPollingBot bot;

    private MessageSender(TelegramLongPollingBot bot) {
        this.bot = bot;
    }

    public static MessageSender getInstance(TelegramLongPollingBot bot) {
        MessageSender result = instance;
        if (result == null) {
            synchronized (MessageSender.class) {
                result = instance;
                if (result == null) {
                    instance = result = new MessageSender(bot);
                }
            }
        }
        return result;
    }
    public static MessageSender getInstance() {
        return instance;
    }

    public synchronized void sendMessage(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        bot.execute(message);
    }

    public synchronized void sendMessage(long chatId, String text, ReplyKeyboard keyboard) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(keyboard);
        bot.execute(message);
    }

    public synchronized void sendMessage(long chatId, String text, InlineKeyboardMarkup keyboard)
            throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(keyboard);
        message.enableHtml(true);
        bot.execute(message);
    }
    public synchronized void editMessageText(long chatId, int messageId, String newText) throws TelegramApiException {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(newText);
        bot.execute(editMessage);
    }

    public synchronized void editMessageText(long chatId, int messageId, String newText, InlineKeyboardMarkup keyboard)
            throws TelegramApiException {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(newText);
        editMessage.setReplyMarkup(keyboard);
        bot.execute(editMessage);
    }

    public synchronized void editMessageReplyMarkup(long chatId, int messageId, InlineKeyboardMarkup keyboard)
            throws TelegramApiException {
        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(chatId);
        editMarkup.setMessageId(messageId);
        editMarkup.setReplyMarkup(keyboard);
        bot.execute(editMarkup);
    }

    public synchronized void deleteMessage(long chatId, int messageId) throws TelegramApiException {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        bot.execute(deleteMessage);
    }

    public synchronized void answerCallbackQuery(String callbackId, String text) throws TelegramApiException {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackId);
        answer.setText(text);
        answer.setShowAlert(false);
        bot.execute(answer);
    }

    public synchronized void answerCallbackQuery(String callbackId, String text, boolean showAlert) throws TelegramApiException {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackId);
        answer.setText(text);
        answer.setShowAlert(showAlert);
        bot.execute(answer);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}