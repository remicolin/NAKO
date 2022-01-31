package com.kfa.kefa.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.calendar.DateTools;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class Structure implements Serializable {
    private String structure_name,geohash,city,type,userID,description = "";
    private double lat,lng = 0.;
    private ArrayList<User> list_users_interested = new ArrayList<>();
    private HashMap<String, Object> interested = new HashMap<>();
    private HashMap<String, Timestamp> hashMap_users_timeStamp_interested = new HashMap<>();
    private Boolean userInterested = false;
    private ArrayList<Object> days = new ArrayList<>();
    //Mont and Year where the User is interested
    private String userInterestedDate = "";
    //Day of the month the user is interested
    private Integer userInterestedDay;
    private boolean monBool = true;
    private boolean tueBool = true;
    private boolean wedBool = true;
    private boolean thuBool = true;
    private boolean friBool = true;
    private boolean satBool = true;
    private boolean sunBool = true;

    public Structure(){}

    public Structure(String structure_name, String geohash, String city, String type, String userID, double lat, double lng) {
        this.structure_name = structure_name;
        this.geohash = geohash;
        this.city = city;
        this.type = type;
        this.lat = lat;
        this.lng = lng;
        this.userID = userID;
    }

    public String getStructure_name() {
        return structure_name;
    }
    public void setStructure_name(String structure_name) {
        this.structure_name = structure_name;
    }

    public String getGeohash() {
        return geohash;
    }
    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }
    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getUserInterested() {
        return userInterested;
    }
    public void setUserInterested(Boolean userInterested) {
        this.userInterested = userInterested;
    }


    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }

    public ArrayList<User> getList_users_interested() {
        return list_users_interested;
    }
    public void setList_users_interested(ArrayList<User> list_users_interested) {
        this.list_users_interested = list_users_interested;
    }

    public HashMap<String, Timestamp> getHashMap_users_timeStamp_interested() {
        return hashMap_users_timeStamp_interested;
    }
    public void setHashMap_users_timeStamp_interested(HashMap<String, Timestamp> hashMap_users_timeStamp_interested) {
        this.hashMap_users_timeStamp_interested = hashMap_users_timeStamp_interested;
    }

    public HashMap<String, Object> getInterested() {
        return interested;
    }
    public void setInterested(HashMap<String, Object> interested) {
        this.interested = interested;
    }

    public ArrayList<Object> getDays() {
        return days;
    }

    public void setDays(ArrayList<Object> days) {
        this.days = days;
    }

    public ArrayList<Timestamp> getOnlyTimestampDays(){
        ArrayList <Timestamp>timestampDays = new ArrayList<>();
        for ( Object day  : days){
            try {
                timestampDays.add((Timestamp) day);
            }
            catch (ClassCastException e){
                System.out.println("exeption casting");
            }
        }
        return  timestampDays;
    }

    public String getUserInterestedDate() {
        return userInterestedDate;
    }

    public void setUserInterestedDate(String userInterestedDate) {
        this.userInterestedDate = userInterestedDate;
    }

    public Integer getUserInterestedDay() {
        return userInterestedDay;
    }

    public void setUserInterestedDay(Integer userInterestedDay) {
        this.userInterestedDay = userInterestedDay;
    }

    //Check if the structure is open on the day of week
    public Boolean getMonBool() {
        return monBool;
    }
    public void setMonBool(Boolean monBool) {
        this.monBool = monBool;
    }
    public Boolean getTueBool() {
        return tueBool;
    }
    public void setTueBool(Boolean tueBool) {
        this.tueBool = tueBool;
    }
    public Boolean getWedBool() {
        return wedBool;
    }
    public void setWedBool(Boolean wedBool) {
        this.wedBool = wedBool;
    }
    public Boolean getThuBool() {
        return thuBool;
    }
    public void setThuBool(Boolean thuBool) {
        this.thuBool = thuBool;
    }
    public Boolean getFriBool() {
        return friBool;
    }
    public void setFriBool(Boolean friBool) {
        this.friBool = friBool;
    }
    public Boolean getSatBool() {
        return satBool;
    }
    public void setSatBool(Boolean satBool) {
        this.satBool = satBool;
    }
    public Boolean getSunBool() {
        return sunBool;
    }
    public void setSunBool(Boolean sunBool) {
        this.sunBool = sunBool;
    }



    public boolean get_boolean_from_day_of_week(int i ){
        switch (i){
            case 1:
                return this.monBool;
            case 2:
                return this.tueBool;
            case 3:
                return this.wedBool;
            case 4:
                return this.thuBool;
            case 5:
                return this.friBool;
            case 6:
                return this.satBool;
            case 7:
                return this.sunBool;
        }
        return false;
    }


    //Methods to like/unlike the structure
    public void like_structure(FirebaseFirestore db, String userID, Timestamp timestamp){

        System.out.println("day:" + this.getUserInterestedDay());
        System.out.println("date:" + this.getUserInterestedDate());
        String proID = this.userID;
        System.out.println(proID +"///" + userID);
        db.collection("structures").document(proID).collection("structure_interested").document(proID).update(userID, timestamp);
        db.collection("users").document(userID).collection("events_interested").document(proID).set(castToEvent());
    }
    public void unLike_structure(FirebaseFirestore db,String userID){
        String proID = this.userID;
        db.collection("structures").document(proID).collection("structure_interested").document(proID).update(userID,null );
        db.collection("users").document(userID).collection("events_interested").document(proID).delete();
    }
    public void makeFavorite(Context context, ImageView imageViewFavorite){
        Drawable drawable_go = ContextCompat.getDrawable(context, R.drawable.ic_sharp_celebration_24_gold);
        imageViewFavorite.setImageDrawable(drawable_go);
    }

    public Event castToEvent(){
        LocalDate localDate = DateTools.getLocalDateFromDDMMYYYY(userInterestedDate,userInterestedDay);
        Timestamp timestamp = DateTools.getTimeStampFromLocalDate(localDate);
        Event event = new Event("Vous allez à ce lieu",structure_name,description,userID,userID,lat,lng,geohash,interested,false,userInterestedDate,userInterestedDay,true,timestamp);

        return  event;
    }
}
