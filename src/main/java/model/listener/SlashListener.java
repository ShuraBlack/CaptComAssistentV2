package model.listener;

import model.manager.DiscordBot;
import model.service.ScheduleService;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import util.ServerUtil;
import java.util.Objects;

public class SlashListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (ServerUtil.MUTE) {
            return;
        }
        if (event.getUser().isBot()) {
            return;
        }

        if (!event.isFromGuild()) {
            event.reply("Global commands are not supported").queue();
            return;
        }

        String command = mapping(event.getName());
        if (command == null) {
            ScheduleService.submit(() -> DiscordBot.INSTANCE.getActionManager()
                    .slashCommandPerform("!" + event.getName(),event.getMember(),event.getChannel().asTextChannel(),event));
            return;
        }

        ScheduleService.submit(() -> DiscordBot.INSTANCE.getActionManager()
                .slashCommandPerform(command,event.getMember(),event.getChannel().asTextChannel(),event));
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (ServerUtil.MUTE) {
            return;
        }
        if (event.getUser().isBot()) {
            return;
        }

        String[] args = Objects.requireNonNull(event.getButton().getId()).split(" ");
        ScheduleService.submit(() -> DiscordBot.INSTANCE.getActionManager()
                .buttonInteractionPerform("!" + args[0],event.getMember(), event.getChannel().asTextChannel(), event));
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (ServerUtil.MUTE) {
            return;
        }
        if (event.getUser().isBot()) {
            return;
        }

        String[] args = Objects.requireNonNull(event.getModalId()).split(" ");
        ScheduleService.submit(() -> DiscordBot.INSTANCE.getActionManager()
                .modalInteractionPerform("!" + args[0],event.getMember(), event.getChannel().asTextChannel(), event));
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        if (ServerUtil.MUTE) {
            return;
        }
        if (event.getUser().isBot()) {
            return;
        }

        String[] args = Objects.requireNonNull(event.getInteraction().getSelectMenu().getId()).split(" ");
        ScheduleService.submit(() -> DiscordBot.INSTANCE.getActionManager()
                .selectInteractionPerform("!" + args[0],event.getMember(),args[1],event.getChannel().asTextChannel(),event));
    }

    private static String mapping(String name) {
        switch (name) {
            case "start":
            case "stop":
            case "remove":
            case "save":
            case "load":
            case "volume":
            case "pop":
            case "search":
                return "!player";
            default:
                return null;
        }
    }
}
