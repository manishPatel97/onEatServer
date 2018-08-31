package com.example.dell.oneatserver.Service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.dell.oneatserver.MainActivity;
import com.example.dell.oneatserver.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static android.support.constraint.Constraints.TAG;

public class myFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage == null)
            return;

        // check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Notification body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage);
        }else{
            Log.e(TAG,"noftification body is null");
        }

        // check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "From: " + remoteMessage.getFrom());
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }else{
            Log.e(TAG,"Message body is null");
        }

       /* if(remoteMessage.getNotification()!=null){
            System.out.println("RemoteMsg Notification is not null");

        }
        else{
            System.out.println("RemoteMsg Notification is null");
        }*/
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        RemoteMessage.Notification  notification= remoteMessage.getNotification();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent= PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        NotificationManager notiManager  = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notiManager.notify(0,builder.build());
    }
}
