package com.example.sergio.madlab;

public class Book {
    String isbn;
    String title;
    String author;
    String publisher;
    String edityear;
    String genre;
    String tags;

    public Book() {

    }

    public Book(String isbn, String title, String author, String publisher, String edityear, String genre, String tags) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.edityear = edityear;
        this.genre = genre;
        this.tags = tags;
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

    public String getEditYear() {
        return edityear;
    }

    public void setEditYear(String editYear) {
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
}