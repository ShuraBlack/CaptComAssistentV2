package model.service.task;

import model.game.event.EventManager;
import org.apache.logging.log4j.Logger;
import util.ServerUtil;

public class WeeklyGameEventTask implements Runnable {

    private final Logger logger;

    public WeeklyGameEventTask(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        this.logger.info("Starting WeeklyGameEventTask CronJob ...");

        EventManager.reset();
        EventManager.createEvent();
        EventManager.updateMessage(ServerUtil.getTextChannel(ServerUtil.getChannelID("game_info")));
        EventManager.save();

        this.logger.info("Successfully finished WeeklyGameEventTask CronJob");
    }

}
