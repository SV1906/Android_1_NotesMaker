package com.example.notesmaker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class Register extends AppCompatActivity {
    EditText rCodeEnter, rEmailId, rPassword, rName;
    FirebaseAuth fAuth;
    Button Next;
    private static final String TAG = "Register";
    private CircleImageView ProfileImage;
    final static int Gallery_Pick = 1;
//    private ProgressDialog loadingBar;

    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        fAuth = FirebaseAuth.getInstance();
        Next = findViewById(R.id.button_next);
        rEmailId = findViewById(R.id.edit_text_emailId);
        rPassword = findViewById(R.id.edit_text_password);
        rName = findViewById(R.id.edit_text_name);
        ProfileImage = (CircleImageView) findViewById(R.id.profile_image);




//        ProfileImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view)
//            {
//
//                currentUserID = fAuth.getCurrentUser().getUid();
//                UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
//                UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
//
//                Intent galleryIntent = new Intent();
//                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//                galleryIntent.setType("image/*");
//                startActivityForResult(galleryIntent, Gallery_Pick);
//            }
//        });

        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = rEmailId.getText().toString().trim();
                String password = rPassword.getText().toString().trim();
                final String name = rName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    rName.setError("Name is mandatory");
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    rEmailId.setError("Email Id is mandatory");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    rPassword.setError("Password is mandatory");
                    return;
                }
                if (password.length() < 6) {
                    rPassword.setError("Password should be longer than 6 characters");
                    return;
                }

                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser user = fAuth.getCurrentUser();
                            user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                  @Override
                                                                                  public void onSuccess(Void aVoid) {
                                                                                      Toast.makeText(Register.this, "Verification Message has been sent", Toast.LENGTH_LONG).show();
                                                                                      UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                                                                                      user.updateProfile(profileUpdates);
                                                                                  }
                                                                              }
                            ).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "On Failure: Message not sent" + e.getMessage());
                                    Toast.makeText(Register.this, "Verification Message not sent", Toast.LENGTH_LONG).show();
                                }
                            });
                            Toast.makeText(Register.this, "User Created", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            Toast.makeText(Register.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data)
//    {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
//        {
//            Uri ImageUri = data.getData();
//
//            CropImage.activity()
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .setAspectRatio(1, 1)
//                    .start(this);
//        }
//
//        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
//        {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//
//            if(resultCode == RESULT_OK)
//            {
////                loadingBar.setTitle("Profile Image");
////                loadingBar.setMessage("Please wait, while we updating your profile image...");
////                loadingBar.show();
////                loadingBar.setCanceledOnTouchOutside(true);
//
////                Uri resultUri = result.getUri();
////
////                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
////
////                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
////                    @Override
////                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task)
////                    {
////                        if(task.isSuccessful())
////                        {
////                            Toast.makeText(Register.this, "Profile Image stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show();
////
////                            final String downloadUrl = task.getResult().getDownloadUrl().toString();
////
////                            UsersRef.child("profileimage").setValue(downloadUrl)
////                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
////                                        @Override
////                                        public void onComplete(@NonNull Task<Void> task)
////                                        {
////                                            if(task.isSuccessful())
////                                            {
////                                                Intent selfIntent = new Intent(Register.this, Register.class);
////                                                startActivity(selfIntent);
////
////                                                Toast.makeText(Register.this, "Profile Image stored to Firebase Database Successfully...", Toast.LENGTH_SHORT).show();
////                                                loadingBar.dismiss();
////                                            }
////                                            else
////                                            {
////                                                String message = task.getException().getMessage();
////                                                Toast.makeText(Register.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
////                                                loadingBar.dismiss();
////                                            }
////                                        }
////                                    });
////                        }
////                    }
////                });
//
//                Uri uri=result.getUri();
//                StorageReference filepath = UserProfileImageRef.child("Images").child(uri.getLastPathSegment());
//                filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
//                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
//
//                                    @Override
//                                    public void onSuccess(Uri uri) {
//                                        UsersRef.child("image").setValue(uri);
//                                    }
//                                });
//                    }
//                });
//            }
//            else
//            {
//                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
////                loadingBar.dismiss();
//            }
//        }
//    }


}










