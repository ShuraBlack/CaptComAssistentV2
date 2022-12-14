package model.command;

import model.command.type.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import util.AssetPool;
import util.ServerUtil;

import java.util.List;

public class Help implements ServerCommand {

    @Override
    public void performTextChannelCommand(Member member, TextChannel channel, String message) {
        if(!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
            return;
        }

        if(!ServerUtil.isChannelId(channel,"request")) {
            return;
        }

        if (message.equals("!help")) {
            channel.createThreadChannel("Befehle",false).complete();
            channel.createThreadChannel("Warframe Tracker", false).complete();

            List<ThreadChannel> subChannel = channel.getThreadChannels();
            for (ThreadChannel tc : subChannel) {
                if (tc.getName().equals("Befehle")) {
                    tc.sendMessageEmbeds(createMessage().build()).complete();
                } else if (tc.getName().equals("Warframe Tracker")) {
                    EmbedBuilder wt  = new EmbedBuilder()
                            .setTitle("Warframe Tracker - Befehle")
                            .setDescription("**Prefix:** !wftracker\n" +
                                    "- cycle (Cycles of the Open-World-Maps)\n" +
                                    "- arbi (Arbitration info, like mission type, faction,...)\n" +
                                    "- construct (Construction Progress of Razorback and Fomorian)\n" +
                                    "- sortie (Sortie Missions & general)\n" +
                                    "- nightwave (Season info & current missions)\n" +
                                    "- voidtrader (When he will be available and what he got)");
                    tc.sendMessageEmbeds(wt.build()).complete();
                }
            }

            channel.sendMessageEmbeds(createMessage().build()).complete();
        }
    }

    @Override
    public void performTextChannelReaction(Member member, TextChannel channel, MessageReaction reaction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void performPrivateChannelCommand(User user, String message) {
        throw new UnsupportedOperationException();
    }

    public EmbedBuilder createMessage () {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setThumbnail(AssetPool.getURL("url_help"));
        eb.setTitle("Allgemeine Befehle: ");
        eb.setDescription("Hier kannst du sehen welche Befehle du als normaler Nutzer ausf??hren darfst");
        eb.addField("__Zufallsbefehle:__","\n" +
                "**!dice** ```cs\n" +
                "wirft einen 6 seitigen W??rfel\n" +
                "```\n" +
                "**!dice [zahl]**```cs\n" +
                "wirft einen [\"zahl\"] seitigen W??rfel\n" +
                "```\n" +
                "**!dice match** ```cs\n" +
                "startet ein W??rfelspiel, beidem ein anderer Nutzer gegen dich spielen kann\n" +
                "# Andere Nutzer k??nnen ??ber Reaction beitreten\n" +
                "```\n" +
                "**!dice invitematch [userID]**```cs\n" +
                "L??d einen Nutzer zu einem aktiven *Dice Match* von dir ein\n" +
                "# Der Nutzer erh??lt ein Link zum Match\n" +
                "```\n" +
                "**!coinflip**```cs\n" +
                "wirft eine M??nze mit Kopf oder Zahl\n" +
                "```\n" +
                "**!mdice [zahl]**```cs\n" +
                "wirft [\"zahl\"] an 6 seitgen W??rfeln\n" +
                "```\n" +
                "**!mdice [zahlA] [zahlB]**```cs\n" +
                "wirft [\"zahlA\"] an [\"zahlB\"] seitigen W??rfeln\n" +
                "```",false);
        eb.addField("__Playlist:__","\n" +
                "**!playlist add [playlistname] [link]**```cs\n" +
                "F??gt [\"link\"] in deine [\"playlistname\"] ein\n" +
                "```\n" +
                "**!playlist remove [ID]**```cs\n" +
                "Entfernt link mit [\"ID\"]\n" +
                "Wenn als [\"ID\"] \"all\" eingetragen wird, werden alle deine Lieder gel??scht\n" +
                "```\n" +
                "**!playlist show**```cs\n" +
                "Zeigt alle deine Lieder (playlist??bergreifend)\n" +
                "```\n" +
                "**!playlist show [playlistname]**```cs\n" +
                "Zeigt alle Lieder aus [\"playlistname\"]\n" +
                "```\n" +
                "**!playlist list**```cs\n" +
                "Zeigt all deine Playlists\n```",false);
        eb.addField("__Tempor??re VoiceChannel:__","Wenn du```cs\n" +
                "\"Create T:Voice\" beitritts, wird automatisch ein neuer VoiceChannel erstellt," +
                " mit der gleichen Maximalanzahl und zus??tzlichen Rechten f??r den Ersteller\n```",false);
        return eb;
    }
}
