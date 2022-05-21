package com.FinalP.finalchat.services;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.json.JSONObject;

import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FirebaseMessagingServices extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        if (FirebaseAuth.getInstance().getCurrentUser()!=null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(DatabaseService.reformString(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail()))).child("token");
            databaseReference.setValue(s);
            Log.e("DONE", "GENERATED!!");
        }
    }
    public static void getUserToken(String id, Callback<String> callback){
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("users").child(id).child("token");
        databaseReference.get().addOnSuccessListener(dataSnapshot -> callback.call(dataSnapshot.getValue(String.class)));
    }
    public static void sendPushedNotification(final String fcmToken) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();

                    JSONObject json = new JSONObject();
                    JSONObject notificationJson = new JSONObject();

                    notificationJson.put("text", "Новое сообщение!");
                    notificationJson.put("title", "Кто-то пытается с вами связаться!");
                    notificationJson.put("priority", "high");

                    json.put("notification", notificationJson);
                    json.put("to", fcmToken);

                    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());

                    Log.e("123","key: "+fcmToken);

                    Request request = new Request.Builder()
                            .header("Authorization", "key=AAAAy7GIffo:APA91bHTWxoyjXcYw3nZ14NkXctTrwL4oex3LqAESatLlGd8w2k_A0ixswMxNYOdOMRKj0utU6vJ1cxK95cgAJ6YDmr0uYFq5h11gleKnJWIcQXtJKPxHYrOXqvlmOFAhk8c4s3sjxtJ")
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();

                    String finalResponse = Objects.requireNonNull(response.body()).string();
                    Log.e("TAG", finalResponse);
                } catch (Exception e) {

                    Log.e("TAG", e.getMessage());
                }
                return null;
            }
        }.execute();
    }
    public static void checkToken(){
        FirebaseMessagingServices.getUserToken(DatabaseService.reformString(Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail())), arg -> {
            if (arg==null) {
                FirebaseMessaging.getInstance().deleteToken();
                Log.e("DONE","DELETED!!");
                Log.e("DONE",FirebaseMessaging.getInstance().getToken().toString());
            }
        });
    }
}
