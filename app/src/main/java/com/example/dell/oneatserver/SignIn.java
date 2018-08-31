package com.example.dell.oneatserver;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dell.oneatserver.Common.currentUser;
import com.example.dell.oneatserver.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.CheckBox;


import android.content.Intent;

import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {
    EditText username,password;
    Button signin;
    FirebaseDatabase database;
    DatabaseReference users;
    CheckBox chkBoxRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        signin = findViewById(R.id.signin);
        chkBoxRemember = findViewById(R.id.chkboxRemember);
        Paper.init(this);
        database = FirebaseDatabase.getInstance();
        users = database.getReference("User");
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chkBoxRemember.isChecked()){
                    Paper.book().write(currentUser.USER_KEY,username.getText().toString());
                    Paper.book().write(currentUser.PWD_KEY,password.getText().toString());

                }
                signInUser(username.getText().toString(),password.getText().toString());
            }
        });



    }

    private void signInUser(String phone, String passw) {
        final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
        mDialog.setMessage("Please Wait..");
        mDialog.show();
        final String localphone= phone;
        final String localpassw = passw;
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(localphone).exists()){
                    mDialog.dismiss();
                    User user =dataSnapshot.child(localphone).getValue(User.class);
                    user.setPhone(localphone);
                    if(Boolean.parseBoolean(user.getIsstaff())) //isStaff = true
                    {
                        if(user.getPassword().equals(localpassw)){
                            Intent homeintent = new Intent (SignIn.this,Home.class);
                            currentUser.currentuser = user;
                            startActivity(homeintent);
                            finish();
                        }
                        else{
                            Toast.makeText(SignIn.this,"Wrong Password!",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(SignIn.this,"Please Login with staff Account",Toast.LENGTH_SHORT).show();
                    }


                }else{
                    mDialog.dismiss();
                    Toast.makeText(SignIn.this,"User not exists",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
