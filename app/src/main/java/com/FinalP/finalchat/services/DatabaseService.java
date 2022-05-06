package com.FinalP.finalchat.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.FinalP.finalchat.listeners.SimpleListener;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.models.domain.UserD;
import com.firebase.ui.database.ClassSnapshotParser;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

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

    public static void uploadPicture(String id,byte[] pic,Callback callback){
        StorageReference imgStorage= FirebaseStorage.getInstance().getReference().child("images/users/"+id+"/avatar.png");
        UploadTask uploadTask = imgStorage.putBytes(pic);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //TODO Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                callback.call(null);
                //TODO taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                //TODO ...
            }
        });
    }
    public static void getPicture(String id,Callback<Bitmap> callback){
        StorageReference imgStorage= FirebaseStorage.getInstance().getReference().child("images/users/"+id+"/avatar.png");
        imgStorage.getBytes(99999999L *999999).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.e("BYTES", Arrays.toString(bytes));
                callback.call(DatabaseService.ByteToBitmap(bytes));
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
    public static void updateUserName(String id,String newName){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/" + id);
        ref.child("/name").setValue(newName);
    }
    public static Bitmap ByteToBitmap(byte[] bytes){
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    public static byte[] BitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }
}
