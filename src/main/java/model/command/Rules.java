package model.command;

import model.command.type.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import util.ServerUtil;

import java.awt.*;
import java.time.OffsetDateTime;

public class Rules implements ServerCommand {
    @Override
    public void performTextChannelCommand(Member member, TextChannel channel, String message) {
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        if (!ServerUtil.isChannelId(channel, "guidelines")) {
            return;
        }

        if (!message.equals("!rules")) {
            return;
        }

        EmbedBuilder rules = new EmbedBuilder()
                .setTitle("Server - Richtlinien")
                .setDescription("**§1** Beleidigungen, unhöfliches oder nervendes Benehmen, sowie Diskriminierung\n\n" +
                        "**§2** Das verhindern jeglicher Kommunikation zu Server Moderatoren\n\n" +
                        "**§3** Provokation jeglicher Art\n\n" +
                        "**§4** Nicknames werden, wenn sie *§1* verletzen, auf dem Server verändert\n\n" +
                        "**§5** Links mit unangemessenem Inhalt (z.B. Pornografische oder Gewaltätige Darstellung, ...)\n\n" +
                        "**§6** Eigenwerbung für z.B. Youtube Kanal, Facebook- und Twitter Fanpages, sowie Klans/Teams\n\n" +
                        "**§7** Hochgeladene Daten dürfen *§1,3,5,6* nicht verletzen\n\n" +
                        "**§8** Spam des Voice- oder TextChannels\n\n" +
                        "**§9** Ungekennzeichnete Bots ohne Absprache\n\n" +
                        "**§10** Betrug jeglicher Art\n\n" +
                        "_Missachten der Server-Richtlinien führt zum Ausschluss!_")
                .setFooter("Gültig ab dem")
                .setTimestamp(OffsetDateTime.now());
        EmbedBuilder ebbottom = new EmbedBuilder();
        ebbottom.setColor(Color.RED);
        ebbottom.addField("Meldet Probleme oder Beschwerden der Server Moderation",
                "Wie auch beim Gesetzt: Unwissenheit schützt nicht vor Strafe und deswegen " +
                        "empfehlen wir die Regeln gut durchzulesen und zu kennen",false);

        EmbedBuilder redirect = new EmbedBuilder();
        redirect.addField("Zur Rollenverteilung:"
                ,"[Roles Channel](https://discord.com/channels/286628427140825088/799449909090713631/1015080368036126832)",false);

        channel.sendMessageEmbeds(rules.build(), ebbottom.build(), redirect.build()).queue();
    }

    @Override
    public void performTextChannelReaction(Member member, TextChannel channel, MessageReaction reaction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void performPrivateChannelCommand(User user, String message) {
        throw new UnsupportedOperationException();
    }
}
