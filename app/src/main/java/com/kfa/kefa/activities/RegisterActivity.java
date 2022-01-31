package com.kfa.kefa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.User;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {
    private Button button;
    private EditText editText_email, editText_password, editText_password2, editText_name, editText_city;
    private String email, password, password2, name, family_name, city;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private User user;
    private int color_interested, color_light;
    private ImageView go_back_to_logIn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        color_interested = ContextCompat.getColor(getApplicationContext(), R.color.colorGold);
        color_light = ContextCompat.getColor(getApplicationContext(), R.color.colorLightShadow);
        button = findViewById(R.id.register_button);
        editText_email = findViewById(R.id.register_editText_email);
        editText_password = findViewById(R.id.register_editText_password);
        editText_password2 = findViewById(R.id.register_editText_password2);
        editText_name = findViewById(R.id.register_editText_name);
        editText_city = findViewById(R.id.register_editText_city);
        go_back_to_logIn = findViewById(R.id.settings_return_to_logIn);

        go_back_to_logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register_user();
            }
        });

        editText_name.addTextChangedListener(textWatcher());
        editText_email.addTextChangedListener(textWatcher());
        editText_password.addTextChangedListener(textWatcher());
        editText_password2.addTextChangedListener(textWatcher());
    }

    //Register the user on firebase.auth
    public void register_user() {
        email = editText_email.getText().toString();
        password = editText_password.getText().toString();
        password2 = editText_password2.getText().toString();
        name = editText_name.getText().toString();
        city = editText_city.getText().toString();

        if (check_email()) {
            System.out.println("user created");
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    String userID = task.getResult().getUser().getUid();
                    System.out.println("user created" + userID);
                    user = new User(userID, name, city);
                    create_user(db, user);
                }
            });
        }
    }

    //Create the user on Firestore
    public void create_user(FirebaseFirestore db, User user) {
        db.collection("users").document(user.getUserID()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    System.out.println("user created successfully");
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("user", (Parcelable) user);
                    ArrayList<User> friendList = new ArrayList<>();
                    intent.putParcelableArrayListExtra("friendList", friendList);
                    startActivity(intent);
                    finish();
                } else {
                    System.out.println("user created error");
                }
            }
        });
    }


    public boolean check_email() {
        if (!email.equals("") && email.contains("@") && email.contains(".")) {
            return check_password();
        } else {
            editText_email.setError("Email non valide");
            return false;
        }
    }

    public boolean check_password() {
        if (password.length() > 6) {
            return check_password2();
        } else {
            editText_password.setError("Mot de passe trop court");
            return false;
        }
    }

    public boolean check_password2() {
        if (password2.equals(password)) {
            return check_name();
        } else {
            editText_password2.setError("Les deux mots de passes ne sont pas similaires");
            return false;
        }
    }

    private boolean check_name() {
        if (name.length() > 2) {
            return true;
        } else {
            editText_name.setError("Nom trop court");
            return false;
        }
    }

    private TextWatcher textWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                check_all_the_editText();
            }
        };
    }

    private void check_all_the_editText() {
        String name = editText_name.getText().toString();
        String email = editText_email.getText().toString();
        String password = editText_password.getText().toString();
        String password_check = editText_password2.getText().toString();
        Boolean allchecked = true;
        allchecked = allchecked && name.length() > 1 && name.length() < 12;
        allchecked = allchecked && email.contains(".") && email.contains("@");
        allchecked = allchecked && password.length() > 5 && password.equals(password_check);
        System.out.println(allchecked);
        if (allchecked) {
            button.setBackgroundColor(color_interested);
        } else {
            button.setBackgroundColor(color_light);

        }
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }
}
