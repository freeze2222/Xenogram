package com.FinalP.finalchat.services;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.FinalP.finalchat.listeners.SimpleListener;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.models.domain.UserD;
import com.firebase.ui.database.ClassSnapshotParser;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class DatabaseService {

    public static DatabaseReference usersRef(String id) {
        return FirebaseDatabase.getInstance()
                .getReference("users/" + id.replaceAll(";", "").replaceAll("\\.", "").replaceAll("@", ""));
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

    public static void getPicture(String id,Callback<Bitmap> callback){
        StorageReference imgStorage= FirebaseStorage.getInstance().getReference().child("avatars/"+id+"/avatar.jpg");
        imgStorage.getBytes(99999999L *999999).addOnSuccessListener(bytes -> {
            Log.e("BYTES", Arrays.toString(bytes));
            callback.call(DatabaseService.ByteToBitmap(bytes));
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
    public static void uploadVideo(Uri videoUri,StorageReference videoRef){
        if(videoUri != null){
            UploadTask uploadTask = videoRef.putFile(videoUri);
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()) {
                        Log.e("Upload", "Done");
                    }
                }
            });
        }
    }
    public static UploadTask uploadImage(Uri imageUri,StorageReference imageRef, Boolean isAvatar){
        if(imageUri != null) {
            if (!isAvatar) {
                UploadTask uploadTask = imageRef.putFile(imageUri);
                return uploadTask;
            }
            else {
                return imageRef.child("avatar.jpg").putFile(imageUri);
            }
        }
        else return null;
    }
    public static void downloadVideo(StorageReference reference,Callback callback){
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                callback.call(uri);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                callback.call(null);
            }
        });
    }
    public static void downloadImage(StorageReference reference,Callback callback){
        try {
            File localFile = File.createTempFile("image", "jpg");
            reference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    callback.call(localFile.toURI());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
