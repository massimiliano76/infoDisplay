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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * infoDisplay uses TelegramBots Java API <https://github.com/rubenlagus/TelegramBots> by Ruben Bermudez.
 * TelegramBots API is licensed under GNU General Public License version 3 <https://www.gnu.org/licenses/gpl-3.0.de.html>.
 *
 * infoDisplay uses parts of the Apache Commons project <https://commons.apache.org/>.
 * Apache commons is licensed under the Apache License Version 2.0 <http://www.apache.org/licenses/>.
 *
 * infoDisplay uses vlcj library <http://capricasoftware.co.uk/#/projects/vlcj>.
 * vlcj is licensed under GNU General Public License version 3 <https://www.gnu.org/licenses/gpl-3.0.de.html>.
 *
 * Thanks to all the people who contributed to the projects that make this
 * program possible.
 */

package org.telegram.bot.commands;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.telegram.bot.Config;
import org.telegram.bot.DisplayBot.*;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.bot.messages.Message;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import java.io.IOException;
import java.util.NoSuchElementException;

import static org.telegram.bot.Main.getFilteredUsername;

/**
 * @author Florian Warzecha
 * @version 1.0.1
 * @date 22 of October of 2016
 *
 * This command gets executed if a user uses the bot for the first time.
 */
public class StartCommand extends BotCommand {

    public static final String LOGTAG = "STARTCOMMAND";

    /**
     * Set the identifier and a short description.
     */
    public StartCommand() {
        super("start", "Mit diesem Befehl kannst du den Bot starten");
    }

    /**
     * Save that the user is active and tell the user about what this bot does.
     * @param absSender
     * @param user
     * @param chat
     * @param strings
     */
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {

        String message;
        SendMessage answer = new SendMessage();

        try {
            DatabaseManager databaseManager = DatabaseManager.getInstance();

            boolean userKnown = false;

            if (databaseManager.getUserState(user.getId())) {
                userKnown = true;
            } else {
                userKnown = false;
            }

            if (userKnown) {
                message = Message.getStartMessage(user, true);
            } else {
                databaseManager.setUserState(user.getId(), true);
                databaseManager.setUserLanguage(user.getId(), Config.Languages.NONE);

                if (databaseManager.getUserRegistrationState(user.getId())) {
                    databaseManager.setUserRegistrationState(user.getId(), true);
                } else {
                    databaseManager.setUserRegistrationState(user.getId(), false);
                }

                message = Message.getStartMessage(user, false);
            }

            databaseManager.setUserWantsRegistrationState(user.getId(), false);
            databaseManager.setUserCommandState(user.getId(), Config.Bot.NO_COMMAND);

            answer.setChatId(chat.getId().toString());
            answer.setText(message);

        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);

            new SendOnErrorOccurred().execute(absSender, user, chat, new String[]{LOGTAG});

            return;
        }

        try {
            absSender.sendMessage(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
}
