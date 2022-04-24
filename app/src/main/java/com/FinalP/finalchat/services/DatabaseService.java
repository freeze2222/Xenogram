package com.FinalP.finalchat.services;

import androidx.annotation.NonNull;

import com.FinalP.finalchat.listeners.SimpleListener;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.models.domain.UserD;
import com.firebase.ui.database.ClassSnapshotParser;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    public static DatabaseReference usersRef(String id) {
        return FirebaseDatabase.getInstance()
                .getReference("users/" + id.replaceAll(";", "").replaceAll("\\.", "").replaceAll("@", ""));
    }

    public static DatabaseReference cleanUsersRef() {
        return FirebaseDatabase.getInstance()
                .getReference("users/");
    }

    public static void addUser(UserD user) {
        DatabaseReference ref = usersRef(user.email);
        ref.setValue(user);
        ref.getKey();
    }
    public static String reformString(String s){
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

    public static void getNameFromKey(String key, Callback<String> callback) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/" + key);
        System.out.println(ref);
        GetNameSupport.getValue(ref, callback);
    }

    public static void getUsers(SimpleListener<List<User>> listener) {
        cleanUsersRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<User> users = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    UserD userD = child.getValue(UserD.class);
                    assert userD != null;
                    users.add(new User(userD));
                }

                listener.onValue(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public static FirebaseRecyclerOptions<String> getUsersOptions(User user) {
        Query query = usersRef(user.id).child("chats");
        ClassSnapshotParser<String> parser = new ClassSnapshotParser<>(String.class);

        return new FirebaseRecyclerOptions.Builder<String>()
                .setQuery(query, parser)
                .build();
    }
}
