package com.kfa.kefa.utils.calendar;

import com.google.firebase.Timestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.Calendar;
import java.util.Date;

public class DateTools {
    public DateTools() {
    }

    public static String nowDDMMYYYY(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date = LocalDate.now();
        return date.format(formatter);
    }
    public static String nowMMYYYY(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        LocalDate date = LocalDate.now();
        return date.format(formatter);
    }
    public static String dateDDMMYYYY(LocalDate localDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return localDate.format(formatter);
    }
    public static String dateMMYYYY(LocalDate localDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        return localDate.format(formatter);
    }

    public static Integer nowDay() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");
        LocalDate date = LocalDate.now();
        return Integer.parseInt(date.format(formatter));
    }

    public static String readDateddMM(String stringDate){
        String day = stringDate.substring(0,1);
        String month = stringDate.substring(3,4);
        return "o";
    }

    public static String getFrenchDayOfWeek(LocalDate date){
        String stringDayOfWeek = "?";
        int dayOfWeek =  date.getDayOfWeek().getValue();
        switch (dayOfWeek) {
            case 1: stringDayOfWeek = "Lundi";
                break;
            case 2: stringDayOfWeek = "Mardi";
                break;
            case 3: stringDayOfWeek = "Mercredi";
                break;
            case 4: stringDayOfWeek = "Jeudi";
                break;
            case 5: stringDayOfWeek = "Vendredi";
                break;
            case 6: stringDayOfWeek = "Samedi";
                break;
            case 7: stringDayOfWeek = "Dimanche";
                break;

        }
        return  stringDayOfWeek;
    }
    public static String getFrenchMonthOfYear(LocalDate date){
        String stringMonthOfYear = "?";
        int dayOfWeek =  date.getMonthValue();
        switch (dayOfWeek) {
            case 1: stringMonthOfYear = "Janvier";
                break;
            case 2: stringMonthOfYear = "Février";
                break;
            case 3: stringMonthOfYear = "Mars";
                break;
            case 4: stringMonthOfYear = "Avril";
                break;
            case 5: stringMonthOfYear = "Mai";
                break;
            case 6: stringMonthOfYear = "Juin";
                break;
            case 7: stringMonthOfYear = "Juillet";
                break;
            case 8: stringMonthOfYear = "Aout";
                break;
            case 9: stringMonthOfYear = "Septembre";
                break;
            case 10: stringMonthOfYear = "Octobre";
                break;
            case 11: stringMonthOfYear = "Novembre";
                break;
            case 12: stringMonthOfYear = "Décembre";
                break;
        }
        return  stringMonthOfYear;
    }

    public static LocalDate getLocalDateFromTimeStamp(Timestamp timestamp){
        return Instant.ofEpochMilli(timestamp.toDate().getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static Timestamp getTimeStampFromLocalDate(LocalDate localDate){
        long u = localDate.toEpochDay();
        u = u*24*3600;
        return new Timestamp(u,0);
    }


    public static String getBandeau(Timestamp timestamp){
        String bandeau = "";
        if (timestamp!= null) {
            LocalDate localDate = getLocalDateFromTimeStamp(timestamp);
            bandeau = getFrenchDayOfWeek(localDate);
            bandeau = bandeau.concat(" ");
            bandeau = bandeau.concat(Integer.toString(localDate.getDayOfMonth()));
            bandeau = bandeau.concat(" ");
            bandeau = bandeau.concat(getFrenchMonthOfYear(localDate));
        }
        return bandeau;
    }

    public static String getStringDateMMYYYYFromTimeStamp(Timestamp timestamp){
        LocalDate localDate = getLocalDateFromTimeStamp(timestamp);
        String date =Integer.toString(localDate.getMonthValue());
        if (date.length() == 1){
            date = "0".concat(date);
        }
        date = date.concat("/");
        date = date.concat(Integer.toString(localDate.getYear()));
        return date;
    }
    public static int getDayFromTimeStamp(Timestamp timestamp){
        LocalDate localDate = getLocalDateFromTimeStamp(timestamp);
        return localDate.getDayOfMonth();
    }

    public static LocalDate getLocalDateFromDDMMYYYY(String MMYYYY,int day ) {
        int month = Integer.parseInt(MMYYYY.substring(0,2));
        int year = Integer.parseInt(MMYYYY.substring(3));
        LocalDate localDate = LocalDate.of(year,month,day);
        return  localDate;
    }


}
