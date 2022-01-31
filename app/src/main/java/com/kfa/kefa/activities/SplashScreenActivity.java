package com.kfa.kefa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.User;

import java.util.ArrayList;

public class SplashScreenActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private int i = 0;
    private Handler hdlr = new Handler();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private User user;
    private ArrayList<User> friend_list = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        progressBar = findViewById(R.id.spashScreen_progressBar);
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar.setMax(300);
        db = FirebaseFirestore.getInstance();

    }

    @Override
    protected void onStart() {
        Intent intent_login = new Intent(getApplicationContext(), LoginActivity.class);
        super.onStart();
            i = progressBar.getProgress();
            new Thread(new Runnable() {
                public void run() {
                    while (i < 300) {
                        i += 1;
                        if (i ==120){
                            if (firebaseAuth.getCurrentUser() != null){
                                get_user(db,firebaseAuth.getCurrentUser().getUid());
                            }
                            else{
                                startActivity(intent_login);
                                finish();
                            }
                        }
                        // Update the progress bar and display the current value in text view
                        hdlr.post(new Runnable() {
                            public void run() {
                                progressBar.setProgress(i);
                            }
                        });
                        try {
                            // Sleep for 100 milliseconds to show the progress slowly.
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }).start();
    }

    public void get_user(FirebaseFirestore db, String userID){
        System.out.println(300);
        db.collection("users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    user = task.getResult().toObject(User.class);
                    load_friend(db,userID);
                }
                else {
                    System.out.println("Task: getUser: " + userID + "is not successful");
                }
            }
        });
    }


    public void load_friend(FirebaseFirestore db, String userID){
        db.collection("relations").document(userID).collection("friends").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        friend_list.add(document.toObject(User.class));
                    }
                    go_to_mainActivity();
                }
                else {

                }
            }
        });
    }
    public void go_to_mainActivity(){
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.putExtra("user", (Parcelable) user);
        System.out.println(user.getUserID() + "///" + "1");
        intent.putParcelableArrayListExtra("friendList",friend_list);
        startActivity(intent);
        finish();
    }
}


