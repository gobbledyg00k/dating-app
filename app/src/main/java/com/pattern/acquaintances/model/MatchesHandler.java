package com.pattern.acquaintances.model;

import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.LogRecord;

public class MatchesHandler {
    String logTag = "MatchesHandler";
    private FirebaseAuth mAuth;
    private FirebaseDatabase dataBase;
    private FirebaseStorage store;
    private DatabaseReference usersRef;
    private DatabaseReference likesRef;
    private StorageReference profilesRef;
    private File filesDir;
    private File uidFile;
    private String userId;
    private Deque<User> usersDeque = new ConcurrentLinkedDeque<User>();
    private Deque<String> idsDeque = new ConcurrentLinkedDeque<String>();
    private Deque<String> matchesIdsDeque = new ConcurrentLinkedDeque<String>();
    private Deque<String> likesIdsDeque = new ConcurrentLinkedDeque<String>();
    private Deque<String> likedIdsDeque = new ConcurrentLinkedDeque<String>();
    private Map<String, User> matchesMap = new ConcurrentHashMap<String, User>();
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
        usersRef = dataBase.getReference("users");
        likesRef = dataBase.getReference("likes");
        profilesRef = store.getReference("profile");
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null){
                    userId = firebaseAuth.getUid();
                    runFillIdDeque();
                    addLikesChangeListener();
                    addLikedChangeListener();
                } else {
                    userId = null;
                }
            }
        });
    }
    public User getNextUser() {
        if (userId == null) {
            Log.e(logTag, "User is not signed in");
            return null;
        }
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
    public void likeUser(User user){
        if (userId == null) {
            Log.e(logTag, "User is not signed in");
            return;
        }
        sendReaction(user, true);
    }
    public void dislikeUser(User user){
        if (userId == null) {
            Log.e(logTag, "User is not signed in");
            return;
        }
        sendReaction(user, false);
    }
    public ArrayList<User> getMatchesArray(){
        ArrayList<User> matches = new ArrayList<>();
        for (User user: matchesMap.values()){
            matches.add(user);
        }
        return  matches;
    }
    private void addLikesChangeListener(){
        likesRef.child(userId).child("myLikes").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue(boolean.class)) {
                    String uid = snapshot.getKey();
                    likesIdsDeque.add(uid);
                    Log.i(logTag, "Added new like");
                    if (likedIdsDeque.contains(uid)) {
                        addNewMatch(uid);
                    }
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String uid = snapshot.getKey();
                if (snapshot.getValue(boolean.class)) {
                    likesIdsDeque.add(uid);
                    Log.i(logTag, "Added new like");
                    if (likedIdsDeque.contains(uid)) {
                        addNewMatch(uid);
                    }
                } else {
                    likesIdsDeque.remove(uid);
                    Log.i(logTag, "Removed like");
                    removeMatch(uid);
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String uid = snapshot.getKey();
                if (snapshot.getValue(boolean.class)) {
                    likesIdsDeque.remove(uid);
                    Log.i(logTag, "Removed like");
                    removeMatch(uid);
                }
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void addLikedChangeListener(){
        likesRef.child(userId).child("liked").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue(boolean.class)) {
                    String uid = snapshot.getKey();
                    likedIdsDeque.add(uid);
                    Log.i(logTag, "Added new liked");
                    if (likesIdsDeque.contains(uid)) {
                        addNewMatch(uid);
                    }
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String uid = snapshot.getKey();
                if (snapshot.getValue(boolean.class)) {
                    likedIdsDeque.add(uid);
                    Log.i(logTag, "Added new liked");
                    if (likesIdsDeque.contains(uid)) {
                        addNewMatch(uid);
                    }
                } else {
                    likedIdsDeque.remove(uid);
                    Log.i(logTag, "Removed liked");
                    removeMatch(uid);
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String uid = snapshot.getKey();
                if (snapshot.getValue(boolean.class)) {
                    likedIdsDeque.remove(uid);
                    Log.i(logTag, "Removed liked");
                    removeMatch(uid);
                }
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void addNewMatch(String uid){
        getUserByUid(uid, new Function<User, Void>() {
            @Override
            public Void apply(User user) {
                matchesMap.put(uid, user);
                return null;
            }
        });
        Log.i(logTag, "New match: " + uid);
    }
    private void removeMatch(String uid){
        matchesIdsDeque.remove(uid);
        matchesMap.remove(uid);
        Log.i(logTag, "Removed match: " + uid);
    }
    private void sendReaction(User user, boolean isLike){
        String uid = user.getUid();
        likesRef.child(uid).child("liked").child(userId).setValue(isLike).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String userName = user.getAccount().getFirstName();
                if (task.isSuccessful()){
                    if (isLike) {
                        Log.i(logTag, "Left like for " + userName);
                    } else {
                        Log.i(logTag, "Left dislike for " + userName);
                    }
                } else {
                    Log.i(logTag, "Couldn't left like for " + userName);
                }
            }
        });
        likesRef.child(userId).child("myLikes").child(uid).setValue(isLike).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String userName = user.getAccount().getFirstName();
                if (task.isSuccessful()){
                    if (isLike) {
                        Log.i(logTag, "Saved like for " + userName);
                    } else {
                        Log.i(logTag, "Saved dislike for " + userName);
                    }
                } else {
                    Log.i(logTag, "Couldn't save like for " + userName);
                }
            }
        });
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
            getUserPhoto(newUid, new OnCompleteListener<Uri>() {
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
    private void getUserByUid(String uid, Function<User, Void> listener){
        User newUser = new User();
        newUser.setUid(uid);
        DatabaseReference userRef = usersRef.child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    newUser.setAccount(snapshot.getValue(Account.class));
                    getUserPhoto(uid, new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                newUser.setProfile(task.getResult());
                                Log.e(logTag, "Couldn't find user profile");
                            }
                            listener.apply(newUser);
                            Log.i(logTag, "Added new user to matches map");
                        }
                    });
                } else {
                    Log.i(logTag, "Couldn't find account data" + uid);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i(logTag, "Couldn't get account data" + uid);
            }
        });
    }
    private void getUserPhoto(String uid, OnCompleteListener<Uri> listener){
        StorageReference profileRef = profilesRef.child(uid);
        profileRef.getDownloadUrl().addOnCompleteListener(listener);
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
