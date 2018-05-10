package com.example.sergio.madlab;

public class User {

    private String  name;
    private String  email;
    private String  city;
    private String  bio;

    public User(){}

    public User(String name, String email, String city, String bio) {
        this.name = name;
        this.email = email;
        this.city = city;
        this.bio = bio;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
