package com.kfa.kefa.utils.calendar;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.SwipingStructureLayout;
import com.kfa.kefa.utils.User;
import com.kfa.kefa.utils.ViewPagerAdapterEventFromCalendar;
import com.kfa.kefa.utils.calendar.calendarfromevent.PopUpCalendarFromEvent;

import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder>
{
    private final ArrayList<CalendarItem> calendarItems;
    private final OnItemListener onItemListener;
    private Context context;
    private String userID;
    private FirebaseFirestore db;
    private ArrayList<User> friendList;
    private View structureLayout;
    private View eventPopUpView;
    private PopUpCalendarFromEvent popUpCalendarFromEvent;
    private SwipingStructureLayout swipingStructureLayout;

    public CalendarAdapter(Context context,ArrayList<CalendarItem> calendarItems,
                           OnItemListener onItemListener,String userID,FirebaseFirestore db,
                           ArrayList<User> friendList,View structureLayout, View eventPopUpView, PopUpCalendarFromEvent popUpCalendarFromEvent, SwipingStructureLayout swipingStructureLayout)
    {
        this.calendarItems = calendarItems;
        this.onItemListener = onItemListener;
        this.context = context;
        this.userID = userID;
        this.db = db;
        this.friendList = friendList;
        this.structureLayout = structureLayout;
        this.eventPopUpView = eventPopUpView;
        this.popUpCalendarFromEvent = popUpCalendarFromEvent;
        this.swipingStructureLayout = swipingStructureLayout;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_item_view, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position)
    {
        if (calendarItems.get(position).getDayOfMonth()!=0){
        holder.textView_dayOfMonth.setText(String.valueOf(calendarItems.get(position).getDayOfMonth()));
        if (calendarItems.get(position).getEventArrayList().size() !=0){
        holder.textView_dayOfMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAbsoluteAdapterPosition();
                eventPopUpView.setVisibility(View.VISIBLE);
                ViewPager recyclerView = eventPopUpView.findViewById(R.id.popup_events_recyclerview);
                ViewPagerAdapterEventFromCalendar eventAdapterCalendar = new ViewPagerAdapterEventFromCalendar(context,calendarItems.get(pos).getEventArrayList(),db,friendList,userID,popUpCalendarFromEvent,swipingStructureLayout);
                recyclerView.setAdapter(eventAdapterCalendar);
                recyclerView.setPadding(50,0,50,0);
                /*
                PopupEventCalendar popUpClass = new PopupEventCalendar();
                popUpClass.showPopupWindow(view,calendarItems.get(pos).getEventArrayList()
                        ,context,userID,db,friendList,structureLayout); */
            }
        }); }
        if (calendarItems.get(position).getEventArrayList().size() == 1){
            holder.one_event(context);
        }
        else if (calendarItems.get(position).getEventArrayList().size() >= 2){
            holder.two_event(context);
        }}
        else{
            holder.textView_dayOfMonth.setText("");
        }
    }

    @Override
    public int getItemCount()
    {
        return calendarItems.size();
    }

    public interface  OnItemListener
    {
        void onItemClick(int position, String dayText);

    }
}