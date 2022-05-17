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
import com.FinalP.finalchat.services.ChatService;
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
    public void onDataChanged() {
        super.onDataChanged();
        this.notifyDataSetChanged();
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
        ImageView imageView;
        ConstraintLayout rootLayout;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.textViewName);
            emailView = itemView.findViewById(R.id.textViewEmail);
            imageView=itemView.findViewById(R.id.messagesCount);
            avatar=itemView.findViewById(R.id.imageViewAvatar);
            rootLayout = itemView.findViewById(R.id.userLayoutId);

            currentUserEmail=DatabaseService.reformString(Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail()));
            rootLayout.setOnCreateContextMenuListener(this);

        }

        public void bind(String key, SimpleListener<String> openChat) throws InterruptedException, ExecutionException {
                DatabaseService.getNameFromKey(key, argument -> {
                    if (argument.id.equals("technicaccount")){
                        avatar.setImageResource(R.drawable.fav);
                        nameView.setText("Избранное");
                        emailView.setText("");
                        emailView.setHeight(0);
                        emailView.setWidth(0);
                        imageView.setVisibility(View.GONE);
                    }
                    else {
                        setImg(argument.id);
                        nameView.setText(argument.name);
                        ChatService.whoseCounter(key, currentUserEmail, new Callback() {
                            @Override
                            public void call(Object arg) {
                                if (arg.equals(currentUserEmail)){
                                    ChatService.getNewMessagesCount(argument.id, currentUserEmail, new Callback() {
                                        @Override
                                        public void call(Object arg) {
                                            switch ((int) arg) {
                                                case 0: {
                                                    imageView.setVisibility(View.GONE);
                                                    break;
                                                }
                                                case 1: {
                                                    imageView.setImageResource(R.drawable.one_m);
                                                    break;
                                                }
                                                case 2: {
                                                    imageView.setImageResource(R.drawable.two_m);
                                                    break;
                                                }
                                                case 3: {
                                                    imageView.setImageResource(R.drawable.three_m);
                                                    break;
                                                }
                                                case 4: {
                                                    imageView.setImageResource(R.drawable.four_m);
                                                    break;
                                                }
                                                case 5: {
                                                    imageView.setImageResource(R.drawable.five_m);
                                                    break;
                                                }
                                                case 6: {
                                                    imageView.setImageResource(R.drawable.six_m);
                                                    break;
                                                }
                                                case 7: {
                                                    imageView.setImageResource(R.drawable.seven_m);
                                                    break;
                                                }
                                                case 8: {
                                                    imageView.setImageResource(R.drawable.eight_m);
                                                    break;
                                                }
                                                case 9: {
                                                    imageView.setImageResource(R.drawable.nine_m);
                                                    break;
                                                }
                                                default: {
                                                    imageView.setImageResource(R.drawable.lotsof_m);
                                                    break;
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        });

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
