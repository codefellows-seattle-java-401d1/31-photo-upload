package com.android.sooz.photouploadapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


//photo -related code provided by Android Guide and class lecture:
//https://developer.android.com/training/camera/photobasics

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_SAVE_PHOTO = 2;


    private String mCurrentPhotoPath;

    //store most recent picture taken
    private StorageReference mStorageRef;

    //use ButterKnife annotations to bind view to the activity main view
    @BindView(R.id.imageView)
    public ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        try {
            downloadFIle();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //use ButterKnife annotations to attach onClickListener to button
    @OnClick(R.id.take_picture)
    public void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //use ButterKnife annotations to attach onClickListener to button
    @OnClick(R.id.save_picture)
    public void dispatchSavePictureIntent(){

        Intent savePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (savePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.android.sooz.photouploadapp",
                        photoFile);
                savePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(savePictureIntent, REQUEST_SAVE_PHOTO);
            }
        }
    }

    //need to help application actually capture photo and save to Firebase storage
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            setPictureFromThumbnail(data);

            } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
                setPictureFromFile();
        }
    }


    public void setPictureFromThumbnail(Intent data){
        Bundle extras = data.getExtras();
        if(extras !=null){
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
        }

    }

    //saves taken picture as an image
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //for use at later date
//    private void galleryAddPic(){
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        File file = new File(mCurrentPhotoPath);
//
//        Uri contentUri = Uri.fromFile(file);
//
//        mediaScanIntent.setData(contentUri);
//
//        this.sendBroadcast(mediaScanIntent);
//    }

    private void setPictureFromFile(){

        //first get the dimensions of of the view where image will be populated
        int targetWidth = mImageView.getWidth();
        int targetHeight = mImageView.getHeight();

        //change picture from bitmap object stored in Firebase Storage to bitmap object for app
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

        bitmapOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(mCurrentPhotoPath, bitmapOptions);

        //set dimensions of picture
        int photoWidth = bitmapOptions.outWidth;
        int photoHeight = bitmapOptions.outHeight;

        //in case either target value is 0
        if(targetHeight == 0){
            targetHeight = targetWidth;
        }

        if(targetWidth == 0){
            targetWidth = targetHeight;
        }

        //determine how much to scale down/up image
        int scaleFactor = Math.min(photoWidth/targetWidth, photoHeight/targetHeight);

        //decode image from bitmap stored in Firebase storage
        bitmapOptions.inJustDecodeBounds = false;
        bitmapOptions.inSampleSize = scaleFactor;
        bitmapOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bitmapOptions);

        mImageView.setImageBitmap(bitmap);

        uploadFile(bitmap);

    }

    public void uploadFile(Bitmap bitmap){
        StorageReference photosRef = mStorageRef.child("photos/mostrecent.jpg");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        photosRef.putBytes(data)
            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Get a URL to the uploaded content
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    exception.printStackTrace();
                }
            });
    }


    public void downloadFIle() throws IOException {

        final File localFile = File.createTempFile("images", "bmp");

        mStorageRef.child("photos/mostrecent.jpg")
                .getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        mImageView.setImageBitmap(bitmap);

                        // Successfully downloaded data to local file
                        String successmsg = "file downloaded successfully";
                        Log.d("successmsg", successmsg);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        exception.printStackTrace();
                    }
        });

    }



}
