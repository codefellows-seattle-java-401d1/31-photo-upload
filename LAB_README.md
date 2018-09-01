# ![CF](http://i.imgur.com/7v5ASc8.png) Photo Uploader

## Resources  
* [Taking Photos Simply in Android](https://developer.android.com/training/camera/photobasics.html)
* [Upload Files Android Firebase](https://firebase.google.com/docs/storage/android/upload-files)
* [Download Files Android Firebase](https://firebase.google.com/docs/storage/android/download-files)

Here's a simple lab to get you used to interacting with Android's camera via
intents, saving photos to files and uploading photos to Firebase Storage.

Build an app that allows people to take a picture and upload a photo to
Firebase Storage. The application should download the image when the user loads
the app again. Store the image at a consistent location like:

```
mStorageRef.child("photos/mostrecent.jpg")
```

Follow the "Taking Photos Simply in Android" guide carefully to create files to
store the photo, to read the photos from a file, to decode and scale the image.

Here are several code snippets useful for interacting with files, images,
bitmaps, uploading and downloading.

Remember to replace the package name with your own apps package name!

#### Compressing A File for Upload
```java
ByteArrayOutputStream baos = new ByteArrayOutputStream();
bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
byte[] data = baos.toByteArray();
```

#### Decoding A File for Download
```java
public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
    mImageView.setImageBitmap(bitmap);
}
```

#### Prevent Divide By Zero Errors
It's possible your layout might make your `ImageView` end up with zero width,
or zero height. If you see any divide-by-zero errors consider using if
statements to convert the width and height scale to one. This may not be
necessary, but you may need it!

```java
if (targetW == 0) {
  targetW = 1;
}

if (targetH == 0) {
  targetH = 1;
}

// Determine how much to scale down the image
int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
```

#### Firebase Read/Write Errors
If you see an error about Firebase read/write permissions you may need to
navigate to the Firebase console, go to Storage, go to Rules and configure the
rules so it returns true for read and write, like below:

```js
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if true; //request.auth != null;
    }
  }
}
```

## Submission Instructions
* Work in a fork of this repository
* Work in a branch on your fork
* Write all of your code in a directory named `lab-` + `<your name>` **e.g.** `lab-susan`
* Open a pull request to this repository
* Submit on canvas a question and observation, how long you spent, and a link to
  your pull request
