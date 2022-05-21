package com.FinalP.finalchat.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.FinalP.finalchat.fragments.ChatFragment;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.models.domain.GroupD;
import com.FinalP.finalchat.models.domain.GroupMetadataD;
import com.FinalP.finalchat.models.domain.MessageD;
import com.firebase.ui.database.ClassSnapshotParser;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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
        Log.e("DEBUG!", currentUser.id);
        MessageD message = new MessageD(text, false, false, currentUser.id, new Date().getTime());
//        ...
        return dialogsRef(currentUser, toUser).child("messages").push().setValue(message);
    }

    public static Task<Void> createDialog(User currentUser, User toUser) {
        GroupD dialog = new GroupD(
                new GroupMetadataD(new Date().getTime()),
                new HashMap<>(), 0, currentUser.id,
                false,false
        );
        return dialogsRef(currentUser, toUser).setValue(dialog)
                .addOnSuccessListener(unused -> {
                    DatabaseService.usersRef(currentUser.id).child("chats/" + toUser.id).setValue(toUser.id);
                    DatabaseService.usersRef(toUser.id).child("chats/" + currentUser.id).setValue(currentUser.id);
                });
    }

    public static DatabaseReference getUserDialogRef(String toUser, String currentUser, Callback callback) {
        DatabaseReference mainRef = FirebaseDatabase.getInstance().getReference().child("chat").child("dialogs");
        mainRef.child(currentUser + "-" + toUser).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mainRef.child(currentUser + "-" + toUser).keepSynced(true);
                    callback.call(mainRef.child(currentUser + "-" + toUser));
                } else {
                    mainRef.child(toUser + "-" + currentUser).keepSynced(true);
                    callback.call(mainRef.child(toUser + "-" + currentUser));
                }

            }
        });
        return null;
    }

    public static void isChatActive(String toUser, String currentUser, Callback callback) {
        getUserDialogRef(toUser, currentUser, new Callback() {
            @Override
            public void call(Object arg) {
                DatabaseReference reference = (DatabaseReference) arg;
                reference.child("isActiveUser1").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshotUser1) {
                        reference.child("isActiveUser2").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot dataSnapshotUser2) {
                                if (dataSnapshotUser1.getValue(Boolean.class)&&dataSnapshotUser2.getValue(Boolean.class)){
                                    callback.call(true);
                                }
                                else{
                                    callback.call(false);
                                }
                            }
                        });

                    }
                });
            }
        });
    }

    public static void toggleActiveIndicator(String toUser, String currentUser) {
        getUserDialogRef(toUser, currentUser, new Callback() {
            @Override
            public void call(Object arg) {
                DatabaseReference reference = (DatabaseReference) arg;
                String[] users = reference.getKey().split("-");
                Log.e("DEB", Arrays.toString(users));
                Log.e("DEB", users[1]);
                Log.e("DEB", currentUser);
                if (currentUser.equals(users[0])){
                    reference.child("isActiveUser1").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            reference.child("isActiveUser1").setValue(!dataSnapshot.getValue(Boolean.class));
                        }
                    });
                }
                else{
                    reference.child("isActiveUser2").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            reference.child("isActiveUser2").setValue(!dataSnapshot.getValue(Boolean.class));
                        }
                    });
                }
            }
        });
    }

    public static void getNewMessagesCount(String toUser, String currentUser, Callback callback) {

                    getUserDialogRef(toUser, currentUser, new Callback() {
                        @Override
                        public void call(Object arg) {
                            DatabaseReference userRef = (DatabaseReference) arg;
                            userRef.child("unread").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                @Override
                                public void onSuccess(DataSnapshot dataSnapshot) {
                                Log.e("DATAFALSE", String.valueOf(dataSnapshot));
                                Log.e("DATAFALSE", String.valueOf(dataSnapshot.getValue()));
                                callback.call(dataSnapshot.getValue(Integer.class));
                                }
                            }
                            );
                    }
        });

    }

    public static void addToMessagesCount(String toUser, String currentUser) {
        isChatActive(toUser, currentUser, new Callback() {
            @Override
            public void call(Object arg) {
                Boolean isActive = (Boolean) arg;
                if (!isActive) {
                    getUserDialogRef(toUser, currentUser, new Callback() {
                        @Override
                        public void call(Object arg) {
                            DatabaseReference userRef = (DatabaseReference) arg;
                            userRef.child("unread").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                @Override
                                public void onSuccess(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        userRef.child("unread").setValue(dataSnapshot.getValue(Integer.class) + 1);
                                        userRef.child("unread_property").setValue(toUser);
                                    }

                                }
                            });
                        }
                    });
                }
                }
            });


    }


    public static void whoseCounter(String toUser, String currentUser, Callback callback) {
        getUserDialogRef(toUser, currentUser, new Callback() {
            @Override
            public void call(Object arg) {
                DatabaseReference userRef = (DatabaseReference) arg;
                getNewMessagesCount(toUser, currentUser, new Callback() {
                    @Override
                    public void call(Object arg) {
                        userRef.child("unread_property").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot dataSnapshot) {
                                callback.call(dataSnapshot.getValue(String.class));
                            }
                        });
                    }
                });
            }
        });


    }

    public static void removeCounter(String toUser, String currentUser) {
        DatabaseReference mainRef = FirebaseDatabase.getInstance().getReference().child("chat").child("dialogs");
        mainRef.child(currentUser + "-" + toUser).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                Log.e("USERS", currentUser + "!!!" + toUser);
                if (dataSnapshot.exists()) {
                    mainRef.child(currentUser + "-" + toUser).child("unread").setValue(0);
                } else {
                    mainRef.child(toUser + "-" + currentUser).child("unread").setValue(0);
                }
            }
        });
    }

    public static FirebaseRecyclerOptions<MessageD> getUserOptions(User currentUser, User toUser) {
        Query query = dialogsRef(currentUser, toUser).child("messages");
        ClassSnapshotParser<MessageD> parser = new ClassSnapshotParser<>(MessageD.class);

        return new FirebaseRecyclerOptions.Builder<MessageD>()
                .setQuery(query, parser)
                .build();
    }

    public static void testLinkListeners(String currentUser) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser).child("chats");
        reference.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                Log.e("DEB", "SUCCESS");
                DataSnapshot lastDataSnapshot = null;
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                for (DataSnapshot snapshot : iterable) {
                    Log.e("DEB", "FOR ITERATOR: " + snapshot.getKey());
                    lastDataSnapshot = snapshot;
                    getUserDialogRef(lastDataSnapshot.getKey(), currentUser, new Callback() {
                        @Override
                        public void call(Object arg) {
                            Log.e("DEB", "CALL");
                            DatabaseReference mainRef = (DatabaseReference) arg;
                            Log.e("DEB", String.valueOf(mainRef));
                            mainRef.child("unread").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Log.e("DEB", "The " + snapshot.getKey() + " test data is " + snapshot.getValue());
                                    ChatFragment.notifyAdapter();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("DEB", "ERROR: " + error.getDetails());
                                }
                            });
                        }
                    });
                }
            }
        });
    }
}
