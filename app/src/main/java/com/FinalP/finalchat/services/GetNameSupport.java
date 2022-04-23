package com.FinalP.finalchat.services;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class GetNameSupport {
    static String res;

    protected static String getValue(DatabaseReference databaseReferences) {
        databaseReferences.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
                res = String.valueOf(value.get("name"));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ERROR", "Error while reading data");

            }

        });
        return res;
    }
}
