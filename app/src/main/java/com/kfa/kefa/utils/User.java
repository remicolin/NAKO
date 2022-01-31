package com.kfa.kefa.utils;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Parcelable, Serializable {
    private String name,surname,userID,userTAG,city,district = "";

    public User() {
    }

    public User(String userID, String userTAG) {
        this.userID = userID;
        this.userTAG = userTAG;
    }
    public User(String userID,String name,String city){
        this.userID = userID;
        this.name = name;
        this.city = city;
    }

    protected User(Parcel in) {
        name = in.readString();
        surname = in.readString();
        userID = in.readString();
        userTAG = in.readString();
        city = in.readString();
        district = in.readString();

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(surname);
        dest.writeString(userID);
        dest.writeString(userTAG);
        dest.writeString(city);
        dest.writeString(district);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }
    public void setSurname(String surname) {
        this.surname = surname;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }


    // Methods Firestore
    public void get_user(FirebaseFirestore db, String userID){
        db.collection("users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    User user = task.getResult().toObject(User.class);
                }
                else {
                    System.out.println("Task: getUser: " + userID + "is not successful");
                }
            }
        });
    }

    public void create_user(FirebaseFirestore db){
        db.collection("users").document(this.getUserID()).set(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    System.out.println("user created successfully");
                }
                else {
                    System.out.println("user created error");
                }
            }
        });
    }
    public void update_user(FirebaseFirestore db){
        db.collection("users").document(this.getUserID()).set(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    System.out.println("user created successfully");
                }
                else {
                    System.out.println("user created error");
                }
            }
        });
    }

    public void update_user_field(FirebaseFirestore db,String key,String value){
        db.collection("users").document(userID).update(key,value).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    System.out.println("user updated successfully");
                }
                else {
                    System.out.println("user update error");
                }
            }
        });

    }

    public void load_friend(FirebaseFirestore db, String userID){
        ArrayList<User> list = new ArrayList<>();
        db.collection("relations").document(userID).collection("friends").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful()){
                for (QueryDocumentSnapshot document : task.getResult()) {
                    list.add(document.toObject(User.class));
                }
            }
            else {

            }
            }
        });
    }

    public void accept_friend(Context context, FirebaseFirestore db, String userID, User friend, User user){
        db.collection("relations").document(userID).collection("friends").document(friend.getUserID()).set(friend).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                db.collection("relations").document(friend.getUserID()).collection("friends").document(userID).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            db.collection("relations").document(userID).collection("requests").document(friend.getUserID()).delete();
                            Toast.makeText(context,"Demande d'amis validée",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }
    public void delete_friend_request(Context context, FirebaseFirestore db, String userID, User friend, User user){
        db.collection("relations").document(userID).collection("requests").document(friend.getUserID()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(context,"Demande d'amis supprimée",Toast.LENGTH_LONG).show();
            }
        });
    }




}
