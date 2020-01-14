package com.example.myruns1;

import androidx.appcompat.app.AppCompatActivity;

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

    public static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final String URI_INSTANCE_STATE_KEY = "photo_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        // initialize saved text preferences
        loadProfile();

        imageView = findViewById(R.id.imageProfile);

        // check if you have camera permissions
        Util.checkPermission(this);

//      if there is already a saved state
        if(savedInstanceState != null) {
            imgUri = savedInstanceState.getParcelable(URI_INSTANCE_STATE_KEY);
            Log.d("exs_onCreate", "we are in onCreate and savedInstanceState not null");
        }

        Log.d("exs_onCreate_new", "in onCreate");

        loadSnap(savedInstanceState);
    }

    // From the Android Developer Docs - Menu Bar
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
        ContentValues values = new ContentValues(1);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        imgUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        intent.putExtra("return-data", true);

//        // save URI and start camera
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    // after the camera activity returns the photo
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // as long as the photo is returned
        if (resultCode == Activity.RESULT_OK) {

            switch (requestCode) {
                // Image was taken from camera
                case REQUEST_IMAGE_CAPTURE:
                    Log.d("exs_before_crop", imgUri.getPath());
//                  Send image taken from camera for cropping
                    beginCrop(imgUri);
                    break;
////
                case Crop.REQUEST_CROP:
                    // Update image view after image crop
                    handleCrop(resultCode, data);

            }
        }
    }


    private void loadSnap(Bundle bundle) {
//
        // Load profile photo from internal storage
        if (imgUri == null) {
            // try to get from file
            try {
                FileInputStream fis = openFileInput(getString(R.string.imgFileName));
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                imageView.setImageBitmap(bitmap);
                fis.close();
                if (imgUri != null) {
                    Log.d("exs_loadTry", imgUri.getPath());
                }
                else {
                    Log.d("exs_loadTry", "was null");
                }
            } catch (IOException e) {

                e.printStackTrace();
                Log.d("exs_loadCatch", "no photo saved");
            }
        }
        else {
            Log.d("exs_onCreate_existingUri", imgUri.getPath());
            // set the image view if imgUri already exists
            imageView.setImageURI(imgUri);
        }
    }

    private void saveSnap() {

        // Save profile image into internal storage.
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        if (imgUri != null)
        Log.d("exs_saveSnap", imgUri.getPath());
        try {
            FileOutputStream fos = openFileOutput(getString(R.string.imgFileName), MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            // get path of cropped image
            imgUri = Crop.getOutput(result);
            imageView.setImageResource(0);
            imageView.setImageURI(imgUri);
            Log.d("exs_handleCrop_uri", imgUri.getPath());


        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
//        saveProfile();

        // Save the image capture uri before the activity goes into background
        if (imgUri != null) {
            Log.d("exs_onSave_newUri", imgUri.getPath());
            bundle.putParcelable(URI_INSTANCE_STATE_KEY, imgUri);
//        }
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
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
        if (class_year != 0)
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
        if (!edit_class.getText().toString().equals("")) {
            editor.putInt("class", Integer.parseInt(edit_class.getText().toString()));
        }
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
        // Save profile
        saveProfile();

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
