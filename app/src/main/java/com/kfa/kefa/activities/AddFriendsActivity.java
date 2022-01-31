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


public class AddFriendsActivity extends AppCompatActivity implements FriendAdapter.OnUserListener{
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
        setContentView(R.layout.activity_add_friends);
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
        button_friendTAG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.getUserTAG()!= null){
                friendTAG = editText_friendTAG.getText().toString();
                if (check_editText(editText_friendTAG)) {
                    get_friendID(friendTAG, db);
                }}
                else{
                    Toast.makeText(getApplicationContext(),"Veuillez définir un @Tag d'utilisateur avant d'ajouter des amis", Toast.LENGTH_SHORT).show();
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

        editText_friendTAG.addTextChangedListener(textWatcher(button_friendTAG));

    }
    //The two methods to add friend: 1 get the userID from the userTAG; 2 send a friend request thanks to the userID
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
        System.out.println("friendsID:" + friendID);
        System.out.println("userID:" + userID);
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
