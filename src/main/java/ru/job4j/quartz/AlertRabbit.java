package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {

    private static final Logger LOG = LoggerFactory.getLogger(AlertRabbit.class.getName());

    public static void main(String[] args) throws Exception {
        Properties properties = getProperties();
        Class.forName(properties.getProperty("db.driver"));
        try (Connection connection = DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.username"),
                properties.getProperty("db.password")
        )) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(properties.getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            LOG.info("Rabbit runs here...");
            Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (PreparedStatement statement = connection.prepareStatement(
                    "insert into rabbit (created_date) values (?)"
            )) {
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                statement.execute();
                LOG.info("Logs of the job execution were saved in DB");
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
