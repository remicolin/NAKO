package com.kfa.kefa.utils.calendar;

import com.kfa.kefa.utils.Event;

import java.util.ArrayList;

public class CalendarPopUpItem {
    private int dayOfMonth;
    private int dayOfWeek;
    private int numberOfEvent;
    private boolean isTheEventOnThisDate;

    public CalendarPopUpItem(int dayOfMonth,boolean isTheEventOnThisDate) {
        this.dayOfMonth = dayOfMonth;
        this.isTheEventOnThisDate = isTheEventOnThisDate;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public int getNumberOfEvent() {
        return numberOfEvent;
    }

    public void setNumberOfEvent(int numberOfEvent) {
        this.numberOfEvent = numberOfEvent;
    }

    public boolean isTheEventOnThisDate() {
        return isTheEventOnThisDate;
    }

    public void setTheEventOnThisDate(boolean theEventOnThisDate) {
        isTheEventOnThisDate = theEventOnThisDate;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

}
