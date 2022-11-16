package model.command;

import model.command.type.ServerCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class Test implements ServerCommand {
    @Override
    public void performTextChannelCommand(Member member, TextChannel channel, String message) {
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            return;
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
}
