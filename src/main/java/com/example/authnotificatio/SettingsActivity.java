package com.example.authnotificatio;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;


import java.util.HashMap;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {


    private EditText userNameEdit, userPhoneEdit, userAddressEdit;
    private RoundedImageView profileImage;
    private Button updateSettings;

    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private StorageReference storageProfilePicsRef;
    private ProgressBar progressBar;
    private String currentUserID;
    private static final int GALLERY_PICK = 1;
    private static final int GALLERY_REQUEST_CODE = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userNameEdit = findViewById(R.id.user_name);

        userAddressEdit = findViewById(R.id.user_address);
        userPhoneEdit = findViewById(R.id.user_phone);
        profileImage = findViewById(R.id.set_profile);
        updateSettings = findViewById(R.id.update_settings);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        storageProfilePicsRef = FirebaseStorage.getInstance().getReference().child("Profile");

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        updateSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = userNameEdit.getText().toString();

                String address = userAddressEdit.getText().toString();
                String phone = userPhoneEdit.getText().toString();

                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(SettingsActivity.this, "Please fill fields", Toast.LENGTH_SHORT).show();
                } else {//
                    updateAccountInfo(username, address, phone);
                }
            }
        });
        RetrieveUserInfo();
    }

    private void updateAccountInfo(final String username, final String address, final String phone) {
        progressBar.setVisibility(View.VISIBLE);
        if (imageUri != null) {
            final StorageReference fileRef = storageProfilePicsRef.child(currentUserID + ".jpg");
            UploadTask uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    progressBar.setVisibility(View.INVISIBLE);
                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();
                        String myUrl = downloadUrl.toString();

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", currentUserID);
                        userMap.put("name", username);
                        userMap.put("address", address);
                        userMap.put("phone", phone);
                        userMap.put("image", myUrl);

                        rootRef.child("Users").child(currentUserID).updateChildren(userMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            sendUserToMainActivity();
                                        } else {
                                            Toast.makeText(SettingsActivity.this, "Error occurred, please try again!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(SettingsActivity.this, "Error occurred, please try again!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void RetrieveUserInfo() {


        DatabaseReference userRef = rootRef.child("Users").child(currentUserID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);

                    String address = dataSnapshot.child("address").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);
                    String image = dataSnapshot.child("image").getValue(String.class);

                    userNameEdit.setText(name);

                    userAddressEdit.setText(address);
                    userPhoneEdit.setText(phone);
                    if (!TextUtils.isEmpty(image)) {
                        Picasso.get().load(image).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void sendUserToMainActivity() {
        Intent loginintent = new Intent(SettingsActivity.this, MainActivity.class);
        loginintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginintent);
        finish();
    }
}