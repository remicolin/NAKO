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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Event implements Serializable {
    private String event_name,event_description, structure_name,structure_description= "";
    private String userID,eventID = "None";
    private Map<String, Object> interested;
    private ArrayList<User> list_users_interested = new ArrayList<>();
    private HashMap<String, Timestamp> hashMap_users_timeStamp_interested = new HashMap<>();
    private Double lat,lng = 0.;
    private String geohash = "";
    private String city = "";
    //If the user is interested
    private Boolean userInterested = false;
    //Timestamp of the ending of the event
    private Timestamp timestampEnding,timestampStarting;
    private ArrayList<Object> days = new ArrayList<>();
    //Mont and Year where the User is interested
    private String userInterestedDate;
    //Day of the month the user is interested
    private Integer userInterestedDay;
    //If the event is only on one day
    private Boolean oneDayEvent;
    //Check if the event is permanent
    private Boolean permanentEvent = false;

    //Check if the event is a structure
    private Boolean structureEvent = false;
    //Booleans for long time events
    private boolean monBool = true;
    private boolean tueBool = true;
    private boolean wedBool = true;
    private boolean thuBool = true;
    private boolean friBool = true;
    private boolean satBool = true;
    private boolean sunBool = true;

    public Event(){}

    public Event(String event_name, String structure_name) {
        this.event_name = event_name;
        this.structure_name = structure_name;
    }

    Event(String event_name, String structure_name, String userID, String eventID, Double lat, Double lng,
          String geohash, Map<String, Object> interested, boolean oneDayEvent)
    {
        this.event_name = event_name;
        this.structure_name = structure_name;
        this.userID = userID;
        this.eventID = eventID;
        this.lat = lat;
        this.lng = lng;
        this.geohash = geohash;
        this.interested = interested;
        this.oneDayEvent = oneDayEvent;
    }
    Event(String event_name, String structure_name, String structure_description, String userID, String eventID, Double lat, Double lng,
          String geohash, Map<String, Object> interested, boolean oneDayEvent, String userInterestedDate,
          int userInterestedDay, boolean structureEvent, Timestamp timestampEnding)
    {
        this.event_name = event_name;
        this.structure_name = structure_name;
        this.structure_description = structure_description;
        this.userID = userID;
        this.eventID = eventID;
        this.lat = lat;
        this.lng = lng;
        this.geohash = geohash;
        this.interested = interested;
        this.oneDayEvent = oneDayEvent;
        this.structureEvent = structureEvent;
        this.userInterestedDate = userInterestedDate;
        this.userInterestedDay = userInterestedDay;
        this.timestampEnding = timestampEnding;
    }

    public String getEvent_name() {
        return event_name;
    }
    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    public String getEvent_description() {
        return event_description;
    }
    public void setEvent_description(String event_description) {
        this.event_description = event_description;
    }

    public String getStructure_name() {
        return structure_name;
    }
    public void setStructure_name(String structure_name) {
        this.structure_name = structure_name;
    }

    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getEventID() {
        return eventID;
    }
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public Map<String, Object> getInterested() {
        return interested;
    }
    public void setInterested(Map<String, Object> interested) {
        this.interested = interested;
    }

    public Double getLat() {
        return lat;
    }
    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }
    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getGeohash() {
        return geohash;
    }
    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }

    public Boolean getUserInterested() {
        return userInterested;
    }
    public void setUserInterested(Boolean userInterested) {
        this.userInterested = userInterested;
    }

    public ArrayList<Object> getDays() {
        return days;
    }
    public void setDays(ArrayList<Object> days) {
        this.days = days;
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

    public Timestamp getTimestampEnding() {
        return timestampEnding;
    }
    public void setTimestampEnding(Timestamp timestampEnding) {
        this.timestampEnding = timestampEnding;
    }

    public Timestamp getTimestampStarting() {
        return timestampStarting;
    }

    public void setTimestampStarting(Timestamp timestampStarting) {
        this.timestampStarting = timestampStarting;
    }

    public HashMap<String, Timestamp> getHashMap_users_timeStamp_interested() {
        return hashMap_users_timeStamp_interested;
    }
    public void setHashMap_users_timeStamp_interested(HashMap<String, Timestamp> hashMap_users_timeStamp_interested) {
        this.hashMap_users_timeStamp_interested = hashMap_users_timeStamp_interested;
    }

    public ArrayList<User> getList_users_interested() {
        return list_users_interested;
    }

    public void setList_users_interested(ArrayList<User> list_users_interested) {
        this.list_users_interested = list_users_interested;
    }

    public Boolean getOneDayEvent() {
        return oneDayEvent;
    }
    public void setOneDayEvent(Boolean oneDayEvent) {
        this.oneDayEvent = oneDayEvent;
    }

    public Boolean getStructureEvent() {
        return structureEvent;
    }

    public void setStructureEvent(Boolean structureEvent) {
        this.structureEvent = structureEvent;
    }

    public String getStructure_description() {
        return structure_description;
    }

    public void setStructure_description(String structure_description) {
        this.structure_description = structure_description;
    }

    public boolean getMonBool() {
        return monBool;
    }
    public void setMonBool(boolean monBool) {
        this.monBool = monBool;
    }

    public boolean getTueBool() {
        return tueBool;
    }
    public void setTueBool(boolean tueBool) {
        this.tueBool = tueBool;
    }

    public boolean getWedBool() {
        return wedBool;
    }
    public void setWedBool(boolean wedBool) {
        this.wedBool = wedBool;
    }

    public boolean getThuBool() {
        return thuBool;
    }
    public void setThuBool(boolean thuBool) {
        this.thuBool = thuBool;
    }

    public boolean getFriBool() {
        return friBool;
    }
    public void setFriBool(boolean friBool) {
        this.friBool = friBool;
    }

    public boolean getSatBool() {
        return satBool;
    }
    public void setSatBool(boolean satBool) {
        this.satBool = satBool;
    }

    public boolean getSunBool() {
        return sunBool;
    }
    public void setSunBool(boolean sunBool) {
        this.sunBool = sunBool;
    }

    public Boolean getPermanentEvent() {
        return permanentEvent;
    }
    public void setPermanentEvent(Boolean permanentEvent) {
        this.permanentEvent = permanentEvent;
    }

    //Get only the Time Stamp Form the list
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    //Check if the event is complete
    public boolean is_complete(){
        return true;
    }

    //Methods to like/unlike the event
    public void like_event(FirebaseFirestore db, String userID, Timestamp timestamp){
        String proID = this.userID;
        db.collection("structures").document(proID).collection("interested").document(eventID).update(userID, timestamp);
        db.collection("users").document(userID).collection("events_interested").document(eventID).set(this);
    }
    public void unLike_event(FirebaseFirestore db,String userID){
        String proID = this.userID;
        db.collection("structures").document(proID).collection("interested").document(eventID).update(userID,null );
        db.collection("users").document(userID).collection("events_interested").document(eventID).delete();
    }
    public void makeFavorite(Context context,ImageView imageViewFavorite){
    Drawable favorite_red = ContextCompat.getDrawable(context, R.drawable.ic_favorite_red_24dp);
    imageViewFavorite.setImageDrawable(favorite_red);
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

    public String get_which_day_of_the_week(){
        String string = "";
        if (monBool){ string = "Lundis";}
        if (tueBool){ string = "Mardis";}
        if (wedBool){ string = "Mercredis";}
        if (thuBool){ string = "Jeudis";}
        if (friBool){ string = "Vendredis";}
        if (satBool){ string = "Samedis";}
        if (sunBool){ string = "Dimanches";}

        return string;
    }
}
