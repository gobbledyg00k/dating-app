package com.pattern.acquaintances.login.registration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.app.Activity;
import android.net.Uri;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.pattern.acquaintances.MainActivity;
import com.pattern.acquaintances.R;

public class PersonalDataActivity extends AppCompatActivity {

    private ImageView imageView;
    private EditText name;
    private EditText surname;
    private EditText age;
    private RadioGroup radioGroup;
    private String gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_data);

        name = findViewById(R.id.name);
        surname = findViewById(R.id.surname);
        age = findViewById(R.id.age);
        radioGroup = findViewById(R.id.radioGroup);

        imageView = findViewById(R.id.imageView);
        Button button = findViewById(R.id.add_photo);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iGallery = new Intent(Intent.ACTION_PICK);
                iGallery.setData(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                galleryActivityResultLauncher.launch(iGallery);
            }
        });
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult( //я надеюсь это работает
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        assert data != null;
                        Uri imageUri = data.getData();

                        imageView.setImageURI(imageUri); //TODO надо ее еще как-то скачать в БД
                    }
                    else {
                        Toast.makeText(PersonalDataActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    public void nextPage(View v){ //TODO обработать не введение данных
        int answerRadioButtonId = radioGroup.getCheckedRadioButtonId();
        if (answerRadioButtonId != -1) {
            RadioButton answer = findViewById(answerRadioButtonId);
            gender = answer.getText().toString();
        } else{
            //TODO обработать не выбор пола
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        System.out.println("Button clicked");
    }
}