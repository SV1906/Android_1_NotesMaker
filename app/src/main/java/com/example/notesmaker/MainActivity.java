package com.example.notesmaker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    EditText lEmailId, lPassword;
    Button Login, ForgotPassword, NewAcc,Resend;
    FirebaseAuth fAuth;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lEmailId = findViewById(R.id.edit_text_emailId);
        lPassword = findViewById(R.id.edit_text_password);
        Login = findViewById(R.id.button_login);
        ForgotPassword = findViewById(R.id.button_forgotPwd) ;
        NewAcc = findViewById(R.id.button_newAcc) ;
        Resend = findViewById(R.id.button_resend);
        fAuth = FirebaseAuth.getInstance();
        //Resend = findViewById(R.id.button3);

        Resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = fAuth.getCurrentUser().getUid();
                FirebaseUser user = fAuth.getCurrentUser();
                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                      @Override
                                                                      public void onSuccess(Void aVoid) {
                                                                          Toast.makeText(MainActivity.this, "Verification Message has been sent", Toast.LENGTH_LONG).show();
                                                                      }
                                                                  }
                ).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "On Failure: Message not sent" + e.getMessage());
                        Toast.makeText(MainActivity.this, "Verification Message not sent", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        Resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = fAuth.getCurrentUser().getUid();
                FirebaseUser user = fAuth.getCurrentUser();
                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                      @Override
                                                                      public void onSuccess(Void aVoid) {
                                                                          Toast.makeText(MainActivity.this, "Verification Message has been sent", Toast.LENGTH_LONG).show();
                                                                      }
                                                                  }
                ).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "On Failure: Message not sent" + e.getMessage());
                        Toast.makeText(MainActivity.this, "Verification Message not sent", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        NewAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Register.class));
            }
        });
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        String Email = lEmailId.getText().toString().trim();
                        String Password = lPassword.getText().toString().trim();


                        if(TextUtils.isEmpty(Email)){
                            lEmailId.setError("Email Id is mandatory");
                            return;
                        }
                        if(TextUtils.isEmpty(Password)){
                            lPassword.setError("Password is mandatory");
                            return;
                        }
                        if(Password.length() < 6) {
                            lPassword.setError("Password should be longer than 6 characters");
                            return;
                        }
                        fAuth.signInWithEmailAndPassword(Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                                                                   @Override
                                                                                                   public void onComplete(@NonNull Task<AuthResult> task) {
                              if(task.isSuccessful()){
                                  String userId = fAuth.getCurrentUser().getUid();
                                  FirebaseUser user = fAuth.getCurrentUser();
                                  if(!user.isEmailVerified()){

                                     // Toast.makeText(MainActivity.this, "Email isn't verified, try again after verification of Email ", Toast.LENGTH_LONG).show();
                                      Resend.setVisibility(View.VISIBLE);
                                      AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                      builder.setTitle("Email ID not verified!");
                                      builder.setMessage("Please verify your Email ID and try again later")
                                              .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                  public void onClick(DialogInterface dialog, int id) {
                                                      // FIRE ZE MISSILES!
                                                  }
                                              });
                                      builder.show();

                                      Toast.makeText(MainActivity.this, "Email isn't verified, try again after verification of Email ", Toast.LENGTH_LONG).show();
                                      Resend.setVisibility(View.VISIBLE);


                                  }
                                  else {
                                      Toast.makeText(MainActivity.this, "You have Logged in", Toast.LENGTH_LONG).show();

                                      // user will be logged in into the notes activity
                                      Intent noteIntent = new Intent(MainActivity.this, NotesActivity.class);
                                      noteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                      startActivity(noteIntent);
                                  }
                              }
                              else{
                                  Toast.makeText(MainActivity.this, "Incorrect Email or Password"+ task.getException().getMessage(), Toast.LENGTH_LONG).show();
                              } }
                        });

                }
                                });
        ForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText resetMail= new EditText(v.getContext());
            AlertDialog.Builder passwordResetDialog =new AlertDialog.Builder(v.getContext());
            passwordResetDialog.setTitle("Reset Password?");
            passwordResetDialog.setMessage("Enter your email to receive reset link");
            passwordResetDialog.setView(resetMail);
            passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which){
                    String mail = resetMail.getText().toString();
                    fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MainActivity.this, "Reset Link sent to your Email", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Error!Reset Link is not sent" + e.getMessage(), Toast.LENGTH_SHORT ).show();
                        }
                    });
                }
            });
            passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which){
                }
            });
            passwordResetDialog.create().show();
            }
        }
        );
    }
}
