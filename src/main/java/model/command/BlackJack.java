package model.command;

import model.command.type.ButtonInteraction;
import model.command.type.ModalInteraction;
import model.command.type.ServerCommand;
import model.database.models.GamePlayerFullModel;
import model.game.blackjack.BlackJackGame;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import util.ConnectionUtil;
import util.ServerUtil;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static util.ServerUtil.sendFeedBack;

public class BlackJack implements ServerCommand, ButtonInteraction, ModalInteraction {

    private static final BlackJackGame GAME = new BlackJackGame();

    @Override
    public void performTextChannelCommand(Member member, TextChannel channel, String message) {
        if (!ServerUtil.isChannelId(channel,"blackjack")) {
            sendFeedBack(String.format("Blackjack Befehle können nur im %s Channel verwendet werden!",
                    ServerUtil.getTextChannel(ServerUtil.getChannelID("blackjack")).getAsMention()), ServerUtil.RED, channel);
            return;
        }

       if (message.equals("!bj info") && member.hasPermission(Permission.ADMINISTRATOR)) {
            createGameMessage(channel);
        } else if (message.equals("!bj reset") && member.hasPermission(Permission.ADMINISTRATOR)) {
            GAME.resetAll();
            updateGameMessage(channel);
        }
    }

    @Override
    public void performTextChannelReaction(Member member, TextChannel channel, MessageReaction reaction) {
        if (!reaction.getMessageId().equals(ServerUtil.getMessageID("blackjack_board"))) {
            return;
        }

        reaction.removeReaction(member.getUser()).queue();

        if (!GAME.isTurn(member.getId())) {
            return;
        }

        String emote = reaction.getEmoji().getName();

        if (emote.equals("☝️")) {
            GAME.playerDraw();
        } else if (emote.equals("✋")) {
            GAME.nextPlayer();
        } else if (emote.equals("\uD83E\uDD1D")) {
            GAME.doubleDown();
        }

        while (GAME.isFinished()) {
            GAME.dealerDraw();
            channel.editMessageEmbedsById(ServerUtil.getMessageID("blackjack_board"), GAME.createResult().build()).complete();
            EmbedBuilder kickMessage = GAME.reset();
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (kickMessage != null) {
                channel.sendMessageEmbeds(kickMessage.build()).complete().delete().queueAfter(20, TimeUnit.SECONDS);
            }

            if (!GAME.isEmpty()) {
                GAME.setState(BlackJackGame.State.STARTING);
                updateGameMessage(channel);
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (GAME.isEmpty()) {
                    GAME.resetAll();
                    updateGameMessage(channel);
                    return;
                }

                if (GAME.startGame()) {
                    updateGameMessage(channel);
                } else {
                    return;
                }
            } else {
                GAME.resetAll();
                updateGameMessage(channel);
                return;
            }
        }
        updateGameMessage(channel);
    }

    @Override
    public void performButtonInteraction(Member member, TextChannel channel, String customID, ButtonInteractionEvent event) {
        switch (customID) {
            case "join":
                if (GAME.isPlaying(member.getId()) || GAME.inQueue(member.getId())) {
                    return;
                }

                TextInput betJoin = TextInput.create("bet", "Wetteinsatz", TextInputStyle.SHORT)
                        .setPlaceholder("Zahl mit 1:100 Coin/s")
                        .setRequired(true)
                        .setRequiredRange(1,17)
                        .build();

                Modal modalJoin = Modal.create("bj join", "Blackjack - Beitreten")
                        .addActionRows(ActionRow.of(betJoin))
                        .build();

                event.replyModal(modalJoin).queue();
                break;
            case "leave":
                if (GAME.isPlaying(member.getId())) {
                    if (GAME.getState().equals(BlackJackGame.State.PLAYING) || GAME.getState().equals(BlackJackGame.State.RESULT)) {
                        sendFeedBack(String.format("%s, du kannst den Tisch nur außerhalb eines Spieles verlassen!"
                                , member.getAsMention()), ServerUtil.RED, event);
                        return;
                    }
                    GAME.removePlayer(member.getId());

                    EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, verlässt den Tisch!", member.getAsMention()));
                    if (GAME.isEmpty()) {
                        GAME.resetAll();
                    }
                    updateGameMessage(channel);
                    event.replyEmbeds(eb.build()).setEphemeral(true).queue();
                }
                break;
            case "change":
                if (GAME.isPlaying(member.getId())) {
                    TextInput betChange = TextInput.create("bet", "Wetteinsatz", TextInputStyle.SHORT)
                            .setPlaceholder("Zahl mit 1:100 Coin/s")
                            .setRequired(true)
                            .setRequiredRange(1,17)
                            .build();

                    Modal modalChange = Modal.create("bj change", "Blackjack - Neuer Wetteinsatz")
                            .addActionRows(ActionRow.of(betChange))
                            .build();

                    event.replyModal(modalChange).queue();
                }
                break;
        }
    }

    @Override
    public void performModalInteraction(Member member, TextChannel channel, String customID, ModalInteractionEvent event) {
        switch (event.getModalId()) {
            case "bj join":
                Optional<GamePlayerFullModel> player = ConnectionUtil.executeSingleSQL(ConnectionUtil.SELECT_GAME_FULL(member.getId()), GamePlayerFullModel.class);

                if (!player.isPresent()) {
                    sendFeedBack(String.format("%s, erstelle dir zuvor im Hub ein Konto!", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                try {
                    long bet = Long.parseLong(event.getValue("bet").getAsString());

                    if (player.get().getMoney() < bet * 100) {
                        sendFeedBack(String.format("%s, du besitzt nicht genug <:gold_coin:886658702512361482>!", member.getAsMention()), ServerUtil.RED, event);
                        return;
                    }

                    int result = GAME.addPlayer(member.getEffectiveName(), member.getId(), member.getEffectiveAvatarUrl()
                            , bet,player.get().getMoney(), player.get().getRating_blackjack(), player.get().getSelectDeck());

                    if (result > 0 ) {
                        sendFeedBack(String.format("%s, der Tisch ist bereits voll oder am spielen!\n" +
                                "Du wurdest in die Warteschlage verschoben. (Platz: %d)", member.getAsMention(), result), ServerUtil.BLUE, event);
                        return;
                    } else if (result < 0) {
                        sendFeedBack(String.format("%s, Es ist ein Fehler aufgetreten", member.getAsMention()), ServerUtil.RED, event);
                        return;
                    }

                    EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, hat sich an den Tisch gesetzt", member.getAsMention()));
                    event.replyEmbeds(eb.build()).setEphemeral(true).queue();
                    if (GAME.playerSize() > 1) {
                        updateGameMessage(channel);
                        return;
                    }

                    GAME.setState(BlackJackGame.State.STARTING);
                    updateGameMessage(channel);
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (GAME.isEmpty()) {
                        GAME.resetAll();
                        updateGameMessage(channel);
                        return;
                    }

                    if (!GAME.startGame()) {
                        return;
                    }

                    while (GAME.isFinished()) {
                        GAME.dealerDraw();
                        channel.editMessageEmbedsById(ServerUtil.getMessageID("blackjack_board"), GAME.createResult().build()).complete();
                        EmbedBuilder kickMessage = GAME.reset();
                        try {
                            TimeUnit.SECONDS.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (kickMessage != null) {
                            channel.sendMessageEmbeds(kickMessage.build()).complete().delete().queueAfter(20, TimeUnit.SECONDS);
                        }

                        if (!GAME.isEmpty()) {
                            GAME.setState(BlackJackGame.State.STARTING);
                            updateGameMessage(channel);
                            try {
                                TimeUnit.SECONDS.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (GAME.isEmpty()) {
                                GAME.resetAll();
                                updateGameMessage(channel);
                                return;
                            }

                            if (GAME.startGame()) {
                                updateGameMessage(channel);
                            } else {
                                return;
                            }
                        } else {
                            GAME.resetAll();
                            updateGameMessage(channel);
                            return;
                        }
                    }
                    updateGameMessage(channel);
                } catch (NumberFormatException ignore) {}
                break;
            case "bj change":
                if (!GAME.isPlaying(member.getId())) {
                    return;
                }

                if (GAME.getState().equals(BlackJackGame.State.PLAYING)) {
                    sendFeedBack(String.format("%s, du kannst dein Wetteinsatz nicht während eines Spiels ändern!"
                            , member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                try {
                    long bet = Long.parseLong(event.getValue("bet").getAsString());
                    if (!GAME.getState().equals(BlackJackGame.State.WAITING) && !GAME.getState().equals(BlackJackGame.State.STARTING)) {
                        return;
                    }

                    if (GAME.updateMoney(member.getId(), bet)) {
                        sendFeedBack(String.format("%s, dein Wetteinsatz wurde erfolgreich auf %s <:gold_coin:886658702512361482> gewechselt!"
                                , member.getAsMention(), BlackJackGame.formatNumber(bet * 100)), ServerUtil.GREEN, event);
                        updateGameMessage(channel);
                        return;
                    }

                    sendFeedBack(String.format("%s, dein Wetteinsatz konnte nicht geändert werden!"
                            , member.getAsMention()), ServerUtil.RED, event);
                } catch (NumberFormatException ignore) {}
                break;
        }
    }

    @Override
    public void performPrivateChannelCommand(User user, String message) {
        throw new UnsupportedOperationException();
    }

    private void createGameMessage(TextChannel channel) {
        EmbedBuilder info = new EmbedBuilder()
                .setTitle("♣️ ♦️ BlackJack - Game ♠️ ♥️")
                .setDescription("Hier erfährst du die wichtigsten Befehle und Interaktionen um Blackjack spielen zu können")
                .addField("Regeln:","```\nJeder Spieler bekommt zum start 2 Karten (Dealer inkl. wobei eine Karte verdeckt liegt). Danach wird nacheinander gezogen." +
                        " Ziel ist es so nah wie möglich an 21 heran zu kommen. Hierbei zählen Nummern-Karten mit Augenzahl, " +
                        "Bube/Dame/König als 10 und ein Ass als 1. Du gewinnst wenn deine Karten näher an 21 sind als die des Dealers (2x dein Einsatz)\n```",false)
                .addField("Interaktion:","```\nNutze ☝️ für draw, ✋ für stand & \uD83E\uDD1D für double down\n```",false)
                .addField("Info:","```\nNachdem ein Spieler beigetreten ist bleiben 10 Sekunden bis die Runde startet. Nach jeder Runde wird ebenfalls 10 Sekunden das Ergebniss angezeigt." +
                        " MAX 4 Spieler sind erlaubt und verändern der Wette geht nur in der STARTING/WAITING Phase\n```",false)
                .setFooter("Der Dealer spielt nach Soft17");

        EmbedBuilder board = new EmbedBuilder()
                .setTitle("Tisch")
                .setDescription("**Spieleranzahl:** []\n**Aktiver Spieler:** [NONE]\n**Phase:** [WAITING]\n**Deck:** [0]")
                .addField("Dealer [0][x]:","Wartet...",false)
                .setImage("https://www.gamblingsites.org/app/themes/gsorg2018/images/blackjack-hard-hand-example-3.png");

        MessageCreateBuilder builder = new MessageCreateBuilder()
                .addEmbeds(board.build())
                .setComponents(
                        ActionRow.of(
                            Button.primary("bj join", "Betreten"),
                            Button.secondary("bj change","Wetteinsatz"),
                            Button.danger("bj leave","Verlassen")
                        ));

        channel.sendMessageEmbeds(info.build()).queue();
        String mesID = channel.sendMessage(builder.build()).complete().getId();
        channel.addReactionById(mesID, Emoji.fromUnicode("☝️")).queue();
        channel.addReactionById(mesID, Emoji.fromUnicode("✋")).queue();
        channel.addReactionById(mesID, Emoji.fromUnicode("\uD83E\uDD1D")).queue();
    }

    private void updateGameMessage(TextChannel channel) {
        channel.editMessageEmbedsById(ServerUtil.getMessageID("blackjack_board"), GAME.createMessage().build()).queue();
    }

    public static String getCards() {
        return GAME.getTopCards();
    }

}
