package com.example.myruns1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    ImageView imageView;

    Uri imgUri;
    File imgFile;
    String imgFileName = "newFile.jpg";
    boolean isTakenFromCamera;

    public static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final String URI_INSTANCE_STATE_KEY = "saved_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        // initialize saved preferences
        loadProfile();

        imageView = findViewById(R.id.imageProfile);

        // check if you have camera permissions
        Util.checkPermission(this);

        // create file object
        imgFile = new File(getExternalFilesDir(null), imgFileName);
        imgUri = FileProvider.getUriForFile(this, "com.example.myruns1", imgFile);

        if(savedInstanceState != null) {
            imgUri = savedInstanceState.getParcelable(URI_INSTANCE_STATE_KEY);
        }

        loadSnap();
    }

    // From the Android Developer's Guide
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dropdown, menu);
        return true;
    }

    public void onChangePhotoClicked(View view) {
        // intent to take a picture
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        // Construct temporary image path and name to save the taken
//        // photo
//        ContentValues values = new ContentValues(1);
//        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
//        imgUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//        // save URI and start camera
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
//        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        isTakenFromCamera = true;
    }

    // after the camera activity returns the photo
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // as long as the photo is returned
        if (resultCode == Activity.RESULT_OK) {

            switch (requestCode) {
                // Image was taken from camera
                case REQUEST_IMAGE_CAPTURE:
//                     Send image taken from camera for cropping
                    beginCrop(imgUri);
                    break;
////
                case Crop.REQUEST_CROP: //We changed the RequestCode to the one being used by the library.
                    // Update image view after image crop
                    handleCrop(resultCode, data);

                    // Delete temporary image taken by camera after crop.
                    if (isTakenFromCamera) {
                        File f = new File(imgUri.getPath());
                        if (f.exists())
                            f.delete();
                    }
                    break;
            }
        }
    }


    private void loadSnap() {

        // Load profile photo from internal storage
        try {
            FileInputStream fis = openFileInput(getString(R.string.imgFileName));
            Bitmap bmap = BitmapFactory.decodeStream(fis);
            imageView.setImageBitmap(bmap);
            fis.close();
        } catch (IOException e) {
            // Default profile photo if no photo saved before.
//            imageView.setImageResource(R.drawable.default_profile);
        }
    }

    private void saveSnap() {

        // Commit all the changes into preference file
        // Save profile image into internal storage.
        imageView.buildDrawingCache();
        Bitmap bmap = imageView.getDrawingCache();
        try {
            FileOutputStream fos = openFileOutput(getString(R.string.imgFileName), MODE_PRIVATE);
            bmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /** Method to start Crop activity using the library
     *  Earlier the code used to start a new intent to crop the image,
     *  but here the library is handling the creation of an Intent, so you don't
     * have to.
     *  **/
    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            imageView.setImageURI(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(bundle);
        saveProfile();
        // Save the image capture uri before the activity goes into background
//        bundle.putParcelable(URI_INSTANCE_STATE_KEY, imgUri);
        bundle.putParcelable(URI_INSTANCE_STATE_KEY, imgUri);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        loadProfile();
//        loadSnap();
    }

    @Override
    public void onResume(){
        super.onResume();
        // Load saved profile data
        loadProfile();
        // Load existing photo
//         loadSnap();
    }

    @Override
    public void onPause(){
        super.onPause();
        // Save existing editTexts
        saveProfile();
        // Save existing photo
         saveSnap();
    }

    private void loadProfile() {
        // Retrieve shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("ProfileData", Context.MODE_PRIVATE);

        // Get Data
        String name = sharedPreferences.getString("name", "");
        String email = sharedPreferences.getString("email", "");
        String phone = sharedPreferences.getString("phone", "");
        int class_year = sharedPreferences.getInt("class", 0);
        String major = sharedPreferences.getString("major", "");

        // Call corresponding text boxes
        EditText edit_name = findViewById(R.id.edit_name);
        EditText edit_email = findViewById(R.id.edit_email);
        EditText edit_phone = findViewById(R.id.edit_phone);
        EditText edit_class = findViewById(R.id.edit_class);
        EditText edit_major = findViewById(R.id.edit_major);

        // Setting the saved data in the EditTexts
        edit_name.setText(name);
        edit_email.setText(email);
        edit_phone.setText(phone);
        edit_class.setText(String.valueOf(class_year));
        edit_major.setText(major);


        String gender = sharedPreferences.getString("gender", "");
        RadioButton selectedGender = null;

        // Check which radio button was clicked before
        switch(gender) {
            case "@string/male":
                selectedGender = findViewById(R.id.male);
                selectedGender.setChecked(true);
                break;
            case "@string/female":
                selectedGender = findViewById(R.id.female);
                selectedGender.setChecked(true);
                break;
        }
    }

    private void saveProfile() {
        // Edit stored preferences
        SharedPreferences sharedPreferences = getSharedPreferences("ProfileData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // redundant code should call/ declare
        // Call corresponding text boxes
        EditText edit_name = findViewById(R.id.edit_name);
        EditText edit_email = findViewById(R.id.edit_email);
        EditText edit_phone = findViewById(R.id.edit_phone);
        EditText edit_class = findViewById(R.id.edit_class);
        EditText edit_major = findViewById(R.id.edit_major);

        // Store values
        editor.putString("name", edit_name.getText().toString());
        editor.putString("email", edit_email.getText().toString());
        editor.putString("phone", String.valueOf(edit_phone.getText()));
        editor.putInt("class", Integer.parseInt(edit_class.getText().toString()));
        editor.putString("major", edit_major.getText().toString());

        // Commit changes
        editor.apply();
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        // Edit stored preferences
        SharedPreferences sharedPreferences = getSharedPreferences("ProfileData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.male:
                if (checked)
                    editor.putString("gender", "@string/male");
                break;
            case R.id.female:
                if (checked)
                    editor.putString("gender", "@string/female");
                break;
        }
        // Commit changes
        editor.apply();
    }

    public void onSaveButtonClicked(View view) {
        // Save snap
        saveSnap();

        // From Android Developers Guide
        CharSequence text = "Application saved.";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(this, text, duration);
        toast.show();

        // close activity
        finish();
    }

    public void onCancelButtonClicked(View view) {
        // close activity
        finish();
    }


}
