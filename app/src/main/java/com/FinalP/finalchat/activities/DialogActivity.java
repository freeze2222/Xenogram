// Если этот код работает, его написал Смульский Григорий,
// а если нет, то не знаю, кто его писал.
package com.FinalP.finalchat.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.adapters.MessageAdapter;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.services.ChatService;
import com.FinalP.finalchat.services.DatabaseService;
import com.FinalP.finalchat.services.FirebaseMessagingServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;
import java.util.UUID;


public class DialogActivity extends AppCompatActivity {
    RecyclerView chatView;
    ImageView sendView;
    ImageView backButton;
    ImageView moreOptions;
    EditText editTextView;
    int btnClicks=0;

    User currentUser;
    User toUser;

    MessageAdapter adapter;
    final OnFailureListener failureListener = e -> {
        e.printStackTrace();
        Toast.makeText(getApplicationContext(), "Error happened!", Toast.LENGTH_SHORT).show();
    };

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();

        setContentView(R.layout.activity_dialog);
        initUsers();
        initViews();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("id", Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail()).replaceAll(";", "").replaceAll("\\.", "").replaceAll("@", ""));
            intent.putExtra("name", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        ChatService.toggleActiveIndicator(toUser.id,currentUser.id);
        moreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater)
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_more_send_options, null);

                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
                popupView.findViewById(R.id.chooseImg).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectImageFromGallery();
                        popupWindow.dismiss();

                    }
                });
                popupView.findViewById(R.id.chooseVideo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectVideoFromGallery();
                        popupWindow.dismiss();

                    }
                });




                popupWindow.setAnimationStyle(R.style.popup_window_animation);

                popupWindow.setElevation(20);
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });
            }
        });
        sendView.setOnClickListener(v -> {
            String text = editTextView.getText().toString();
            if (text.isEmpty()) return;

            if (adapter.getItemCount() == 0) {
                ChatService.createDialog(currentUser, toUser)
                        .addOnSuccessListener(unused -> ChatService.sendMessage(text, currentUser, toUser,"text")
                                .addOnSuccessListener(unused1 -> {
                                    editTextView.getText().clear();
                                    adapter.notifyDataSetChanged();
                                    chatView.smoothScrollToPosition(adapter.getItemCount());

                                })
                                .addOnFailureListener(failureListener)).addOnFailureListener(failureListener);
            } else {
                ChatService.sendMessage(text, currentUser, toUser,"text")
                        .addOnSuccessListener(unused -> {
                            editTextView.getText().clear();
                            adapter.notifyDataSetChanged();
                            chatView.smoothScrollToPosition(adapter.getItemCount());
                            FirebaseMessagingServices.getUserToken(toUser.id, arg -> {
                                FirebaseMessagingServices.sendPushedNotification(arg);
                                Log.e("TAGGER","call");
                            });
                            ChatService.addToMessagesCount(toUser.id,currentUser.id);
                        })
                        .addOnFailureListener(failureListener);

            }
            editTextView.setOnClickListener(view -> chatView.smoothScrollToPosition(adapter.getItemCount()));

        });
        ChatService.whoseCounter(toUser.id, currentUser.id, arg -> {
            if (arg.equals(currentUser.id)){
                ChatService.removeCounter(toUser.id,currentUser.id);
            }
        });
    }

    void initUsers() {
       toUser = (User) getIntent().getSerializableExtra("DIALOG_WITH");
       currentUser = (User) getIntent().getSerializableExtra("DIALOG_FROM");
    }

    public void initViews() {
        backButton = findViewById(R.id.back);
        chatView = findViewById(R.id.dialogChatView);
        sendView = findViewById(R.id.button);
        moreOptions=findViewById(R.id.moreOptions);
        editTextView = findViewById(R.id.editTextTextPersonName2);

        adapter = new MessageAdapter(ChatService.getUserOptions(currentUser, toUser), currentUser, toUser,getApplicationContext()) {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChanged() {
                chatView.smoothScrollToPosition(adapter.getItemCount());
            }
        };
        adapter.startListening();

        chatView.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(RecyclerView.VERTICAL);
        chatView.setLayoutManager(manager);
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            adapter.startListening();
            chatView.smoothScrollToPosition(adapter.getItemCount());

        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        adapter.stopListening();
        ChatService.toggleActiveIndicator(toUser.id,currentUser.id);
        super.onStop();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            UUID uuid=UUID.randomUUID();
            StorageReference imageRef = storageRef.child("messages/images/" + uuid.toString());
            ChatService.sendMessage(uuid.toString(),currentUser,toUser,"image").addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    adapter.notifyDataSetChanged();
                }
            });
            DatabaseService.uploadImage(selectedImage,imageRef,false);
            Toast.makeText(getApplicationContext(),"Загрузка началась!",Toast.LENGTH_LONG).show();

        }
        else if(requestCode == 100 && resultCode == RESULT_OK)
        {
            Uri videoUri= data.getData();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            UUID uuid=UUID.randomUUID();
            StorageReference videoRef = storageRef.child("messages/videos/" + uuid.toString());
            DatabaseService.uploadVideo(videoUri,videoRef);
            ChatService.sendMessage(uuid.toString(),currentUser,toUser,"video");
            Toast.makeText(getApplicationContext(),"Загрузка началась!",Toast.LENGTH_LONG).show();
        }
    }

    public void selectVideoFromGallery()
    {
        Intent intent;
        if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        }
        else
        {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI);
        }
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra("return-data", true);
        startActivityForResult(intent,100);
    }
    public void selectImageFromGallery()
    {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 2);
    }
}