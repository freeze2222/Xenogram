package com.FinalP.finalchat.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.adapters.LastMessagesAdapter;
import com.FinalP.finalchat.fragments.ChatFragment;
import com.FinalP.finalchat.fragments.ProfileFragment;
import com.FinalP.finalchat.fragments.UserListFragment;
import com.FinalP.finalchat.listeners.SimpleListener;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.services.DatabaseService;
import com.FinalP.finalchat.services.WrapContentLinearLayoutManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;


public class ChatActivity extends AppCompatActivity {
    User currentUser;

    FloatingActionButton floatingActionButton;
    RecyclerView userRecyclerView;

    LastMessagesAdapter adapter;

    BottomNavigationView bottomNavigationView;

    Fragment chatFragment = new ChatFragment();
    Fragment profileFragment=new ProfileFragment();
    Fragment addFragment = new UserListFragment();

    Fragment current = chatFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_chat);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_content, chatFragment, "chats")
                .add(R.id.frame_content, profileFragment, "profile")
                .add(R.id.frame_content, addFragment,"add")
                .hide(profileFragment)
                .hide(addFragment)
                .commit();

        initViews();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        changeFragment(chatFragment);
                        return true;
                    case R.id.navigation_profile:
                        changeFragment(profileFragment);
                        return true;
                    case R.id.navigation_addUser:
                        changeFragment(addFragment);
                }
                return false;
            }
        });


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

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
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
    private void changeFragment(Fragment newFragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .hide(current)
                .show(newFragment)
                .commit();
        current = newFragment;
    }
}