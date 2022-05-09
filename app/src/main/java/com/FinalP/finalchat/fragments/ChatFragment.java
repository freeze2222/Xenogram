package com.FinalP.finalchat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.activities.ChatActivity;
import com.FinalP.finalchat.activities.DialogActivity;
import com.FinalP.finalchat.adapters.LastMessagesAdapter;
import com.FinalP.finalchat.listeners.SimpleListener;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.models.domain.UserD;
import com.FinalP.finalchat.services.Callback;
import com.FinalP.finalchat.services.ChatService;
import com.FinalP.finalchat.services.DatabaseService;
import com.FinalP.finalchat.services.WrapContentLinearLayoutManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        userRecyclerView = view.findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        String id = getActivity().getIntent().getStringExtra("id");
        adapter=((ChatActivity)getActivity()).getAdapter();
            DatabaseService.getUser(id, new SimpleListener<User>() {
                @Override
                public void onValue(User user) {
                    if (user != null) {
                        currentUser = user;
                        if (adapter==null) {
                            adapter = new LastMessagesAdapter(DatabaseService.getUsersOptions(currentUser), new SimpleListener<String>() {
                                @Override
                                public void onValue(String toId) {
                                    DatabaseService.getUser(toId, new SimpleListener<User>() {
                                        @Override
                                        public void onValue(User value) {
                                            Intent intent = new Intent(getContext(), DialogActivity.class);
                                            intent.putExtra("DIALOG_WITH", value);
                                            intent.putExtra("DIALOG_FROM", user);
                                            startActivity(intent);
                                        }
                                    });
                                }
                            }, getContext()
                            );
                        }

                        adapter.startListening();
                        userRecyclerView.setAdapter(adapter);
                        registerForContextMenu(userRecyclerView);
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
    public LastMessagesAdapter getAdapter(){
        return adapter;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){

        switch (item.getItemId()){
            case 121:
                removeUser(item.getGroupId());
                displayMessage("121");
            case 122:
                displayMessage("122");
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void displayMessage(String message){
        Toast.makeText(getContext(),message,Toast.LENGTH_LONG);
    }
    private void removeUser(int position){
        DatabaseReference referenceRaw=adapter.getRef(position);
        DatabaseReference referenceCheck=FirebaseDatabase.getInstance().getReference().child("users").child(referenceRaw.getKey()).child("name");
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.id);
        Callback callback=new Callback() {
            @Override
            public void call(Object arg) {
                if (!((String) arg).equals("Избранное")){
                    reference.child("chats").child(referenceRaw.getKey()).removeValue();
                }
            }
        };
        referenceCheck.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
               callback.call(dataSnapshot.getValue(String.class));
            }
        });


    }
}