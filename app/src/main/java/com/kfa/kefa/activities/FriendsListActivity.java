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


public class FriendsListActivity extends AppCompatActivity implements FriendAdapter.OnUserListener{
    private FirebaseFirestore db;
    private EditText editText_userTAG,editText_friendTAG;
    private FirebaseAuth firebaseAuth;
    private String userID,userTAG,friendTAG,friendID;
    private ArrayList<User> list_user,friend_list = new ArrayList<>();
    private RecyclerView recyclerView_friend;
    private User user;
    private ImageView buttonImageView_go_to_mainActivity, imageView_refresh;
    private int color_gold,color_light;
    private Toast toast;
    private final int userTAG_length = 20;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        //Get resources
        buttonImageView_go_to_mainActivity = findViewById(R.id.settings_return_to_mainActivity);
        editText_userTAG = findViewById(R.id.settings_userTAG);
        editText_friendTAG = findViewById(R.id.settings_friendTAG);
        recyclerView_friend = findViewById(R.id.recyclerView_friends);
        //imageView_refresh = findViewById(R.id.settings_imageView_refresh);


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


        //Call functions
        updateRecyclerFriends();
    }

    @Override
    protected void onStart() {
        super.onStart();
        load_friend(db,user.getUserID());
    }

    // Update RecyclerView
    private void updateRecyclerFriends(){
        FriendAdapter friendAdapter = new FriendAdapter(getApplicationContext(), this,user, friend_list,true,db);
        recyclerView_friend.setAdapter(friendAdapter);
        recyclerView_friend.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }
    //Check if the EditText is ok
    private boolean check_editText(EditText editText){
        Editable text = editText.getText();
        String string = text.toString();
        if (TextUtils.isEmpty(text)){
            editText.setError("Le texte est vide");
            return false;
        }
        if (string.length() < 5){
            editText.setError("@userTAG trop court");
            return false;

        }
        if (string.length() > userTAG_length){
            editText.setError("@userTAG trop long");
            return false;

        }
        else{
            return true;
        }
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

    //Get the user's friends
    public void load_friend(FirebaseFirestore db, String userID){
        friend_list = new ArrayList<>();
        db.collection("relations").document(userID).collection("friends").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        friend_list.add(document.toObject(User.class));
                    }
                    updateRecyclerFriends();
                }
                else {

                }
            }
        });
    }

    public void refresh(FirebaseFirestore db,String userID){
        //load_friend(db,userID);
        //get_friend_request_list(db, userID);
        update_user_for_friends(db,user);
    }

    public void update_user_for_friends(FirebaseFirestore db, User user){
        db.collectionGroup("friends").whereEqualTo("userID",user.getUserID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (DocumentSnapshot documentSnapshot: task.getResult()) {
                        documentSnapshot.getReference().update((Map<String, Object>) user);
                    }
                }
            }
        });
    }


}
