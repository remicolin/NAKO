package com.kfa.kefa.utils.calendar.calendarfromevent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.Event;
import com.kfa.kefa.utils.calendar.CalendarPopUpItem;
import com.kfa.kefa.utils.calendar.DateTools;
import com.kfa.kefa.utils.calendar.calendarfromevent.CalendarFromEventPopUpAdapter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;

public class PopUpCalendarFromEvent implements  CalendarFromEventPopUpAdapter.OnItemListener{
    private ViewGroup root;
    private View calendarPopUpView;
    private LayoutInflater inflater;
    private LocalDate localDate = LocalDate.now();
    private ImageView previousMonth,nextMonth;
    private RecyclerView recyclerView;
    private Context context;
    private Event event;
    private String userID;
    private FirebaseFirestore db;
    private TextView textView;
    private ArrayList<CalendarPopUpItem> calendarPopUpItemArrayList = new ArrayList<>();
    private ImageView imageViewFavorite;

    public PopUpCalendarFromEvent(Context context, ViewGroup root, LayoutInflater inflater, String userID, FirebaseFirestore db) {
        this.root = root;
        this.inflater = inflater;
        this.context = context;
        this.userID = userID;
        this.db = db;
        init();
    }

    public void init(){
        calendarPopUpView = (View) inflater.inflate(R.layout.popup_calendar,root,false);
        root.addView(calendarPopUpView);
        recyclerView = calendarPopUpView.findViewById(R.id.popup_calendar_recycler);
        calendarPopUpView.setVisibility(View.GONE);
        calendarPopUpView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendarPopUpView.setVisibility(View.GONE);
            }
        });
        previousMonth = calendarPopUpView.findViewById(R.id.popup_calendar_previous);
        previousMonth.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_arrow_back_24));
        previousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                localDate = localDate.minusMonths(1);
                initCalendarPopUp(context,event,localDate,imageViewFavorite);
            }
        });
        nextMonth = calendarPopUpView.findViewById(R.id.popup_calendar_next);
        nextMonth.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_arrow_back_24));
        nextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                localDate = localDate.plusMonths(1);
                initCalendarPopUp(context,event,localDate,imageViewFavorite);
            }
        });

        textView = calendarPopUpView.findViewById(R.id.popup_calendar_textView);
    }


    public void initCalendarPopUp(Context context, Event event, LocalDate calendarLocalDate,ImageView imageViewFavorite){
        this.calendarPopUpItemArrayList = new ArrayList<>();
        this.context = context;
        this.event = event;
        this.localDate = calendarLocalDate;
        this.imageViewFavorite = imageViewFavorite;
        final LocalDate actualDate = LocalDate.now();

        ArrayList<Timestamp> arrayList = event.getOnlyTimestampDays();
        HashMap<Integer,Boolean> hashMap = new HashMap();

        //Get max days in month
        for (int i=1 ;i<=7;i+=1){
            System.out.println("the i:" + i);
            System.out.println(event.get_boolean_from_day_of_week(i));
        }

        YearMonth yearMonth = YearMonth.from(calendarLocalDate);
        int daysInMonth = yearMonth.lengthOfMonth();
        for (int i= 1;i<=daysInMonth;i+=1){
            //System.out.println("i:" + localDate.getDayOfWeek().getValue() );
            localDate = localDate.withDayOfMonth(i);
            if (event.getPermanentEvent()){

                if (this.event.get_boolean_from_day_of_week(localDate.getDayOfWeek().getValue()) && !localDate.withDayOfMonth(i).isBefore(actualDate)  ){
                    // && localDate.getMonthValue() >= actualDate.getMonthValue() && localDate.getYear() >= actualDate.getYear()
                    hashMap.put(i,true);
                    //System.out.println("i:" + localDate.getDayOfWeek().getValue() );
                }
                else {
                    hashMap.put(i, false);
                }
            }
            else{
                if (event.getTimestampStarting() != null && event.getTimestampEnding() != null) {
                    LocalDate localDate_start = DateTools.getLocalDateFromTimeStamp(event.getTimestampStarting());
                    LocalDate localDate_end = DateTools.getLocalDateFromTimeStamp(event.getTimestampEnding());
                    if (this.event.get_boolean_from_day_of_week( localDate.getDayOfWeek().getValue()) && localDate.withDayOfMonth(i).isAfter(localDate_start)&& localDate.withDayOfMonth(i).isBefore(localDate_end) && !localDate.withDayOfMonth(i).isBefore(actualDate)  ){
                        // && localDate.getMonthValue() >= actualDate.getMonthValue() && localDate.getYear() >= actualDate.getYear()
                        hashMap.put(i,true);
                        //System.out.println("i:" + localDate.getDayOfWeek().getValue() );
                    }
                    else {
                        hashMap.put(i, false);
                    }}
                else {
                    if (this.event.get_boolean_from_day_of_week(localDate.getDayOfWeek().getValue()) && !localDate.withDayOfMonth(i).isBefore(actualDate)  ){
                        // && localDate.getMonthValue() >= actualDate.getMonthValue() && localDate.getYear() >= actualDate.getYear()
                        hashMap.put(i,true);
                        //System.out.println("i:" + localDate.getDayOfWeek().getValue() );
                    }
                    else {
                        hashMap.put(i, false);
                    }
                }
            }

        }


        /*
        for (int i= 1;i<=daysInMonth;i+=1){
            hashMap.put(i,false);
        }


        /*
        for(Timestamp day : arrayList){
            LocalDate eventLocalDate = DateTools.getLocalDateFromTimeStamp(day);
            if (eventLocalDate.getMonthValue() == calendarLocalDate.getMonthValue() && eventLocalDate.getYear() ==calendarLocalDate.getYear()){
                hashMap.put(eventLocalDate.getDayOfMonth(),true);}
        } */

        for (int i = 1;i<=daysInMonth;i+=1){
            calendarPopUpItemArrayList.add(new CalendarPopUpItem(i,hashMap.get(i)));
        }
        int dayOfWeek =calendarLocalDate.withDayOfMonth(1).getDayOfWeek().getValue();
        for (int i =  0;i<dayOfWeek;i+=1){
            if (dayOfWeek != 7){
                calendarPopUpItemArrayList.add(0,new CalendarPopUpItem(0,false));}
        }
        calendarPopUpView.setVisibility(View.VISIBLE);
        CalendarFromEventPopUpAdapter calendarFromEventPopUpAdapter = new CalendarFromEventPopUpAdapter(context,calendarPopUpItemArrayList,this,userID,db,event,localDate.getMonthValue(),localDate.getYear(),calendarPopUpView,imageViewFavorite);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(context, 7);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(calendarFromEventPopUpAdapter);

        String date = localDate.getMonth().toString() + " " + localDate.getYear();
        textView.setText(date);
    }

    @Override
    public void onItemClick(int position) {
    }
}
