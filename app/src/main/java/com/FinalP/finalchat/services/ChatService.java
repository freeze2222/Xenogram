package com.FinalP.finalchat.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.FinalP.finalchat.fragments.ChatFragment;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.models.domain.GroupD;
import com.FinalP.finalchat.models.domain.GroupMetadataD;
import com.FinalP.finalchat.models.domain.MessageD;
import com.firebase.ui.database.ClassSnapshotParser;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class ChatService {
    public static DatabaseReference dialogsRef() {
        return FirebaseDatabase.getInstance()
                .getReference("chat")
                .child("dialogs");
    }

    public static DatabaseReference getUserDialogRef(String userA, String userB) {
        String[] strings = new String[]{userA, userB};
        Arrays.sort(strings);
        String chatId = strings[0] + "-" + strings[1];

        return dialogsRef().child(chatId);
    }

    public static Task<Void> sendMessage(String text, User currentUser, User toUser,String type) {
        MessageD message = new MessageD(text, currentUser.id, new Date().getTime(),type);
        return getUserDialogRef(currentUser.id, toUser.id).child("messages").push().setValue(message);
    }

    public static Task<Void> createDialog(User currentUser, User toUser) {
        GroupD dialog = new GroupD(
                new GroupMetadataD(new Date().getTime()),
                new HashMap<>(), 0, currentUser.id,
                false,false
        );
        return getUserDialogRef(currentUser.id, toUser.id).setValue(dialog)
                .addOnSuccessListener(unused -> {
                    DatabaseService.usersRef(currentUser.id).child("chats/" + toUser.id).setValue(toUser.id);
                    DatabaseService.usersRef(toUser.id).child("chats/" + currentUser.id).setValue(currentUser.id);
                });
    }

    public static void isChatActive(String toUser, String currentUser, Callback callback) {
        DatabaseReference reference = getUserDialogRef(toUser, currentUser);
            reference.child("isActiveUser1").get().addOnSuccessListener(dataSnapshotUser1 -> reference.child("isActiveUser2").get().addOnSuccessListener(dataSnapshotUser2 -> {
                if (dataSnapshotUser1.getValue(Boolean.class) && dataSnapshotUser2.getValue(Boolean.class)) {
                    callback.call(true);
                } else {
                    callback.call(false);
                }
            }));
    }

    public static void toggleActiveIndicator(String toUser, String currentUser) {
            DatabaseReference reference = getUserDialogRef(toUser, currentUser);
            String[] users = reference.getKey().split("-");
            Log.e("DEB", Arrays.toString(users));
            Log.e("DEB", users[1]);
            Log.e("DEB", currentUser);
            if (currentUser.equals(users[0])){
                reference.child("isActiveUser1").get().addOnSuccessListener(dataSnapshot -> reference.child("isActiveUser1").setValue(!dataSnapshot.getValue(Boolean.class)));
            }
            else{
                reference.child("isActiveUser2").get().addOnSuccessListener(dataSnapshot -> reference.child("isActiveUser2").setValue(!dataSnapshot.getValue(Boolean.class)));
            }
        }

    public static void getNewMessagesCount(String toUser, String currentUser, Callback callback) {
                        DatabaseReference userRef =getUserDialogRef(toUser, currentUser);
                        userRef.child("unread").get().addOnSuccessListener(dataSnapshot -> {
                                    callback.call(dataSnapshot.getValue(Integer.class));
                                });
    }

    public static void addToMessagesCount(String toUser, String currentUser) {
        isChatActive(toUser, currentUser, arg -> {
            Boolean isActive = (Boolean) arg;
            if (!isActive) {
                DatabaseReference userRef =getUserDialogRef(toUser, currentUser);
                    userRef.child("unread").get().addOnSuccessListener(dataSnapshot -> {
                        if (dataSnapshot.exists()) {
                            userRef.child("unread").setValue(dataSnapshot.getValue(Integer.class) + 1);
                            userRef.child("unread_property").setValue(toUser);
                        }

                    });
            }});
    }

    public static void whoseCounter(String toUser, String currentUser, Callback callback) {
        DatabaseReference userRef = getUserDialogRef(toUser, currentUser);
            getNewMessagesCount(toUser, currentUser, arg1 -> userRef.child("unread_property").get().addOnSuccessListener(dataSnapshot -> {
                callback.call(dataSnapshot.getValue(String.class));
            }));
    }

    public static void removeCounter(String toUser, String currentUser) {
        DatabaseReference mainRef = FirebaseDatabase.getInstance().getReference().child("chat").child("dialogs");
        mainRef.child(currentUser + "-" + toUser).get().addOnSuccessListener(dataSnapshot -> {
            Log.e("USERS", currentUser + "!!!" + toUser);
            if (dataSnapshot.exists()) {
                mainRef.child(currentUser + "-" + toUser).child("unread").setValue(0);
            } else {
                mainRef.child(toUser + "-" + currentUser).child("unread").setValue(0);
            }
        });
    }

    public static FirebaseRecyclerOptions<MessageD> getUserOptions(User currentUser, User toUser) {
        Query query = getUserDialogRef(currentUser.id, toUser.id).child("messages");
        ClassSnapshotParser<MessageD> parser = new ClassSnapshotParser<>(MessageD.class);

        return new FirebaseRecyclerOptions.Builder<MessageD>()
                .setQuery(query, parser)
                .build();
    }

    public static void testLinkListeners(String currentUser) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser).child("chats");
        reference.get().addOnSuccessListener(dataSnapshot -> {
            DataSnapshot lastDataSnapshot;
            Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
            for (DataSnapshot snapshot : iterable) {
                lastDataSnapshot = snapshot;
                DatabaseReference mainRef =getUserDialogRef(lastDataSnapshot.getKey(), currentUser);
                    mainRef.child("unread").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot1) {
                            Log.e("DEB", "The " + snapshot1.getKey() + " test data is " + snapshot1.getValue());
                            ChatFragment.notifyAdapter();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("DEB", "ERROR: " + error.getDetails());
                    }
                });
            }
        });
    }
}
