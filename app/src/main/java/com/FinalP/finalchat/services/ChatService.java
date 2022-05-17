package com.FinalP.finalchat.services;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.fragments.ChatFragment;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.models.domain.GroupD;
import com.FinalP.finalchat.models.domain.GroupMetadataD;
import com.FinalP.finalchat.models.domain.MessageD;
import com.firebase.ui.database.ClassSnapshotParser;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class ChatService {
    public static DatabaseReference dialogsRef() {
        return FirebaseDatabase.getInstance()
                .getReference("chat")
                .child("dialogs");
    }

    public static DatabaseReference dialogsRef(User userA, User userB) {
        String[] strings = new String[]{userA.id, userB.id};
        Arrays.sort(strings);
        String chatId = strings[0] + "-" + strings[1];

        return dialogsRef().child(chatId);
    }

    public static Task<Void> sendMessage(String text, User currentUser, User toUser) {
        Log.e("DEBUG!",currentUser.id);
        MessageD message = new MessageD(text, false, false, currentUser.id, new Date().getTime());
//        ...
        return dialogsRef(currentUser, toUser).child("messages").push().setValue(message);
    }

    public static Task<Void> createDialog(User currentUser, User toUser) {
        GroupD dialog = new GroupD(
                new GroupMetadataD(new Date().getTime()),
                new HashMap<>(),0, currentUser.id
        );
        return dialogsRef(currentUser, toUser).setValue(dialog)
                .addOnSuccessListener(unused -> {
                    DatabaseService.usersRef(currentUser.id).child("chats/"+toUser.id).setValue(toUser.id);
                    DatabaseService.usersRef(toUser.id).child("chats/"+currentUser.id).setValue(currentUser.id);
                });
    }
    public static DatabaseReference getUserRef(String toUser,String  currentUser,Callback callback){
        DatabaseReference mainRef=FirebaseDatabase.getInstance().getReference().child("chat").child("dialogs");
        mainRef.child(currentUser+"-"+toUser).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    callback.call(mainRef.child(currentUser+"-"+toUser));
                }
                else {
                    callback.call(mainRef.child(toUser+"-"+currentUser));
                }

            }
        });
        return null;
    }
    public static void getNewMessagesCount(String toUser, String currentUser,Callback callback){
        getUserRef(toUser, currentUser, new Callback() {
            @Override
            public void call(Object arg) {
                DatabaseReference userRef=(DatabaseReference)arg;
                userRef.child("unread").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot){
                        Log.e("DATAFALSE", String.valueOf(dataSnapshot));
                        Log.e("DATAFALSE", String.valueOf(dataSnapshot.getValue()));
                        callback.call(dataSnapshot.getValue(Integer.class));
                    }
            }
                );
            }});
        }
    public static void addToMessagesCount(String toUser,String currentUser) {
        getUserRef(toUser, currentUser, new Callback() {
            @Override
            public void call(Object arg) {
                DatabaseReference userRef=(DatabaseReference) arg;
                ChatFragment.notifyDataChanged();
                userRef.child("unread").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            userRef.child("unread").setValue(dataSnapshot.getValue(Integer.class) + 1);
                            userRef.child("unread_property").setValue(toUser);
                            ChatFragment.notifyDataChanged();
                        }

                    }
                });
            }});
        }
        DatabaseReference mainRef=FirebaseDatabase.getInstance().getReference().child("chat").child("dialogs");


    public static void whoseCounter(String toUser,String currentUser,Callback callback){
        getUserRef(toUser, currentUser, new Callback() {
            @Override
            public void call(Object arg) {
                DatabaseReference userRef=(DatabaseReference) arg;
                getNewMessagesCount(toUser, currentUser, new Callback() {
                    @Override
                    public void call(Object arg) {
                        userRef.child("unread_property").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot dataSnapshot) {
                                callback.call(dataSnapshot.getValue(String.class));
                            }
                        });
                    }
                });
            }
        });



    }
    public static void removeCounter(String toUser,String currentUser){
        DatabaseReference mainRef=FirebaseDatabase.getInstance().getReference().child("chat").child("dialogs");
        mainRef.child(currentUser+"-"+toUser).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                Log.e("USERS",currentUser+"!!!"+toUser);
                if (dataSnapshot.exists()){
                    mainRef.child(currentUser+"-"+toUser).child("unread").setValue(0);
                }
                else {
                    mainRef.child(toUser+"-"+currentUser).child("unread").setValue(0);
                }
            }
        });
    }

    public static FirebaseRecyclerOptions<MessageD> getUserOptions(User currentUser, User toUser) {
        Query query = dialogsRef(currentUser, toUser).child("messages");
        ClassSnapshotParser<MessageD> parser = new ClassSnapshotParser<>(MessageD.class);

        return new FirebaseRecyclerOptions.Builder<MessageD>()
                .setQuery(query, parser)
                .build();
    }
}
