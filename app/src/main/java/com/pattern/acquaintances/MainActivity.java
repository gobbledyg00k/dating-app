package com.pattern.acquaintances;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.pattern.acquaintances.databinding.ActivityMainBinding;
import com.pattern.acquaintances.model.Account;
import com.pattern.acquaintances.model.AuthManager;
import com.pattern.acquaintances.model.DBManager;
import com.pattern.acquaintances.model.MatchesHandler;
import com.pattern.acquaintances.model.StorageManager;
import com.pattern.acquaintances.model.User;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    StorageManager store;
    Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DBManager db = new DBManager();
        AuthManager auth = new AuthManager();
        store = new StorageManager();
        auth.signIn("m.pomogaev@g.nsu.ru", "123123");
        super.onCreate(savedInstanceState);
        MatchesHandler handler = new MatchesHandler(getFilesDir());
        Handler h = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                User newUser = handler.getNextUser();
                if (newUser != null) {
                    Account userAccount = newUser.getAccount();
                    Log.i("MainActivity", "Got new user: " + userAccount.getLastName() + " " + userAccount.getFirstName());
                } else {
                    Log.i("MainActivity", "No user");
                }
            }
        };
        for (int i = 0; i < 30; ++i) {
            h.postDelayed(r, 5000 + i * 450);
        }

        mainHandler = new Handler();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        // register the UI widgets with their appropriate IDs
        BSelectImage = findViewById(R.id.BSelectImage);
        IVPreviewImage = findViewById(R.id.IVPreviewImage);

        // handle the Choose Image button to trigger
        // the image chooser function
        BSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });
        store.setGetProfileOnCompete(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    String result = task.getResult().toString();
                    Log.i("Storage","succses" + result);
                    new ImageDownloader(result).start();
                }
                else {
                    Log.i("Storage", "123123");
                }
            }
        });
        store.getProfilePhoto();
    }
    // One Button
    Button BSelectImage;

    // One Preview Image
    ImageView IVPreviewImage;

    // constant to compare
    // the activity result code
    int SELECT_PICTURE = 200;
    // this function is triggered when
    // the Select Image Button is clicked
    void imageChooser() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    IVPreviewImage.setImageURI(selectedImageUri);
                    store.saveProfile(selectedImageUri);
                }

            }
        }
    }

    class ImageDownloader extends Thread{
        String URL;
        Bitmap bitmap;
        ImageDownloader(String url){
            URL = url;
        }

        @Override
        public void run(){
            super.run();
            InputStream stream;
            try {
                stream = new java.net.URL(URL).openStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            bitmap = BitmapFactory.decodeStream(stream);

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    IVPreviewImage.setImageBitmap(bitmap);
                    Log.i("Storage", "Image set");
                }
            });
        }
    }
}