package com.pattern.acquaintances.model;

import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pattern.acquaintances.MainActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileSystem;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.LogRecord;

public class MatchesHandler {
    String logTag = "MatchesHandler";
    private FirebaseAuth mAuth;
    private FirebaseDatabase dataBase;
    private FirebaseStorage store;
    private DatabaseReference usersRef;
    private StorageReference profilesRef;
    private File filesDir;
    private File uidFile;
    private String userId;
    private Deque<String> idsDeque = new ConcurrentLinkedDeque<String>();
    private Deque<User> usersDeque = new ConcurrentLinkedDeque<User>();
    private AtomicBoolean isGettingNewAccounts = new AtomicBoolean(false);
    private int loadIdsNum = 5;

    // из mainActivity передавать в качестве параметра результат функции getFilesDir
    public MatchesHandler(File filesDir_){
        filesDir = filesDir_;
        uidFile = new File(filesDir,"lastUid.conf");
        try {
            if (uidFile.createNewFile()) {
                Log.i(logTag, "UID file created");
                saveNewUid(UUID.randomUUID().toString().replace("-", "").substring(0, 28));
            } else{
                Log.i(logTag, "UID file existed");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        dataBase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        store = FirebaseStorage.getInstance();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null){
                    userId = firebaseAuth.getUid();
                }
            }
        });
        usersRef = dataBase.getReference("users");
        profilesRef = store.getReference("profile");
        runFillIdDeque();
    }
    public User getNextUser(){
        User nextUser = new User();
        if (!idsDeque.isEmpty()) {
            idsDeque.pollFirst();
            nextUser = usersDeque.pollFirst();
        } else {
            nextUser = null;
            Log.e(logTag, "No user saved in deque");
        }
        runFillIdDeque();
        return nextUser;
    }
    private void runFillIdDeque(){
        if (isGettingNewAccounts.compareAndSet(false, true)) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    fillIdDeque();
                }
            };
            r.run();
        }
    }
    private void fillIdDeque(){
        if (idsDeque.size() < loadIdsNum) {
            addIdToQueue();
        }
        else {
            //for (String id: idsDeque){
            //    Log.i(logTag, "Deque contains: " + id);
            //}
            isGettingNewAccounts.set(false);
        }
    }
    private void addIdToQueue(){
        Query q = usersRef.orderByKey().startAfter(getNextUID()).limitToFirst(1);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        getUserData(child);
                    }
                } else {
                    Log.i(logTag, "Last user met starting again");
                    saveNewUid("0");
                    fillIdDeque();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i(logTag, error.getMessage());
            }
        });
    }
    private void getUserData(DataSnapshot user){
        String newUid = user.getKey();
        Account newAcc = user.getValue(Account.class);
        User newUser = new User();
        newUser.setUid(newUid);
        newUser.setAccount(newAcc);
        if (!newUid.equals(userId) && !idsDeque.contains(newUid)) {
            StorageReference profileRef = profilesRef.child(newUid);
            profileRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri result = task.getResult();
                        Log.i(logTag, "Got user profile photo" + result);
                        newUser.setProfile(result);
                    } else {
                        Log.i(logTag, "Couldn't find profile");
                    }
                    idsDeque.add(newUid);
                    usersDeque.add(newUser);
                    Log.i(logTag, "Add new user to deque: " + newAcc.getLastName() + " " + newAcc.getFirstName());
                    fillIdDeque();
                }
            });
            saveNewUid(newUid);
        } else {
            saveNewUid(newUid);
            fillIdDeque();
        }
    }
    private String getNextUID(){
        String uid;
        try {
            Scanner scanner = new Scanner(uidFile);
            uid = scanner.nextLine();
            scanner.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return uid;
    }
    private void saveNewUid(String uid){
        //Log.i(logTag, "New saved last id: " + uid);
        FileWriter writer = null;
        try {
            writer = new FileWriter(uidFile);
            writer.write(uid);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
