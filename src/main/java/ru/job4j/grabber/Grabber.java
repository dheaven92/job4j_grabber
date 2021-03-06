package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.util.SqlRuDateTimeParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {

    private final static int PAGES = 5;
    private final static String SEARCH_QUERY = "java";

    private final Properties config = new Properties();

    public Store store() throws SQLException, ClassNotFoundException {
        return new PsqlStore(config);
    }

    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    private void config() throws IOException {
        try (InputStream in = new FileInputStream("./src/main/resources/grabber.properties")) {
            config.load(in);
        }
    }

    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder schedule = simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt(config.getProperty("quartz.interval")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(schedule)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {

        @Override
        public void execute(JobExecutionContext context) {
            JobDataMap data = context.getJobDetail().getJobDataMap();
            Store store = (Store) data.get("store");
            Parse parse = (Parse) data.get("parse");
            int page = 1;
            while (page <= PAGES) {
                parse.list("https://www.sql.ru/forum/job-offers/" + page).stream()
                        .filter(post -> post.getTitle().toLowerCase().contains(SEARCH_QUERY))
                        .forEach(store::save);
                page++;
            }
        }
    }

    public void web(Store store) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Integer.parseInt(config.getProperty("http.port")))) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        for (Post post : store.getAll()) {
                            out.write(post.toString().getBytes(Charset.forName("Windows-1251")));
                            out.write(System.lineSeparator().getBytes());
                        }
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) throws IOException, SchedulerException, SQLException, ClassNotFoundException {
        Grabber grabber = new Grabber();
        grabber.config();
        grabber.init(
                new SqlRuParse(new SqlRuDateTimeParser()),
                grabber.store(),
                grabber.scheduler()
        );
        grabber.web(grabber.store());
    }
}
