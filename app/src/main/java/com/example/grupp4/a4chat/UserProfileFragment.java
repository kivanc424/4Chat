package com.example.grupp4.a4chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class UserProfileFragment extends Fragment implements ChangePhotoDialog.OnPhotoReceivedListener,
        ChangeNameDialog.OnNameReceivedListener, View.OnClickListener {

    private ImageView userProfileImage;
    private TextView userProfileName;
    private TextView userProfileEmail;
    private FirebaseAuth mFirebaseAuth;
    private ProgressBar mProgressBar;
    private String TAG = "UserProfileFragment";

    public UserProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        view.findViewById(R.id.userProfileImage).setOnClickListener(this);
        view.findViewById(R.id.userProfileName).setOnClickListener(this);
        view.findViewById(R.id.userProfileNameHeader).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.userProfileImage:
                openChangePhotoDialog(view);
                break;
            case R.id.userProfileName:
                openChangeNameDialog(view);
                break;
            case R.id.userProfileNameHeader:
                openChangeNameDialog(view);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();
        String profile_user_id = mFirebaseAuth.getCurrentUser().getUid();
        FirebaseFirestore userFireStoreReference = FirebaseFirestore.getInstance();

        userProfileImage = (ImageView) getView().findViewById(R.id.userProfileImage);
        userProfileName = (TextView) getView().findViewById(R.id.userProfileName);
        userProfileEmail = (TextView) getView().findViewById(R.id.userProfileEmail);

        userFireStoreReference.collection("users").document(profile_user_id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    String name = documentSnapshot.getString("name");
                    String email = documentSnapshot.getString("email");
                    String image = documentSnapshot.getString("avatar");
                    userProfileName.setText(name);
                    userProfileEmail.setText(email);
                    Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(userProfileImage);
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }

    //Så fort namn ändras i fragmentet ChangeNameDialog notifieras OnNameRecievedListener och ändrat namn skickas hit
    //Sätter namn i UserProfileFragment och i NavToolbar
    @Override
    public void getName(String name) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).update(
                "name", name);

        userProfileName.setText(name);

        ((MainActivity)getActivity()).updateNavProfileName(name); //för att NavBar skapas inte om, måste uppdatera dirr. Borde EJ lägga NavBar i main.
    }

    //Så fort bild ändras i fragmentet ChangePhotoDialog notifieras OnPhotoRecievedListener och ändrad bildURL skickas hit
    //Laddar upp vald bild på databas Storage via PhotoUploader. Det är denna URL som sedan OnPhotoRecievedListener notifieras och skickar hit
    //Sätter bild i UserProfileFragment och i NavToolbar
    @Override
    public void getImagePath(Uri imagePath) {

        if (!imagePath.toString().equals("")) {
            Context context = getActivity();
            String userId = mFirebaseAuth.getCurrentUser().getUid();
            PhotoUploader uploader = new PhotoUploader(userId, context);
            uploader.uploadNewPhoto(imagePath);

            Picasso.get().load(imagePath.toString()).placeholder(R.drawable.default_avatar).into(userProfileImage);
            ((MainActivity)getActivity()).updateNavProfileImage(imagePath); //för att NavBar skapas inte om, måste uppdatera dirr. Borde EJ lägga NavBar i main.
        }
    }

    private void openChangePhotoDialog(View view) {
        Log.d(TAG, "onClick: Image button clicked");
        ChangePhotoDialog dialog = new ChangePhotoDialog();
        dialog.setTargetFragment(UserProfileFragment.this, 1);
        dialog.show(getFragmentManager(), "ChangePhotoDialog");
    }

    private void openChangeNameDialog(View view) {
        ChangeNameDialog dialog = new ChangeNameDialog();
        dialog.setTargetFragment(UserProfileFragment.this,2);
        dialog.show(getFragmentManager(), "ChangeNameDialog");
    }

    private void showLoader() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

}



