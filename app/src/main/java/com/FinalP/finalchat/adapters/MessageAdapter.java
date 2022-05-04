package com.FinalP.finalchat.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.models.application.Message;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.models.domain.MessageD;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;


public abstract class MessageAdapter extends FirebaseRecyclerAdapter<MessageD, MessageAdapter.MessageViewHolder> {
    public User currentUser;
    public User toUser;
    public FirebaseRecyclerOptions<MessageD> options;
    public MessageAdapter(@NonNull FirebaseRecyclerOptions<MessageD> options, User currentUser, User toUser) {
        super(options);
        this.currentUser = currentUser;
        this.toUser = toUser;
        this.options=options;
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull MessageD model) {
        holder.bind(model, currentUser, toUser);
    }
    @Override
    public int getItemViewType(int position) {
        if (!options.getSnapshots().get(position).fromID.equals(currentUser.id)){
            return 0;
        }
        else {
            return 1;
        }

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==0) {
            int layout = R.layout.item_dialog_row_to_me;
            View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            return new MessageViewHolder(view);
        }
        else {
            int layout = R.layout.item_dialog_row_from_me;
            View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            return new MessageViewHolder(view);
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView timeView;
        TextView textView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            //itemView.setMinimumHeight(1000);
            timeView = itemView.findViewById(R.id.textView3);
            textView = itemView.findViewById(R.id.textView2);
        }

        public void bind(MessageD messageD, User from, User to) {
            Message message = new Message(messageD, from, to, "");
            textView.setText(message.text);
            timeView.setText(message.creationDate.toLocaleString());
        }
    }

}
