package model.service.task;

import model.manager.DiscordBot;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.logging.log4j.Logger;
import util.ServerUtil;
import java.util.Objects;

public class UpdateStatsTask implements Runnable {

    private final Logger logger;

    public UpdateStatsTask(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        this.logger.info("Starting UpdateStatsTask CronJob ...");

        Guild guild = DiscordBot.INSTANCE.getManager().getGuildById(ServerUtil.GUILD);
        ServerUtil.GLOBAL_LOGGER.info("Boost/s: " + guild.getBoostCount() + " | Member/s: " + guild.getMemberCount());

        Objects.requireNonNull(Objects.requireNonNull(guild).getVoiceChannelById(ServerUtil.getChannelID("stats_member")))
                .getManager().setName("Member/s: " + guild.getMemberCount()).queue();
        Objects.requireNonNull(Objects.requireNonNull(guild).getVoiceChannelById(ServerUtil.getChannelID("stats_boost")))
                .getManager().setName("Boost/s: " + guild.getBoostCount()).queue();

        this.logger.info("Successfully finished UpdateStatsTask CronJob");
    }
}
