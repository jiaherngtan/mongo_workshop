package com.vttp2022.day27.models;

public class Edit {

    private String text;
    private int rating;
    private String posted;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getPosted() {
        return posted;
    }

    public void setPosted(String posted) {
        this.posted = posted;
    }

    @Override
    public String toString() {
        return "Edited [text=" + text + ", rating=" + rating + ", posted=" + posted + "]";
    }

}
