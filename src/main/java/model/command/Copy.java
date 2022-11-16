package model.command;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import model.command.type.ServerCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import util.ServerUtil;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Copy implements ServerCommand {

    @Override
    public void performTextChannelCommand(Member member, TextChannel channel, String message) {
        if(!member.hasPermission(channel, Permission.MESSAGE_MANAGE)) {
            return;
        }

        String[] args = message.split(" ");
        try {
            int amount = Integer.parseInt(args[2]);
            TextChannel newChannel = channel.getGuild().getTextChannelById(args[1]);
            assert newChannel != null;
            if (!member.hasPermission(newChannel, Permission.MESSAGE_MANAGE)) {
                return;
            }

            List<Message> messages = get(channel, amount);
            Collections.reverse(messages);
            for (Message mes : messages) {
                WebhookEmbedBuilder eb = new WebhookEmbedBuilder()
                        .setAuthor(new WebhookEmbed.EmbedAuthor(member.getEffectiveName(), member.getAvatarUrl(), null))
                        .setTimestamp(OffsetDateTime.now())
                        .setDescription(mes.getContentDisplay());
                if (!mes.getAttachments().isEmpty()) {
                    List<Message.Attachment> attach = mes.getAttachments();
                    for (Message.Attachment ma : attach) {
                        eb.setImageUrl(ma.getUrl());
                        ServerUtil.sendWebHookMessage(newChannel.getId(), eb);
                    }
                } else {
                    if (mes.getContentDisplay().contains("https")) {
                        String[] parts = mes.getContentDisplay().split(" ");
                        if (parts.length != 1) {
                            return;
                        }
                        for (String s : parts) {
                            if (s.contains("http")) {
                                eb.setImageUrl(s);
                                break;
                            }
                        }
                    }
                    ServerUtil.sendWebHookMessage(newChannel.getId(), eb);
                }
            }
            channel.sendMessage(String.format("%d Nachrichten wurden verschoben!", amount))
                    .complete().delete().queueAfter(3, TimeUnit.SECONDS);

        } catch (NumberFormatException ignored) {
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

    private List<Message> get(MessageChannel channel, int amount) {
        List<Message> mes = new ArrayList<>();
        int i = amount;
        boolean first = true;
        for (Message message : channel.getIterableHistory().cache(false)) {
            if (first) {
                first = false;
                continue;
            }
            if(!message.isPinned()) {
                mes.add(message);
                if(--i <= 0) {
                    break;
                }
            }
        }
        return mes;
    }

}
