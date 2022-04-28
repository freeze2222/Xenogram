package com.FinalP.finalchat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.activities.DialogActivity;
import com.FinalP.finalchat.adapters.LastMessagesAdapter;
import com.FinalP.finalchat.listeners.SimpleListener;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.models.domain.UserD;
import com.FinalP.finalchat.services.ChatService;
import com.FinalP.finalchat.services.DatabaseService;
import com.FinalP.finalchat.services.WrapContentLinearLayoutManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ChatFragment extends Fragment {
    User currentUser;

    FloatingActionButton floatingActionButton;
    RecyclerView userRecyclerView;

    LastMessagesAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        floatingActionButton = view.findViewById(R.id.floatingActionButton);
        userRecyclerView = view.findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        String id = getActivity().getIntent().getStringExtra("id");
        DatabaseService.getUser(id, new SimpleListener<User>() {
            @Override
            public void onValue(User user) {
                if (user != null) {
                    currentUser = user;
                    adapter = new LastMessagesAdapter(DatabaseService.getUsersOptions(currentUser), new SimpleListener<String>() {
                        @Override
                        public void onValue(String toId) {
                            //if (id.equals(toId)) {
                                DatabaseService.getUser(toId, new SimpleListener<User>() {
                                    @Override
                                    public void onValue(User value) {
                                        Intent intent = new Intent(getContext(), DialogActivity.class);
                                        intent.putExtra("DIALOG_WITH", user);
                                        intent.putExtra("DIALOG_FROM", value);
                                        startActivity(intent);
                                    }
                                });
                            //}
                        }
                    }
                    );
                    adapter.startListening();
                    userRecyclerView.setAdapter(adapter);
                    //if (adapter.getItemCount()==0){
                       // Log.e("WORK!!","WORK!!!");
                        //ChatService.createDialog(currentUser, new User(new UserD(true)));
                    //}
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        if (adapter != null) {
            adapter.startListening();
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        adapter.stopListening();
        super.onStop();
    }
}