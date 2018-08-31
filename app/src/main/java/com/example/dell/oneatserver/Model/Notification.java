package com.example.dell.oneatserver.Model;

public class Notification {
    public String Title;
    public String Body;

    public Notification(String title, String body) {
        Title = title;
        Body = body;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getBody() {
        return Body;
    }

    public void setBody(String body) {
        Body = body;
    }
    public Notification(){}
}
