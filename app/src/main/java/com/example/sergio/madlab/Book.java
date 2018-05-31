package com.example.sergio.madlab;

import com.google.android.gms.maps.model.LatLng;

public class Book {

    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String edityear;
    private String genre;
    private String tags;
    private String condition;
    private String user;
    private double latitude;
    private double longitude;


    public Book(){}

    public Book(String isbn, String title, String author, String publisher, String edityear, String genre, String tags, String condition, String user, double latitude, double longitude) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.edityear = edityear;
        this.genre = genre;
        this.tags = tags;
        this.condition = condition;
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getEdityear() {
        return edityear;
    }

    public void setEdityear(String edityear) {
        this.edityear = edityear;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public double getLatitude() { return latitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }
}