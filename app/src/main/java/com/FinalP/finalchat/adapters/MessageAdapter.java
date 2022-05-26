package com.FinalP.finalchat.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.models.application.Message;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.models.domain.MessageD;
import com.FinalP.finalchat.services.Callback;
import com.FinalP.finalchat.services.DatabaseService;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public abstract class MessageAdapter extends FirebaseRecyclerAdapter<MessageD, MessageAdapter.MessageViewHolder> {
    public final User currentUser;
    public final User toUser;
    public final FirebaseRecyclerOptions<MessageD> options;
    private static Context context;
    public MessageAdapter(@NonNull FirebaseRecyclerOptions<MessageD> options, User currentUser, User toUser,Context context) {
        super(options);
        this.currentUser = currentUser;
        this.toUser = toUser;
        this.options=options;
        this.context=context;
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull MessageD model) {
        holder.bind(model, currentUser, toUser);
    }
    @Override
    public int getItemViewType(int position) {
        switch (options.getSnapshots().get(position).type) {
            case "text":
                if (!options.getSnapshots().get(position).fromID.equals(currentUser.id)) {
                    return 0;
                }
                else {
                    return 1;
                }
            case "image":
                if (!options.getSnapshots().get(position).fromID.equals(currentUser.id)) {
                    return 2;
                }
                else {
                    return 3;
                }
            case "video":
                if (!options.getSnapshots().get(position).fromID.equals(currentUser.id)) {
                    return 4;
                }
                else {
                    return 5;
                }
            default:
                return -1;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout;
        View view;
        switch (viewType){
            case 0:
                layout = R.layout.item_dialog_row_message_to_me;
                view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
                return new MessageViewHolder(view);
            case 1:
                layout = R.layout.item_dialog_row_message_from_me;
                view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
                return new MessageViewHolder(view);
            case 2:
                layout = R.layout.item_dialog_row_image_to_me;
                view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
                return new MessageViewHolder(view);
            case 3:
                layout = R.layout.item_dialog_row_image_from_me;
                view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
                return new MessageViewHolder(view);
            case 4:
                layout = R.layout.item_dialog_row_video_to_me;
                view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
                return new MessageViewHolder(view);
            case 5:
                layout = R.layout.item_dialog_row_video_from_me;
                view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
                return new MessageViewHolder(view);
            default:
                layout = R.layout.item_dialog_row_errored;
                view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
                return new MessageViewHolder(view);
        }

    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView timeView;
        TextView textView;
        Button videoButton;
        ImageView imageView;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
                Log.e("DEB","BINDING!");
                timeView = itemView.findViewById(R.id.textView3);
                textView = itemView.findViewById(R.id.textView2);
                Log.e("DEB", String.valueOf(textView));
                videoButton=itemView.findViewById(R.id.videoButton);
                imageView=itemView.findViewById(R.id.imageMessage);

        }

        public void bind(MessageD messageD, User from, User to) {
            Message message = new Message(messageD, from, to, "");
            int type=getItemViewType();
            if (type==0||type==1) {
                textView.setText(message.text);
                timeView.setText(message.creationDate.toLocaleString());
            }
            else if (type==2||type==3){
                StorageReference reference= FirebaseStorage.getInstance().getReference().child("messages").child("images").child(message.text);
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context)
                                .load(uri)
                                .into(imageView);
                    }
                });

                /*
                DatabaseService.downloadImage(reference, new Callback() {
                    @Override
                    public void call(Object arg) {
                        imageView.setImageURI(Uri.parse(String.valueOf(arg)));
                    }
                });
                */
            }


            else if (type==4||type==5){
                videoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StorageReference reference= FirebaseStorage.getInstance().getReference().child("messages").child("videos").child(message.text);
                        try {
                        DatabaseService.downloadVideo(reference, new Callback() {
                            @Override
                            public void call(Object arg) {
                                Uri uri=(Uri) arg;
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(uri, "video/mp4");
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                        });
                        }
                        catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(context,"Видео ещё не загружено",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }

}
