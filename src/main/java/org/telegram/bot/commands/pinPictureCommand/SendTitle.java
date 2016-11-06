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

package org.telegram.bot.commands.pinPictureCommand;

import liketechnik.InfoDisplay.Config;
import org.telegram.bot.commands.SendOnErrorOccurred;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import java.nio.file.FileAlreadyExistsException;

/**
 * @author liketechnik
 * @version 1.0
 * @date 01 of November 2016
 *
 * This class handles an incoming message containing the title for a new picture.
 */
public class SendTitle extends BotCommand {

    public static final String LOGTAG = "PINPICTURECOMMAND_SENDTITLE";

    /**
     * Set the identifier for the command and a short description.
     */
    public SendTitle() {
        super("send_title", "Evaluates the answer after a user executed /pin_picture.");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        SendMessage answer = new SendMessage();

        try {

            DatabaseManager databaseManager = DatabaseManager.getInstance();

            StringBuilder messageBuilder = new StringBuilder();

            String message = arguments[0];

            String displayFileName;
            displayFileName = message + ".jpg";

            messageBuilder.append("Sende mir nun bitte eine (kurze) Beschreibung / Überschrift für das Bild.");

            try {
                databaseManager.createConfigurationFile(databaseManager.getDatabaseDisplayFilePath(displayFileName));
                databaseManager.setCurrentPictureTitle(user.getId(), displayFileName);
                databaseManager.setUserCommandState(user.getId(), Config.Bot.PIN_PICTURE_COMMAND_SEND_DESCRIPTION);
            } catch (FileAlreadyExistsException e) {
                messageBuilder = new StringBuilder();
                messageBuilder.append("Dieser Name wurde schon für ein Bild gewählt, bitte sende mir einen anderen.");
            }

            answer.setChatId(chat.getId().toString());
            answer.setText(messageBuilder.toString());
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