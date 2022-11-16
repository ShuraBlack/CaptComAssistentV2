package model.command;

import model.command.type.ButtonInteraction;
import model.command.type.ModalInteraction;
import model.command.type.ServerCommand;
import model.database.models.GamePlayerFullModel;
import model.database.models.GamePlayerModel;
import model.game.ChestRequest;
import model.game.blackjack.BlackJackGame;
import model.game.event.EventManager;
import model.service.LogService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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

import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static util.ServerUtil.*;

public class Game implements ServerCommand, ButtonInteraction, ModalInteraction {

    @Override
    public void performTextChannelCommand(Member member, TextChannel channel, String message) {
        if (message.startsWith("!game add")) {
            if (!member.hasPermission(Permission.ADMINISTRATOR)) {
                return;
            }

            String[] args = message.split(" ");

            ConnectionUtil.executeSQL(ConnectionUtil.UPDATE_GAME_MONEY(args[2], Long.parseLong(args[3])));
            EmbedBuilder eb = new EmbedBuilder()
                    .setColor(ServerUtil.GREEN)
                    .setDescription(String.format("%s <:gold_coin:886658702512361482> wurden erfolgreich dem Konto **ID:%s** hinzugefügt!", formatNumber(Long.parseLong(args[3])), args[2]));
            channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(10, TimeUnit.SECONDS);
        } else if (message.equals("!game hub")) {
            if (!member.hasPermission(Permission.ADMINISTRATOR)) {
                return;
            }
            //sendLeaderBoard(channel);
            sendRatingOverview(channel);
            sendGameInfo(channel);
        } else if (message.equals("!game shop")) {
            if (!member.hasPermission(Permission.ADMINISTRATOR)) {
                return;
            }
            sendShop(channel);
        } else if (message.equals("!game info")) {
            if (!member.hasPermission(Permission.ADMINISTRATOR)) {
                return;
            }
            sendInfoBoard(channel);
            sendEventMessage(channel);
        }
    }

    private void sendInfoBoard(TextChannel channel) {
        EmbedBuilder eb = new EmbedBuilder()
                .setThumbnail("https://cdn.discordapp.com/attachments/990065719217647686/1007338272785170442/newspaper.png")
                .setTitle("Info Board")
                .setDescription("In dieser Nachricht werden Neuerungen sowie Änderungen Angezeigt!")
                .addBlankField(false)
                .addField("Chest Changes","```Die Belohnung der Chests wurden verändert und ein Daily Limit wurde eingeführt." +
                        " Chests stehen dadurch wieder zur Verfügung.```",false)
                .setTimestamp(OffsetDateTime.now());

        channel.editMessageEmbedsById("1007341404227387472", eb.build()).queue();
        //channel.sendMessageEmbeds(eb.build()).queue();
    }

    private void sendEventMessage(TextChannel channel) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Dauerhaftes Event")
                .setDescription("Temporäre Nachricht");

        channel.sendMessageEmbeds(eb.build()).queue();
    }

    private void sendLeaderBoard(TextChannel channel) {
        EmbedBuilder leaderboard = new EmbedBuilder()
                .setThumbnail("https://s20.directupload.net/images/220619/4vo59odf.png")
                .setTitle("Bestenliste")
                .addField("Meistes Geld","\uD83E\uDD47.\n\uD83E\uDD48.\n\uD83E\uDD49.\n",true)
                .addField("Blackjack","\uD83E\uDD47.\n\uD83E\uDD48.\n\uD83E\uDD49.\n",true)
                .addField("Chest Opening","\uD83E\uDD47.\n\uD83E\uDD48.\n\uD83E\uDD49.\n",true)
                .setFooter("Letzte Aktualisierung")
                .setTimestamp(OffsetDateTime.now());

        String mesID = channel.sendMessageEmbeds(leaderboard.build()).complete().getId();
        System.out.println(mesID);
    }

    private void sendRatingOverview(TextChannel channel) {
        EmbedBuilder rating = new EmbedBuilder()
                .setTitle("Game Rating")
                .setImage("https://cdn.discordapp.com/attachments/990065719217647686/1007421265725902989/ranks_explain.png")
                .setDescription("Dein Rating wird erhöht durch das Gewinnen von Spielen und das eingehen von größeren Risiken!" +
                        " Ränge zwischen Bronze bis Diamant besitzen zusätzlich 5 Divisionen (100 Punkte entsprichen einer Division)" +
                        ".\n\n__Blackjack:__ Pro Runde erhälst/verlierst du 2.5 EP (bis zu 20 EP + 5 extra EP für double down)\n" +
                        "__Mystery Box:__ Je nach Auswahl deiner Chest verdienst du EP (Elegant 0.4, Jewel 1.2, Royal 2.0). " +
                        "Täglich verliert jeder Spieler eine gewisse Menge an EP\n\n");
        channel.sendMessageEmbeds(rating.build()).queue();
    }

    private void sendGameInfo(TextChannel channel) {
        EmbedBuilder hub = new EmbedBuilder()
                .setTitle("Game - Hub")
                .setDescription("Dies sind die allgemeinen Befehle, welche dir zur Verfügung stehen")
                .addField("Daily:","Hol dir einmal pro Tag eine 2.000 <:gold_coin:886658702512361482> Belohnung ab (Reset um 3:00)",false)
                .addField("Statistik:", "Sieh nach, wie viel <:gold_coin:886658702512361482> du hast und in welchem Spiel du am meisten verdienst",false)
                .addField("Transfer:","Über diesen Button kannst du jemand anderen <:gold_coin:886658702512361482> senden.\nDafür musst du in Discord unter den Einstellungen/Erweitert den Entwicklermodus aktivieren",false)
                .addField("Decks:","Ändere dein Deck Aussehen, nachdem du weitere freigeschalten hast",false)
                .addField("Boost:","Damit kannst du jeden anderen Spieler einmal pro Tag boosten (500 <:gold_coin:886658702512361482>)",false)
                .addField("Suche","Suche nach dem Profil eines anderen Spielers",false);

        MessageCreateBuilder messageBuilder = new MessageCreateBuilder()
                .addEmbeds(hub.build())
                .setComponents(
                        ActionRow.of(
                            Button.secondary("game daily", "Daily"),
                            Button.secondary("game stats","Statistik"),
                            Button.secondary("game send","Transfer"),
                            Button.secondary("game deck","Decks"),
                            Button.primary("game boost","Boost")
                        ));
        channel.sendMessage(messageBuilder.build()).queue();

        hub = new EmbedBuilder().setDescription("Template. Please remove!");
        messageBuilder.clear().addEmbeds(hub.build()).setComponents(ActionRow.of(
                Button.secondary("game search","Suche")
        ));
        channel.sendMessage(messageBuilder.build()).queue();
    }

    private void sendShop(TextChannel channel) {
        Role moneyRole = channel.getGuild().getRoleById("988280763831173170");
        Role blackjackRole = channel.getGuild().getRoleById("988280464626311180");

        if (Objects.isNull(moneyRole) || Objects.isNull(blackjackRole)) {
            sendFeedBack("Konnte Rollen nicht laden!",ServerUtil.RED,channel);
            return;
        }

        EmbedBuilder shop = new EmbedBuilder()
                .setThumbnail("https://www.iconpacks.net/icons/2/free-dollar-coin-icon-2149-thumb.png")
                .setTitle("Shop")
                .setDescription(String.format("Benutze das unten vorhandene Auswahl Menu um den jeweiligen Artikel zu kaufen\n" +
                        "\n-> **Rang** %s - __3.000.000__ <:gold_coin:886658702512361482>\n-> **Rang** %s - <:gold:1007022493955010631> Gold Elo\n" +
                        "-> Daily um 100 <:gold_coin:886658702512361482> erhöhen - __5.000__ <:gold_coin:886658702512361482>\n", moneyRole.getAsMention(), blackjackRole.getAsMention()));

        MessageCreateBuilder messageBuilder = new MessageCreateBuilder()
                .addEmbeds(shop.build())
                .setComponents(ActionRow.of(
                        Button.secondary("game shop_money_maker", "Kaufe Money Maker"),
                        Button.secondary("game shop_blackjack_peaker","Kaufe Blackjack Peaker"),
                        Button.secondary("game shop_daily_bonus","Kaufe Daily erhöhen")
                ));
        channel.sendMessage(messageBuilder.build()).queue();

        EmbedBuilder deck = new EmbedBuilder()
                .setTitle("Deck´s")
                .setThumbnail("https://cdn.discordapp.com/attachments/990065719217647686/991118602273050724/deck.png")
                .setDescription("Kauf dir Skins für deine Spielmodis, die du im Hub wechseln kannst")
                .addField("","**Elegant** (Deck)\n[Keine Animation]\n__100.000__ <:gold_coin:886658702512361482>\n<:elegant_two:990748540550672465>" +
                        " <:elegant_ass:990748519969214474>\n<:elegant_spades:990748536482185286> <:elegant_clubs:990748522481598534>",true)
                .addField("","**Jewel** (Deck)\n[Keine Animation]\n__250.000__ <:gold_coin:886658702512361482>\n<:jewel_two:991646397524881459>" +
                        " <:jewel_ass:991646398950952971>\n<:jewel_clubs:991646402453196800> <:jewel_diamond:991646405221425183>",true)
                .addField("","**Royal** (Deck)\n[Spezial Ass]\n__1.000.000__ <:gold_coin:886658702512361482>\n<:royal_two:991668023284998245>" +
                        " <a:royal_ass:991668026225209344>\n<:royal_heart:991668013000572949> <:royal_diamonds:991668008143560774>",true);

        messageBuilder.clear().addEmbeds(deck.build())
                .setComponents(ActionRow.of(
                        Button.secondary("game shop_deck_elegant", "Kaufe Elegant"),
                        Button.secondary("game shop_deck_jewel", "Kaufe Jewel"),
                        Button.secondary("game shop_deck_royal","Kaufe Royal")
                ));
        channel.sendMessage(messageBuilder.build()).queue();

        EmbedBuilder lootbox = new EmbedBuilder()
                .setThumbnail("https://cdn.discordapp.com/attachments/990065719217647686/991124506338934934/box.png")
                .setImage("https://cdn.discordapp.com/attachments/990065719217647686/1008099380873678999/chest.png")
                .setTitle("Mystery Chest")
                .setDescription("Du magst den Nervenkitzel? Dann Versuch doch dein Glück und öffne ein paar Kisten.\n" +
                        "```diff\n- Jeder Spieler darf pro Tag 50 Elegant, 25 Jewel & 10 Royal Chests öffnen\n```");

        messageBuilder.clear().addEmbeds(lootbox.build())
                .setComponents(ActionRow.of(
                        Button.primary("game shop_chest", "Chest/s kaufen"),
                        Button.danger("game shop_chest_max", "Max kaufen")
                ));
        channel.sendMessage(messageBuilder.build()).queue();
    }

    private EmbedBuilder sendStats(GamePlayerFullModel model, Member m) {
        return new EmbedBuilder()
                .setAuthor(m.getEffectiveName(), m.getEffectiveAvatarUrl(), m.getEffectiveAvatarUrl())
                .setDescription(String.format("**User:** %s\n**ID:** %s\n**Daily Bonus:** %s\n\n", m.getAsMention(), m.getId()
                        , BlackJackGame.formatNumber((long)model.getDailyBonus()) + " <:gold_coin:886658702512361482>"))
                .addField("__Kontostand__",String.format("> %s <:gold_coin:886658702512361482>", (model.getMoney() > 0 ? "+ " : "")
                        + formatNumber(model.getMoney()).replace("-","- ")),true)
                .addBlankField(true)
                .addField("__Rating__", String.format("> Blackjack: %s _%.1f_ EP\n> Chest: %s _%.1f EP_"
                        , pointsToRank((int)model.getRating_blackjack()), model.getRating_blackjack()
                        , pointsToRank((int) model.getRating_chest()), model.getRating_chest()).replace(".","/"), true);
    }

    @Override
    public void performTextChannelReaction(Member member, TextChannel channel, MessageReaction reaction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void performPrivateChannelCommand(User user, String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void performButtonInteraction(Member member, TextChannel channel, String customID, ButtonInteractionEvent event) {
        Optional<GamePlayerFullModel> player = ConnectionUtil.executeSingleSQL(ConnectionUtil.SELECT_GAME_FULL(member.getId()), GamePlayerFullModel.class);
        switch (customID) {
            case "daily":
                if (!player.isPresent()) {
                    ConnectionUtil.executeSQL(ConnectionUtil.INSERT_GAME(member.getId()));
                    ConnectionUtil.executeSQL(ConnectionUtil.UPDATE_GAME_DAILY(member.getId(), 7000L));

                    sendFeedBack(String.format("**User:** %s\nDu hast dein tägliches Geld abgeholt\n**Kontostand:** 7000 <:gold_coin:886658702512361482>", member.getAsMention()), ServerUtil.GREEN, event);
                    LogService.addLog("game_event",String.format("%s (%s) created a new Game account", member.getEffectiveName(), member.getId()));
                } else {
                    if (player.get().getDaily()) {
                        long daily = player.get().getDailyBonus() + 2000L;
                        daily *= EventManager.getDailyMultiply();
                        ConnectionUtil.executeSQL(ConnectionUtil.UPDATE_GAME_DAILY(member.getId(), daily));

                        sendFeedBack(String.format("**User:** %s\nDu hast deine tägliche Belohnnung abgeholt\n**Kontostand:** %s <:gold_coin:886658702512361482>"
                                , member.getAsMention(), formatNumber(player.get().getMoney() + daily)), ServerUtil.GREEN, event);
                        LogService.addLog("game_event",String.format("%s (%s) received his daily login", member.getEffectiveName(), member.getId()));
                    } else {

                        sendFeedBack(String.format("**User:** %s\nDu kannst aktuell deine tägliche Belohnung noch nicht abholen!" +
                                "\n**Verfügbar:** 03:00 \n**Kontostand:** %s <:gold_coin:886658702512361482>", member.getAsMention(), formatNumber(player.get().getMoney())), ServerUtil.RED, event);
                    }
                }
                break;
            case "stats":
                if (!player.isPresent()) {
                    ConnectionUtil.executeSQL(ConnectionUtil.INSERT_GAME(member.getId()));
                    event.replyEmbeds(sendStats(new GamePlayerFullModel(member.getId(), true, "",0
                            , 5000L, 0.0, 0.0, "DEFAULT","DEFAULT,"), member)
                            .build()).setEphemeral(true).queue();
                } else {
                    event.replyEmbeds(sendStats(player.get(), member).build()).setEphemeral(true).queue();
                }
                break;
            case "send":
                TextInput receiverSend = TextInput.create("receiver", "An:", TextInputStyle.SHORT)
                        .setPlaceholder("ID des Users (z.B. 286628057551208450)")
                        .setRequiredRange(18,18)
                        .setRequired(true)
                        .build();

                TextInput amount = TextInput.create("amount", "Summe:", TextInputStyle.SHORT)
                        .setPlaceholder("Menge die du senden willst")
                        .setRequired(true)
                        .setMinLength(1)
                        .build();

                Modal modalSend = Modal.create("game send", "Game - Transfer")
                        .addActionRows(ActionRow.of(receiverSend), ActionRow.of(amount))
                        .build();

                event.replyModal(modalSend).queue();
                break;
            case "boost":
                TextInput receiverBoost = TextInput.create("receiver", "User ID:", TextInputStyle.SHORT)
                        .setPlaceholder("z.B. 286628057551208450")
                        .setRequiredRange(18,18)
                        .setRequired(true)
                        .build();

                Modal modalBoost = Modal.create("game boost", "Game - Player Boost")
                        .addActionRows(ActionRow.of(receiverBoost))
                        .build();

                event.replyModal(modalBoost).queue();
                break;
            case "deck":
                if (!player.isPresent()) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt noch kein Konto!", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                TextInput deckAvailable = TextInput.create("available", "Deine Verfügbaren Decks:", TextInputStyle.SHORT)
                        .setPlaceholder(player.get().getAvaiableDecks())
                        .setRequiredRange(1,1)
                        .setRequired(false)
                        .build();

                TextInput deckSelect = TextInput.create("select", "Deck:", TextInputStyle.SHORT)
                        .setPlaceholder("Trage eins von den oben stehenden Decks ein")
                        .setRequired(true)
                        .build();

                Modal modalDeck = Modal.create("game deck", "Game - Deck auswahl")
                        .addActionRows(ActionRow.of(deckAvailable), ActionRow.of(deckSelect))
                        .build();

                event.replyModal(modalDeck).queue();
                break;
            case "search":
                TextInput search = TextInput.create("userid", "User-ID:", TextInputStyle.SHORT)
                        .setPlaceholder("ID des Users (z.B. 286628057551208450)")
                        .setRequiredRange(18,18)
                        .setRequired(true)
                        .build();

                Modal modalSearch = Modal.create("game search", "Game - Suche")
                        .addActionRows(ActionRow.of(search))
                        .build();

                event.replyModal(modalSearch).queue();
                break;
            case "shop_money_maker":
                if (member.getRoles().stream().map(ISnowflake::getId).anyMatch(id -> id.equals("988280763831173170"))) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt bereits diesen Rang!", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                if (!player.isPresent()) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt noch kein Konto!", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                if (player.get().getMoney() < 3000000L) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt nicht genug <:gold_coin:886658702512361482>!\n**Kontostand:** "
                            + BlackJackGame.formatNumber(player.get().getMoney()) + " <:gold_coin:886658702512361482>", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                ConnectionUtil.executeSQL(ConnectionUtil.UPDATE_GAME_MONEY(member.getId(),-3000000));
                channel.getGuild().addRoleToMember(UserSnowflake.fromId(member.getId()), channel.getGuild().getRoleById("988280763831173170")).queue();

                sendFeedBack(String.format("**User:** %s\nDu besitzt nun den Rang des Money Makers!", member.getAsMention()), ServerUtil.GREEN, event);
                LogService.addLog("game_event",String.format("%s (%s) bought the @Money_Maker rank", member.getEffectiveName(), member.getId()));
                break;
            case "shop_blackjack_peaker":
                if (member.getRoles().stream().map(ISnowflake::getId).anyMatch(id -> id.equals("988280464626311180"))) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt bereits diesen Rang!", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                if (!player.isPresent()) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt noch kein Konto!", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                if (player.get().getRating_blackjack() < 1500) {
                    sendFeedBack(String.format("**User:** %s\nDein _Blackjack Rating_ ist zu niedrig!\n**Blackjack:** "
                            + player.get().getRating_blackjack() + pointsToRank((int)player.get().getRating_blackjack()), member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                channel.getGuild().addRoleToMember(UserSnowflake.fromId(member.getId()), channel.getGuild().getRoleById("988280763831173170")).queue();

                sendFeedBack(String.format("**User:** %s\nKeiner zählt die Karten so wie du es tust!", member.getAsMention()), ServerUtil.GREEN, event);
                LogService.addLog("game_event",String.format("%s (%s) bought the @Blackjack Peaker rank", member.getEffectiveName(), member.getId()));
                break;
            case "shop_daily_bonus":
                if (!player.isPresent()) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt noch kein Konto!", member.getAsMention()), ServerUtil.RED,event);
                    return;
                }

                TextInput dailySum = TextInput.create("amount","Menge", TextInputStyle.SHORT)
                        .setRequired(true)
                        .setPlaceholder("Die Menge die du haben willst")
                        .build();

                Modal dailyModal = Modal.create("game daily","Game - Daily einkaufen")
                        .addActionRows(ActionRow.of(dailySum))
                        .build();

                event.replyModal(dailyModal).queue();
                break;
            case "shop_deck_elegant":
                if (!player.isPresent()) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt noch kein Konto!", member.getAsMention()), ServerUtil.RED,event);
                    return;
                }

                if (player.get().getAvaiableDecks().contains("ELEGANT")) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt bereits das Deck!", member.getAsMention()), ServerUtil.RED,event);
                    return;
                }

                if (player.get().getMoney() < 100000L) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt nicht genug <:gold_coin:886658702512361482>!\n**Kontostand:** "
                            + BlackJackGame.formatNumber(player.get().getMoney()) + " <:gold_coin:886658702512361482>", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                ConnectionUtil.executeSQL(ConnectionUtil.UPDATE_GAME_MONEY(member.getId(),-100000));
                ConnectionUtil.executeSQL(ConnectionUtil.UPDATE_GAME_DECKS(member.getId(), player.get().getAvaiableDecks() + "ELEGANT,"));
                sendFeedBack(String.format("**User:** %s\nDu besitzt nun das Elegant Deck!", member.getAsMention()), ServerUtil.GREEN, event);
                LogService.addLog("game_event",String.format("%s (%s) bought the Elegant deck", member.getEffectiveName(), member.getId()));
                break;
            case "game shop_deck_jewel":
                if (!player.isPresent()) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt noch kein Konto!", member.getAsMention()), ServerUtil.RED,event);
                    return;
                }

                if (player.get().getAvaiableDecks().contains("JEWEL")) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt bereits das Deck!", member.getAsMention()), ServerUtil.RED,event);
                    return;
                }

                if (player.get().getMoney() < 250000L) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt nicht genug <:gold_coin:886658702512361482>!\n**Kontostand:** "
                            + BlackJackGame.formatNumber(player.get().getMoney()) + " <:gold_coin:886658702512361482>", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                ConnectionUtil.executeSQL(ConnectionUtil.UPDATE_GAME_MONEY(member.getId(),-250000));
                ConnectionUtil.executeSQL(ConnectionUtil.UPDATE_GAME_DECKS(member.getId(), player.get().getAvaiableDecks() + "JEWEL,"));
                sendFeedBack(String.format("**User:** %s\nDu besitzt nun das Jewel Deck!", member.getAsMention()), ServerUtil.GREEN, event);
                LogService.addLog("game_event",String.format("%s (%s) bought the Jewel deck", member.getEffectiveName(), member.getId()));
                break;
            case "shop_deck_royal":
                if (!player.isPresent()) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt noch kein Konto!", member.getAsMention()), ServerUtil.RED,event);
                    return;
                }

                if (player.get().getAvaiableDecks().contains("ROYAL")) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt bereits das Deck!", member.getAsMention()), ServerUtil.RED,event);
                    return;
                }

                if (player.get().getMoney() < 1000000L) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt nicht genug <:gold_coin:886658702512361482>!\n**Kontostand:** "
                            + BlackJackGame.formatNumber(player.get().getMoney()) + " <:gold_coin:886658702512361482>", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                ConnectionUtil.executeSQL(ConnectionUtil.UPDATE_GAME_MONEY(member.getId(),-1000000));
                ConnectionUtil.executeSQL(ConnectionUtil.UPDATE_GAME_DECKS(member.getId(), player.get().getAvaiableDecks() + "ROYAL,"));
                sendFeedBack(String.format("**User:** %s\nDu besitzt nun das Royal Deck!", member.getAsMention()), ServerUtil.GREEN, event);
                LogService.addLog("game_event",String.format("%s (%s) bought the Royal deck", member.getEffectiveName(), member.getId()));
                break;
            case "shop_chest":
                if (!player.isPresent()) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt noch kein Konto!", member.getAsMention()), ServerUtil.RED,event);
                    return;
                }

                TextInput elegantChest = TextInput.create("elegant", "Elegant Chest/s", TextInputStyle.SHORT)
                        .setValue("0")
                        .setPlaceholder("Zahl größer >= 0")
                        .setRequired(true)
                        .setMinLength(1)
                        .build();

                TextInput jewelChest = TextInput.create("jewel", "Jewel Chest/s", TextInputStyle.SHORT)
                        .setValue("0")
                        .setPlaceholder("Zahl größer >= 0")
                        .setRequired(true)
                        .setMinLength(1)
                        .build();

                TextInput royalChest = TextInput.create("royal", "Royal Chest/s", TextInputStyle.SHORT)
                        .setValue("0")
                        .setPlaceholder("Zahl größer >= 0")
                        .setRequired(true)
                        .setMinLength(1)
                        .build();

                Modal modal = Modal.create("game bundle", "Game - Box Bundle kaufen")
                        .addActionRows(ActionRow.of(elegantChest), ActionRow.of(jewelChest), ActionRow.of(royalChest))
                        .build();

                event.replyModal(modal).queue();
                break;
            case "shop_chest_max":
                if (!player.isPresent()) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt noch kein Konto!", member.getAsMention()), ServerUtil.RED,event);
                    return;
                }

                ChestRequest request = new ChestRequest(player.get());
                ChestRequest.getLeftChests(request);

                long price = request.getPrice();

                if (price == 0L) {
                    sendFeedBack(String.format("**User:** %s\nDu hast bereits das Limit erreicht!", member.getAsMention()), ServerUtil.RED,event);
                    return;
                }

                if (player.get().getMoney() < price) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt nicht genug <:gold_coin:886658702512361482>!\n**Kontostand:** "
                            + BlackJackGame.formatNumber(player.get().getMoney()) + " <:gold_coin:886658702512361482>", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                ChestRequest.getElegentWorker().accept(request);
                ChestRequest.getJewelWorker().accept(request);
                ChestRequest.getRoyalWorker().accept(request);

                ConnectionUtil.executeSQL(ConnectionUtil.UPDATE_GAME_BUNDLE(request));
                event.replyEmbeds(ChestRequest.requestToMessage(member, request).build()).setEphemeral(true).queue();
                LogService.addLog("game_event", String.format("%s (%s) bought a bundle (%d E, %d J, %d R) and received %s Unit/s"
                        , member.getEffectiveName(), member.getId(), request.getElegant(), request.getJewel(), request.getRoyal(), BlackJackGame.formatNumber(request.getMoney())));

                break;
        }
    }

    @Override
    public void performModalInteraction(Member member, TextChannel channel, String customID, ModalInteractionEvent event) {
        switch (customID) {
            case "game send": {
                Optional<GamePlayerModel> sender = ConnectionUtil.executeSingleSQL(ConnectionUtil.SELECT_GAME(member.getId()), GamePlayerModel.class);

                if (!sender.isPresent()) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt noch kein Konto um Geld zu senden!", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                Optional<GamePlayerModel> receiver = ConnectionUtil.executeSingleSQL(ConnectionUtil.SELECT_GAME
                        (event.getValue("receiver").getAsString()), GamePlayerModel.class);
                if (!receiver.isPresent()) {
                    sendFeedBack(String.format("**User:** %s\nKonnte angegebenen User nicht finden!", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                long amount;
                try {
                    amount = Long.parseLong(event.getValue("amount").getAsString());
                } catch (NumberFormatException ignored) {
                    sendFeedBack(String.format("**User:** %s\nBitte trage eine gültige Geld summe ein! (Nur Zahlen)", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                if (amount == 0) {
                    sendFeedBack(String.format("**User:** %s\nDu musst mehr als 0 <:gold_coin:886658702512361482> senden!", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                if (amount > sender.get().getMoney()) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt nicht genug geld!\n**Kontostand:** %s <:gold_coin:886658702512361482>"
                            , member.getAsMention(), sender.get().getMoney()), ServerUtil.RED, event);
                    return;
                }

                ConnectionUtil.executeSQL(ConnectionUtil.UPDATE_GAME_MONEY(member.getId(), -amount));
                ConnectionUtil.executeSQL(ConnectionUtil.UPDATE_GAME_MONEY(receiver.get().getUserid(), amount));
                sendFeedBack(String.format("**User:** %s\nDu hast erfolgreich Geld versendet!\n**Kontostand:** %s<:gold_coin:886658702512361482>\n\n**An:** %s\n**Summe:** %s<:gold_coin:886658702512361482>"
                        , member.getAsMention(), formatNumber(sender.get().getMoney() - amount), receiver.get().getUserid(), formatNumber(amount)), ServerUtil.GREEN, event);
                LogService.addLog("game_transaction", String.format("%s (%s) sended %s Unit/s to %s", member.getEffectiveName(), member.getId(), amount, receiver.get().getUserid()));
                break;
            }
            case "game boost": {
                Optional<GamePlayerModel> receiver = ConnectionUtil.executeSingleSQL(ConnectionUtil.SELECT_GAME
                        (event.getValue("receiver").getAsString()), GamePlayerModel.class);

                if (!receiver.isPresent()) {
                    sendFeedBack(String.format("**User:** %s\nDer User besitzt noch kein Konto!", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                if (event.getValue("receiver").getAsString().equals(member.getId())) {
                    sendFeedBack(String.format("**User:** %s\nNice try, aber du kannst dich nicht selber boosten!", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                if (receiver.get().getBooster().contains(member.getId())) {
                    sendFeedBack(String.format("**User:** %s\nDu hast bereits diesen User heute geboostet!", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                ConnectionUtil.executeSQL(ConnectionUtil.UPDATE_GAME_BOOST(receiver.get().getUserid(), member.getId(), receiver.get().getBooster()));
                sendFeedBack(String.format("**User:** %s\nDu hast erfolgreich _%s_ geboostet!", member.getAsMention(), receiver.get().getUserid()), ServerUtil.GREEN, event);
                LogService.addLog("game_event", String.format("%s (%s) boosted %s", member.getEffectiveName(), member.getId(), receiver.get().getUserid()));
                break;
            }
            case "game deck":
                Optional<GamePlayerModel> player = ConnectionUtil.executeSingleSQL(ConnectionUtil.SELECT_GAME(member.getId()), GamePlayerModel.class);

                String deck = event.getValue("select").getAsString();

                if (!player.get().getAvaiableDecks().contains(deck)) {
                    sendFeedBack(String.format("**User:** %s\nDieses Deck ist nicht in deinem besitzt oder existiert nicht!", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                if (player.get().getSelectDeck().equals(deck)) {
                    sendFeedBack(String.format("**User:** %s\nDu hast dieses Deck bereits ausgewählt!", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                ConnectionUtil.executeSQL(ConnectionUtil.UPDATE_GAME_SELECT_DECK(member.getId(), deck));
                sendFeedBack(String.format("**User:** %s\nDein Deck wurde erfolgreich auf %s gewechselt!", member.getAsMention(), deck), ServerUtil.GREEN, event);
                break;
            case "game search":
                String searchID = event.getValue("userid").getAsString();
                Optional<GamePlayerFullModel> searchRequest = ConnectionUtil.executeSingleSQL(ConnectionUtil.SELECT_GAME_FULL(searchID), GamePlayerFullModel.class);

                if (!searchRequest.isPresent()) {
                    sendFeedBack(String.format("**User:** %s\nKonnte Angegebenen User nicht finden!", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                event.replyEmbeds(sendStats(searchRequest.get(),channel.getGuild().retrieveMemberById(searchID).complete()).build())
                        .setEphemeral(true).queue();
                break;
            case "game bundle":
                int elegantChest;
                int jewelChest;
                int royalChest;

                String tmp = event.getValue("elegant").getAsString();
                if (notNumeric(tmp)) {
                    sendFeedBack(String.format("**User:** %s\nGib bitte nur Zahlen ein, für das Bundle!", member.getAsMention()), RED, event);
                }
                elegantChest = Integer.parseInt(tmp);

                tmp = event.getValue("jewel").getAsString();
                if (notNumeric(tmp)) {
                    sendFeedBack(String.format("**User:** %s\nGib bitte nur Zahlen ein, für das Bundle!", member.getAsMention()), RED, event);
                }
                jewelChest = Integer.parseInt(tmp);

                tmp = event.getValue("royal").getAsString();
                if (notNumeric(tmp)) {
                    sendFeedBack(String.format("**User:** %s\nGib bitte nur Zahlen ein, für das Bundle!", member.getAsMention()), RED, event);
                }
                royalChest = Integer.parseInt(tmp);

                Optional<GamePlayerModel> creator = ConnectionUtil.executeSingleSQL(ConnectionUtil.SELECT_GAME(member.getId()), GamePlayerModel.class);
                if (!creator.isPresent()) {
                    return;
                }

                ChestRequest request = new ChestRequest(creator.get());
                request.setChestAmount(elegantChest, jewelChest, royalChest);
                if (!ChestRequest.validRequest(request)) {
                    sendFeedBack(String.format("**User:** %s\nDein Einkauf überschreitet das Daily Limit!", member.getAsMention()), RED, event);
                    return;
                }

                long price = request.getPrice();
                request.addMoney(-price);

                if (creator.get().getMoney() < price) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt nicht genug <:gold_coin:886658702512361482>!\n**Kontostand:** "
                            + BlackJackGame.formatNumber(creator.get().getMoney()) + " <:gold_coin:886658702512361482>", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                ChestRequest.getElegentWorker().accept(request);
                ChestRequest.getJewelWorker().accept(request);
                ChestRequest.getRoyalWorker().accept(request);

                ConnectionUtil.executeSQL(ConnectionUtil.UPDATE_GAME_BUNDLE(request));
                event.replyEmbeds(ChestRequest.requestToMessage(member, request).build()).setEphemeral(true).queue();
                LogService.addLog("game_event", String.format("%s (%s) bought a bundle (%d E, %d J, %d R) and received %s Unit/s"
                        , member.getEffectiveName(), member.getId(), elegantChest, jewelChest, royalChest, BlackJackGame.formatNumber(request.getMoney())));

                break;
            case "game daily":
                Optional<GamePlayerModel> buyer = ConnectionUtil.executeSingleSQL(ConnectionUtil.SELECT_GAME(member.getId()), GamePlayerModel.class);
                if (!buyer.isPresent()) {
                    return;
                }

                if (notNumeric(event.getValue("amount").getAsString())) {
                    sendFeedBack(String.format("**User:** %s\nGib bitte nur Zahlen ein!", member.getAsMention()), RED, event);
                }

                int amount = Integer.parseInt(event.getValue("amount").getAsString());

                if (amount < 1) {
                    sendFeedBack(String.format("**User:** %s\nGib bitte nur Zahlen ein welche größer als 0 sind!", member.getAsMention()), RED, event);
                }

                if (buyer.get().getMoney() < (amount * 5000L)) {
                    sendFeedBack(String.format("**User:** %s\nDu besitzt nicht genug <:gold_coin:886658702512361482>!\n**Kontostand:** "
                            + BlackJackGame.formatNumber(buyer.get().getMoney()) + " <:gold_coin:886658702512361482>", member.getAsMention()), ServerUtil.RED, event);
                    return;
                }

                ConnectionUtil.executeSQL(ConnectionUtil.UPDATE_GAME_DAILY_BONUS(member.getId(), amount * 100));
                sendFeedBack(String.format("**User:** %s\nDein Daily Bonus wurde um %s <:gold_coin:886658702512361482> erhöht\nund ist somit auf %s <:gold_coin:886658702512361482>!"
                        , member.getAsMention(), BlackJackGame.formatNumber(amount * 100L)
                        , BlackJackGame.formatNumber((long) buyer.get().getDailyBonus() + (amount * 100L))), GREEN, event);
                break;
        }
    }

    public static String pointsToRank(int points) {
        if (points >= 3000) {
            return "<:legende:1007022496433848400>";
        }
        if (points >= 2500) {
            return "<:diamant:1007022492000452789>" + getDivision(points - 2500);
        }
        if (points >= 2000) {
            return "<:platin:1007022498296103024>" + getDivision(points - 2000);
        }
        if (points >= 1500) {
            return "<:gold:1007022493955010631>" + getDivision(points - 1500);
        }
        if (points >= 1000) {
            return "<:silber:1007022487411896320>" + getDivision(points - 1000);
        }
        if (points >= 500) {
            return "<:bronze:1007022490037518526>" + getDivision(points - 500);
        }
        if (points >= -50) {
            return "<:unranked:1007022488754081822>";
        }
        return "(Ruiniert)";
    }

    private static String getDivision(int points) {
        int div = 1 + (points / 100);
        switch (div) {
            case 1: return "Div I";
            case 2: return "Div II";
            case 3: return "Div III";
            case 4: return "Div IV";
            case 5: return "Div V";
            default: return "undefine";
        }
    }

    private String formatNumber(Long value) {
        DecimalFormat df = new DecimalFormat("#,###.##");
        return df.format(value);
    }

    private boolean notNumeric(String value) {
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return true;
        }
        return false;
    }
}
