package com.example.sergio.madlab;

public class User {

    private String  username,
            email,
            city,
            bio;

    public User(){}

    public User(String username, String email, String city, String bio) {
        this.username = username;
        this.email = email;
        this.city = city;
        this.bio = bio;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
