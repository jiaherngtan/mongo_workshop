package com.vttp2022.day27.models;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.bson.Document;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

public class Game {

    private int gid;
    private String name;
    private int year;
    private int ranking;
    private int usersRated;
    private String url;
    private String imageUrl;
    private List<Comment> comments = new LinkedList<>();

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public int getUsersRated() {
        return usersRated;
    }

    public void setUsersRated(int usersRated) {
        this.usersRated = usersRated;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "Game [gid=" + gid + ", name=" + name + ", year=" + year + ", ranking=" + ranking + ", usersRated="
                + usersRated + ", url=" + url + ", imageUrl=" + imageUrl + ", comments=" + comments + "]";
    }

    public static Game create(Document d) {
        Game game = new Game();
        game.setGid(d.getInteger("gid"));
        game.setName(d.getString("name"));
        game.setYear(d.getInteger("year"));
        game.setRanking(d.getInteger("ranking"));
        game.setUsersRated(d.getInteger("users_rated"));
        game.setUrl(d.getString("url"));
        game.setImageUrl(d.getString("image"));
        List<Comment> comments = new LinkedList<>();
        if (null != d.get("reviews")) {
            List<Document> docs = d.get("reviews", List.class);
            for (Document doc : docs) {
                Comment comment = new Comment();
                comment.setId(doc.get("_id").toString());
                comment.setRating(doc.getInteger("rating"));
                comment.setUser(doc.getString("user"));
                comment.setText(doc.getString("c_text"));
                comments.add(comment);
            }
            game.setComments(comments);
        }
        return game;
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("game_id", defaultValue(gid, null))
                .add("name", defaultValue(name, "not found"))
                // .add("year", defaultValue(year, null))
                .add("ranking", defaultValue(ranking, null))
                // .add("users_rated", defaultValue(usersRated, null))
                // .add("url", defaultValue(url, null))
                // .add("image_url", defaultValue(imageUrl, null))
                .build();
    }

    public JsonObject toJsonFull() {
        return Json.createObjectBuilder()
                .add("game_id:", defaultValue(gid, null))
                .add("name:", defaultValue(name, "not found"))
                .add("year:", defaultValue(year, null))
                .add("ranking:", defaultValue(ranking, null))
                .add("users_rated:", defaultValue(usersRated, null))
                .add("url:", defaultValue(url, null))
                .add("image_url:", defaultValue(imageUrl, null))
                .add("timestamp:", (new Date()).toString())
                .build();
    }

    public JsonObject toJsonWithCommentId() {

        Integer ratingSum = 0;
        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
        for (Comment c : comments) {
            ratingSum += c.getRating();
            String review = "/review/" + c.getId();
            arrBuilder.add(review);
        }
        JsonArray reviewsArr = arrBuilder.build();

        return Json.createObjectBuilder()
                .add("game_id", defaultValue(gid, null))
                .add("name", defaultValue(name, null))
                .add("year", defaultValue(year, null))
                .add("rank", defaultValue(ranking, null))
                .add("average", defaultValue(ratingSum / comments.size(), null))
                .add("users_rated", defaultValue(usersRated, null))
                .add("url", defaultValue(url, null))
                .add("thumbnail", defaultValue(imageUrl, null))
                .add("reviews", reviewsArr)
                .add("timestamp", defaultValue((new Date()).toString(), null))
                .build();
    }

    public JsonObject toJsonWithHighestOrLowestRating(Comment comment) {
        return Json.createObjectBuilder()
                .add("_id", defaultValue(gid, null))
                .add("name", defaultValue(name, null))
                .add("rating", defaultValue(comment.getRating(), null))
                .add("user", defaultValue(comment.getUser(), null))
                .add("comment", defaultValue(comment.getText(), null))
                .add("review_id", defaultValue(comment.getId(), null))
                .build();
    }

    public <T> T defaultValue(T actualVal, T defaultVal) {
        if (null == actualVal)
            return defaultVal;
        return actualVal;
    }

}
