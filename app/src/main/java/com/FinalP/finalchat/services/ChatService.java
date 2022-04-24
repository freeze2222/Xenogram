package com.FinalP.finalchat.services;

import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.models.domain.GroupD;
import com.FinalP.finalchat.models.domain.GroupMetadataD;
import com.FinalP.finalchat.models.domain.MessageD;
import com.firebase.ui.database.ClassSnapshotParser;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class ChatService {
    public static DatabaseReference dialogsRef() {
        return FirebaseDatabase.getInstance()
                .getReference("chat")
                .child("dialogs");
    }

    public static DatabaseReference dialogsRef(User userA, User userB) {
        String[] strings = new String[]{userA.id, userB.id};
        Arrays.sort(strings);
        String chatId = strings[0] + "-" + strings[1];

        return dialogsRef().child(chatId);
    }

    public static Task<Void> sendMessage(String text, User currentUser, User toUser) {
        MessageD message = new MessageD(text, false, false, currentUser.id, new Date().getTime());
//        ...
        return dialogsRef(currentUser, toUser).child("messages").push().setValue(message);
    }

    public static Task<Void> createDialog(User currentUser, User toUser) {
        GroupD dialog = new GroupD(
                new GroupMetadataD(new Date().getTime()),
                new HashMap<>()
        );
        return dialogsRef(currentUser, toUser).setValue(dialog)
                .addOnSuccessListener(unused -> {
                    DatabaseService.usersRef(currentUser.id).child("chats").push().setValue(toUser.id);
                    DatabaseService.usersRef(toUser.id).child("chats").push().setValue(currentUser.id);
                });
//        ...
    }

    public static FirebaseRecyclerOptions<MessageD> getUserOptions(User currentUser, User toUser) {
        Query query = dialogsRef(currentUser, toUser).child("messages");
        ClassSnapshotParser<MessageD> parser = new ClassSnapshotParser<>(MessageD.class);

        return new FirebaseRecyclerOptions.Builder<MessageD>()
                .setQuery(query, parser)
                .build();
    }
}
