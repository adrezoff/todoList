package org.example.bot.handlers;

import org.example.bot.handlers.commands.*;
import org.example.bot.handlers.dialogs.*;
import org.example.bot.utils.MessageSender;
import org.example.models.User;
import org.example.repositories.TaskRepository;
import org.example.repositories.UserRepository;
import org.example.bot.utils.UserState;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;

public class MessageHandler {
    private final MessageSender messageSender;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final HashMap<String, Command> commands;
    private final HashMap<UserState, Dialog> dialogs;

    public MessageHandler(MessageSender messageSender,
                          UserRepository userRepository,
                          TaskRepository taskRepository) {
        this.messageSender = messageSender;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.commands = new HashMap<>();
        this.dialogs = new HashMap<>();
        registerCommands();
        registerDialogs();
    }

    private void registerCommands() {
        commands.put("/start", new StartCommand());
        commands.put("/help", new HelpCommand());
        commands.put("/tasks", new TasksCommand());
        commands.put("/cancel", new CancelCommand());
        commands.put("/settimezone", new SetTimeZoneCommand());
        commands.put("/createtask", new CreateTaskCommand());
    }

    private void registerDialogs() {
        dialogs.put(UserState.AWAITING_TIME_ZONE, new TimeZoneDialog());
        dialogs.put(UserState.AWAITING_TASK_TITLE, new TaskTitleDialog());
        dialogs.put(UserState.AWAITING_TASK_DESCRIPTION, new TaskDescriptionDialog());
        dialogs.put(UserState.AWAITING_TASK_START_DATE, new TaskStartDateDialog());
        dialogs.put(UserState.AWAITING_TASK_END_DATE, new TaskEndDateDialog());
    }

    public void handle(Message message) throws TelegramApiException {
        String text = message.getText();
        long chatId = message.getChatId();
        long userId = message.getFrom().getId();

        User user = userRepository.findById(userId).orElseGet(() -> {
            User newUser = new User(userId);
            newUser.setUsername(message.getFrom().getUserName());
            userRepository.save(newUser);
            return newUser;
        });

        // Обработка команд
        if (text.startsWith("/")) {
            Command command = commands.get(text.split(" ")[0].toLowerCase());
            if (command != null) {
                command.execute(user, chatId, messageSender, userRepository, taskRepository);
            } else {
                messageSender.sendMessage(chatId, "❌ Неизвестная команда");
            }
            return;
        }

        Dialog dialog = dialogs.get(user.getState());
        if (dialog != null) {
            dialog.execute(user, chatId, text, messageSender, userRepository, taskRepository);
        } else {
            messageSender.sendMessage(chatId, "⚠️ Введите команду /help");
        }
    }
}