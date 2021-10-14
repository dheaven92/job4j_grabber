package ru.job4j.grabber.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.SqlRuDateTimeParser;

public class SqlRuParse {

    public static void main(String[] args) throws Exception {
        int page = 1;
        while (page < 6) {
            Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers/" + page).get();
            Elements rows = doc.select(".postslisttopic");
            for (Element td : rows) {
                Element href = td.child(0);
                System.out.println(href.attr("href"));
                System.out.println(href.text());
                Element parent = td.parent();
                System.out.println(new SqlRuDateTimeParser().parse(parent.children().get(5).text()));
                System.out.println();
            }
            page++;
        }
    }
}
