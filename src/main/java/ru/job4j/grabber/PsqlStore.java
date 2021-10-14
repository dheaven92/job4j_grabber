package ru.job4j.grabber;

import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.util.SqlRuDateTimeParser;
import ru.job4j.quartz.AlertRabbit;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private final Connection connection;

    public PsqlStore(Properties properties) throws ClassNotFoundException, SQLException {
        Class.forName(properties.getProperty("jdbc.driver"));
        connection = DriverManager.getConnection(
                properties.getProperty("jdbc.url"),
                properties.getProperty("jdbc.username"),
                properties.getProperty("jdbc.password")
        );
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into post (title, description, link, created) values (?,?,?,?)"
        )) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("select * from post")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(new Post(
                            resultSet.getInt("id"),
                            resultSet.getString("title"),
                            resultSet.getString("description"),
                            resultSet.getString("link"),
                            resultSet.getTimestamp("created").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement = connection.prepareStatement(
                "select * from post where id = ?"
        )) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    post = new Post(
                            resultSet.getInt("id"),
                            resultSet.getString("title"),
                            resultSet.getString("description"),
                            resultSet.getString("link"),
                            resultSet.getTimestamp("created").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("grabber.properties")) {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Parse parse = new SqlRuParse(new SqlRuDateTimeParser());
        Store store = new PsqlStore(getProperties());
        List<Post> posts = parse.list("https://www.sql.ru/forum/job-offers/1");
        posts.forEach(store::save);
        List<Post> postsFromDb = store.getAll();
        postsFromDb.forEach(System.out::println);
        if (postsFromDb.size() > 0) {
            Post postFromDb = store.findById(postsFromDb.get(0).getId());
            System.out.println(postFromDb);
        }
    }
}
