package model.service.task;

import org.apache.logging.log4j.Logger;
import util.ConnectionUtil;

public class TrackHistoryClearTask implements Runnable {

    private final Logger logger;

    public TrackHistoryClearTask(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        this.logger.info("Starting TrackHistoryClear CronJob ...");

        ConnectionUtil.executeSQL(ConnectionUtil.DELETE_OLD_TRACKS());

        this.logger.info("Successfully finished TrackHistoryClear CronJob");
    }

}
