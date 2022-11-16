package model.command;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import model.command.type.ButtonInteraction;
import model.command.type.ModalInteraction;
import model.command.type.SelectionMenuInteraction;
import model.command.type.ServerCommand;
import model.database.models.TrackHistoryModel;
import model.manager.DiscordBot;
import model.manager.MusicManager;
import model.service.ScheduleService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import util.ConnectionUtil;
import util.ServerUtil;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Player implements ServerCommand, ButtonInteraction, ModalInteraction, SelectionMenuInteraction {

    @Override
    public void performTextChannelCommand(Member member, TextChannel channel, String message) {
        String[] args = message.split(" ");
        MusicManager musicManager = DiscordBot.INSTANCE.getAudioPlayer();

        if (message.equals("!player")) {

            if (!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
                return;
            }
            EmbedBuilder help = createHelp();
            channel.sendMessageEmbeds(help.build()).complete();

            EmbedBuilder player = createTemplateMessage();
            MessageCreateBuilder messageBuilder = new MessageCreateBuilder()
                    .addEmbeds(player.build())
                    .setComponents(ActionRow.of(
                       Button.secondary("player link","Link"),
                            Button.secondary("player search","Suche"),
                            Button.primary("player load", "Laden"),
                            Button.secondary("player repeatTrack","Track Wdh."),
                            Button.secondary("player repeatQueue", "Queue Wdh.")
                    ));
            channel.sendMessage(messageBuilder.build()).complete();

            EmbedBuilder queue = createQueueMessage();
            channel.sendMessageEmbeds(queue.build()).complete();
        } else if (args.length == 3 && args[1].equals("volume")) {

            int volume = Integer.parseInt(args[2]);
            if (volume > 100 || volume < 0) {
                EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, Lautstärke ist außerhalb des Rahmens (0-100)", member.getAsMention()));
                channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
                return;
            }
            musicManager.player.setVolume(Integer.parseInt(args[2]));
        } else if (args.length == 3 && args[1].equals("save")) {

            if (musicManager.player.getPlayingTrack() == null) {
                EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, zum abspeichern muss ein Song bereits abgespielt werden", member.getAsMention()));
                channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
                return;
            }
            AudioTrackInfo info = musicManager.player.getPlayingTrack().getInfo();

            ConnectionUtil.executeSQL(ConnectionUtil.INSERT_SONG(member.getId(), args[2], info.uri));
            EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, Song **%s** wurde in der Playlist **%s** gespeichert", member.getAsMention(), info.title,args[2]));
            channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);

        } else if (args.length == 3 && args[1].equals("pop")) {
            try {
                int trackNumber = Integer.parseInt(args[2]);
                if (musicManager.scheduler.popTrack(trackNumber)) {
                    EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, die Playlist ist leer oder die Zahl ist außerhalb der Track Nummern", member.getAsMention()));
                    channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
                }
            } catch (NumberFormatException nfe) {
                EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, **%s** ist keine gültige Zahl", member.getAsMention(), args[2]));
                channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
            }
        } else if (args.length == 3 && args[1].equals("remove")) {
            try {
                int trackNumber = Integer.parseInt(args[2]);
                if (musicManager.scheduler.removeTrack(trackNumber)) {
                    EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, die Playlist ist leer oder die Zahl ist außerhalb der Track Nummern", member.getAsMention()));
                    channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
                }
            } catch (NumberFormatException nfe) {
                EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, **%s** ist keine gültige Zahl", member.getAsMention(), args[2]));
                channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
            }
        } else if (args.length == 3 && args[1].equals("start")) {
            try {
                int trackNumber = Integer.parseInt(args[2]);
                if (musicManager.scheduler.playTrack(trackNumber)) {
                    EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, die Playlist ist leer oder die Zahl ist außerhalb der Track Nummern", member.getAsMention()));
                    channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
                }
            } catch (NumberFormatException nfe) {
                EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, **%s** ist keine gültige Zahl", member.getAsMention(), args[2]));
                channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
            }
        } else if (args[1].equals("history") && member.hasPermission(Permission.ADMINISTRATOR)) {

            try {
                int amount = Integer.parseInt(args[2]);
                EmbedBuilder eb = new EmbedBuilder().setTitle("Track History - Player")
                        .setFooter("Diese Nachricht wird automatisch nach 5 Minuten gelöscht");
                List<TrackHistoryModel> modelList = ConnectionUtil.executeSQL(ConnectionUtil.SELECT_TITLES(amount), TrackHistoryModel.class);
                if (modelList.size() != 0) {
                    StringBuilder tracks = new StringBuilder();
                    modelList.forEach(model ->
                            tracks.append(model.getTitle()).append("\n")
                                    .append(model.getLink()).append("\n")
                                    .append(model.getDate()).append("\n\n"));
                    eb.setDescription(tracks.toString());
                } else {
                    eb.setDescription("Historie ist leer!");
                }
                channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(5, TimeUnit.MINUTES);
            } catch (NumberFormatException nfe) {
                EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, **%s** ist keine gültige Zahl", member.getAsMention(), args[2]));
                channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void performTextChannelReaction(Member member, TextChannel channel, MessageReaction reaction) {
        if (!reaction.getMessageId().equals(ServerUtil.getMessageID("player_main"))) {
            return;
        }

        reaction.removeReaction(member.getUser()).queue();

        if (!ServerUtil.hasRole(member,"871292341053431899")  && !member.hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        MusicManager musicManager = DiscordBot.INSTANCE.getAudioPlayer();

        if (!member.getGuild().getAudioManager().isConnected()) {
            return;
        }

        VoiceChannel vc = (VoiceChannel) Objects.requireNonNull(member.getVoiceState()).getChannel();

        if (vc == null && !member.hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        assert vc != null;
        if (vc.getMembers().stream().map(ISnowflake::getId)
                .noneMatch(id -> id.equals(DiscordBot.INSTANCE.getManager().getSelfUser().getId()))) {
            EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, verbinde dich zuvor mit dem aktiven VoiceChannel", member.getAsMention()));
            channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
            return;
        }

        String emote = reaction.getEmoji().getName();

        switch (emote) {
            case "⏹": // Stop

                if (musicManager.player.getPlayingTrack() == null) {
                    musicManager.player.setVolume(20);
                    musicManager.player.setPaused(false);
                    musicManager.scheduler.setRepeatQueue(false);

                    channel.getGuild().getAudioManager().closeAudioConnection();
                }

                musicManager.player.stopTrack();
                musicManager.scheduler.clear();
                musicManager.scheduler.clearMessages();
                break;
            case "⏯": // Resume/Pause
                musicManager.player.setPaused(!musicManager.player.isPaused());
                break;
            case "⏩": // Next
                skipTrack();
                break;
            case "\uD83D\uDD00": //Randomize
                musicManager.scheduler.randomizeQueue();
                break;
            case "\uD83D\uDD3B": // Minus
                int volumeDown = musicManager.player.getVolume() - 10;
                if (volumeDown >= 0) {
                    int oldVolume = musicManager.player.getVolume();
                    musicManager.player.setVolume(volumeDown);
                    EmbedBuilder eb = new EmbedBuilder()
                            .setDescription(String.format("%s, Lautstärke von %d auf %d", member.getAsMention(), oldVolume, volumeDown));
                    channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
                    return;
                }
                EmbedBuilder eb = new EmbedBuilder()
                        .setDescription(String.format("%s, Lautstärke ist bereits auf 0", member.getAsMention()));
                channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
                break;
            case "\uD83D\uDD3A": // Plus
                int volumeUp = musicManager.player.getVolume() + 10;
                if (volumeUp <= 100) {
                    int oldVolume = musicManager.player.getVolume();
                    musicManager.player.setVolume(volumeUp);
                    EmbedBuilder eb4 = new EmbedBuilder()
                            .setDescription(String.format("%s, Lautstärke von %d auf %d", member.getAsMention(), oldVolume, volumeUp));
                    channel.sendMessageEmbeds(eb4.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
                    return;
                }
                EmbedBuilder eb5 = new EmbedBuilder()
                        .setDescription(String.format("%s, Lautstärke ist bereits auf 100", member.getAsMention()));
                channel.sendMessageEmbeds(eb5.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
                break;
        }
    }

    @Override
    public void performButtonInteraction(Member member, TextChannel channel, String customID, ButtonInteractionEvent event) {
        if (!ServerUtil.hasRole(member,"871292341053431899") && !member.hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        switch (customID) {
            case "link":
                TextInput link = TextInput.create("url","Link:", TextInputStyle.SHORT)
                        .setPlaceholder("Trage deinen Link hier ein")
                        .setRequired(true)
                        .setMinLength(1)
                        .build();

                Modal linkModal = Modal.create("player link", "YT Video/Playlist Link")
                        .addActionRow(link)
                        .build();

                event.replyModal(linkModal).queue();
                break;
            case "search":
                TextInput search = TextInput.create("query","Suchfeld:", TextInputStyle.SHORT)
                        .setPlaceholder("Was suchst du?")
                        .setRequired(true)
                        .setMinLength(1)
                        .build();

                Modal searchModal = Modal.create("player search", "YouTube Suche")
                        .addActionRow(search)
                        .build();

                event.replyModal(searchModal).queue();
                break;
            case "load":
                TextInput load = TextInput.create("playlist","Playlist:", TextInputStyle.SHORT)
                        .setPlaceholder("Gebe den Namen deiner Playlist ein")
                        .setRequired(true)
                        .setMinLength(1)
                        .build();

                Modal loadModal = Modal.create("player load", "CaptCom Playlist")
                        .addActionRow(load)
                        .build();

                event.replyModal(loadModal).queue();
                break;
            case "repeatTrack":
                MusicManager repeatTrack = DiscordBot.INSTANCE.getAudioPlayer();
                if (repeatTrack.player.getPlayingTrack() != null) {
                    if (repeatTrack.scheduler.getAudioTrack() == null) {
                        repeatTrack.scheduler.setAudioTrack(repeatTrack.player.getPlayingTrack().makeClone());
                        event.getInteraction().editButton(event.getButton().withStyle(ButtonStyle.SUCCESS)).queue();
                    } else {
                        repeatTrack.scheduler.setAudioTrack(null);
                        event.getInteraction().editButton(event.getButton().withStyle(ButtonStyle.SECONDARY)).queue();
                    }
                }
                break;
            case "repeatQueue":
                MusicManager repeatQueue = DiscordBot.INSTANCE.getAudioPlayer();
                if (repeatQueue.player.getPlayingTrack() != null) {
                    if (repeatQueue.scheduler.isRepeatQueue()) {
                        repeatQueue.scheduler.setRepeatQueue(false);
                        event.getInteraction().editButton(event.getButton().withStyle(ButtonStyle.SECONDARY)).queue();
                    } else {
                        repeatQueue.scheduler.setRepeatQueue(true);
                        event.getInteraction().editButton(event.getButton().withStyle(ButtonStyle.SUCCESS)).queue();
                    }
                }
                break;
        }
    }

    @Override
    public void performModalInteraction(Member member, TextChannel channel, String customID, ModalInteractionEvent event) {
        if (!ServerUtil.hasRole(member,"871292341053431899") && !member.hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        MusicManager musicManager = DiscordBot.INSTANCE.getAudioPlayer();
        switch (customID) {
            case "player link":
                String url = event.getValue("url").getAsString();
                DiscordBot.INSTANCE.getPlayerManager().loadItemOrdered(musicManager,
                        url, new LoadResultHandler(musicManager, member, channel, url, event, false, false));
                break;
            case "player search":
                String query = event.getValue("query").getAsString();
                DiscordBot.INSTANCE.getPlayerManager().loadItemOrdered(musicManager,
                        "ytsearch:" + query, new LoadResultHandler(musicManager, member, channel, query, event, false, true));
                break;
            case "player load":
                String playlist = event.getValue("playlist").getAsString();

                List<String> list = ConnectionUtil.executeSQL(ConnectionUtil.SELECT_SONGS_OF_PLAYLIST(member.getId(), playlist), String.class);
                if (list.isEmpty()) {
                    EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, du hast keine Playlist mit dem Namen %s", member.getAsMention(), playlist));
                    event.replyEmbeds(eb.build()).setEphemeral(true).queue();
                }
                for (String s : list) {
                    DiscordBot.INSTANCE.getPlayerManager().loadItemOrdered(musicManager
                            , s, new LoadResultHandler(musicManager, member, channel, playlist, event, true,false));
                }
                EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, deine Playlist mit dem Namen %s wird hinzugefügt", member.getAsMention(), playlist));
                event.replyEmbeds(eb.build()).setEphemeral(true).queue();

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        musicManager.scheduler.editQueueMessage();
                    }
                },5000);
                break;
        }
    }

    @Override
    public void performPrivateChannelCommand(User user, String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void performSelectInteraction(Member member, TextChannel channel, List<String> values, String customID, SelectMenuInteractionEvent event) {
        if (!ServerUtil.hasRole(member,"871292341053431899") && !member.hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        if (!customID.equals("queue")) {
            return;
        }

        MusicManager musicManager = DiscordBot.INSTANCE.getAudioPlayer();
        event.editComponents(ActionRow.of(musicManager.scheduler.playTrackSelect(values.get(0)))).queue();
    }

    private Boolean play(Guild guild, MusicManager musicManager, AudioTrack track, Member m, TextChannel channel) {
        if (connectToVoiceChannel(guild.getAudioManager(), m, channel)) {
            musicManager.scheduler.queue(track);
            return true;
        }
        return false;
    }

    private void skipTrack() {
        MusicManager musicManager = DiscordBot.INSTANCE.getAudioPlayer();
        musicManager.scheduler.nextTrack();
    }

    private static Boolean connectToVoiceChannel(AudioManager audioManager, Member m, TextChannel channel) {
        if (!audioManager.isConnected()) {
            VoiceChannel vc = (VoiceChannel) Objects.requireNonNull(m.getVoiceState()).getChannel();
            if (vc != null) {
                Runnable task = () -> {
                    if (audioManager.isConnected() && vc.getMembers().size() == 1) {
                        MusicManager musicManager = DiscordBot.INSTANCE.getAudioPlayer();
                        musicManager.player.stopTrack();
                        musicManager.scheduler.clear();
                        musicManager.scheduler.clearMessages();
                        musicManager.player.setVolume(20);
                        musicManager.player.setPaused(false);
                        musicManager.scheduler.setRepeatQueue(false);
                        audioManager.closeAudioConnection();
                        ScheduleService.descheduleTask("PlayerConnectionTask");
                    }
                };
                if (!ScheduleService.getTasks().containsKey("PlayerConnectionTask")) {
                    ScheduleService.submitCronJob("*/10 * * * *","PlayerConnectionTask" ,task);
                }
                audioManager.openAudioConnection(vc);
                return true;
            } else {
                EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, verbinde dich zuvor mit einem VoiceChannel", m.getAsMention()));
                channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
                return false;
            }
        } else {
            VoiceChannel vc = (VoiceChannel) Objects.requireNonNull(m.getVoiceState()).getChannel();
            if (vc != null) {
                if (vc.getMembers().stream().map(ISnowflake::getId)
                        .noneMatch(id -> id.equals(DiscordBot.INSTANCE.getManager().getSelfUser().getId()))) {
                    EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, verbinde dich zuvor mit einem aktiven VoiceChannel", m.getAsMention()));
                    channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
                    return false;
                }
                return true;
            } else {
                EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("%s, verbinde dich zuvor mit einem aktiven VoiceChannel", m.getAsMention()));
                channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
                return false;
            }
        }
    }

    public void editChannelMessage (TextChannel channel) {
        String topic = "⏹ Stop/Leave — ⏯ Pause/Resume - ⏩ Skip" +
                " - \uD83D\uDD02 Repeat Current Track — \uD83D\uDD3A \uD83D\uDD3B Volume Up/Down 10";
        channel.getManager().setTopic(topic).queue();
    }

    private EmbedBuilder createTemplateMessage () {
        return new EmbedBuilder()
                .setTitle("Aktuelles Lied")
                .setImage("https://images.wallpaperscraft.com/image/single/headphones_camera_retro_122094_1280x720.jpg")
                .addField("Author","Titel",false)
                .addField("Ersteller:","Null",false)
                .addField("Dauer:","00h 00m 00s",false);
    }

    private EmbedBuilder createQueueMessage () {
        return new EmbedBuilder().setTitle("Warteschlange:").setDescription("...");
    }

    private EmbedBuilder createHelp() {
        return new EmbedBuilder()
                .setThumbnail("https://s20.directupload.net/images/210422/zpyz5gkv.png")
                .setTitle("CaptCommunity MusicPlayer")
                .setDescription("Verfügbare Chat Befehle: ")
                .addField("!player save [playlist]","```cs\nSpeichert auf eine Bot Playlist\n```",true)
                .addField("!player volume\n[0-100]","```cs\nVerändert die Lautstärke\n```",true)
                .addField("!player remove [trackNo.]","```cs\nEntfernt den Track mit der Nummer\n```",true)
                .addField("!player start [trackNo.]","```cs\nStartet den Track mit der Nummer\n```",true)
                .addField("!player pop [trackNo.]","```cs\nSchiebt Track an oberster Stelle\n```",true)
                .addField("Besuche unseren Web-Player:","[Web-Player](http://178.32.109.146:8000/player/)",false)
                .setFooter("Made by ShuraBlack - Head of Server");
    }

    public void addReactions (TextChannel channel) {
        String mesID = ServerUtil.getMessageID("player_main");

        channel.retrieveMessageById(mesID).complete().clearReactions().queue();
        channel.addReactionById(mesID,Emoji.fromUnicode("⏹")).queue();
        channel.addReactionById(mesID,Emoji.fromUnicode("⏯")).queue();
        channel.addReactionById(mesID,Emoji.fromUnicode("⏩")).queue();
        channel.addReactionById(mesID,Emoji.fromUnicode("\uD83D\uDD00")).queue();
        channel.addReactionById(mesID,Emoji.fromUnicode("\uD83D\uDD3B")).queue();
        channel.addReactionById(mesID,Emoji.fromUnicode("\uD83D\uDD3A")).queue();
    }

    public class LoadResultHandler implements AudioLoadResultHandler {

        private final MusicManager musicManager;
        private final Member m;
        private final TextChannel channel;
        private final ModalInteractionEvent event;
        private final String value;
        private final boolean load;
        private final boolean ytSearch;

        public LoadResultHandler(MusicManager musicManager, Member m, TextChannel channel, String value, ModalInteractionEvent event, boolean load, boolean ytSearch) {
            this.musicManager = musicManager;
            this.m = m;
            this.channel = channel;
            this.value = value;
            this.event = event;
            this.load = load;
            this.ytSearch = ytSearch;
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            if(!play(channel.getGuild(), musicManager, track, m, channel)) return;
            if (!load) {
                EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("Hinzugefügt zur Warteschlange **%s**", track.getInfo().title));
                event.replyEmbeds(eb.build()).setEphemeral(true).queue();
            }
            if (!load) {
                musicManager.scheduler.editQueueMessage();
            }
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            if (ytSearch) {
                if(!play(channel.getGuild(), musicManager, playlist.getTracks().get(0), m, channel)) return;
            } else {
                for (AudioTrack at : playlist.getTracks()) {
                    if(!play(channel.getGuild(), musicManager, at, m, channel)) return;
                }
            }

            if (!load) {
                musicManager.scheduler.editQueueMessage();
                EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("Hinzugefügt zur Warteschlange **%s**", playlist.getName()));
                event.replyEmbeds(eb.build()).setEphemeral(true).queue();
            }
        }

        @Override
        public void noMatches() {
            EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("Kein Ergebnis für **%s**", value));
            event.replyEmbeds(eb.build()).setEphemeral(true).queue();
        }

        @Override
        public void loadFailed(FriendlyException e) {
            EmbedBuilder eb = new EmbedBuilder().setDescription(String.format("Konnte **%s** nicht abspielen", value))
                    .addField("Error:",e.getMessage(),false);
            event.replyEmbeds(eb.build()).setEphemeral(true).queue();
        }
    }
}
