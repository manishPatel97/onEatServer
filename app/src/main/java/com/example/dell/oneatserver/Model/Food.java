package com.example.dell.oneatserver.Model;

public class Food {
    private   String  name , image , description, price , discount , menuid ;

    public Food(){}
    public Food(String  Name , String Image ,  String Description,String Price ,String Discount ,String Menuid){
        name = Name;
        image = Image;
        description = Description;
        price = Price;
        discount = Discount;
        menuid = Menuid;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getMenuid() {
        return menuid;
    }

    public void setMenuid(String menuid) {
        this.menuid = menuid;
    }
}
