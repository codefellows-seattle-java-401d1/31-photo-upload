package com.example.panos.photo_upload;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView mImageView;
    String mCurrentPhotoPath;
    private StorageReference mStorageReference;
    private StorageReference imgRef;
    Uri photoURI;
    Uri downloadURI;
    File photoFile;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        StorageReference localRef = mStorageReference.child("images/");
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.pictureDisplay);
        Button cameraButton = findViewById(R.id.toCamera);
        imgRef = mStorageReference.child("images/");
        try {
            final File photoFile = createImageFile();
            try {
                localRef.getFile(photoFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                while(taskSnapshot.getTotalByteCount() > taskSnapshot.getBytesTransferred()){
                                    Log.d("Currently:",taskSnapshot.getBytesTransferred()+"/"+taskSnapshot.getTotalByteCount());
                                }
                                Uri imageUri = Uri.parse(photoFile.toURI().toString());
                                try {
                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), imageUri);
                                    mImageView.setImageBitmap(bitmap);
                                }catch(IOException ex){
                                    Log.d("Error:", ex.toString());
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Failed to download:", e.toString());
                    }
                });

            }catch(Exception e){
                Log.d("Failed to create file:",e.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try{
                        photoFile = createImageFile();
                    }catch(IOException ex){
                    }
                    if(photoFile != null) {
                        photoURI = FileProvider.getUriForFile(MainActivity.this,"com.example.panos.photo_upload",photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        System.out.println(takePictureIntent);
                    }
                }
            }
        }

        );

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Bundle extras = data.getExtras();
//        Bitmap image = (Bitmap) extras.get("data");
        Bitmap image = null;
        imgRef = mStorageReference.child("images/");
        try {
            image = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), photoURI);
        }catch(IOException e){
            System.out.print(e.getMessage());
        }
        mImageView.setImageBitmap(image);
        imgRef.putFile(photoURI)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        downloadURI = taskSnapshot.getDownloadUrl();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.print(e);
                        Log.d("exception:",e.toString());
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
        } else {
            signInAnonymously();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "/labPicture";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir+imageFileName);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new  OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // do your stuff
            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d("signInAnonymously:", exception.toString());
                    }
                });
    }
}