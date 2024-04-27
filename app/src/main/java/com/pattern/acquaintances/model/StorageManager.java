package com.pattern.acquaintances.model;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class StorageManager {
    private String logTag = "Storage";
    private FirebaseAuth mAuth;
    private FirebaseStorage store;
    private OnCompleteListener<Uri> getProfileOnCompete = null;
    public StorageManager(){
        store = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        });
    }

    /**
     * сохраняет фото переданное в photo в качестве фото-аватарки
     * если пользователь не авторизован - ничего не происходит
     */
    public void saveProfile(Uri photo){
        String uid = mAuth.getUid();
        if (uid == null){
            Log.i(logTag, "User uid equals null/unauthorized.");
            return;
        }
        StorageReference refProfile = store.getReference("profile").child(uid);
        refProfile.putFile(photo);
        Log.i(logTag, "Saved profile photo");
    }

    /**
     * начинает task по получению url профиля,
     * устанавливает onCompleteListener переданный методом setGetProfileOnCompete
     */
    public void getProfilePhoto(){
        String uid = mAuth.getUid();
        if (uid == null){
            Log.i(logTag, "User uid equals null/unauthorized.");
            return;
        }
        StorageReference refProfile = store.getReference("profile").child(uid);
        if (getProfileOnCompete != null){
            refProfile.getDownloadUrl().addOnCompleteListener(getProfileOnCompete);
        } else {
            refProfile.getDownloadUrl();
        }
        Log.i(logTag, "Getting user profile photo");
    }

    /**
     * устанавливает onCompleteListener получения фото профиля
     */
    public void setGetProfileOnCompete(OnCompleteListener<Uri> getProfileOnCompete) {
        this.getProfileOnCompete = getProfileOnCompete;
    }
}
