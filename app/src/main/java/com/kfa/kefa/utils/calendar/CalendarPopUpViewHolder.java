package com.kfa.kefa.utils.calendar;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kfa.kefa.R;
import com.kfa.kefa.utils.calendar.calendarfromevent.CalendarFromEventPopUpAdapter;

public class CalendarPopUpViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    public final TextView textView_dayOfMonth;
    private final CalendarFromEventPopUpAdapter.OnItemListener onItemListener;
    public CalendarPopUpViewHolder(@NonNull View itemView, CalendarFromEventPopUpAdapter.OnItemListener onItemListener)
    {
        super(itemView);
        textView_dayOfMonth = itemView.findViewById(R.id.calendar_item_textView);
        this.onItemListener = onItemListener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        int position = getAbsoluteAdapterPosition();
        onItemListener.onItemClick(position);
    }


    public void whiteTheCase(){
        textView_dayOfMonth.setTextColor(Color.parseColor("#EEF0F5"));
    }

}