package model.command.type;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

/**
 * Interface for {@link ModalInteractionEvent Modal Interactions}, which got send via the {@link net.dv8tion.jda.api.hooks.ListenerAdapter}.
 * <br><br>
 * Other Classes need to implement the function of this Interface to answer corresponding requests
 * @since July.15.2022
 * @author ShuraBlack
 */
public interface ModalInteraction {

    /**
     * Function which needs to be fully implemented to process the event.
     * <br><br>
     * The Discord API forces you to answer within 3 seconds or otherwise the request will be shown as a failure.
     * Alernativly you can use the event function {@link ModalInteractionEvent#deferReply()}
     * @param member which created the Event Interaction
     * @param channel where the Event got created
     * @param customID of the {@link net.dv8tion.jda.api.interactions.components.Modal Modal}. Internally known as model id
     * @param event the original source created by the jda
     */
    void performModalInteraction(Member member, TextChannel channel, String customID, ModalInteractionEvent event);

}
