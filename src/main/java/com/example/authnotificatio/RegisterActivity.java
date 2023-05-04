package com.example.authnotificatio;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterActivity extends AppCompatActivity {

    private Button signup;
    private EditText email,password;
    private TextView login;

    private FirebaseAuth auth;
    private DatabaseReference ref;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        auth=FirebaseAuth.getInstance();
        ref= FirebaseDatabase.getInstance().getReference();

        signup=findViewById(R.id.register);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        login=findViewById(R.id.loginn);
        progressDialog=new ProgressDialog(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailText=email.getText().toString();
                String passwordText=email.getText().toString();

                if(TextUtils.isEmpty(emailText))
                {
                    Toast.makeText(RegisterActivity.this,"Please enter the email",Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(passwordText))
                {
                    Toast.makeText(RegisterActivity.this,"Please enter the password",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    progressDialog.setTitle("Creating New Account");
                    progressDialog.setMessage("please wait, While we are creating a new account for you...");
                    progressDialog.setCanceledOnTouchOutside(true);
                    progressDialog.show();
                    auth.createUserWithEmailAndPassword(emailText,passwordText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                String deviceToken= task.getResult().toString();
                                String currentUserID=auth.getCurrentUser().getUid();
                                ref.child("Users").child(currentUserID).setValue("");
                                ref.child("Users").child(currentUserID).child("device_token").setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();
                                                Toast.makeText(RegisterActivity.this,"Account created Successfully",Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                            else
                            {
                                String error=task.getException().toString();
                                Toast.makeText(RegisterActivity.this,"Error :"+error,Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.dismiss();
                        }
                    });
                }

            }
        });
    }



}