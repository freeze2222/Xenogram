package com.FinalP.finalchat.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
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

import java.util.Arrays;
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
        SimpleListener<String> listener = new SimpleListener<String>() {
            @Override
            public void onValueReg(String val, String val2) {
                UserD userD = new UserD(new Date().getTime(), val, val2,"Default");
                DatabaseService.addUser(userD);
                final User[] currentUser = new User[1];
                final User[] toUser=new User[1];
                Callback callback=new Callback() {
                    @Override
                    public void call(Object arg) {
                        DatabaseService.getUser(DatabaseService.reformString(FirebaseAuth.getInstance().getCurrentUser().getEmail()), new SimpleListener<User>() {
                            @Override
                            public void onValue(User value) {
                                currentUser[0] =value;
                            }
                        });
                        DatabaseService.getUser("technicuser", new SimpleListener<User>() {
                            @Override
                            public void onValue(User value) {
                                toUser[0]=value;
                            }
                        });
                        ChatService.createDialog(currentUser[0], toUser[0]);
                    }
                };
                callback.call("123");

                //ChatService.createDialog(new User(userD),new User(new UserD(Long.parseLong("1651422238059"),"technic@acc.ount","Избранное","Default")));
            }
        };
        //IdpResponse response = result.getIdpResponse();

        final User[] currentUser = new User[1];
        if (result.getResultCode() == RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            assert user != null;
            listener.onValueReg(user.getEmail(), user.getDisplayName());
                    Intent signInIntent = new Intent(this, ChatActivity.class);
                    signInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    signInIntent.putExtra("id", Objects.requireNonNull(user.getEmail()).replaceAll(";", "").replaceAll("\\.", "").replaceAll("@", ""));
                    signInLauncher.launch(signInIntent);
                    finish();
            }}
}