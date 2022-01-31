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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.calendar.DateTools;
import com.kfa.kefa.utils.calendar.calendarfromevent.PopUpCalendarFromEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewPagerAdapterEventFromCalendar extends PagerAdapter {
    private Context context;
    private ArrayList<StorageReference> storageReferenceArrayList;
    private ArrayList<Event> eventArrayList;
    private Event event;
    private long mLastClickTime = System.currentTimeMillis();
    private ImageView favorite;
    private Drawable favorite_black, favorite_red,go_empty,go_full;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private int lastPos = -1;
    private FirebaseFirestore db;
    private String userID,proID,eventID;
    private int color_interested;
    private ArrayList<User> friendList;
    private PopUpCalendarFromEvent popUpCalendarFromEvent;
    private ArrayList<String> friendListID = new ArrayList<>();
    private Map<String,User> map_friends = new HashMap<>();
    private SwipingStructureLayout swipingStructureLayout;

    public ViewPagerAdapterEventFromCalendar(Context context, ArrayList<Event> eventArrayList, FirebaseFirestore db, ArrayList<User> friendList, String userID, PopUpCalendarFromEvent popUpCalendarFromEvent,SwipingStructureLayout swipingStructureLayout) {
        this.context = context;
        this.storageReferenceArrayList = storageReferenceArrayList;
        this.eventArrayList = eventArrayList;
        this.favorite_black = ContextCompat.getDrawable(context, R.drawable.ic_favorite_black_24dp);
        this.favorite_red = ContextCompat.getDrawable(context, R.drawable.ic_favorite_red_24dp);
        this.go_empty = ContextCompat.getDrawable(context, R.drawable.ic_sharp_celebration_24_white);
        this.go_full = ContextCompat.getDrawable(context, R.drawable.ic_sharp_celebration_24_gold);
        this.color_interested = ContextCompat.getColor(context, R.color.colorGold);
        this.userID = userID;
        this.db = db;
        this.friendList = friendList;
        this.popUpCalendarFromEvent = popUpCalendarFromEvent;
        for(User friend : friendList){
            this.friendListID.add(friend.getUserID());
            this.map_friends.put(friend.getUserID(),friend);
        }
        this.swipingStructureLayout = swipingStructureLayout;


    }

    public ViewPagerAdapterEventFromCalendar(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
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
        ConstraintLayout constraintLayout_structure;
        View cardView = LayoutInflater.from(context).inflate(R.layout.event_layout_calendar, container, false);
        event = eventArrayList.get(position);

        event_name = cardView.findViewById(R.id.textView_event_name);
        imageView_event = cardView.findViewById(R.id.imageView_event);
        imageView_structure = cardView.findViewById(R.id.imageView_structure);
        imageView_favorite = cardView.findViewById(R.id.imageView_favorite);
        imageView_info = cardView.findViewById(R.id.event_layout_info);
        event_bandeau = cardView.findViewById(R.id.textView_bandeau);
        k = cardView.findViewById(R.id.textView_counter);
        structure_name = cardView.findViewById(R.id.textView_structure_name);
        structure_description = cardView.findViewById(R.id.structure_description);
        constraintLayout_structure = cardView.findViewById(R.id.constraint_layout_structure);

        event_name.setText(event.getEvent_name());
        String bandeau;
        if (event.getStructureEvent()){
            String date = DateTools.getBandeau(event.getTimestampEnding());
            bandeau = date + " · " + "Toulouse";
        }

        else if (event.getPermanentEvent() && event.getOneDayEvent()){
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
        event_bandeau.setText(bandeau);
        structure_name.setText(event.getStructure_name());
        structure_description.setText(event.getStructure_description());
        StorageReference pathReference_event;
        if (event.getStructureEvent()){
            pathReference_event = storageReference.child("structures").child(event.getUserID()).child("pictures").child("structure1.png");

        }
        else{
            pathReference_event = storageReference.child("structures").child(event.getUserID()).child("events").child(event.getEventID()).child("event.png");

        }
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
                //event = eventArrayList.get(getAbsoluteAdapterPosition());
                if (event.getUserInterested()) {
                    imageView_favorite.setImageDrawable(favorite_black);
                    event.unLike_event(db,userID);
                    event.setUserInterested(false);
                }
                else {
                    if(!event.getOneDayEvent()){
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
        imageView_favorite.setVisibility(View.GONE);

        imageView_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Des informations seront bientôt disponibles",Toast.LENGTH_LONG).show();
            }
        });
        constraintLayout_structure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                get_structure(eventArrayList.get(position).getUserID());
            }
        });
        container.addView(cardView, position);
        return cardView;
    }

    //Destroy item ?

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
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
            /*
            //double tap
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
                    event.like_event(db,userID);
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

    public void get_structure(String structureID) {
        System.out.println(structureID);
        db.collection("structures").document(structureID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Structure structure =  documentSnapshot.toObject(Structure.class);
                find_structure_interested(structure,friendListID);
            }
        });
    }

    public void find_structure_interested(Structure structure, ArrayList<String> list_friendID){
        HashMap<String, Object> map_friend_interested = new HashMap();
        db.collection("structures").document(structure.getUserID()).collection("structure_interested").document(structure.getUserID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // Document found in the offline cache
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                    //UserID | Object (Timestamp)
                    Map<String, Object> map_interested = document.getData();
                    //Cast to have only timestamp
                    HashMap<String, Timestamp> hashMap_interested_casted = Utils.getOnlyTimestamp(map_interested);
                    //ArrayList which contains User of friends interested by the structure
                    ArrayList<User> list_friends_interested = new ArrayList<>();
                    for (String friendID : list_friendID) {
                        if (hashMap_interested_casted.containsKey(friendID) && hashMap_interested_casted.get(friendID) != null && (hashMap_interested_casted.get(friendID).getSeconds()) -(Timestamp.now().getSeconds())  >= -3600*29) {
                            System.out.println("A friend is interested:" + friendID);
                            map_friend_interested.put(friendID, hashMap_interested_casted.get(friendID));
                            list_friends_interested.add(map_friends.get(friendID));
                        }
                    }
                    structure.setUserInterested(hashMap_interested_casted.containsKey(userID) && (hashMap_interested_casted.get(userID)) != null);
                    structure.setList_users_interested(list_friends_interested);
                    structure.setHashMap_users_timeStamp_interested(hashMap_interested_casted);
                    swipingStructureLayout.set_layout_with_structure(structure);
                    swipingStructureLayout.translateDown();

                    }
                    else{
                    }
                } else {
                }
            }
        });
    }
}

