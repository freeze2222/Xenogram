package com.FinalP.finalchat.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.listeners.SimpleListener;
import com.FinalP.finalchat.services.DatabaseService;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.concurrent.ExecutionException;


public class LastMessagesAdapter extends FirebaseRecyclerAdapter<String, LastMessagesAdapter.UserViewHolder> {
    SimpleListener<String> openChat;

    public LastMessagesAdapter(@NonNull FirebaseRecyclerOptions<String> options, SimpleListener<String> openChat) {
        super(options);
        this.openChat = openChat;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull String model) {
        try {
            holder.bind(model, openChat);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_row, parent, false);
        return new UserViewHolder(view);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameView;
        TextView emailView;
        ConstraintLayout rootLayout;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.textViewName);
            emailView = itemView.findViewById(R.id.textViewEmail);
            rootLayout = itemView.findViewById(R.id.userLayoutId);
        }

        public void bind(String key, SimpleListener<String> openChat) throws InterruptedException, ExecutionException {
            DatabaseService.getNameFromKey(key, arg -> nameView.setText(arg));
            emailView.setText(key);

            rootLayout.setOnClickListener(v -> openChat.onValue(DatabaseService.reformString(key)));
        }
    }
}
