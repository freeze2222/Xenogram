package com.FinalP.finalchat.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.listeners.SimpleListener;
import com.FinalP.finalchat.models.domain.UserD;
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
            signInLauncher.launch(signInIntent);
        }
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        SimpleListener<String> listener = new SimpleListener<String>() {
            @Override
            public void onValueReg(String val, String val2) {
                UserD userD = new UserD("Guest" + UUID.randomUUID(), new Date().getTime(), val, val2);
                DatabaseService.addUser(userD);
            }
        };
        //IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            assert user != null;
            listener.onValueReg(user.getEmail(), user.getDisplayName());

            Intent signInIntent = new Intent(this, ChatActivity.class);
            signInIntent.putExtra("id", Objects.requireNonNull(user.getEmail()).replaceAll(";", "").replaceAll("\\.", "").replaceAll("@", ""));
            signInLauncher.launch(signInIntent);

        }  // Sign in failed. If response is null the user canceled the
        // sign-in flow using the back button. Otherwise check
        // response.getError().getErrorCode() and handle the error.
        // ...

    }
}