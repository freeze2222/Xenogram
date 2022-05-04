package com.FinalP.finalchat.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.listeners.SimpleListener;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.models.domain.UserD;
import com.FinalP.finalchat.services.Callback;
import com.FinalP.finalchat.services.ChatService;
import com.FinalP.finalchat.services.DatabaseService;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;

import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Login_activity extends AppCompatActivity {
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_login);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build()
            );

            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setLogo(R.drawable.alien)
                    .setTheme(com.google.android.material.R.style.Base_Theme_Material3_Dark)
                    .setTosAndPrivacyPolicyUrls("Тут ничего нет", "Серьёзно")
                    .build();
            signInLauncher.launch(signInIntent);
        } else {
            Intent signInIntent = new Intent(this, ChatActivity.class);
            signInIntent.putExtra("id", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail()).replaceAll(";", "").replaceAll("\\.", "").replaceAll("@", ""));
            signInIntent.putExtra("name", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            signInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            signInLauncher.launch(signInIntent);
            finish();
        }
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        SimpleListener<String> listener = new SimpleListener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onValueReg(String val, String val2) {
                UserD userD = new UserD(new Date().getTime(), val, val2,"Default");
                DatabaseService.addUser(userD);

                addFav(new Callback() {
                    @Override
                    public void call(Object arg) {
                        User[] users=new User[2];
                        users= (User[]) arg;
                        ChatService.createDialog(users[0],users[1]);
                    }
                });

            }
        };
        FirebaseUserMetadata metadata = FirebaseAuth.getInstance().getCurrentUser().getMetadata();
        if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
            listener.onValueReg(user.getEmail(), user.getDisplayName());
        }
        if (result.getResultCode() == RESULT_OK) {
            Intent signInIntent = new Intent(this, ChatActivity.class);
            signInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            signInIntent.putExtra("id", Objects.requireNonNull(user.getEmail()).replaceAll(";", "").replaceAll("\\.", "").replaceAll("@", ""));
            signInLauncher.launch(signInIntent);
            finish();
        }}



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addFav(Callback callback){

        User[] users=new User[2];
        DatabaseService.getUser(DatabaseService.reformString(FirebaseAuth.getInstance().getCurrentUser().getEmail()), new SimpleListener<User>() {
            @Override
            public void onValue(User value) {
                users[0] =value;
            }
        });
        DatabaseService.getUser("technicaccount", new SimpleListener<User>() {
            @Override
            public void onValue(User value) {
                users[1]=value;
                callback.call(users);
            }
        });

    };
    }
