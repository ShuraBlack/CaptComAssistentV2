package model.listener;

import model.manager.DiscordBot;
import model.service.ScheduleService;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import util.ServerUtil;

import java.util.Objects;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (ServerUtil.MUTE) {
            return;
        }
        if (event.getAuthor().isBot()) {
            return;
        }

        String message = event.getMessage().getContentDisplay();
        String command = message.split(" ")[0];
        Runnable task = () -> {
            switch (event.getChannelType()) {
                case TEXT:
                    if (!event.getMessage().getContentRaw().startsWith("!")) {
                        if (event.getChannel().getId().equals(ServerUtil.getChannelID("music"))) {
                            event.getMessage().delete().queue();
                            DiscordBot.INSTANCE.getActionManager()
                                    .textChannelMessagePerform("!player", event.getMember(), event.getChannel().asTextChannel(), "!player " + message + "XESCAPEX");
                        }
                        return;
                    }
                    if (command.equals("!search")) {
                        event.getMessage().delete().queue();
                        DiscordBot.INSTANCE.getActionManager()
                                .textChannelMessagePerform("!player", event.getMember(), event.getChannel().asTextChannel(), message.replace("!search", "!player search"));
                        return;
                    }
                    event.getMessage().delete().queue();
                    DiscordBot.INSTANCE.getActionManager()
                            .textChannelMessagePerform(command, event.getMember(), event.getChannel().asTextChannel(), message);
                    break;
                case PRIVATE:
                    DiscordBot.INSTANCE.getActionManager()
                            .privateChannelMessagePerform(command, event.getAuthor(), message);
                    break;
                case NEWS:
                case STAGE:
                case VOICE:
                    break;
            }
        };
        ScheduleService.submit(task);
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (ServerUtil.MUTE) {
            return;
        }
        if (Objects.requireNonNull(event.getUser()).isBot()) {
            return;
        }

        if (!event.isFromGuild()) {
            return;
        }

        if (!event.getChannelType().equals(ChannelType.TEXT)) {
            return;
        }

        Runnable task = () -> {
             if (event.getMessageId().compareTo(ServerUtil.getMessageID("player_main")) == 0) {
                DiscordBot.INSTANCE.getActionManager()
                        .textChannelReactionPerform("!player", event.getMember(), event.getChannel().asTextChannel(), event.getReaction());
            } else if (event.getMessageId().compareTo(ServerUtil.getChannelID("request")) == 0) {
                DiscordBot.INSTANCE.getActionManager()
                        .textChannelReactionPerform("!gdice", event.getMember(), event.getChannel().asTextChannel(), event.getReaction());
            } else if (event.getMessageId().compareTo(ServerUtil.getMessageID("blackjack_board")) == 0) {
                 DiscordBot.INSTANCE.getActionManager()
                         .textChannelReactionPerform("!bj", event.getMember(), event.getChannel().asTextChannel(), event.getReaction());
             }
        };
        ScheduleService.submit(task);
    }
}
