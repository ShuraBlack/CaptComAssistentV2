package model.command;

import model.command.type.ServerCommand;
import model.manager.ActionManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import util.ServerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Clear implements ServerCommand {

    @Override
    public void performTextChannelCommand(Member member, TextChannel channel, String message) {
        if (!member.hasPermission(Permission.MANAGE_CHANNEL)) {
            return;
        }

        String[] args = message.split(" ");

        if (args.length == 1) {
            try {
                channel.purgeMessages(get(channel));
                channel.sendMessage("Alle verfügbaren Nachrichten wurden gelöscht!")
                        .complete().delete().queueAfter(3, TimeUnit.SECONDS);
            } catch (IllegalArgumentException e) {
                ActionManager.LOGGER.info(String.format("Couldnt delete requested Messages <%s,%s>", member.getEffectiveName(),channel.getId()));
            }
        } else if (args.length == 2 && args[1].equals("reset")) {
            if (!ServerUtil.isChannelType(channel, ChannelType.TEXT)) {
                return;
            }

            channel.createCopy().setPosition(channel.getPosition()).queue();
            channel.delete().queue();
        } else if (args.length == 2) {
            try {
                int amount = Integer.parseInt(args[1]);
                channel.purgeMessages(get(channel, amount));
                channel.sendMessage(String.format("%d Nachrichten wurden gelöscht!",amount))
                        .complete().delete().queueAfter(3, TimeUnit.SECONDS);
            } catch (NumberFormatException ignored) {
            }
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

    /**
     * Create an List with the History of an channel
     * @param channel in which it got called
     * @return up to 100 entries in a List
     */
    private List<Message> get(MessageChannel channel) {
        List<Message> mes = new ArrayList<>();
        int i = 100;
        for (Message message : channel.getIterableHistory().cache(false)) {
            if(!message.isPinned()) {
                mes.add(message);
                if(--i <= 0) {
                    break;
                }
            }
        }
        return mes;
    }

    /**
     * Create an List with the History of an channel
     * @param channel in which it got called
     * @param amount of how many messages should be saved
     * @return List based on amount
     */
    private List<Message> get(MessageChannel channel, int amount) {
        List<Message> mes = new ArrayList<>();
        int i = amount;
        for (Message message : channel.getIterableHistory().cache(false)) {
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
