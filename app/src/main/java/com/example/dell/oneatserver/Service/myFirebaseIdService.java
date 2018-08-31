package com.example.dell.oneatserver.Service;

import com.example.dell.oneatserver.Common.currentUser;
import com.example.dell.oneatserver.Model.Token;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class myFirebaseIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        updateToServer(refreshToken);

    }

    private void updateToServer(String refreshToken) {
        FirebaseDatabase db= FirebaseDatabase.getInstance();
        DatabaseReference Tokens =db.getReference("Tokens");
        Token token = new Token(refreshToken,true);
        System.out.println("phone "+currentUser.currentuser.getPhone());
        Tokens.child(currentUser.currentuser.getPhone()).setValue(token);//make userphone number as a key
    }
}
