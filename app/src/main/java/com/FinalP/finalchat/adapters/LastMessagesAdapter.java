package com.FinalP.finalchat.adapters;


import android.graphics.Bitmap;
import android.view.ContextMenu;
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
import com.FinalP.finalchat.services.Callback;
import com.FinalP.finalchat.services.DatabaseService;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


public class LastMessagesAdapter extends FirebaseRecyclerAdapter<String, LastMessagesAdapter.UserViewHolder> implements Serializable {
    SimpleListener<String> openChat;
    static String currentEmail;
    static String currentUserEmail;
    public LastMessagesAdapter(@NonNull FirebaseRecyclerOptions<String> options, SimpleListener<String> openChat) {
        super(options);
        this.openChat = openChat;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull String model) {
        holder.itemView.setLongClickable(true);
        try {
            currentEmail=model;
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

    static class UserViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView nameView;
        TextView emailView;
        ImageView avatar;
        ConstraintLayout rootLayout;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.textViewName);
            emailView = itemView.findViewById(R.id.textViewEmail);
            avatar=itemView.findViewById(R.id.imageViewAvatar);
            rootLayout = itemView.findViewById(R.id.userLayoutId);
            currentUserEmail=DatabaseService.reformString(Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail()));
            rootLayout.setOnCreateContextMenuListener(this);

        }

        public void bind(String key, SimpleListener<String> openChat) throws InterruptedException, ExecutionException {
                DatabaseService.getNameFromKey(key, arg -> {
                    if (arg.id.equals("technicaccount")){
                        avatar.setImageResource(R.drawable.fav);
                        nameView.setText("Избранное");
                        emailView.setText("");
                        emailView.setHeight(0);
                        emailView.setWidth(0);
                    }
                    else {

                        setImg(arg.id);
                        nameView.setText(arg.name);
                    }
                });
                emailView.setText(key);

            rootLayout.setOnClickListener(v -> openChat.onValue(DatabaseService.reformString(key)));
        }
        public void setImg(String id){
            getImg(arg -> avatar.setImageBitmap((Bitmap) arg),id);
        }
        public static void getImg(Callback callback, String argId){
            Thread thread= new Thread(){
                @Override
                public void run(){
                    DatabaseService.getPicture(argId, callback);
                }
            };
            thread.start();
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle("Выберите действие");
            contextMenu.add(this.getAdapterPosition(),121,0,"Удалить пользователя");
            contextMenu.add(this.getAdapterPosition(),122,0,"...");

        }

    }

}
