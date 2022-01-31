package com.kfa.kefa.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.user_adapter.FriendAdapter;
import com.kfa.kefa.utils.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class SettingsActivity extends AppCompatActivity implements FriendAdapter.OnUserListener{
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
        setContentView(R.layout.activity_settings);
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
        button_userTAG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userTAG = editText_userTAG.getText().toString();
                if (check_editText(editText_userTAG)){
                    check_if_userTAG_exists(db,userTAG);
                }
            }
        });
        button_friendTAG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                friendTAG = editText_friendTAG.getText().toString();
                if (check_editText(editText_friendTAG)) {
                    get_friendID(friendTAG, db);
                }
            }
        });
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
        button_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                Intent intent = new Intent(getApplicationContext(),SplashScreenActivity.class);
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

        editText_userTAG.addTextChangedListener(textWatcher(button_userTAG));
        editText_friendTAG.addTextChangedListener(textWatcher(button_friendTAG));

        //Call functions
        get_friend_request_list(db,userID);
        updateRecyclerFriends();
    }
    //The two méthods to add friend: 1 get the userID from the userTAG; 2 send a friend request thanks to the userID
    public void get_friendID(String friendTAG, FirebaseFirestore db){
        Map<String, Object> friendRequest = new HashMap<>();
        db.collection("userTAG").document(friendTAG).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null && documentSnapshot.toObject(TagHelper.class)!=null){
                friendID = Objects.requireNonNull(documentSnapshot.toObject(TagHelper.class)).getUserID();
                add_friend(userID,friendID,db);
            }
            else {
                editText_friendTAG.setError("Cet @userTAG n'existe pas ");
                //Toast.makeText(getApplicationContext(),"Cet @userTAG n'existe pas", Toast.LENGTH_LONG).show();
                }
            }

        });


    }
    public void add_friend(String userID, String friendID, FirebaseFirestore db){
        Map<String, Object> friendRequest = new HashMap<>();
        friendRequest.put("userID",userID);
        friendRequest.put("name",user.getName());
        friendRequest.put("surname",user.getSurname());
        friendRequest.put("userTAG",user.getUserTAG());
        db.collection("relations").document(friendID).collection("requests").document(userID).set(friendRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Demande d'amis envoyée", Toast.LENGTH_LONG).show();
                    editText_friendTAG.setText("");
                }
                else {
                    Toast.makeText(getApplicationContext(),"Erreur, vérifiez votre connexion internet", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //The two methods to change the userTAG: 1 check if the userTAG exists; 2 change the userTAG
    public void check_if_userTAG_exists( FirebaseFirestore db,String userTAG){
        Map<String, Object> friend = new HashMap<>();
        db.collection("userTAG" ).document(userTAG).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "userTAG exists!");
                        editText_userTAG.setError("Cet @userTAG est déjà utilisé");
                    } else {
                        Log.d(TAG, "Setting userTAG");
                        set_userTAG(userTAG,userID,db);
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });
    }
    public void set_userTAG(String userTAG, String userID, FirebaseFirestore db){
        String pastUserTAG = user.getUserTAG();
        user.setUserTAG(userTAG);
        Map<String, Object> userTAG_doc = new HashMap<>();
        userTAG_doc.put("userTAG" , userTAG);
        userTAG_doc.put("userID" , userID);
        HashMap<String,String> publicData = new HashMap<>();
        publicData.put("userTAG",userTAG);
        db.collection("userTAG").document(userTAG).set(userTAG_doc).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                db.collection("users").document(userID).collection("public_data").document("public_data").set(publicData).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getApplicationContext(),"@userTAG changé", Toast.LENGTH_LONG).show();
                            if (pastUserTAG!=null && !pastUserTAG.equals("")){
                            db.collection("userTAG").document(pastUserTAG).delete();}
                            db.collection("users").document(userID).update("userTAG",userTAG);
                            user.setUserTAG(userTAG);
                            editText_userTAG.setText("");
                        }
                        else{

                        }
                    }
                });
            }
        });
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

    // Class to help put data on Firestore or to get the friend requests ?
    public static class TagHelper implements Serializable {
        private String userID,userTAG;

        public TagHelper(String userID, String userTAG) {
            this.userID = userID;
            this.userTAG = userTAG;
        }

        public TagHelper() {
        }

        public String getUserID() {
            return userID;
        }

        public void setUserID(String userID) {
            this.userID = userID;
        }

        public String getUserTAG() {
            return userTAG;
        }

        public void setUserTAG(String userTAG) {
            this.userTAG = userTAG;
        }

    }
    private TextWatcher textWatcher(TextView textView) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if(text.length()> 4 && text.length()<= userTAG_length){
                    textView.setBackgroundColor(color_gold);
                }
                else{
                    textView.setBackgroundColor(color_light);
                }
            }
        };
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
        //update_user_for_friends(db,user);
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
