// Если этот код работает, его написал Смульский Григорий,
// а если нет, то не знаю, кто его писал.
package com.FinalP.finalchat.activities;

import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.adapters.MessageAdapter;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.services.Callback;
import com.FinalP.finalchat.services.ChatService;
import com.FinalP.finalchat.services.FirebaseMessagingServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;


public class DialogActivity extends AppCompatActivity {
    RecyclerView chatView;
    Button sendView;
    ImageView backButton;
    EditText editTextView;

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
        editTextView = findViewById(R.id.editTextTextPersonName2);

        adapter = new MessageAdapter(ChatService.getUserOptions(currentUser, toUser), currentUser, toUser) {
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



}