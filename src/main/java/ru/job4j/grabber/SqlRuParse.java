package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.util.DateTimeParser;
import ru.job4j.grabber.util.SqlRuDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SqlRuParse implements Parser {

    private final DateTimeParser dateTimeParser;

    public SqlRuParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        int page = 1;
        while (page < 6) {
            Document doc = null;
            try {
                doc = Jsoup.connect("https://www.sql.ru/forum/job-offers/" + page).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (doc != null) {
                Elements rows = doc.select(".postslisttopic");
                for (Element td : rows) {
                    Post post = new Post();
                    Element href = td.child(0);
                    post.setTitle(href.text());
                    post.setLink(href.attr("href"));
                    post.setCreated(dateTimeParser.parse(td.parent().children().get(5).text()));
                    posts.add(post);
                }
                page++;
            }
        }
        return posts;
    }

    @Override
    public Post detail(String link) {
        Post post = null;
        Document doc = null;
        try {
            doc = Jsoup.connect(link).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (doc != null) {
            post = new Post();
            post.setDescription(doc.select(".msgBody").get(1).text());
            post.setCreated(new SqlRuDateTimeParser().parse(
                    doc.select(".msgFooter").text().split("\\[\\d*")[0].trim()
            ));
        }
        return post;
    }
}