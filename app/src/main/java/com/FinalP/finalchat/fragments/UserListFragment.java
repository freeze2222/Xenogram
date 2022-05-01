package com.FinalP.finalchat.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.activities.DialogActivity;
import com.FinalP.finalchat.listeners.SimpleListener;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.services.Callback;
import com.FinalP.finalchat.services.ChatService;
import com.FinalP.finalchat.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.EventListener;
import java.util.Objects;

public class UserListFragment extends Fragment {
    EditText username;
    Button confirm;
    String findEmail;
    DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
    DatabaseReference userNameRef;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        View rootView=inflater.inflate(R.layout.fragment_user_list, container, false);

        confirm=rootView.findViewById(R.id.confirm);
        username=rootView.findViewById(R.id.userTag);
        confirm.setOnClickListener(view -> {
            String findEmail=username.getText().toString();
            if (!findEmail.equals("")){
                userNameRef=rootRef.child("users").child(DatabaseService.reformString(findEmail));
                this.findEmail=findEmail;
                listener(new Callback() {
                    @Override
                    public void call(Object arg) {
                        Log.e("CALLING!","WORK!");
                        User[] list= (User[]) arg;
                        User currentUser=list[0];
                        User toUser=list[1];
                        if (currentUser!=null&&toUser!=null&& !toUser.id.equals(currentUser.id) && !toUser.id.equals("TechnicAccount")) {
                            ChatService.createDialog(currentUser, toUser);
                            Intent intent = new Intent(getContext(), DialogActivity.class);
                            intent.putExtra("DIALOG_WITH", toUser);
                            intent.putExtra("DIALOG_FROM", currentUser);
                            startActivity(intent);
                        }
                        else {
                            Log.e("EXCEPTION!","NULL! "+currentUser+" : "+toUser);
                        }
                    }

                });


            }
        });
        return rootView;
    }
    public void listener(Callback callback){
        ValueEventListener eventListener = new ValueEventListener() {
            User curUser;
            User toUsr;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    DatabaseService.getUser(DatabaseService.reformString(FirebaseAuth.getInstance().getCurrentUser().getEmail()), new SimpleListener<User>() {
                        @Override
                        public void onValue(User user) {
                            if (user != null) {
                                curUser = user;
                            }
                        }
                    });
                    DatabaseService.getUser(DatabaseService.reformString(findEmail), new SimpleListener<User>() {
                        @Override
                        public void onValue(User user) {
                            if (user != null) {
                                toUsr = user;
                                User[] users={curUser,toUsr};
                                callback.call(users);
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(getContext(),"Пользователь не найден!",Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("CANCELED!", databaseError.getMessage()); //Don't ignore errors!
            }
        };
        userNameRef.addListenerForSingleValueEvent(eventListener);
    }

}