package com.kfa.kefa.utils.calendar.calendarfromstructure;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kfa.kefa.R;
import com.kfa.kefa.utils.calendar.calendarfromevent.CalendarFromEventPopUpAdapter;

public class CalendarPopUpViewHolderFromStructure extends RecyclerView.ViewHolder implements View.OnClickListener
{
    public final TextView textView_dayOfMonth;
    private final CalendarFromStructurePopUpAdapter.OnItemListener onItemListener;
    public CalendarPopUpViewHolderFromStructure(@NonNull View itemView, CalendarFromStructurePopUpAdapter.OnItemListener onItemListener)
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