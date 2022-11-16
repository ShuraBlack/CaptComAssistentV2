package model.manager;

import model.command.*;
import model.command.type.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ActionManager {

    public static final Logger LOGGER = LogManager.getLogger(ActionManager.class);

    private final ConcurrentHashMap<String, ServerCommand> commands;

    public ActionManager() {
        // Create a HashMap
        this.commands = new ConcurrentHashMap<>();

        // Commands

        /*-- Managment Commands --------------------------------------------------------------------------------------*/
        this.commands.put("!clear", new Clear());
        this.commands.put("!copy", new Copy());
        this.commands.put("!test",new Test());
        /*------------------------------------------------------------------------------------------------------------*/

        /*-- Server Messages and User Assistent ----------------------------------------------------------------------*/
        this.commands.put("!sub", new Subscription());
        this.commands.put("!ruler", new Ruler());
        this.commands.put("!rules", new Rules());
        this.commands.put("!help", new Help());
        /*------------------------------------------------------------------------------------------------------------*/

        /*-- Music ---------------------------------------------------------------------------------------------------*/
        // Control music player
        this.commands.put("!player", new Player());
        this.commands.put("!playlist", new Playlist());
        /*------------------------------------------------------------------------------------------------------------*/

        /*-- Randomizer ----------------------------------------------------------------------------------------------*/
        this.commands.put("!coinflip", new CoinFlip());
        this.commands.put("!dice", new Dice());
        /*------------------------------------------------------------------------------------------------------------*/

        /*-- Warframe ------------------------------------------------------------------------------------------------*/
        this.commands.put("!wft", new WarframeTracker());
        /*------------------------------------------------------------------------------------------------------------*/

        /*-- Game ----------------------------------------------------------------------------------------------------*/
        this.commands.put("!game", new Game());
        this.commands.put("!bj", new BlackJack());
        /*------------------------------------------------------------------------------------------------------------*/

    }

    public void textChannelMessagePerform(String command, Member m, TextChannel channel, String message) {
        ServerCommand cmd;
        if ((cmd = this.commands.get(command.toLowerCase())) != null) {
            cmd.performTextChannelCommand(m, channel, message);
        }
    }

    public void privateChannelMessagePerform(String command, User u, String message) {
        ServerCommand cmd;
        if ((cmd = this.commands.get(command.toLowerCase())) != null) {
            cmd.performPrivateChannelCommand(u, message);
        }
    }

    public void textChannelReactionPerform(String command, Member m, TextChannel channel, MessageReaction reaction) {
        ServerCommand cmd;
        if ((cmd = this.commands.get(command.toLowerCase())) != null) {
            cmd.performTextChannelReaction(m, channel, reaction);
        }
    }

    public void slashCommandPerform(String command, Member m, TextChannel channel, SlashCommandInteractionEvent event) {
        ServerCommand cmd;
        if ((cmd = this.commands.get(command.toLowerCase())) != null) {
            ((SlashCommand)cmd).performSlashCommand(m,channel, event.getName(), event);
        }
    }

    public void buttonInteractionPerform(String command, Member m, TextChannel channel, ButtonInteractionEvent event) {
        ServerCommand cmd;
        if ((cmd = this.commands.get(command.toLowerCase())) != null) {
            String content = Objects.requireNonNull(event.getButton().getId()).replace(command.replace("!","") + " ", "");
            ((ButtonInteraction)cmd).performButtonInteraction(m,channel, content, event);
        }
    }

    public void modalInteractionPerform(String command, Member m, TextChannel channel, ModalInteractionEvent event) {
        ServerCommand cmd;
        if ((cmd = this.commands.get(command.toLowerCase())) != null) {
            ((ModalInteraction)cmd).performModalInteraction(m,channel, event.getModalId(), event);
        }
    }

    public void selectInteractionPerform(String command, Member m, String conent, TextChannel channel, SelectMenuInteractionEvent event) {
        ServerCommand cmd;
        if ((cmd = this.commands.get(command.toLowerCase())) != null) {
            ((SelectionMenuInteraction)cmd).performSelectInteraction(m,channel,event.getValues(),conent,event);
        }
    }
}
