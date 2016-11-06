/*
 * Copyright (C) 2016  liketechnik <flowa2000@gmail.com>
 *
 * This file is part of infoDisplay.
 *
 * infoDisplay is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * infoDisplay is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://github.com/rubenlagus/TelegramBots>.
 *
 * infoDisplay uses TelegramBots Java API <https://github.com/rubenlagus/TelegramBots> by Ruben Bermudez.
 * TelegramBots API is licensed under GNU General Public License version 3 <https://github.com/rubenlagus/TelegramBots>.
 *
 * infoDisplay uses parts of the Apache Commons project <https://commons.apache.org/>.
 * Apache commons is licensed under the Apache License Version 2.0 <http://www.apache.org/licenses/>.
 */

package org.telegram.bot.commands.askCommand;

import org.telegram.bot.commands.SendOnErrorOccurred;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import static org.telegram.bot.Main.getSpecialFilteredUsername;

/**
 * @author liketechnik
 * @version 1.0.1
 * @date 01 of November of 2016
 *
 * This command sends a question to the administrator.
 */
public class WriteQuestion extends BotCommand {

    public static final String LOGTAG = "ASKCOMMAND_WRITEQUESTION";

    /**
     * Set the identifier and a short description of the command.
     */
    public WriteQuestion() {
        super("write_question", "Receive the question from the user, after /ask was executed by him.");
    }

    /**
     * Evaluate the message send by a user.
     * This is done by sending the question of a user to the administrator .
     * @param absSender
     * @param user
     * @param chat
     * @param arguments
     */
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        SendMessage question = new SendMessage();
        SendMessage answer = new SendMessage();

        try {
            String message = arguments[0];

            StringBuilder questionBuilder = new StringBuilder().append(message);
            questionBuilder.append("\n").append("From user ").append(getSpecialFilteredUsername(user))
                    .append(" .");

            DatabaseManager.getInstance().setUserCommandState(user.getId(),
                    org.telegram.bot.Config.Bot.NO_COMMAND);

            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Deine Frage wurde weitergeleitet.");

            question.setChatId(org.telegram.bot.Config.Bot.ADMIN_CHAT_ID.toString());
            question.setText(questionBuilder.toString());
            answer.setChatId(chat.getId().toString());
            answer.setText(messageBuilder.toString());

            DatabaseManager.getInstance().createQuestion(questionBuilder.toString(), chat.getId().longValue());
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);

            new SendOnErrorOccurred().execute(absSender, user, chat, new String[]{LOGTAG});

            return;
        }

        try {
            absSender.sendMessage(question);
            absSender.sendMessage(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
}
