package com.example.sergio.madlab.Classes;

public class BookRequest {

    private String requesterID;
    private String bookOwnerID;
    private String bookName;
    private String bookISBN;
    private String status;



    public BookRequest(){};

    public String getRequesterID() {
        return requesterID;
    }

    public void setRequesterID(String requesterID) {
        this.requesterID = requesterID;
    }

    public String getBookOwnerID() {
        return bookOwnerID;
    }

    public void setBookOwnerID(String bookOwnerID) {
        this.bookOwnerID = bookOwnerID;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookISBN() {
        return bookISBN;
    }

    public void setBookISBN(String bookISBN) {
        this.bookISBN = bookISBN;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
