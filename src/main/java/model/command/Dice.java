package model.command;

import model.command.type.ServerCommand;
import model.command.type.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import util.AssetPool;
import util.ServerUtil;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Dice implements ServerCommand, SlashCommand {

    @Override
    public void performTextChannelCommand(Member member, TextChannel channel, String message) {
        String[] args = message.split(" ");

        if (args.length == 1) {
            String result = dice(1,6);
            EmbedBuilder eb = new EmbedBuilder()
                    .setColor(ServerUtil.BLUE)
                    .setAuthor(member.getEffectiveName(), member.getAvatarUrl(), null)
                    .setDescription(String.format("Dein Würfel fällt auf: **%s**", result))
                    .setThumbnail(AssetPool.getURL("url_dice_" + result));
            channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(5, TimeUnit.SECONDS);
            return;
        }

        if (args.length == 2) {
            if (isNonNumeric(args[1])) {
                return;
            }
            EmbedBuilder eb = new EmbedBuilder()
                    .setColor(ServerUtil.BLUE)
                    .setAuthor(member.getEffectiveName(), member.getAvatarUrl(), null)
                    .setDescription(String.format("Deine %s Würfel fallen auf:\n**%s**", args[1], dice(Integer.parseInt(args[1]),6)))
                    .setFooter("Nachricht wird in 20 Sekunden gelöscht");
            channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(20, TimeUnit.SECONDS);
            return;
        }

        if (args.length == 3) {
            if (isNonNumeric(args[1]) || isNonNumeric(args[2])) {
                return;
            }
            EmbedBuilder eb = new EmbedBuilder()
                    .setColor(ServerUtil.BLUE)
                    .setAuthor(member.getEffectiveName(), member.getAvatarUrl(), null)
                    .setDescription(String.format("Deine %s Würfel fallen auf:\n**%s**\nAugenzahl: %s", args[1], dice(Integer.parseInt(args[1]),Integer.parseInt(args[2])), args[2]))
                    .setFooter("Nachricht wird in 20 Sekunden gelöscht");
            channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(20, TimeUnit.SECONDS);
        }
    }

    @Override
    public void performSlashCommand(Member member, TextChannel channel, String command, SlashCommandInteractionEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.WHITE);
        eb.setAuthor(member.getEffectiveName(), member.getEffectiveAvatarUrl(), member.getEffectiveAvatarUrl());
        int dices = 1;
        int eyes = 6;
        for (OptionMapping o : event.getOptions()) {
            if (o.getName().equals("dices")) {
                dices = o.getAsInt();
                if (dices < 1) {
                    dices = 1;
                }
            }
            if (o.getName().equals("eyes")) {
                eyes = o.getAsInt();
                if (eyes < 2) {
                    eyes = 2;
                }
            }
        }
        if (dices == 1) {
            eb.setTitle("Würfel fällt auf:");
        } else {
            eb.setTitle("Würfel fallen auf:");
        }
        eb.setDescription(dice(dices,eyes));
        event.replyEmbeds(eb.build()).setEphemeral(true).queue();
    }

    @Override
    public void performTextChannelReaction(Member member, TextChannel channel, MessageReaction reaction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void performPrivateChannelCommand(User user, String message) {
        throw new UnsupportedOperationException();
    }

    private String dice(int dices, int eyes) {
        StringBuilder s = new StringBuilder();

        for (int i = 0 ; i < dices ; i++) {
            s.append(ThreadLocalRandom.current().nextInt(1,eyes+1));
            if (i < dices-1) {
                s.append(", ");
            }
        }

        return s.toString();
    }

    private boolean isNonNumeric(String string) {
        try {
            Integer.parseInt(string);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }
}
