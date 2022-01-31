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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private Button button;
    private CardView cardView_google_logIn;
    private TextView textView_go_to_register;
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private String email,password;
    private EditText editText_email,editText_password;
    private String personGivenName,personFamilyName;
    private User user;
    private final ArrayList<User> friend_list = new ArrayList<>();
    private int color_interested,color_light;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Set the colors
        color_interested = ContextCompat.getColor(getApplicationContext(), R.color.colorGold);
        color_light = ContextCompat.getColor(getApplicationContext(), R.color.colorLightShadow);
        //Set up the layout
        button = findViewById(R.id.login_button);
        textView_go_to_register = findViewById(R.id.login_textView_go_to_register);
        cardView_google_logIn = findViewById(R.id.activity_login_cardView_google);
        editText_email = findViewById(R.id.login_email);
        editText_password = findViewById(R.id.login_password);
        //Initialize the database
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        //Set the click listener on the different buttons
        cardView_google_logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        textView_go_to_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
                finish();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = editText_email.getText().toString();
                password = editText_password.getText().toString();
                if (TextUtils.isEmpty(email)){
                    editText_email.setError("Email is required");
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    editText_password.setError("Password is required");
                    return;
                }
                else {
                    login();
                }
            }
        });

        editText_email.addTextChangedListener(textWatcher());
        editText_password.addTextChangedListener(textWatcher());
        //Initialize Google Request
        createRequest();
    }

    //Google Login Stuff
    //Initialize Google Request
    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    //When the Google Button is pressed
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    //Get the result of the Google SignIn intent
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
}
    //Login on Firebase from Google Auth Token
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        personGivenName = acct.getGivenName();
        personFamilyName = acct.getFamilyName();

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("name",personGivenName);
                            map.put("surname",personFamilyName);
                            map.put("city","TOULOUSE");
                            String userID = firebaseAuth.getCurrentUser().getUid();
                            db.collection("users").document(userID).set(map,SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    get_user(db,userID);
                                }
                            });
                            // Sign in success, update UI with the signed-in user's information


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    //Login on Firebase with Firebase auth
    private void login(){
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    String userID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
                    get_user(db,userID);
                }
                else{
                    Toast.makeText(getApplicationContext(),"email ou mot de passe invalide",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Once Logged
    //Get user's Data
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
    //Get the user's friends
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
    //Got to the main activity once everything is done
    public void go_to_mainActivity(){
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.putExtra("user",(Parcelable) user);
        intent.putExtra("friendList",friend_list);
        System.out.println(user.getUserTAG());
        startActivity(intent);
        finish();
    }

    // Trash
    public void create_user(){
        System.out.println(400);
        db.collection("users").document(user.getUserID()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    System.out.println("user created successfully");
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.putExtra("user",(Parcelable) user);
                    startActivity(intent);
                }
                else {
                    System.out.println("user created error");
                }
            }
        });
    }

    public void get_user_new(FirebaseFirestore db, String userID){
        System.out.println(300);
        db.collection("users").document(userID).collection("private_data").document("private_data").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
        String email = editText_email.getText().toString();
        String password = editText_password.getText().toString();
        Boolean allchecked = true;
        allchecked = allchecked && email.contains(".") && email.contains("@");
        allchecked = allchecked && password.length() > 0 ;
        System.out.println(allchecked);
        if (allchecked) {
            button.setBackgroundColor(color_interested);
        } else {
            button.setBackgroundColor(color_light);

        }
    }

}
