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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.User;
import com.kfa.kefa.utils.user_adapter.FriendAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class UpdateProfileActivity extends AppCompatActivity implements FriendAdapter.OnUserListener {
    private FirebaseFirestore db;
    private TextView button_name, button_surname, city;
    private EditText editText_name, editText_surname;
    private FirebaseAuth firebaseAuth;
    private String userID, userTAG, friendTAG, friendID;
    private ArrayList<User> list_user, friend_list = new ArrayList<>();
    private User user;
    private ImageView buttonImageView_go_to_mainActivity;
    private int color_gold, color_light;
    private Toast toast;
    private final int maxLength = 10;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_profile);
        //get data from the intent
        user = getIntent().getParcelableExtra("user");
        userID = user.getUserID();
        friend_list = getIntent().getParcelableArrayListExtra("friendList");

        //Get resources
        buttonImageView_go_to_mainActivity = findViewById(R.id.settings_return_to_mainActivity);
        button_name = findViewById(R.id.update_profile_button_name);
        button_surname = findViewById(R.id.update_profile_button_surname);
        city = findViewById(R.id.textView_city);


        editText_name = findViewById(R.id.editText_name);
        editText_surname = findViewById(R.id.editText_surname);

        //Set the name and the surname on the editText
        editText_name.setText(user.getName());
        editText_surname.setText(user.getSurname());


        //Get the colors
        color_gold = ContextCompat.getColor(getApplicationContext(), R.color.colorGold);
        color_light = ContextCompat.getColor(getApplicationContext(), R.color.colorLightShadow);


        //Instantiate Database
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Set the clickListener on the buttons
        buttonImageView_go_to_mainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("user", (Parcelable) user);
                intent.putParcelableArrayListExtra("friendList", friend_list);
                startActivity(intent);
                finish();
            }
        });
        button_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText_name.getText().toString();
                if (text.length() > 1 && text.length() <= maxLength && !text.equals(user.getName())){
                update_user(userID, db, "name", editText_name.getText().toString());}
            }
        });
        button_surname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText_surname.getText().toString();
                if (text.length() > 1 && text.length() <= maxLength && !text.equals(user.getSurname())){
                update_user(userID, db, "surname", editText_surname.getText().toString()); }
            }
        });
        city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Bientôt disponible dans d'autres villes !", Toast.LENGTH_LONG).show();
            }
        });

        editText_name.addTextChangedListener(textWatcher(button_name, "name"));
        editText_surname.addTextChangedListener(textWatcher(button_surname, "surname"));
    }


    public void update_user(String userID, FirebaseFirestore db, String field, String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(field, value);
        db.collection("users").document(userID).collection("public_data").document("public_data").set(data, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                db.collection("users").document(userID).collection("private_data").document("private_data").set(data, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {


                            db.collection("users").document(userID).set(data, SetOptions.merge());


                            if (field.equals("name")) {
                                user.setName(value);
                            }
                            if (field.equals("surname")) {
                                user.setSurname(value);
                            }
                            update_user_field_for_friends(db, field, value);

                        } else {

                        }
                    }
                });
            }
        });
    }
    public void update_user_field_for_friends(FirebaseFirestore db, String field, String value) {
        db.collectionGroup("friends").whereEqualTo("userID", userID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        documentSnapshot.getReference().update(field, value);
                    }
                    if (field.equals("name")) {
                        user.setName(value);
                        button_name.setBackgroundColor(color_light);
                        Toast.makeText(getApplicationContext(),"Nom changé", Toast.LENGTH_SHORT).show();
                    }
                    if (field.equals("surname")) {
                        user.setSurname(value);
                        button_surname.setBackgroundColor(color_light);
                        Toast.makeText(getApplicationContext(),"Prénom changé", Toast.LENGTH_SHORT).show();
                    }

                } else {


                }
            }

        });
    }


    //Check if the EditText is ok
    private boolean check_editText(EditText editText) {
        Editable text = editText.getText();
        String string = text.toString();
        if (TextUtils.isEmpty(text)) {
            editText.setError("Le texte est vide");
            return false;
        }
        if (string.length() < 5) {
            editText.setError("@userTAG trop court");
            return false;

        }
        if (string.length() > maxLength) {
            editText.setError("@userTAG trop long");
            return false;

        } else {
            String specialCharacters = " !#$%&'()*+,-./:;<=>?@[]^`{|}";
            String normalChar = "azertyuiopqsdfghjklmwxcvbn_0123456789";
            String requestedUserTAG = editText.getText().toString();

            for (int i = 0; i < requestedUserTAG.length(); i++) {
                //Checking if the input string contain any of the specified Characters
                if (!normalChar.contains(Character.toString(requestedUserTAG.charAt(i)))) {
                    editText.setError("Pour une meilleure lisibilité, veuillez ne pas rentrer de caractère spécial hormis: _");
                    return false;
                }
            }
            return true;
        }
    }


    private TextWatcher textWatcher(TextView textView, String field) {
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

                if (field.equals("name")) {
                    if (text.length() > 1 && text.length() <= maxLength && !text.equals(user.getName())) {
                        textView.setBackgroundColor(color_gold);
                    } else {
                        textView.setBackgroundColor(color_light);
                    }
                }
                if (field.equals("surname")) {
                    if (text.length() > 1 && text.length() <= maxLength && !text.equals(user.getSurname())) {
                        textView.setBackgroundColor(color_gold);
                    } else {
                        textView.setBackgroundColor(color_light);
                    }
                }


            }
        };
    }



    //Wtf is this class ?
    public void validate_friend(String userID, String friendID, FirebaseFirestore db) {
        Map<String, Object> friend = new HashMap<>();

        db.collection("relations").document(userID).collection("friends").document(friendID).set(friend);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("user", (Parcelable) user);
        intent.putParcelableArrayListExtra("friendList", friend_list);
        startActivity(intent);
        finish();
    }

    @Override
    public void onUserClick(int position, String userID, String friendID) {

    }


}
