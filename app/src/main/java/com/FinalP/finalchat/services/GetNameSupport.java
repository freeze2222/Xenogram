package com.FinalP.finalchat.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class GetNameSupport {

    protected static void getValue(DatabaseReference databaseReferences, Callback<String> callback) {
        databaseReferences.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String res = String.valueOf(dataSnapshot.getValue());
                callback.call(res);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ERROR", "Error while reading data");
            }
        });
    }
}
