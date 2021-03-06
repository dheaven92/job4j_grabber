package ru.job4j.grabber;

import ru.job4j.grabber.model.Post;

import java.util.ArrayList;
import java.util.List;

public class MemStore implements Store {

    private static List<Post> posts = new ArrayList<>();

    @Override
    public void save(Post post) {
        posts.add(post);
    }

    @Override
    public List<Post> getAll() {
        return posts;
    }

    @Override
    public Post findById(int id) {
        return posts.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
