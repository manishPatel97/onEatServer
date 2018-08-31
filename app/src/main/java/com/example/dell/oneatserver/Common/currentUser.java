package com.example.dell.oneatserver.Common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.example.dell.oneatserver.Model.Request;
import com.example.dell.oneatserver.Model.User;
import com.example.dell.oneatserver.Remote.APIService;
import com.example.dell.oneatserver.Remote.FCMRetrofitClient;
import com.example.dell.oneatserver.Remote.IGeoCoordinates;
import com.example.dell.oneatserver.Remote.RetrofitClient;

import retrofit2.Retrofit;

public class currentUser {
    public static User currentuser;
    public static Request currentrequest;
    public static int number;
    public static String update= "Update";
    public static String delete = "Delete";
    public static final String USER_KEY ="User";
    public static final String PWD_KEY = "Password";
    public static final String baseUrl= "https://maps.googleapis.com";
    public static final String FCM_URL ="https://fcm.googleapis.com/";

    public static APIService getFCMService(){
        return FCMRetrofitClient.getClient(FCM_URL).create(APIService.class);
    }

    public static final int PICK_IMAGE_REQUEST =71;
    public static String convertCodeToStatus(String status) {
        if(status.equals("0")){
            return "Placed";
        }
        else if(status.equals("1")){
            return "On my way";
        }
        else{
            return "Shipped";
        }
    }

    public static IGeoCoordinates getGeoCodeService(){
        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap,int newWidth,int newHeight){
        Bitmap scaledBitmap =Bitmap.createBitmap(newWidth,newHeight,Bitmap.Config.ARGB_8888);
        float ScaleX = newWidth/(float)bitmap.getWidth();
        float ScaleY = newHeight/(float)bitmap.getHeight();
        float PivotX =0,PivotY=0;
        Matrix scaleMatrix =new Matrix();
        scaleMatrix.setScale(ScaleX,ScaleY,PivotX,PivotY);

        Canvas canvas =new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));
        return scaledBitmap;
    }
}
