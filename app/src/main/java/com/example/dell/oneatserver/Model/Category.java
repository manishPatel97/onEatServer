package com.example.dell.oneatserver.Model;

public class Category {
    private String name,image;
    public Category(){}
    public Category(String Name, String Image){
        name=Name;

        image =Image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


}
