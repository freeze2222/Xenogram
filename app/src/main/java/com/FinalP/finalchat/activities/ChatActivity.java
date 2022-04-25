package com.FinalP.finalchat.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;
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

    Fragment chatFragment = new ChatFragment();
    Fragment profileFragment=new ProfileFragment();
    Fragment addFragment = new UserListFragment();

    Fragment current = chatFragment;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_chat);
        initViews();


        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_content, chatFragment, "home")
                .add(R.id.frame_content, profileFragment, "profile")
                .add(R.id.frame_content, addFragment,"add")
                .hide(addFragment)
                .hide(profileFragment)
                .commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Log.e("DEB","HOME");
                    changeFragment(chatFragment);
                    return true;
                case R.id.navigation_profile:
                    Log.e("DEB","Profile");
                    changeFragment(profileFragment);
                    return true;
                case R.id.navigation_addUser:
                    Log.e("DEB","ClickADD");
                    changeFragment(addFragment);
            }
            return false;
        });



        //floatingActionButton.setOnClickListener(v -> {
        //  Intent intent = new Intent(v.getContext(), UserListActivity.class);
        //  intent.putExtra("DIALOG_FROM", currentUser);
        //  startActivity(intent);
        //});
    }

    void initViews() {
        //floatingActionButton = findViewById(R.id.floatingActionButton);
    }

    private void changeFragment(Fragment newFragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .hide(current)
                .show(newFragment)
                .commit();
        current = newFragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Toast.makeText(
                this,
                "requestCode:"+requestCode+"|resultCode:"+resultCode,
                Toast.LENGTH_LONG
        ).show();

        super.onActivityResult(requestCode, resultCode, data);
    }
}