package com.kfa.kefa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.User;
import com.kfa.kefa.utils.user_adapter.FriendAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ContactActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_contact);
        //Get resources
        buttonImageView_go_to_mainActivity = findViewById(R.id.settings_return_to_mainActivity);




        //Get the colors
        color_gold = ContextCompat.getColor(getApplicationContext(), R.color.colorGold);
        color_light = ContextCompat.getColor(getApplicationContext(), R.color.colorLightShadow);
        //get data from the intent
        user = getIntent().getParcelableExtra("user");
        userID =user.getUserID();
        friend_list = getIntent().getParcelableArrayListExtra("friendList");

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



}
