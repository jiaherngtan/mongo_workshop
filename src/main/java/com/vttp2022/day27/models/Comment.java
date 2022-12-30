package com.vttp2022.day27.models;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.bson.Document;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class Comment {

    private String id;
    private String user;
    private int rating;
    private String text;
    private int gid;
    private String posted;
    private Double score;
    private List<Edit> edits = new LinkedList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public String getPosted() {
        return posted;
    }

    public void setPosted(String posted) {
        this.posted = posted;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public List<Edit> getEdits() {
        return edits;
    }

    public void setEdits(List<Edit> edits) {
        this.edits = edits;
    }

    @Override
    public String toString() {
        return "Comment [id=" + id + ", user=" + user + ", rating=" + rating + ", text=" + text + ", gid=" + gid
                + ", posted=" + posted + ", score=" + score + ", edits=" + edits + "]";
    }

    public static Comment create(Document d) {
        Comment comment = new Comment();
        comment.setId(d.getString("c_id"));
        comment.setUser(d.getString("user"));
        comment.setRating(d.getInteger("rating"));
        comment.setText(d.getString("c_text"));
        comment.setGid(d.getInteger("gid"));
        comment.setScore(d.getDouble("score"));
        List<Edit> edits = new LinkedList<>();
        if (null != d.get("edited")) {
            List<Document> docs = d.get("edited", List.class);
            for (Document doc : docs) {
                Edit edit = new Edit();
                Document ratingDoc = doc.get("rating", Document.class);
                edit.setRating(ratingDoc.getInteger("num"));
                Document commentDoc = doc.get("comment", Document.class);
                edit.setText(commentDoc.getString("value"));
                Document postedDoc = doc.get("posted", Document.class);
                edit.setPosted(postedDoc.getString("value"));
                edits.add(edit);
            }
            comment.setEdits(edits);
        }
        return comment;
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("user", defaultValue(user, "not found"))
                .add("rating", defaultValue(rating, null))
                .add("c_text", defaultValue(text, null))
                .add("gid", defaultValue(gid, null))
                .build();
    }

    public JsonArray toJson(List<Edit> edits) {

        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
        for (Edit e : edits) {
            JsonObjectBuilder objBuilder = Json.createObjectBuilder();
            objBuilder.add("comment", defaultValue(e.getText(), null));
            objBuilder.add("rating", defaultValue(e.getRating(), null));
            objBuilder.add("posted", defaultValue(e.getPosted(), null));
            arrBuilder.add(objBuilder);
        }
        JsonArray editsArr = arrBuilder.build();

        return editsArr;
    }

    public JsonObject toJsonWithCommentHistory(String gameName) {

        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
        for (Edit e : edits) {
            JsonObjectBuilder objBuilder = Json.createObjectBuilder();
            objBuilder.add("comment", e.getText());
            objBuilder.add("rating", e.getRating());
            objBuilder.add("posted", e.getPosted());
            arrBuilder.add(objBuilder);
        }
        JsonArray editsArr = arrBuilder.build();

        return Json.createObjectBuilder()
                .add("user", defaultValue(user, "not found"))
                .add("rating", defaultValue(rating, null))
                .add("comment", defaultValue(text, null))
                .add("ID", defaultValue(gid, null))
                .add("posted", defaultValue(posted, "no info available"))
                .add("name", defaultValue(gameName, null))
                .add("edited", editsArr)
                .add("timestamp", defaultValue((new Date()).toString(), null))
                .build();
    }

    public JsonObject toJsonWithLatestComment(String gameName, boolean ifEdited) {

        return Json.createObjectBuilder()
                .add("user", defaultValue(user, "not found"))
                .add("rating", defaultValue(rating, null))
                .add("comment", defaultValue(text, null))
                .add("ID", defaultValue(gid, null))
                .add("posted", defaultValue(posted, "no info available"))
                .add("name", defaultValue(gameName, null))
                .add("edited", defaultValue(ifEdited, null))
                .add("timestamp", defaultValue((new Date()).toString(), null))
                .build();
    }

    public <T> T defaultValue(T actualVal, T defaultVal) {
        if (null == actualVal)
            return defaultVal;
        return actualVal;
    }

}
