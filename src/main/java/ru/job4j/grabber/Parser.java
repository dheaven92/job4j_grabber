package ru.job4j.grabber;

import ru.job4j.grabber.model.Post;

import java.util.List;

public interface Parser {

    List<Post> list(String link);

    Post detail(String link);
}
