package com.FinalP.finalchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.adapters.LastMessagesAdapter;
import com.FinalP.finalchat.listeners.SimpleListener;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.services.DatabaseService;
import com.FinalP.finalchat.services.WrapContentLinearLayoutManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;


public class ChatActivity extends AppCompatActivity {
    User currentUser;

    FloatingActionButton floatingActionButton;
    RecyclerView userRecyclerView;

    LastMessagesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_chat);

        initViews();
        String id = getIntent().getStringExtra("id");
        DatabaseService.getUser(id, new SimpleListener<User>() {
            @Override
            public void onValue(User user) {
                if (user != null) {
                    currentUser = user;
                    adapter = new LastMessagesAdapter(DatabaseService.getUsersOptions(currentUser), new SimpleListener<String>() {
                        @Override
                        public void onValue(String toId) {
                            if (id.equals(toId)) {
                                DatabaseService.getUser(toId, new SimpleListener<User>() {
                                    @Override
                                    public void onValue(User value) {
                                        Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
                                        intent.putExtra("DIALOG_WITH", user);
                                        intent.putExtra("DIALOG_FROM", value);
                                        startActivity(intent);
                                    }
                                });
                            }
                        }
                    });
                    adapter.startListening();

                    userRecyclerView.setAdapter(adapter);
                }
            }
        });

        floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), UserListActivity.class);
            intent.putExtra("DIALOG_FROM", currentUser);
            startActivity(intent);
        });
    }

    void initViews() {
        floatingActionButton = findViewById(R.id.floatingActionButton);
        userRecyclerView = findViewById(R.id.userRecyclerView);

        userRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected void onStart() {
        if (adapter != null) {
            adapter.startListening();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        adapter.stopListening();
        super.onStop();
    }
}