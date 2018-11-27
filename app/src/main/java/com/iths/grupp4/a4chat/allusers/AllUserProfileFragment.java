package com.iths.grupp4.a4chat.allusers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iths.grupp4.a4chat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;


public class AllUserProfileFragment extends Fragment {

    private static final String TAG = "Error";
    private MenuItem itemAddFriend;
    private MenuItem itemRemoveFriend;
    private Button addFriend;
    private Button removeFriend;

    private FirebaseAuth mAuth;
    private FirebaseFirestore friendRequestReference;
    private FirebaseFirestore acceptedFriendReference;
    private CollectionReference requestReference;
    private CollectionReference friendsReference;
    private FirebaseUser current_user;

    private String current_state;
    String sender_user_id;
    String receiver_user_id;


    public AllUserProfileFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_user_profile, container, false);

        FirebaseFirestore userFireStoreReference = FirebaseFirestore.getInstance();
        TextView allUserName = (TextView) view.findViewById(R.id.allUserProfileName);
        TextView allUserEmail = (TextView) view.findViewById(R.id.allUserProfileEmail);
        ImageView allUserImage = (ImageView) view.findViewById(R.id.allUserProfileImage);
        addFriend = (Button) view.findViewById(R.id.addFriend);
        removeFriend = (Button) view.findViewById(R.id.removeFriend);

        friendRequestReference = FirebaseFirestore.getInstance();
        acceptedFriendReference = FirebaseFirestore.getInstance();
        friendsReference = acceptedFriendReference.collection("users");
        requestReference = friendRequestReference.collection("friend_request");
        current_user = FirebaseAuth.getInstance().getCurrentUser();
        current_state = "not_friends";

        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        receiver_user_id = bundle.getString("visit_user_id");
        removeFriend.setVisibility(View.INVISIBLE);


        userFireStoreReference.collection("users").document(receiver_user_id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    String name = documentSnapshot.getString("name");
                    String email = documentSnapshot.getString("email");
                    String image = documentSnapshot.getString("avatar");
                    allUserName.setText(name);
                    allUserEmail.setText(email);
                    Picasso.get().load(image).transform(new CropCircleTransformation()).placeholder(R.drawable.default_avatar).into(allUserImage);

                    /* OM NI ÄNDRAR NÅGOT PÅ DENNA FRAGMENTEN SÅ SÄG TILL MIG INNAN NI GÖR DET // Kivanc
                    TODO Fixa problemet när friend_request på firestore inte finns så crashar appen.

                    This function checks if you have sent a request to other users and it updates
                    their button if you have sent one.

                     */
                    requestReference.document(current_user.getUid()).collection(receiver_user_id).document("request").addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                            if (documentSnapshot.exists()){
                                String request_type = documentSnapshot.getString("request_type");
                                if(request_type.equals("received")){

                                    current_state = "request_received";
                                    addFriend.setText("Accept Friend Request");

                                    removeFriend.setText("Decline friend Request");
                                    removeFriend.setVisibility(View.VISIBLE);
                                    removeFriend.setEnabled(true);


                                } else if(request_type.equals("sent")) {

                                    current_state = "request_sent";
                                    addFriend.setText("Cancel Friend Request");

                                    removeFriend.setVisibility(View.INVISIBLE);
                                    removeFriend.setEnabled(false);

                                }
                            }

                        }
                    });

                    friendsReference.document("friends").collection(current_user.getUid()).document(receiver_user_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot.exists()){
                                String currentState = documentSnapshot.getString("state");
                                if (currentState.equals("friend")){
                                    current_state = "friends";
                                    addFriend.setVisibility(View.INVISIBLE);
                                    removeFriend.setVisibility(View.VISIBLE);
                                    removeFriend.setEnabled(true);
                                    removeFriend.setText("Unfriend this person");
                                }
                            }
                        }
                    });



                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });

        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend.setEnabled(false);

                /*
                When you press on add friend button it will create new collection and document
                in firestore, that you have sent a friend request.

                NOT FRIENDS !
                */
                if (current_state.equals("not_friends")){
                    Map<String, Object> sent = new HashMap<>();
                    sent.put("request_type", "sent");
                    removeFriend.setVisibility(View.INVISIBLE);

                    friendRequestReference.collection("friend_request").document(current_user.getUid())
                            .collection(receiver_user_id).document("request")
                            .set(sent).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Map<String, Object> received = new HashMap<>();
                                received.put("request_type", "received");
                                friendRequestReference.collection("friend_request").document(receiver_user_id)
                                        .collection(current_user.getUid()).document("request")
                                        .set(received).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        addFriend.setEnabled(true);
                                        current_state = "request_sent";
                                        addFriend.setText("Cancel Friend Request");

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                }
                /*
                When you have sent friend request to the other user the state will be changed to request sent
                and you can cancel the request where the collection and document will be removed.

                REQUEST SENT !
                 */
                if (current_state.equals("request_sent")){
                    friendRequestReference.collection("friend_request").document(current_user.getUid())
                            .collection(receiver_user_id).document("request").delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendRequestReference.collection("friend_request").document(receiver_user_id).
                                    collection(current_user.getUid()).document("request")
                                    .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    current_state = "not_friends";
                                    addFriend.setText("Send friend request");
                                }
                            });
                        }
                    });

                    addFriend.setEnabled(true);
                }


                // REQUEST RECEIVED

                if(current_state.equals("request_received")){
                    String currentDate = DateFormat.getDateInstance().format(new Date());
                    Map<String, Object> senderFriendData = new HashMap<>();
                    senderFriendData.put(current_user.getUid(), currentDate);
                    senderFriendData.put("state", "friend");

                    acceptedFriendReference.collection("users").document("friends")
                            .collection(current_user.getUid()).document(receiver_user_id).set(senderFriendData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Map<String, Object> receiverFriendData = new HashMap<>();
                            receiverFriendData.put(receiver_user_id, currentDate);
                            receiverFriendData.put("state", "friend");

                            friendRequestReference.collection("users").document("friends")
                                    .collection(receiver_user_id).document(current_user.getUid())
                                    .set(receiverFriendData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    friendRequestReference.collection("friend_request").document(current_user.getUid())
                                            .collection(receiver_user_id).document("request").delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    friendRequestReference.collection("friend_request").document(receiver_user_id).
                                                            collection(current_user.getUid()).document("request")
                                                            .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            current_state = "friends";
                                                            addFriend.setText("Unfriend this person");
                                                        }
                                                    });
                                                }
                                            });
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            }
        });

        removeFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_state.equals("friends")){
                    friendsReference.document(current_user.getUid()).collection(receiver_user_id).document("time").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendsReference.document(receiver_user_id).collection(current_user.getUid()).document("time").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    current_state = "not_friends";
                                    addFriend.setVisibility(View.VISIBLE);
                                    addFriend.setText("Send friend request");

                                    removeFriend.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    });
                }else if (current_state.equals("request_received")){
                    friendRequestReference.collection("friend_request").document(current_user.getUid())
                            .collection(receiver_user_id).document("request").delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    friendRequestReference.collection("friend_request").document(receiver_user_id).
                                            collection(current_user.getUid()).document("request")
                                            .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            current_state = "not_friends";
                                            addFriend.setText("Send friend request");
                                            removeFriend.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            });
                }
            }
        });


        return view;
    }

}