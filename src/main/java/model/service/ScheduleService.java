package model.service;

import it.sauronsoftware.cron4j.InvalidPatternException;
import it.sauronsoftware.cron4j.Scheduler;
import model.service.task.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author ShuraBlack
 * @since 03-20-2022
 * https://www.sauronsoftware.it/projects/cron4j/manual.php
 */
public class ScheduleService {

    private static final ThreadPoolExecutor SERVICE = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final Scheduler SCHEDULER = new Scheduler();
    private static final Map<String, String> TASKS = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LogManager.getLogger(ScheduleService.class);

    public static void submit(Runnable task) {
        SERVICE.submit(task);
    }

    public static void shutdownScheduler() {
        SERVICE.shutdown();
        if (!SERVICE.isShutdown()) {
            SERVICE.shutdownNow();
        }
        if (SCHEDULER.isStarted()) {
            SCHEDULER.stop();
        }
    }

    public static void submitCronJob(String timePattern, String name, Runnable task) {
        if (!SCHEDULER.isStarted()) {
            SCHEDULER.start();
        }
        try {
            LOGGER.info("Scheduled CronJob <\u001b[32;1m" + name + "\u001b[0m> with time <\u001b[32;1m" + timePattern + "\u001b[0m>" );
            TASKS.put(name,SCHEDULER.schedule(timePattern, task));
        } catch (InvalidPatternException e) {
            LOGGER.error("Invalid pattern in Cron Job scheduling <\u001b[31m" + timePattern + "\u001b[0m>",e);
        }
    }

    public static void descheduleTask(String name) {
        String taskID = TASKS.remove(name);
        if (taskID == null) {
            return;
        }
        SCHEDULER.deschedule(taskID);
        LOGGER.info("Descheduled CronJob <\u001b[32;1m" + name + "\u001b[0m>" );
    }

    public static void startCronJobs() {
        // 2 hours earlier, duo to Server location
        submitCronJob("0 1 * * 1-7","UpdateStatsTask" ,new UpdateStatsTask(LOGGER));
        submitCronJob("0 1 * * 1-7","GameDailyTask" ,new GameDailyTask(LOGGER));
        submitCronJob("0 3 * * 1-7","TrackHistoryClearTask" ,new TrackHistoryClearTask(LOGGER));
        submitCronJob("0 2 * * 1-7", "RulerReenableUserTask", new RulerReenableUserTask(LOGGER));
        submitCronJob("55 1 * * 1-7", "SubReenableUserTask", new SubReenableUserTask(LOGGER));
        submitCronJob("00 13 * * Sun", "WeeklyGameEventTask", new WeeklyGameEventTask(LOGGER));
    }

    public static Map<String, String> getTasks() {
        return TASKS;
    }

    public static ThreadFactory getThreadFactory() {
        return SERVICE.getThreadFactory();
    }

}
