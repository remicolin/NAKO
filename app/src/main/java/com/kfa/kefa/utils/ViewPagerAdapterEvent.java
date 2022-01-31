package com.kfa.kefa.utils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.calendar.DateTools;
import com.kfa.kefa.utils.calendar.calendarfromevent.PopUpCalendarFromEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class ViewPagerAdapterEvent extends PagerAdapter {
    private Context context;
    private ArrayList<StorageReference> storageReferenceArrayList;
    private ArrayList<Event> eventArrayList;
    private Event event;
    private long mLastClickTime = System.currentTimeMillis();
    private ImageView favorite;
    private Drawable favorite_black, favorite_red;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private int lastPos = -1;
    private FirebaseFirestore db;
    private String userID,proID,eventID;
    private int color_interested;
    private ArrayList<User> friendList;
    private PopUpCalendarFromEvent popUpCalendarFromEvent;


    public ViewPagerAdapterEvent(Context context, ArrayList<Event> eventArrayList, FirebaseFirestore db, ArrayList<User> friendList, String userID, PopUpCalendarFromEvent popUpCalendarFromEvent) {
        this.context = context;
        this.storageReferenceArrayList = storageReferenceArrayList;
        this.eventArrayList = eventArrayList;
        this.favorite_black = ContextCompat.getDrawable(context, R.drawable.ic_favorite_black_24dp);
        this.favorite_red = ContextCompat.getDrawable(context, R.drawable.ic_favorite_red_24dp);
        this.color_interested = ContextCompat.getColor(context, R.color.colorGold);
        this.userID = userID;
        this.db = db;
        this.friendList = friendList;
        this.popUpCalendarFromEvent = popUpCalendarFromEvent;


    }

    public ViewPagerAdapterEvent(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        System.out.println("currents size:" + eventArrayList.size());
        return eventArrayList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {

        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        TextView event_name,k,structure_name,structure_description, event_bandeau;
        ImageView imageView_event, imageView_structure, imageView_favorite,imageView_info;
        View cardView = LayoutInflater.from(context).inflate(R.layout.event_layout_map, container, false);
        Event event = eventArrayList.get(position);
        event_name = cardView.findViewById(R.id.textView_event_name);
        imageView_event = cardView.findViewById(R.id.imageView_event);
        imageView_structure = cardView.findViewById(R.id.imageView_structure);
        imageView_favorite = cardView.findViewById(R.id.imageView_favorite);
        imageView_info = cardView.findViewById(R.id.event_layout_map_imageView_info);
        event_bandeau = cardView.findViewById(R.id.textView_bandeau);
        k = cardView.findViewById(R.id.textView_counter);
        structure_name = cardView.findViewById(R.id.textView_structure_name);
        structure_description = cardView.findViewById(R.id.structure_description);
        String bandeau;
        if (event.getPermanentEvent() && event.getOneDayEvent()){
            bandeau =  "Tous les " + event.get_which_day_of_the_week() +  " · " + "Toulouse";
        }
        else if (event.getPermanentEvent() && !event.getOneDayEvent()){
            bandeau = "évènement permanant" +  " · " + "Toulouse";
        }

        else{

            String date = DateTools.getBandeau(event.getTimestampEnding());
            bandeau = date + " · " + "Toulouse";
            if (!event.getPermanentEvent() && !event.getOneDayEvent()){
                String starting_date = DateTools.getBandeau(event.getTimestampStarting());
                bandeau = starting_date.concat(" · ").concat(bandeau);
            }
        }
        event_name.setText(event.getEvent_name());
        event_bandeau.setText(bandeau);
        structure_name.setText(event.getStructure_name());
        structure_description.setText(event.getStructure_description());

        StorageReference pathReference_event = storageReference.child("structures").child(event.getUserID()).child("events").child(event.getEventID()).child("event.png");
        StorageReference pathReference_structure = storageReference.child("structures").child(event.getUserID()).child("logo").child("logo.png");
        GlideApp.with(context).load(pathReference_event).diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().transition(withCrossFade()).into(imageView_event);
        GlideApp.with(context).load(pathReference_structure).diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().transition(withCrossFade()).into(imageView_structure);

       //Check if the user is interested by the event
        if (event.getUserInterested()) {
            imageView_favorite.setImageDrawable(favorite_red);
        } else {
            imageView_favorite.setImageDrawable(favorite_black);
        }
        //Set the color if friends are interested
        HashMap<String, Timestamp> hashMap_string_timestamp = event.getHashMap_users_timeStamp_interested();
        if (!hashMap_string_timestamp.isEmpty()) {
            //Set the popup window when long click on the cardView
            PopupEventFriendList popUpClass = new PopupEventFriendList();
            //popUpClass.showPopupWindow(view);
            k.setTextColor(color_interested);
            k.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popUpClass.showPopupWindow(view, event, context);

                }
            });
        }

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.requestFocus();
                on_click(position, imageView_favorite);
            }
        });

        imageView_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Event event = eventArrayList.get(position);
                //event = eventArrayList.get(getAbsoluteAdapterPosition());
                if (event.getUserInterested()) {
                    imageView_favorite.setImageDrawable(favorite_black);
                    event.unLike_event(db,userID);
                    event.setUserInterested(false);
                }
                else {
                    if(!event.getOneDayEvent() || event.getPermanentEvent()){
                        //The event is a long term event where the user has to choose a date, we create a custom popUp calendarView
                        popUpCalendarFromEvent.initCalendarPopUp(context,event,LocalDate.now(),imageView_favorite);
                    }

                    else{
                        imageView_favorite.setImageDrawable(favorite_red);
                        event.setUserInterestedDate(DateTools.getStringDateMMYYYYFromTimeStamp(event.getTimestampEnding()));
                        event.setUserInterestedDay(DateTools.getDayFromTimeStamp(event.getTimestampEnding()));
                        event.like_event(db,userID,event.getTimestampEnding());
                        event.setUserInterested(true);}
                }
            }
        });

        imageView_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Des informations seront bientôt disponibles",Toast.LENGTH_LONG).show();
            }
        });

        container.addView(cardView, position);
        return cardView;
    }

    //Destroy item ?

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        //container.removeView((View) object);
    }


    public void on_click(int pos, ImageView imageView_favorite) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable, ViewConfiguration.getDoubleTapTimeout());
        long currTime = System.currentTimeMillis();
        if (currTime - mLastClickTime < ViewConfiguration.getDoubleTapTimeout() && lastPos == pos) {
            handler.removeCallbacksAndMessages(null);
            //double tap
            /*
            event = eventArrayList.get(pos);
            proID = eventArrayList.get(pos).getUserID();
            eventID = eventArrayList.get(pos).getEventID();

            if (event.getUserInterested()) {
                imageView_favorite.setImageDrawable(favorite_black);
                event.unLike_event(db,userID);
                event.setUserInterested(false);
            }
            else {
                if(!event.getOneDayEvent()){
                    //The event is a long term event where the user has to choose a date, we create a custom popUp calendarView
                    popUpCalendarFromEvent.initCalendarPopUp(context,event, LocalDate.now(),imageView_favorite);
                }
                else{
                    imageView_favorite.setImageDrawable(favorite_red);
                    event.like_event(db,userID,event.getTimestampEnding());
                    event.setUserInterested(true);}
            }

            /*
            if (event.getInterested().get(userID) != null ) {
                imageView_favorite.setImageDrawable(favorite_black);
                event.unLike_event(db,userID);
                event.getInterested().put(userID, null);
            } else {
                imageView_favorite.setImageDrawable(favorite_red);
                event.like_event(db,userID);
                event.getInterested().put(userID, DateTools.nowDDMMYYYY());
            } */
        }
        mLastClickTime = currTime;
        lastPos = pos;
    }
}

