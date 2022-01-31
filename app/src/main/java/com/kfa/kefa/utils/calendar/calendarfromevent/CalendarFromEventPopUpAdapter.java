package com.kfa.kefa.utils.calendar.calendarfromevent;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.Event;
import com.kfa.kefa.utils.User;
import com.kfa.kefa.utils.calendar.CalendarPopUpItem;
import com.kfa.kefa.utils.calendar.CalendarPopUpViewHolder;
import com.kfa.kefa.utils.calendar.DateTools;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class CalendarFromEventPopUpAdapter extends RecyclerView.Adapter<CalendarPopUpViewHolder>
{
    private final ArrayList<CalendarPopUpItem> calendarItems;
    private final OnItemListener onItemListener;
    private Context context;
    private String userID;
    private FirebaseFirestore db;
    private Event event;
    private int month,year;
    private View popUpView;
    private ImageView imageViewFavorite;

    public CalendarFromEventPopUpAdapter(Context context, ArrayList<CalendarPopUpItem> calendarItems,
                                         OnItemListener onItemListener, String userID, FirebaseFirestore db,
                                         Event event, int month, int year, View popUpView, ImageView imageViewFavorite)
    {
        this.calendarItems = calendarItems;
        this.onItemListener = onItemListener;
        this.context = context;
        this.userID = userID;
        this.db = db;
        this.event = event;
        this.month = month;
        this.year = year;
        this.popUpView = popUpView;
        this.imageViewFavorite = imageViewFavorite;
    }

    @NonNull
    @Override
    public CalendarPopUpViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_item_view, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        return new CalendarPopUpViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarPopUpViewHolder holder, int position)
    {
        if (calendarItems.get(position).getDayOfMonth()!=0){
            int dayOfMonth = calendarItems.get(position).getDayOfMonth();
            holder.textView_dayOfMonth.setText(String.valueOf(dayOfMonth));
        if (calendarItems.get(position).isTheEventOnThisDate()){
            holder.textView_dayOfMonth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = holder.getAbsoluteAdapterPosition();
                    int dayOfMonth = calendarItems.get(pos).getDayOfMonth();
                    System.out.println("month: " + Integer.toString(month));
                    System.out.println("pos!: " + Integer.toString(pos));
                    String date =Integer.toString(month);
                    if (date.length() == 1){
                        date = "0".concat(date);
                    }
                    date = date.concat("/");
                    date = date.concat(Integer.toString(year));
                    LocalDate localDate = DateTools.getLocalDateFromDDMMYYYY(date,dayOfMonth);
                    Timestamp timestamp = DateTools.getTimeStampFromLocalDate(localDate);
                    event.setUserInterestedDay(dayOfMonth);
                    event.setUserInterestedDate(date);
                    event.setUserInterested(true);
                    event.like_event(db,userID,timestamp);
                    popUpView.setVisibility(View.GONE);
                    event.makeFavorite(context,imageViewFavorite);
                }
            });
        }
        else {
            holder.whiteTheCase();
        }
        }
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
        void onItemClick(int position);
    }
}