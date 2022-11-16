package model.command.type;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Interface for {@link SlashCommandInteractionEvent Slash Command}, which got send via the {@link net.dv8tion.jda.api.hooks.ListenerAdapter}.
 * <br><br>
 * Other Classes need to implement the function of this Interface to answer corresponding requests
 * @since July.15.2022
 * @author ShuraBlack
 */
public interface SlashCommand {

    /**
     * Function which needs to be fully implemented to process the event.
     * <br><br>
     * The Discord API forces you to answer within 3 seconds or otherwise the request will be shown as a failure.
     * Alernativly you can use the event function {@link SlashCommandInteractionEvent#deferReply()}
     * @param member which created the Event Interaction
     * @param channel where the Event got created
     * @param command is the command without slash
     * @param event the original source created by the jda
     */
    void performSlashCommand(Member member, TextChannel channel, String command, SlashCommandInteractionEvent event);

}
