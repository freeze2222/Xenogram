package com.FinalP.finalchat.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.FinalP.finalchat.listeners.SimpleListener;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.models.domain.UserD;
import com.firebase.ui.database.ClassSnapshotParser;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;

public class DatabaseService {

    public static DatabaseReference usersRef(String id) {
        return FirebaseDatabase.getInstance()
                .getReference("users/" + id.replaceAll(";", "").replaceAll("\\.", "").replaceAll("@", ""));
    }

    public static void addAvatar(String userId,String bitmapPicBase64){
        usersRef(userId+"/avatar").setValue(bitmapPicBase64);
    }
    public static void addUser(UserD user) {
        DatabaseReference ref = usersRef(user.email);
        ref.setValue(user);
        ref.getKey();
    }

    public static String reformString(String s) {
        return s.replaceAll(";", "").replaceAll("\\.", "").replaceAll("@", "");
    }

    public static void getUser(String id, SimpleListener<User> listener) {
        usersRef(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserD userD = snapshot.getValue(UserD.class);
                if (userD == null) {
                    listener.onException();
                    return;
                }

                User user = new User(userD);
                listener.onValue(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onDatabaseError();
            }
        });
    }

    public static void getNameFromKey(String key, Callback<User> callback) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/" + key);
        System.out.println(ref);
        GetUserSupport.getValue(ref, callback);
    }


    public static FirebaseRecyclerOptions<String> getUsersOptions(User user) {
        Query query = usersRef(user.id).child("chats");
        ClassSnapshotParser<String> parser = new ClassSnapshotParser<>(String.class);

        return new FirebaseRecyclerOptions.Builder<String>()
                .setQuery(query, parser)
                .build();
    }
    public static void updateUserName(String id,String newName){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/" + id);
        ref.child("/name").setValue(newName);
    }
    public static String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
    public static Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
}
