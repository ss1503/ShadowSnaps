package com.example.shadowsnaps;

import java.util.ArrayList;
import java.util.List;

public class Users
{
    //components
    private String UserId;
    private List<TextTranslate> translate;
    private int toCapture;

    //constructor
    public Users(String UserId)
    {
        this.UserId = UserId;
        this.translate = new ArrayList<>();
        this.toCapture = 0;
    }

    //default constructor
    public Users() {
    }

    //Setters and Getters
    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public List<TextTranslate> getTranslate() {
        return translate;
    }

    public void setTranslate(List<TextTranslate> translate) {
        this.translate = translate;
    }

    public int getToCapture() {
        return toCapture;
    }

    public void setToCapture(int toCapture) {
        this.toCapture = toCapture;
    }

    //add to translate
    public void addTranslate(TextTranslate t)
    {
        this.translate.add(t);
    }

    public void resetTranslate()
    {
        this.translate = new ArrayList<TextTranslate>();
    }
}