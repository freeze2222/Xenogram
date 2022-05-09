// Если этот код работает, его написал Смульский Григорий,
// а если нет, то не знаю, кто его писал.
package com.FinalP.finalchat.activities;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.adapters.MessageAdapter;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.services.ChatService;
import com.google.android.gms.tasks.OnFailureListener;

import java.util.Objects;


public class DialogActivity extends AppCompatActivity {
    int notificationID = 404;
    String ChannelID = "DefaultChannel";
    RecyclerView chatView;
    Button sendView;
    ImageView backButton;
    EditText editTextView;

    User currentUser;
    User toUser;

    MessageAdapter adapter;
    OnFailureListener failureListener = e -> {
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
        backButton.setOnClickListener(view -> finish());
        sendView.setOnClickListener(v -> {
            String text = editTextView.getText().toString();
            if (text.isEmpty()) return;

            if (adapter.getItemCount() == 0) {
                ChatService.createDialog(currentUser, toUser)
                        .addOnSuccessListener(unused -> ChatService.sendMessage(text, currentUser, toUser)
                                .addOnSuccessListener(unused1 -> {
                                    editTextView.getText().clear();
                                    adapter.notifyDataSetChanged();
                                    chatView.smoothScrollToPosition(adapter.getItemCount());
                                })
                                .addOnFailureListener(failureListener)).addOnFailureListener(failureListener);
            } else {
                ChatService.sendMessage(text, currentUser, toUser)
                        .addOnSuccessListener(unused -> {
                            editTextView.getText().clear();
                            adapter.notifyDataSetChanged();
                            chatView.smoothScrollToPosition(adapter.getItemCount());
                        })
                        .addOnFailureListener(failureListener);

            }
            editTextView.setOnClickListener(view -> chatView.smoothScrollToPosition(adapter.getItemCount()));

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
        editTextView = findViewById(R.id.editTextTextPersonName2);

        adapter = new MessageAdapter(ChatService.getUserOptions(currentUser, toUser), currentUser, toUser) {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChanged() {
                chatView.smoothScrollToPosition(adapter.getItemCount());
                if (currentUser.id.equals(adapter.currentUser.id)) {
                    Log.e("TODO","TODO");
                } else {
                    notifySend();
                }

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
        super.onStop();
    }

    public void notifySend() {
        Intent pendingTempIntent = new Intent(getApplicationContext(), ChatActivity.class);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, pendingTempIntent, 0);
        Notification builder = new NotificationCompat.Builder(this, ChannelID)
                .setSmallIcon(R.drawable.alien)
                .setContentTitle("С вами пытается кто-то связаться!")
                .setContentText("Новое сообщение!")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent).build();
        builder.vibrate = new long[]{1000, 1000, 1000, 1000, 1000};

        NotificationManagerCompat.from(this).notify(notificationID, builder);


    }

}