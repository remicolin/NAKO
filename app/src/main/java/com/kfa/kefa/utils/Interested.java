package com.kfa.kefa.utils;

import java.io.Serializable;

public class Interested implements Serializable {
    private String userID,structureID,eventID;
    private int interested;

    public Interested(String userID, String structureID, String eventID, int interested) {
        this.userID = userID;
        this.structureID = structureID;
        this.eventID = eventID;
        this.interested = interested;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getStructureID() {
        return structureID;
    }

    public void setStructureID(String structureID) {
        this.structureID = structureID;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public int getInterested() {
        return interested;
    }

    public void setInterested(int interested) {
        this.interested = interested;
    }
}
