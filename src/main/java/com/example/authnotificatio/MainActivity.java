package com.example.authnotificatio;

import android.annotation.SuppressLint;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView userNameTextView, userAddressTextView, userPhoneTextView;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    ImageView logout, update;
    FirebaseMessaging firebaseMessaging;
    FirebaseAuth auth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profileImageView = findViewById(R.id.profile_image);
        userNameTextView = findViewById(R.id.name);
        auth = FirebaseAuth.getInstance();
        userAddressTextView = findViewById(R.id.address);
        userPhoneTextView = findViewById(R.id.phone);
        logout = findViewById(R.id.logout);
        update = findViewById(R.id.update);

        FirebaseMessaging.getInstance().subscribeToTopic("profile_topic")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.e("tag", "Done");
                        if (!task.isSuccessful()) {
                            Log.e("tag", "Failed");
                        }
                    }
                });
        logout.setOnClickListener(v -> {

            auth.signOut();
            sendUserToLoginActivity();

        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(loginIntent);
            }
        });

        FirebaseMessaging.getInstance().subscribeToTopic("profile-topic")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.e("tag", "Done");
                        if (!task.isSuccessful()) {
                            Log.e("tag", "Failed");
                        }
                    }
                });


        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference().child("Profile").child(currentUser.getUid() + ".jpg");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {

                String userName = dataSnapshot.child("name").getValue(String.class);

                String address = dataSnapshot.child("address").getValue(String.class);
                String phone = dataSnapshot.child("phone").getValue(String.class);


                userNameTextView.setText("User Name: " + userName);


                userAddressTextView.setText("Address: " + address);
                userPhoneTextView.setText("Phone: " + phone);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });


        storageReference.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(profileImageView));

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            sendUserToLoginActivity();
        } else {

            VerifyUserexistance();
        }
    }


    private void VerifyUserexistance() {
        String currentUserID = auth.getCurrentUser().getUid();
        databaseReference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("name").exists()) {
                    Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_SHORT).show();
                } else {
                    //sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToLoginActivity() {
        Intent loginintent = new Intent(MainActivity.this, LoginActivity.class);
        loginintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginintent);
        finish();
    }

//    private void sendUserToSettingsActivity() {
//        Intent settingsintent = new Intent(MainActivity.this, SettingsActivity.class);
//        startActivity(settingsintent);
//    }
}
