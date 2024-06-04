package com.pattern.acquaintances.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;

public class ChatManager {
    private String logTag = "Chat";
    private FirebaseAuth auth;
    private FirebaseDatabase dataBase;
    private DatabaseReference chatRef;
    private String selfUid;
    private String otherUid;
    private boolean isFirst;
    private long newestMsgDate;
    private ConcurrentLinkedDeque<Long> datesReadList = new ConcurrentLinkedDeque<>();
    private int datesReadLimit = 30;
    private Function<Message, Void> onNewMsg;
    private Function<Message, Void> onLastMsg;
    public ChatManager(User user, Function<Message, Void> onNewMsg_, Function<Message, Void> onLastMsg_){
        onNewMsg = onNewMsg_;
        onLastMsg = onLastMsg_;
        auth = FirebaseAuth.getInstance();
        dataBase = FirebaseDatabase.getInstance();
        newestMsgDate = System.currentTimeMillis();
        if (auth.getCurrentUser() == null){
            throw new RuntimeException("User is not signed in");
        }
        selfUid = auth.getCurrentUser().getUid();
        String firstUid = selfUid;
        otherUid = user.getUid();
        String secondUid = otherUid;
        isFirst = true;
        if (firstUid.compareTo(secondUid) < 0){
            String temp = firstUid;
            firstUid = secondUid;
            secondUid = temp;
            isFirst = false;
        }
        chatRef = dataBase.getReference().child("chats").child(firstUid).child(secondUid);
        addOnChildListener();
        Log.i(logTag, "Chat with " + user.getUid());
    }
    public void sendMsg(String msgVal){
        long date = System.currentTimeMillis();
        Log.i(logTag, "sending msg " + String.valueOf(date));
        DatabaseReference msgRef = chatRef.child(String.valueOf(date));
        MessageForm msg = new MessageForm(msgVal, isFirst);
        msgRef.setValue(msg);
    }
    private void addOnChildListener(){

        Log.i(logTag, "last msg date: " + String.valueOf(newestMsgDate));
        chatRef.orderByKey().endBefore(String.valueOf(newestMsgDate)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MessageForm form = snapshot.getValue(MessageForm.class);
                long date = Long.valueOf(snapshot.getKey());
                addMsg(form, date);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        chatRef.orderByKey().startAfter(String.valueOf(newestMsgDate)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MessageForm form = snapshot.getValue(MessageForm.class);
                long date = Long.valueOf(snapshot.getKey());
                addMsg(form, date);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void addMsg(MessageForm form, long date){
        String uid = chooseUid(form.isFirstUid());
        Message msg = new Message(uid, new Date(date), form.getMsg());
        if (date > newestMsgDate) {
            newestMsgDate = date;
            onNewMsg.apply(msg);
        } else {
            if (!datesReadList.contains(date)) {
                datesReadList.addFirst(date);
                onLastMsg.apply(msg);
                if (datesReadList.size() > datesReadLimit) {
                    datesReadList.pollLast();
                }
            }
        }
    }
    private String chooseUid(boolean isFirstUid) {
        if (isFirstUid) {
            if (isFirst)
                return selfUid;
            return otherUid;
        }
        if (!isFirst) {
            return selfUid;
        }
        return otherUid;
    }
}

class MessageForm{
    private String msg;
    private boolean isFirstUid;
    MessageForm(){}
    MessageForm(String msg_, Boolean isFirstUid_){
        msg = msg_;
        isFirstUid = isFirstUid_;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public boolean isFirstUid() {
        return isFirstUid;
    }
    public void setFirstUid(boolean firstUid) {
        isFirstUid = firstUid;
    }
}