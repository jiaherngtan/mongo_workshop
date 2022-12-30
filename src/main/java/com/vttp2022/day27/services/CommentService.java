package com.vttp2022.day27.services;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vttp2022.day27.models.Comment;
import com.vttp2022.day27.models.Edit;
import com.vttp2022.day27.repositories.CommentRepository;
import com.vttp2022.day27.repositories.LogRepository;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LogRepository logRepository;

    public List<Comment> textSearch(String query, Float score, int limit, int offset) {

        List<String> matches = new LinkedList<>();
        List<String> notMatch = new LinkedList<>();

        for (String word : query.split(" ")) {
            String w = word.trim();
            if (w.startsWith("-"))
                notMatch.add(w.substring(1));
            else
                matches.add(w);
        }

        try {
            return commentRepository.textSearch(matches, notMatch, limit, offset)
                    .stream()
                    .filter(c -> c.getScore() >= score)
                    .toList();
        } finally {
            logRepository.log(query);
        }
    }

    public Integer addReview(Document doc) {
        return commentRepository.addReview(doc);
    }

    public Comment browseCommentById(String id) {
        return commentRepository.browseCommentById(id);
    }

    public Comment updateReview(String id, String comment, Integer rating, String timestamp) {

        Comment c = browseCommentById(id);

        Edit e = new Edit();
        e.setText(c.getText());
        e.setRating(c.getRating());
        e.setPosted((new Date()).toString());
        List<Edit> edits = new LinkedList<>();
        if (null != c.getEdits()) {
            System.out.println("edits is not null");
            edits = c.getEdits();
        }
        edits.add(e);
        c.setEdits(edits);
        c.setText(comment);
        c.setRating(rating);
        c.setPosted(timestamp);

        return commentRepository.updateReview(id, c);
    }

}
