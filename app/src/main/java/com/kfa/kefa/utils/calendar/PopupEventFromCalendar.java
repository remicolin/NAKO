package com.kfa.kefa.utils.calendar;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.Event;
import com.kfa.kefa.utils.User;
import com.kfa.kefa.utils.user_adapter.FriendInterestedAdapter;

import java.util.ArrayList;

public class PopupEventFromCalendar {

    //PopupWindow display method

    public void showPopupWindow(final View view, ArrayList<Event> arrayList, Context context, String userID,
                                FirebaseFirestore db,ArrayList<User> friendList, View structureLayout) {


        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_events_calendar, null);
        ScrollView scrollView = new ScrollView(context);

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen

        //Initialize the elements of our window, install the handler

        /*
        RecyclerView recyclerView = popupView.findViewById(R.id.popup_events_recyclerview);
        EventAdapterCalendar eventAdapterCalendar = new EventAdapterCalendar(context,arrayList,userID,db,friendList,structureLayout);
        recyclerView.setAdapter(eventAdapterCalendar);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

*/


        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);



        //Handler for clicking on the inactive zone of the window

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //Close the window when clicked
                popupWindow.dismiss();
                return true;
            }
        });
    }

}