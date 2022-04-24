package com.FinalP.finalchat.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.activities.DialogActivity;
import com.FinalP.finalchat.adapters.UsersAdapter;
import com.FinalP.finalchat.listeners.SimpleListener;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.services.DatabaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_user_list);

        ArrayList<User> users = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.userListView);
        UsersAdapter adapter = new UsersAdapter(users, new SimpleListener<User>() {
            @Override
            public void onValue(User user) {
                Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
                intent.putExtra("DIALOG_WITH", user);
                intent.putExtra("DIALOG_FROM", getIntent().getSerializableExtra("DIALOG_FROM"));
                startActivity(intent);
                finish();
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(RecyclerView.VERTICAL);

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        DatabaseService.getUsers(new SimpleListener<List<User>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onValue(List<User> userList) {
                users.addAll(userList);
                adapter.notifyDataSetChanged();
            }
        });
    }
}