package com.kfa.kefa.utils.calendar;

import com.kfa.kefa.utils.Event;

import java.util.ArrayList;

public class CalendarItem {
    private int dayOfMonth;
    private int dayOfWeek;
    private ArrayList<Event> eventArrayList;

    public CalendarItem(int dayOfMonth,ArrayList<Event> eventArrayList) {
        this.dayOfMonth = dayOfMonth;
        this.eventArrayList = eventArrayList;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public ArrayList<Event> getEventArrayList() {
        return eventArrayList;
    }

    public void setEventArrayList(ArrayList<Event> eventArrayList) {
        this.eventArrayList = eventArrayList;
    }
}
