package org.telegram.bot.commands;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.telegram.bot.Config;
import org.telegram.bot.DisplayBot.*;
import org.telegram.bot.database.DatabaseManager;
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
import static org.telegram.bot.Main.sendOnErrorOccurred;
//TODOC add javadoc /documentation
/**
 * @author florian
 * @version 1.0
 * @date 22 of Oktober of 2016
 */
public class StartCommand extends BotCommand {

    public static final String LOGTAG = "STARTCOMMAND";

    public StartCommand() {
        super("start", "With this command you can start the bot. \n    Mit diesem Befehl kannst du den Bot starten");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {

        StringBuilder messageBuilder = new StringBuilder();
        SendMessage answer = new SendMessage();

        try {
            DatabaseManager databaseManager = DatabaseManager.getInstance();

            String userName = getFilteredUsername(user);

            boolean userKnown = false;

            if (databaseManager.getUserState(user.getId())) {
                userKnown = true;
            } else {
                userKnown = false;
            }

            if (userKnown) {
                messageBuilder.append("Hello ").append(userName).append(",\n");
                messageBuilder.append("I think you know how to use this bot, as you already know it.").append("\n\n");
                messageBuilder.append("Hallo ").append(userName).append(",\n");
                messageBuilder.append("ich denke Du weißt wie du den Bot benutzen kannst, da du ihn schon kennst.");
            } else {
                databaseManager.setUserState(user.getId(), true);

                if (databaseManager.getUserRegistrationState(user.getId())) {
                    databaseManager.setUserRegistrationState(user.getId(), true);
                } else {
                    databaseManager.setUserRegistrationState(user.getId(), false);
                }
            }

            databaseManager.setUserWantsRegistrationState(user.getId(), false);
            databaseManager.setUserCommandState(user.getId(), Config.Bot.NO_COMMAND);

            messageBuilder.append("Hello ").append(userName).append(",\n");
            messageBuilder.append("if you know this bot, you probably already know how to use it, although you can " +
                    "use the '/help' command. If not, then it " +
                    "will not be usefull for you. If you have a question concerning this bot, feel free to ask it" +
                    " via '/ask'.").append("\n\n");
            messageBuilder.append("Hallo ").append(userName).append(",\n");
            messageBuilder.append("wenn du diesen Bot kennst, weißt du wahrscheinlich schon wie er zu bedienen ist, " +
                    "ansonsten benutze den '/help' Befehl. Wenn du diesen Bot noch nicht kennst, wirst " +
                    "wahrscheinlich nichts mit ihm Anfangen können. Wenn du eine Frage zu diesem Bot hast, stelle " +
                    "sie mit '/ask'.");

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