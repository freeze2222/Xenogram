package com.FinalP.finalchat.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.models.domain.UserD;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class GetUserSupport {

    protected static void getValue(DatabaseReference databaseReferences, Callback<User> callback) {
        databaseReferences.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User res = new User(Objects.requireNonNull(dataSnapshot.getValue(UserD.class)));
                    callback.call(res);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ERROR", "Error while reading data");
            }
        });
    }
}
