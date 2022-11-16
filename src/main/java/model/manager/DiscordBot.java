package model.manager;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import model.command.BlackJack;
import model.database.MySQLConnectionPool;
import model.game.SkinLoader;
import model.listener.DefaultListener;
import model.listener.MessageListener;
import model.listener.SlashListener;
import model.listener.VoiceChannelListener;
import model.service.LogService;
import model.service.ScheduleService;
import model.web.Server;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.http.client.config.RequestConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.AssetPool;
import util.ServerUtil;
import util.ConnectionUtil;
import util.FileUtil;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

/**
 * @author ShuraBlack
 * @since 03-20-2022
 */
public class DiscordBot {

    public static DiscordBot INSTANCE;

    //Manager
    private JDA Manager;
    private final ActionManager actionManager;

    // AudioPlayer
    private final AudioPlayerManager playerManager;
    private final MusicManager musicManager;

    private final Server server;

    private static final Logger LOGGER = LogManager.getLogger(DiscordBot.class);

    public static void main(String[] args) {

        try {
            LOGGER.debug("Register MySQL Driver ...");
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            new DiscordBot();
        } catch (
                IllegalArgumentException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            LOGGER.debug("Couldnt register MySQL Driver. Shutting down Bot ...",e);
            System.exit(1);
        }
    }

    private DiscordBot() {

        INSTANCE = this;
        LOGGER.debug("Load Server properties ...");
        Properties PROPERTIES = FileUtil.loadProperties("bot.properties");

        if (!PROPERTIES.containsKey("bot_token")) {
            LOGGER.error("Missing bot_token properties. Shutting down Bot ...");
            System.exit(1);
        }

        if (!PROPERTIES.containsKey("db_url")
                && !PROPERTIES.containsKey("db_username")
                && !PROPERTIES.containsKey("db_password")
                && !PROPERTIES.containsKey("db_poolsize")) {
            LOGGER.error("Missing db properties. Shutting down Bot ...");
            System.exit(1);
        }

        this.playerManager = new DefaultAudioPlayerManager();
        this.playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        this.playerManager.setHttpRequestConfigurator((config) -> RequestConfig.copy(config).setConnectTimeout(5000).build());
        this.musicManager = new MusicManager(this.playerManager);
        this.actionManager = new ActionManager();

        init();

        ConnectionUtil.CONNECTIONPOOL = new MySQLConnectionPool(
                PROPERTIES.getProperty("db_url"),
                PROPERTIES.getProperty("db_username"),
                PROPERTIES.getProperty("db_password"),
                Integer.parseInt(PROPERTIES.getProperty("db_poolsize")));

        try {

            this.Manager = JDABuilder.createDefault(PROPERTIES.getProperty("bot_token"))
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT)
                    .disableIntents(getDisabledIntents())
                    .disableCache(getDisabledCacheFlags())
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setLargeThreshold(50)
                    .addEventListeners(new DefaultListener())
                    .addEventListeners(new MessageListener())
                    .addEventListeners(new VoiceChannelListener())
                    .addEventListeners(new SlashListener())
                    .setStatus(OnlineStatus.ONLINE)
                    .setActivity(Activity.watching("The CaptCom Server"))
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.error("Couldnt create JDA. Shutting down Bot ...",e);
            System.exit(1);
        }

        try {
            AudioSourceManagers.registerRemoteSources(playerManager);
            AudioSourceManagers.registerLocalSource(playerManager);
            LOGGER.info("LavaPlayer got registered and handler will be set on use");
        } catch (Exception e) {
            LOGGER.error("Couldnt register AudioPlayer. The Bot might not work correctly");
        }
        LOGGER.info("CaptCommunity Assistent v2 - Bot starting ...");

        LOGGER.info("Starting HTTP Server ...");
        server = new Server();

        LOGGER.info("Load skins.json ...");
        SkinLoader.loadData();

        command();
    }

    private void init() {
        try {
            AssetPool.init();
            ServerUtil.init();
        } catch (Exception e) {
            LOGGER.error("Detected Exception in initializing properties depeding Classes. The Bot might not work correctly");
        }
    }

    private void command() {
        new Thread(() -> {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                Thread.sleep(2000);
                System.out.println("\n====================================================\n" +
                        "Commands:\n" +
                        "mute <on/off> -> Un/mute bot listener (on <time_min>)\n" +
                        "tasks -> Show current CronJobs\n" +
                        "tasks r <name> -> Remove activ CronJobs\n" +
                        "logs flush -> Flushes the logs and saves them\n" +
                        "blackjack deck -> Few the top cards\n" +
                        "game skins -> Shows Skin tree\n" +
                        "upload slash -> Upload slash command information\n" +
                        "update props -> Updates the Properties\n" +
                        "exit -> Turn off the bot" +
                        "\n====================================================\n");
                while ((line = reader.readLine()) != null) {
                    if(line.equalsIgnoreCase("exit")) {
                        AudioTrack currenetTrack = musicManager.player.getPlayingTrack();
                        if (currenetTrack != null) {
                            List<AudioTrack> queue = new ArrayList<>(musicManager.scheduler.getQueue());

                            FileWriter writer = new FileWriter("player.tmp");
                            writer.write("channel:" + DiscordBot.INSTANCE.getManager().getGuildById(ServerUtil.GUILD).getAudioManager().getConnectedChannel().getId() + "\n");
                            writer.write("playing:" + currenetTrack.getInfo().uri + " " + currenetTrack.getPosition() + "\n");
                            for (AudioTrack track : queue) {
                                writer.write("queue:" + track.getInfo().uri + "\n");
                            }
                            writer.flush();
                            writer.close();
                        }
                        if (Manager != null) {
                            reader.close();
                            Manager.getPresence().setStatus(OnlineStatus.OFFLINE);
                            Manager.shutdown();
                            ScheduleService.shutdownScheduler();
                            LOGGER.debug("Successfully shutdown JDA");
                        }
                        reader.close();
                        try {
                            server.getServer().stop(0);
                        } catch (NullPointerException ignored) {}
                        for (String file : LogService.getFiles()) {
                            LOGGER.info("Flush " + file + ".log");
                            LogService.writeFile(file);
                        }
                        LOGGER.info("Successfully flushed all logs");
                        System.exit(0);
                    } else if (line.equalsIgnoreCase("mute on")) {
                        ServerUtil.MUTE = true;
                        LOGGER.info("Bot got muted");
                    } else if (line.startsWith("mute on")) {
                        ServerUtil.MUTE = true;
                        ScheduleService.submitCronJob(
                                "*/" + line.replace("mute on ","") + " * * * *",
                                "MuteBotTask",
                                () -> {
                                    ServerUtil.MUTE = false;
                                    ScheduleService.descheduleTask("MuteBotTask");
                                    LOGGER.info("Bot got unmuted");
                        });
                    } else if (line.equalsIgnoreCase("mute off")) {
                        ServerUtil.MUTE = false;
                        LOGGER.info("Bot got unmuted");
                    } else if (line.equals("logs flush")) {
                        for (String file : LogService.getFiles()) {
                            LOGGER.info("Flush " + file + ".log");
                            LogService.writeFile(file);
                        }
                        LOGGER.info("Successfully flushed all logs");
                    } else if (line.equals("game skins")) {
                        LOGGER.info("\n" + SkinLoader.showTree());
                    } else if (line.equalsIgnoreCase("tasks")) {
                        StringBuilder s = new StringBuilder("<");
                        for (Map.Entry<String, String> entry : ScheduleService.getTasks().entrySet()) {
                            s.append("\u001b[32;1m").append(entry.getKey()).append("\u001b[0m | ").append(entry.getValue()).append(", ");
                        }
                        if (s.toString().equals("<")) {
                            s.append("No CronJob activ");
                        }
                        s.append(">");
                        LOGGER.info(s.toString());
                    } else if (line.startsWith("tasks r ")) {
                        String name = line.replace("tasks r ", "");
                        ScheduleService.descheduleTask(name);
                        LOGGER.info("Tried to deschedule tasks <" + name + ">");
                    } else if (line.equals("blackjack deck")) {
                        LOGGER.info("Top cards are: " + BlackJack.getCards());
                    } else if (line.equalsIgnoreCase("upload slash")) {
                        uploadSlashCommands();
                    } else if (line.equals("update props")) {
                        AssetPool.clear();
                        AssetPool.init();
                        ServerUtil.clear();
                        ServerUtil.init();
                    }
                }
            } catch (IOException | InterruptedException ignored) { }
        },"Console_Thread").start();
    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public synchronized MusicManager getAudioPlayer() {
        Objects.requireNonNull(this.Manager.getGuildById(ServerUtil.GUILD)).getAudioManager()
                .setSendingHandler(musicManager.getSendHandler());
        return this.musicManager;
    }

    public JDA getManager() {
        return Manager;
    }

    private List<GatewayIntent> getDisabledIntents () {
        List<GatewayIntent> list = new LinkedList<>();
        list.add(GatewayIntent.DIRECT_MESSAGE_REACTIONS);
        list.add(GatewayIntent.DIRECT_MESSAGE_TYPING);
        list.add(GatewayIntent.GUILD_PRESENCES);
        return list;
    }

    private List<CacheFlag> getDisabledCacheFlags () {
        List<CacheFlag> list = new LinkedList<>();
        list.add(CacheFlag.ACTIVITY);
        list.add(CacheFlag.CLIENT_STATUS);
        return list;
    }

    private void uploadSlashCommands() {
        LOGGER.info("Try to upload new Slash command set ...");
        CommandListUpdateAction commands = Manager.updateCommands();
        commands.addCommands().queue();

        Manager.updateCommands().addCommands(
                Commands.slash("coinflip","Wirft eine Münze für dich"),
                Commands.slash("dice","Wirft Würfel für dich")
                        .addOptions(new OptionData(INTEGER,"dices","Anzahl an Würfel die geworfen werden").setRequired(false))
                        .addOptions(new OptionData(INTEGER,"eyes","Anzahl an Augen der Würfels").setRequired(false))
        ).queue();

        Guild guild = Manager.getGuildById("286628427140825088");

        guild.updateCommands().addCommands(
                Commands.slash("load","Lädt angegebene Playlist")
                        .addOptions(new OptionData(STRING, "playlist","Deine Bot Playlist").setRequired(true)),
                Commands.slash("save", "Speichert aktuellen Track in Playlist")
                        .addOptions(new OptionData(STRING,"playlist","In welche Playlist gespeichert wird").setRequired(true)),
                Commands.slash("volume", "Verändert die Lautstärke")
                        .addOptions(new OptionData(INTEGER,"value","Lautstärke zwischen 0-100").setRequired(true)),
                Commands.slash("remove", "Entfernt Track mit der angegebenen Nummer")
                        .addOptions(new OptionData(INTEGER,"tracknumber","Die Nummer des Tracks").setRequired(true)),
                Commands.slash("start", "Startet Track mit der angegebenen Nummer")
                        .addOptions(new OptionData(INTEGER,"tracknumber","Die Nummer des Tracks").setRequired(true)),
                Commands.slash("stop", "Leert die Queue und beendet das aktuelles Lied"),
                Commands.slash("pop","Nächster Track ist die angegebene Nummer")
                        .addOptions(new OptionData(INTEGER,"tracknumber","Die Nummer des Tracks").setRequired(true)),
                Commands.slash("search","Sucht Video auf YouTube")
                        .addOptions(new OptionData(STRING,"query","Was gesucht werden soll").setRequired(true))
        ).queue();
        LOGGER.info("Successfully uploaded Slash command set. The Commands will be activated within an hour");
    }
}
