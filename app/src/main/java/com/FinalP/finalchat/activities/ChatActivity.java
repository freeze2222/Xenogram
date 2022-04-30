package com.FinalP.finalchat.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.adapters.LastMessagesAdapter;
import com.FinalP.finalchat.fragments.ChatFragment;
import com.FinalP.finalchat.fragments.ProfileFragment;
import com.FinalP.finalchat.fragments.UserListFragment;
import com.FinalP.finalchat.listeners.SimpleListener;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.services.DatabaseService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;


public class ChatActivity extends AppCompatActivity {

    Fragment chatFragment = new ChatFragment();
    Fragment profileFragment=new ProfileFragment();
    Fragment addFragment = new UserListFragment();
    Fragment current = chatFragment;

    User currentUser;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_chat);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.MANAGE_EXTERNAL_STORAGE},
                1);

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .add(R.id.frame_content, chatFragment, "homeFragment")
                .add(R.id.frame_content, profileFragment, "profileFragment")
                .add(R.id.frame_content, addFragment,"addFragment")
                .hide(addFragment)
                .hide(profileFragment)
                .commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setItemIconTintList(null);
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
        String id = getIntent().getStringExtra("id");
        DatabaseService.getUser(id, new SimpleListener<User>() {
            @Override
            public void onValue(User user) {
                if (user != null) {
                    currentUser = user;
                }
            }
        });

    }


    private void changeFragment(Fragment newFragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
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