package ru.job4j.grabber.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {

    public static void main(String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(getProperties().getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
        }
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        try (BufferedReader in = new BufferedReader(
                new FileReader("./src/main/resources/rabbit.properties")
        )) {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                try {
                    String[] parts = line.split("=");
                    properties.setProperty(parts[0], parts[1]);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid file format");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
