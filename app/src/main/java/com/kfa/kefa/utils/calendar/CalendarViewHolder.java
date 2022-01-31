package com.kfa.kefa.utils.calendar;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.kfa.kefa.R;
import com.kfa.kefa.utils.Event;

public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, EventAdapterCalendar.OnEventListener
{
    public final TextView textView_dayOfMonth;
    private final CalendarAdapter.OnItemListener onItemListener;
    public CalendarViewHolder(@NonNull View itemView, CalendarAdapter.OnItemListener onItemListener)
    {
        super(itemView);
        textView_dayOfMonth = itemView.findViewById(R.id.calendar_item_textView);
        this.onItemListener = onItemListener;
        itemView.setOnClickListener(this);
    }
    @Override
    public void onClick(View view)
    {
        onItemListener.onItemClick(getAbsoluteAdapterPosition(), (String) textView_dayOfMonth.getText());
    }

    public void one_event(Context context){
        View circle = new Circle(context,itemView,1);
        ConstraintLayout constraintLayout = itemView.findViewById(R.id.calendar_item_view_constraintLayout);
        constraintLayout.addView(circle);
    }
    public void two_event(Context context){
        View circle = new Circle(context,itemView,2);
        ConstraintLayout constraintLayout = itemView.findViewById(R.id.calendar_item_view_constraintLayout);
        constraintLayout.addView(circle);
    }

    @Override
    public void onEventClick(int position, String eventID, String proID, Event event) {

    }
}