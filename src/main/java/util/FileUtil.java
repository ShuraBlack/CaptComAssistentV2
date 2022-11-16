package util;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import model.manager.DiscordBot;
import model.manager.MusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author ShuraBlack
 * @since 03-20-2022
 */
public class FileUtil {

    private static final Logger LOGGER = LogManager.getLogger(FileUtil.class);

    public static Properties loadProperties(String filename) {
        Properties properties = new Properties();

        File propertiesFile = new File(filename);
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(propertiesFile);
            properties.load(inputStream);
        } catch (IOException e) {
            LOGGER.error("Couldnt load properties file <" + propertiesFile.getAbsolutePath() + ">",e);
        }
        return properties;
    }

    public static void loadPlayerTmp() {
        File file = new File("player.tmp");
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("player.tmp"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("channel:")) {
                    line = line.substring(8);
                    VoiceChannel channel = DiscordBot.INSTANCE.getManager().getGuildById(ServerUtil.GUILD).getVoiceChannelById(line);
                    DiscordBot.INSTANCE.getManager().getGuildById(ServerUtil.GUILD).getAudioManager().openAudioConnection(channel);
                } else if (line.startsWith("playing:")) {
                    line = line.substring(8);
                    String[] args = line.split(" ");
                    MusicManager musicManager = DiscordBot.INSTANCE.getAudioPlayer();
                    DiscordBot.INSTANCE.getPlayerManager().loadItemOrdered(musicManager, args[0], new FileUtilPlayerHandler(musicManager, ServerUtil.getTextChannel(ServerUtil.getChannelID("music"))));
                    while (DiscordBot.INSTANCE.getAudioPlayer().player.getPlayingTrack() == null) {
                        Thread.yield();
                    }
                    DiscordBot.INSTANCE.getAudioPlayer().player.getPlayingTrack().setPosition(Long.parseLong(args[1]));
                } else if (line.startsWith("queue:")) {
                    line = line.substring(6);
                    MusicManager musicManager = DiscordBot.INSTANCE.getAudioPlayer();
                    DiscordBot.INSTANCE.getPlayerManager().loadItemOrdered(musicManager, line, new FileUtilPlayerHandler(musicManager, ServerUtil.getTextChannel(ServerUtil.getChannelID("music"))));
                }
            }
            MusicManager musicManager = DiscordBot.INSTANCE.getAudioPlayer();
            musicManager.scheduler.editQueueMessage();
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Boolean play(Guild guild, MusicManager musicManager, AudioTrack track) {
        if (guild.getAudioManager().isConnected()) {
            musicManager.scheduler.queue(track);
            return true;
        }
        return false;
    }

    private static class FileUtilPlayerHandler implements AudioLoadResultHandler {

        private final MusicManager musicManager;
        private final TextChannel channel;

        public FileUtilPlayerHandler(MusicManager musicManager, TextChannel channel) {
            this.musicManager = musicManager;
            this.channel = channel;
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            play(channel.getGuild(), musicManager, track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            for (AudioTrack track : playlist.getTracks()) {
                play(channel.getGuild(), musicManager, track);
            }
        }

        @Override
        public void noMatches() {
            EmbedBuilder eb = new EmbedBuilder().setDescription("Kein Ergebnis");
            channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7,TimeUnit.SECONDS);
        }

        @Override
        public void loadFailed(FriendlyException e) {
            EmbedBuilder eb = new EmbedBuilder().setDescription("Konnte Track nicht abspielen");
            channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7,TimeUnit.SECONDS);
        }
    }
}
