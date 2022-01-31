package com.kfa.kefa.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.User;
import com.kfa.kefa.utils.user_adapter.FriendAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class FriendRequestsActivity extends AppCompatActivity implements FriendAdapter.OnUserListener{
    private FirebaseFirestore db;
    private TextView button_userTAG,button_friendTAG,button_logout;
    private EditText editText_userTAG,editText_friendTAG;
    private FirebaseAuth firebaseAuth;
    private String userID,userTAG,friendTAG,friendID;
    private ArrayList<User> list_user,friend_list = new ArrayList<>();
    private RecyclerView recyclerView_friend_request,recyclerView_friend;
    private User user;
    private ImageView buttonImageView_go_to_mainActivity, imageView_refresh;
    private int color_gold,color_light;
    private Toast toast;
    private final int userTAG_length = 20;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        //Get resources
        buttonImageView_go_to_mainActivity = findViewById(R.id.settings_return_to_mainActivity);
        button_friendTAG = findViewById(R.id.settings_button_addFriend);
        button_userTAG = findViewById(R.id.settings_button_userTAG);
        button_logout = findViewById(R.id.settings_button_logout);
        editText_userTAG = findViewById(R.id.settings_userTAG);
        editText_friendTAG = findViewById(R.id.settings_friendTAG);
        recyclerView_friend_request = findViewById(R.id.recyclerView_friend_request);
        recyclerView_friend = findViewById(R.id.recyclerView_friend_list);
        imageView_refresh = findViewById(R.id.settings_imageView_refresh);


        //Get the colors
        color_gold = ContextCompat.getColor(getApplicationContext(), R.color.colorGold);
        color_light = ContextCompat.getColor(getApplicationContext(), R.color.colorLightShadow);
        //get data from the intent
        user = getIntent().getParcelableExtra("user");
        userID =user.getUserID();
        friend_list = getIntent().getParcelableArrayListExtra("friendList");

        //Instantiate Database
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Set the clickListener on the buttons
        buttonImageView_go_to_mainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.putExtra("user",(Parcelable) user);
                intent.putParcelableArrayListExtra("friendList",friend_list);
                startActivity(intent);
                finish();
            }
        });


        imageView_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh(db,userID);
            }
        });

        //Call functions
        get_friend_request_list(db,userID);
    }


    //Get a list of the friend request
    public void get_friend_request_list(FirebaseFirestore db, String userID){
        list_user = new ArrayList<>();
        db.collection("relations").document(userID).collection("requests").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        list_user.add(document.toObject(User.class));
                    }
                    updateRecyclerFriends_request();
                }
            }
        });
    }

    // Update RecyclerView
    private void updateRecyclerFriends_request() {
        FriendAdapter friendAdapter = new FriendAdapter(getApplicationContext(), this,user, list_user,false,db);
        recyclerView_friend_request.setAdapter(friendAdapter);
        recyclerView_friend_request.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

    }


    //Wtf is this class ?
    public void validate_friend(String userID,String friendID, FirebaseFirestore db){
        Map<String, Object> friend = new HashMap<>();

        db.collection("relations").document(userID).collection("friends").document(friendID).set(friend);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.putExtra("user",(Parcelable) user);
        intent.putParcelableArrayListExtra("friendList",friend_list);
        startActivity(intent);
        finish();
    }
    @Override
    public void onUserClick(int position, String userID, String friendID) {

    }


    public void refresh(FirebaseFirestore db,String userID){
        //load_friend(db,userID);
        get_friend_request_list(db, userID);
    }

}
