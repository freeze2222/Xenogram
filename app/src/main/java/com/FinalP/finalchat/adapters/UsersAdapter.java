package com.FinalP.finalchat.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.listeners.SimpleListener;
import com.FinalP.finalchat.models.application.User;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
    ArrayList<User> users;
    SimpleListener<User> openChat;

    public UsersAdapter(ArrayList<User> users, SimpleListener<User> openChat) {
        this.users = users;
        this.openChat = openChat;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_row, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position), openChat);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarView;
        TextView nameView;
        TextView emailView;
        ConstraintLayout rootLayout;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.imageViewAvatar);
            nameView = itemView.findViewById(R.id.textViewName);
            emailView = itemView.findViewById(R.id.textViewEmail);
            rootLayout = itemView.findViewById(R.id.userLayoutId);
        }

        public void bind(User user, SimpleListener<User> openChat) {
            nameView.setText(user.name);
            emailView.setText(user.email);
            avatarView.setImageResource(R.drawable.alien);
            rootLayout.setOnClickListener(v -> openChat.onValue(user));
        }
    }
}
