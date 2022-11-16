package util;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import model.manager.DiscordBot;
import model.service.ScheduleService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author ShuraBlack
 * @since 03-20-2022
 */
public class ServerUtil {

    public static final Logger GLOBAL_LOGGER = LogManager.getLogger("Global_Logger");

    private static final Map<String,String> CHANNELIDS = new HashMap<>();
    private static final Map<String, String> MESSAGEIDS = new HashMap<>();
    private static final Map<String,String> WEBHOOKS = new HashMap<>();

    /**
     * Color code for default use (grey)
     */
    public static int DEFAULT_COLOR = 3289650;

    /**
     * Color code for positive use (green)
     */
    public static int GREEN = 4437377;

    /**
     * Color code for negative use (red)
     */
    public static int RED = 16729871;

    /**
     * Color code for neutral use (blue)
     */
    public static int BLUE = 3375061;

    public static String GUILD = "286628427140825088";

    public static String TMP_VOICE_CATEGORY = "820340114538102814";

    public static boolean MUTE = false;

    /**
     * Initialize ServerUtil. This will load data from channels.properties & webhooks.properties
     */
    public static void init() {
        GLOBAL_LOGGER.info("Try to load channel data for ServerUtil <\u001b[32;1mwebhooks.properties, channels.properties, message.properties\u001b[0m>");
        Properties properties = FileUtil.loadProperties("channels.properties");
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            CHANNELIDS.put((String) entry.getKey(), (String) entry.getValue());
        }
        properties = FileUtil.loadProperties("messages.properties");
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            MESSAGEIDS.put((String) entry.getKey(), (String) entry.getValue());
        }
        properties = FileUtil.loadProperties("webhooks.properties");
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            WEBHOOKS.put((String) entry.getKey(), (String) entry.getValue());
        }
        GLOBAL_LOGGER.info("Successfully finished to load channel data for ServerUtil");
    }

    public static void clear() {
        CHANNELIDS.clear();
        MESSAGEIDS.clear();
        WEBHOOKS.clear();
    }

    public static String getChannelID(@Nonnull final String key) {
        return CHANNELIDS.getOrDefault(key, "");
    }

    public static String getMessageID(@Nonnull final String key) {
        return MESSAGEIDS.getOrDefault(key, "");
    }

    public static String getWebHookLink(@Nonnull final String key) {
        return WEBHOOKS.getOrDefault(key, "");
    }

    /**
     * Creates an {@link Thread} which will handle the webhook event.
     * This will need and Object of {@link WebhookEmbedBuilder}, where you define the look & feel of your message
     * @param webHookLink corresponding link where it should be send to
     * @param builder Message Object
     */
    public static void sendWebHookMessage(String webHookLink, WebhookEmbedBuilder builder) {
        WebhookClientBuilder clientBuilder = new WebhookClientBuilder(webHookLink);

        clientBuilder.setThreadFactory(ScheduleService.getThreadFactory());

        clientBuilder.setWait(true);
        WebhookClient client = clientBuilder.build();

        client.send(builder.build());
        client.close();
    }

    public static void sendFeedBack(String message, int color, SelectMenuInteractionEvent event) {
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(color)
                .setDescription(message);
        event.replyEmbeds(eb.build()).setEphemeral(true).queue();
    }

    public static void sendFeedBack(String message, int color, ModalInteractionEvent event) {
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(color)
                .setDescription(message);
        event.replyEmbeds(eb.build()).setEphemeral(true).queue();
    }

    public static void sendFeedBack(String message, int color, ButtonInteractionEvent event) {
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(color)
                .setDescription(message);
        event.replyEmbeds(eb.build()).setEphemeral(true).queue();
    }
    public static void sendThumbnailFeedBack(String message, int color, boolean visible, String image, ButtonInteractionEvent event) {
        EmbedBuilder eb = new EmbedBuilder()
                .setThumbnail(image)
                .setColor(color)
                .setDescription(message);
        event.replyEmbeds(eb.build()).setEphemeral(visible).queue();
    }

    public static void sendFeedBack(String message, int color, TextChannel channel) {
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(color)
                .setDescription(message);
        channel.sendMessageEmbeds(eb.build()).complete().delete().queueAfter(7, TimeUnit.SECONDS);
    }

    public static boolean hasRole(Member member, String roleid) {
        return member.getRoles().stream().anyMatch(role -> role.getId().equals(roleid));
    }

    public static TextChannel getTextChannel(String id) {
        return DiscordBot.INSTANCE.getManager().getGuildById(GUILD).getTextChannelById(id);
    }

    public static VoiceChannel getVoicChannel(String id) {
        return DiscordBot.INSTANCE.getManager().getGuildById(GUILD).getVoiceChannelById(id);
    }

    public static Member getUser(String id) {
        return DiscordBot.INSTANCE.getManager().getGuildById(GUILD).retrieveMemberById(id).complete();
    }

    public static boolean isChannelType(Channel channel, ChannelType type) {
        return channel.getType().equals(type);
    }

    public static boolean isChannelId(Channel channel, String propertiesName) {
        return channel.getId().equals(ServerUtil.getChannelID(propertiesName));
    }

}
