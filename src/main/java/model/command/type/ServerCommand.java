package model.command.type;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

/**
 * Interface for the basic events {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent (Private) Message},
 * {@link MessageReactionAddEvent Reaction add}, which got send via the {@link net.dv8tion.jda.api.hooks.ListenerAdapter}.
 * <br><br>
 * Other Classes need to implement all functions of this Interface to answer corresponding requests
 * @since July.15.2022
 * @author ShuraBlack
 */
public interface ServerCommand {

    /**
     * Function which needs to be fully implemented to process the {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent}.
     * @param member which created the Event Interaction
     * @param channel where the Event got created
     * @param message is the raw content which got send (must be a registered command)
     */
    void performTextChannelCommand(Member member, TextChannel channel, String message);

    /**
     * Function which needs to be fully implemented to process the {@link MessageReactionAddEvent}.
     * @param member which created the Event Interaction
     * @param channel where the Event got created
     * @param reaction the original source created by the jda
     */
    void performTextChannelReaction (Member member, TextChannel channel, MessageReaction reaction);

    /**
     * Function which needs to be fully implemented to process the {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent}
     * , for private requests.
     * @param user which created the Event Interaction
     * @param message is the raw content which got send (must be a registered command)
     */
    void performPrivateChannelCommand(User user, String message);

}

