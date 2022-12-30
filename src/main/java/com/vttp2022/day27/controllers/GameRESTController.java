package com.vttp2022.day27.controllers;

import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vttp2022.day27.models.Comment;
import com.vttp2022.day27.models.Edit;
import com.vttp2022.day27.models.Game;
import com.vttp2022.day27.services.CommentService;
import com.vttp2022.day27.services.GameService;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

@RestController
public class GameRESTController {

    @Autowired
    private GameService gameService;

    @Autowired
    private CommentService commentService;

    // db.games.find(
    // { $text: { $search: "Twilight Inperium" }}).limit(25).skip(0)
    @GetMapping(path = "/games", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> gameSearch(
            @RequestParam String query,
            @RequestParam Integer limit,
            @RequestParam Integer offset) {

        List<Game> games = gameService.gameSearch(query, limit, offset);

        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
        for (Game game : games)
            arrBuilder.add(game.toJson());

        JsonObject objBuilder = Json.createObjectBuilder()
                .add("games:", arrBuilder.build())
                .add("offset:", offset)
                .add("limit:", limit)
                .add("total:", games.size())
                .add("timestamp:", (new Date()).toString())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objBuilder.toString());
    }

    // db.games.aggregate([ { $sort: { ranking: 1 }} ])
    @GetMapping(path = "/games/rank", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> browseGamesByRank() {

        List<Game> games = gameService.browseGamesByRank(10, 0);

        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
        for (Game game : games)
            arrBuilder.add(game.toJson());

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(arrBuilder.build().toString());
    }

    @GetMapping(path = "/games/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> browseGameById(@PathVariable String id) {

        try {

            Game game = gameService.browseGameByGid(id);

            JsonObject jsonObj = game.toJsonFull();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonObj.toString());

        } catch (Exception e) {

            String errMsg = "Game not found for id " + id;

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errMsg);
        }
    }

    @PostMapping(path = "/review")
    public ResponseEntity<String> addReview(@RequestBody MultiValueMap<String, String> form) {

        try {

            String user = form.getFirst("user");
            Integer rating = Integer.parseInt(form.getFirst("rating"));
            String comment = form.getFirst("comment");
            String id = form.getFirst("id");
            String posted = (new Date()).toString();
            Comment c = new Comment();
            c.setUser(user);
            c.setRating(rating);
            c.setText(comment);
            c.setGid(Integer.parseInt(id));
            c.setPosted(posted);

            Game game = gameService.browseGameByGid(id);

            JsonObject objBuilder = Json.createObjectBuilder()
                    .add("user:", user)
                    .add("rating:", rating)
                    .add("comment:", comment)
                    .add("ID:", id)
                    .add("posted:", posted)
                    .add("name:", game.getName())
                    .build();

            // add the review
            Integer insertSize = commentService.addReview(Document.parse(c.toJson().toString()));
            System.out.println("insert size: " + insertSize);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objBuilder.toString());

        } catch (Exception e) {

            String errMsg = "Error adding comment...";

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errMsg);
        }
    }

    @PutMapping(path = "/review/{id}")
    public ResponseEntity<String> updateReview(
            @PathVariable String id,
            @RequestBody Edit edit) {

        try {

            String comment = edit.getText();
            Integer rating = edit.getRating();
            String timestamp = (new Date()).toString();

            Comment c = commentService.updateReview(id, comment, rating, timestamp);

            Game game = gameService.browseGameByGid(Integer.toString(c.getGid()));
            String gameName = game.getName();

            JsonObject jsonObj = c.toJsonWithCommentHistory(gameName);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonObj.toString());

        } catch (Exception e) {

            String errMsg = "Couldn't update comment for comment id " + id;

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errMsg);
        }
    }

    @GetMapping(path = "/review/{id}")
    public ResponseEntity<String> displayLatestReview(
            @PathVariable String id) {

        try {

            Comment c = commentService.browseCommentById(id);

            boolean ifEdited = false;
            if (null != c.getEdits())
                ifEdited = true;
            Game game = gameService.browseGameByGid(Integer.toString(c.getGid()));
            String gameName = game.getName();

            JsonObject jsonObj = c.toJsonWithLatestComment(gameName, ifEdited);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonObj.toString());

        } catch (Exception e) {

            String errMsg = "Couldn't find status for comment id " + id;

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errMsg);
        }
    }

    @GetMapping(path = "/review/{id}/history")
    public ResponseEntity<String> displayFullReview(
            @PathVariable String id) {

        try {

            Comment c = commentService.browseCommentById(id);

            Game game = gameService.browseGameByGid(Integer.toString(c.getGid()));
            String gameName = game.getName();

            JsonObject jsonObj = c.toJsonWithCommentHistory(gameName);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonObj.toString());

        } catch (Exception e) {

            String errMsg = "Couldn't find status for comment id " + id;

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errMsg);
        }
    }

    // db.games.aggregate([
    // { $match: { name: "Tal der Könige" }},
    // {
    // $lookup: {
    // from: "comments",
    // foreignField: "gid",
    // localField: "gid",
    // as: "reviews"
    // }
    // }])
    @GetMapping(path = "/game/{id}/reviews")
    public ResponseEntity<String> displayGameReviews(
            @PathVariable String id) {

        try {

            Game game = gameService.browseGameByIdWithComments(id);

            JsonObject jsonObj = game.toJsonWithCommentId();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonObj.toString());

        } catch (

        Exception e) {

            String errMsg = "Couldn't find details for game id " + id;

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errMsg);
        }
    }

    // db.games.aggregate([
    // {
    // $lookup: {
    // from: "comments",
    // foreignField: "gid",
    // localField: "gid",
    // as: "comments",
    // pipeline: [
    // {
    // $sort: { rating: -1 }
    // },
    // {
    // $limit: 1
    // }]
    // }
    // }
    // ])
    @GetMapping(path = "/games/{query}")
    public ResponseEntity<String> listHighestOrLowest(
            @PathVariable String query) {

        try {

            List<Game> games = gameService.browseGamesByRating(query);

            JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
            for (Game game : games) {
                if (game.getComments().size() == 1)
                    arrBuilder.add(game.toJsonWithHighestOrLowestRating(game.getComments().get(0)));
            }

            JsonObject jsonObj = Json.createObjectBuilder()
                    .add("rating", query)
                    .add("games:", arrBuilder.build())
                    .add("timestamp:", (new Date()).toString())
                    .build();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonObj.toString());

        } catch (Exception e) {

            String errMsg = "Couldn't find details for " + query;

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errMsg);
        }
    }

}
