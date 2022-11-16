package model.command;

import model.command.type.ServerCommand;
import model.command.type.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import util.AssetPool;
import util.ServerUtil;

import java.awt.*;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CoinFlip implements ServerCommand, SlashCommand {

    @Override
    public void performTextChannelCommand(Member member, TextChannel channel, String message) {
        if (!ServerUtil.isChannelId(channel,"request")) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setColor(Color.WHITE)
                    .setDescription(String.format("Nutze den %s TextChannel!", Objects.requireNonNull(channel.getGuild()
                            .getTextChannelById(ServerUtil.getChannelID("request"))).getAsMention()));
            channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(5, TimeUnit.SECONDS);
            return;
        }

        Random rand = new Random();
        int rdmNum = rand.nextInt(2);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.WHITE);
        eb.setTitle("Münze fällt für");
        eb.setDescription(String.format("%s auf:", member.getAsMention()));

        if(rdmNum == 1) {
            // HEAD
            eb.setThumbnail(AssetPool.getURL("url_coin_head"));
        } else {
            // TAIL
            eb.setThumbnail(AssetPool.getURL("url_coin_tail"));
        }
        channel.sendMessageEmbeds(eb.build())
                .complete().delete().queueAfter(5, TimeUnit.SECONDS);
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
    public void performSlashCommand(Member m, TextChannel channel, String command, SlashCommandInteractionEvent event) {
        Random rand = new Random();
        int rdmNum = rand.nextInt(2);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.WHITE);
        eb.setTitle("Münze fällt für");
        eb.setDescription(String.format("%s auf:", m.getAsMention()));

        if(rdmNum == 1) {
            // HEAD
            eb.setThumbnail(AssetPool.getURL("url_coin_head"));
        } else {
            // TAIL
            eb.setThumbnail(AssetPool.getURL("url_coin_tail"));
        }
        event.replyEmbeds(eb.build()).setEphemeral(true).queue();
    }
}
