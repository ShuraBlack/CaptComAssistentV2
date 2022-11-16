package model.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import model.manager.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import util.ServerUtil;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private AudioTrack audioTrack = null;

    public static TrackScheduler INSTANCE;

    private boolean repeatQueue = false;
    private AudioTrack rqAudioTrack = null;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        INSTANCE = this;
    }

    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.add(track);
        }
    }

    public void nextTrack() {
        if (queue.size() == 0 && audioTrack == null) {
            clearMessages();
        }

        if (audioTrack != null) {
            player.startTrack(audioTrack.makeClone(),false);
        } else {
            if (repeatQueue) {
                if (player.getPlayingTrack() != null) {
                    queue.add(player.getPlayingTrack().makeClone());
                } else {
                    queue.add(rqAudioTrack);
                }
            }
            player.startTrack(queue.poll(), false);
        }
        editQueueMessage();
    }

    public boolean removeTrack (int trackNumber) {
        if (queue.size() == 0 || queue.size()+1 < trackNumber) {
            return true;
        }
        int count = 0;
        List<AudioTrack> audioTracks = new ArrayList<>(queue);
        queue.clear();
        for (AudioTrack audioTrack : audioTracks) {
            count++;
            if (count == trackNumber) {
               continue;
            }
            queue.add(audioTrack);
        }
        editQueueMessage();
        return false;
    }

    public boolean popTrack(int trackNumber) {
        if (queue.size() == 0 || queue.size()+1 < trackNumber) {
            return true;
        }
        List<AudioTrack> audioTracks = new ArrayList<>(queue);
        queue.clear();
        queue.add(audioTracks.remove(trackNumber-1));
        queue.addAll(audioTracks);
        editQueueMessage();
        return false;
    }

    public boolean playTrack (int trackNumber) {
        if (queue.size() == 0 || queue.size()+1 < trackNumber) {
            return true;
        }
        int count = 0;
        List<AudioTrack> audioTracks = new ArrayList<>(queue);
        queue.clear();
        for (AudioTrack audioTrack : audioTracks) {
            count++;
            if (count == trackNumber) {
                player.startTrack(audioTrack, false);
                continue;
            }
            queue.add(audioTrack);
        }
        editQueueMessage();
        return false;
    }

    public SelectMenu playTrackSelect (String identifier) {
        if (queue.size() == 0) {
            return null;
        }
        List<AudioTrack> audioTracks = new ArrayList<>(queue);
        queue.clear();
        for (AudioTrack audioTrack : audioTracks) {
            if (audioTrack.getInfo().title.equals(identifier)) {
                player.startTrack(audioTrack, false);
                continue;
            }
            queue.add(audioTrack);
        }
        return createMenu();
    }

    private SelectMenu createMenu() {
        SelectMenu.Builder menu = SelectMenu.create("player queue")
                .setPlaceholder("Lieder: " + queue.size() + " - Gesamtdauer: "
                        + createTime(queue.stream().filter(at -> !at.getInfo().isStream)
                        .mapToLong(audioTrack -> audioTrack.getInfo().length).sum(), false));

        int count = 0;
        for (AudioTrack at : this.queue) {
            if (count == 10) {
                break;
            }

            CustomEmoji icon = null;

            if (at.getInfo().uri.contains("youtube") || at.getInfo().uri.contains("youtu.be")) {
                icon = Emoji.fromCustom("youtube",1042066466746413136L,false);
            } else if (at.getInfo().uri.contains("soundcloud")) {
                icon = Emoji.fromCustom("soundcloud",1042184901308452965L,false);
            } else if (at.getInfo().uri.contains("twitch")) {
                icon = Emoji.fromCustom("twitch",1042184899597180988L,false);
            }
            AudioTrackInfo info = at.getInfo();
            menu.addOption(at.getInfo().title,at.getInfo().title, createTime(info.length, info.isStream),icon);
            count++;
        }

        return menu.build();
    }

    public void randomizeQueue() {
        List<AudioTrack> list = new ArrayList<>(queue);
        Collections.shuffle(list);
        queue.clear();
        queue.addAll(list);
        editQueueMessage();
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        AudioTrackInfo info = track.getInfo();

        String url = info.uri;
        EmbedBuilder eb = (new EmbedBuilder()).setColor(new Color(70,130,220)).addField(info.author, "["
                + info.title + "](" + url + ")", false)
                .addField("Dauer: ", createTime(info.length, info.isStream), true);
        if (info.uri.contains("youtube") || info.uri.contains("youtu.be")) {
            eb.setImage("https://img.youtube.com/vi/" + info.identifier + "/hqdefault.jpg");
        } else if (info.uri.contains("soundcloud")) {
            eb.setImage("https://wallpaperaccess.com/full/1112346.jpg");
        } else if (info.uri.contains("twitch")) {
            eb.setImage("https://wallpapercave.com/wp/wp1957865.jpg");
        }

        Objects.requireNonNull(Objects.requireNonNull(DiscordBot.INSTANCE.getManager().getGuildById(ServerUtil.GUILD))
                        .getTextChannelById(ServerUtil.getChannelID("music")))
                .editMessageEmbedsById(ServerUtil.getMessageID("player_main"),eb.build()).queue();
    }

    private String createTime(long length, boolean isStream) {
        long sec = length / 1000L;
        long min = sec / 60L;
        long hour = min / 60L;
        sec %= 60L;
        min %= 60L;
        hour %= 24L;
        return isStream ? "\uD83D\uDD34 Stream" : (hour > 0L ? hour + "h " : "")
                + (min < 10 ? "0" + min : min) + "m "
                + (sec < 10 ? "0" + sec : sec) + "s";
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (repeatQueue) {
                rqAudioTrack = track.makeClone();
            }
            nextTrack();
        }
    }

    public void editQueueMessage () {
        if (this.queue.isEmpty()) {
            return;
        }
        TextChannel musicChannel = DiscordBot.INSTANCE.getManager().getGuildById(ServerUtil.GUILD)
                .getTextChannelById(ServerUtil.getChannelID("music"));
        musicChannel.editMessageComponentsById(ServerUtil.getMessageID("player_queue"), ActionRow.of(createMenu())).queue();
    }

    public void clear () {
        queue.clear();
        audioTrack = null;
    }

    public void clearMessages () {
        TextChannel musicChannel = Objects.requireNonNull(DiscordBot.INSTANCE.getManager().getGuildById(ServerUtil.GUILD))
                .getTextChannelById(ServerUtil.getChannelID("music"));
        EmbedBuilder current = new EmbedBuilder()
                .setDescription("Warte auf neues Lied ...")
                .setImage("https://images.wallpaperscraft.com/image/single/headphones_camera_retro_122094_1280x720.jpg");

        musicChannel.editMessageEmbedsById(ServerUtil.getMessageID("player_main"), current.build()).queue();
        musicChannel.editMessageComponentsById(ServerUtil.getMessageID("player_main"),
                ActionRow.of(
                        net.dv8tion.jda.api.interactions.components.buttons.Button.secondary("player link","Link"),
                        net.dv8tion.jda.api.interactions.components.buttons.Button.secondary("player search","Suche"),
                        net.dv8tion.jda.api.interactions.components.buttons.Button.primary("player load", "Laden"),
                        net.dv8tion.jda.api.interactions.components.buttons.Button.secondary("player repeatTrack","Track Wdh."),
                        Button.secondary("player repeatQueue", "Queue Wdh.")
                )
        ).queue();

        musicChannel.editMessageComponentsById(ServerUtil.getMessageID("player_queue")
                , ActionRow.of(Button.secondary("empty","Keine Warteschlange").asDisabled())).queue();
    }

    public AudioTrack getAudioTrack() {
        return audioTrack;
    }

    public void setAudioTrack(AudioTrack audioTrack) {
        this.audioTrack = audioTrack;
    }

    public boolean isRepeatQueue() {
        return repeatQueue;
    }

    public void setRepeatQueue(boolean repeatQueue) {
        this.repeatQueue = repeatQueue;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }
}

