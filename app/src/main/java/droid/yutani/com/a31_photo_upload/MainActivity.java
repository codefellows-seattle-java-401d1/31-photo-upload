package droid.yutani.com.a31_photo_upload;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_FULL_IMAGE_CAPTURE = 2;

    private int pictureType;

    @BindView(R.id.image_view) ImageView mImageView;
    @BindView(R.id.thumbnail_button) Button mTakeThumbnail;
    @BindView(R.id.picture_button) Button mTakePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.thumbnail_button)
    public void setThumbImg() {
        pictureType = REQUEST_IMAGE_CAPTURE;
        takePictureIntent();
    }

    @OnClick(R.id.picture_button)
    public void setFullImg() {
        pictureType = REQUEST_FULL_IMAGE_CAPTURE;
        takePictureIntent();
    }

    public void takePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, pictureType);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(bitmap);
        } else if (requestCode == REQUEST_FULL_IMAGE_CAPTURE && resultCode == RESULT_OK) {

        }
    }
}
