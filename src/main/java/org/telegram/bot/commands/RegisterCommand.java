package org.telegram.bot.commands;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.output.StringBuilderWriter;
import org.telegram.bot.Config;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import java.io.IOException;

import static org.telegram.bot.Main.getSpecialFilteredUsername;
import static org.telegram.bot.Main.sendOnErrorOccurred;
//TODOC add javadoc /documentation
/**
 * @author florian
 * @version 1.0
 * @date 24 of Oktober of 2016
 */
public class RegisterCommand extends BotCommand {

    public static final String LOGTAG = "REGISTERCOMMAND";

    public RegisterCommand() {
        super("register", "Registriere dich als Benutzer um Bilder auf das virtuelle Brett hochladen zu können.");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        SendMessage answer = new SendMessage();

        try {

            DatabaseManager databaseManager = DatabaseManager.getInstance();
            databaseManager.setUserState(user.getId(), true);

            StringBuilder messageBuilder = new StringBuilder();

            if (databaseManager.getUserRegistrationState(user.getId())) {
                messageBuilder.append("Du bist bereits als Benutzer dieses Bots registriert, deine Telegramm UserID " +
                        "ist: ");
            } else {
                if (databaseManager.getUserWantsRegistrationState(user.getId())) {
                    messageBuilder.append("Du hast bereits eine Anfrage zur Registrierung abgeschickt, deine " +
                            "Telegramm UserID ist: ");
                } else {
                    databaseManager.setUserWantsRegistrationState(user.getId(), true);

                    messageBuilder.append("Der Adminsitrator dieses Bots (@liketechnik2000) " +
                            "weiß nun, dass du dich registrieren lassen möchtest. Er wird dich kontaktieren, deine " +
                            "Telegramm UserID ist: ");

                    StringBuilder requestBuilder = new StringBuilder();
                    requestBuilder.append("Der Benutzer ");
                    requestBuilder.append(getSpecialFilteredUsername(user));
                    requestBuilder.append(" möchte sich registrieren lassen. Seine userID ist: ").append(user.getId());

                    SendMessage request = new SendMessage();
                    request.setChatId(Config.Bot.ADMIN_CHAT_ID.toString());
                    request.setText(requestBuilder.toString());

                    try {
                        absSender.sendMessage(request);
                    } catch (Exception e1) {
                        BotLogger.error(LOGTAG, e1);
                        messageBuilder.append("\n\n").append("Nachricht an den Administrator konnte nicht gesendet werden. Bitte " +
                                "probiere es erneut.");
                    }
                }
            }

            messageBuilder.append(user.getId());

            messageBuilder.append("\n").append("/help");

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