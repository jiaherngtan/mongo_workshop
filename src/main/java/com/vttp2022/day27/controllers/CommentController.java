package com.vttp2022.day27.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vttp2022.day27.models.Comment;
import com.vttp2022.day27.services.CommentService;

@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping(path = "/search")
    public String getSearchResult(
            @RequestParam String query,
            @RequestParam Float score,
            Model model) {

        List<Comment> comments = commentService.textSearch(query, score, 20, 0);

        System.out.println(comments.size());

        model.addAttribute("num", comments.size());
        model.addAttribute("query", query);
        model.addAttribute("hasResult", comments.size() > 0);
        model.addAttribute("comments", comments);

        return "result";
    }
}
