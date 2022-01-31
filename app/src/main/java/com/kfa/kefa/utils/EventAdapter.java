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
import androidx.recyclerview.widget.RecyclerView;

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

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private String title, societyname, eventID, proID, date, bandeau, structure_description;
    private ArrayList<Event> liste;
    private Map liste_interested;
    private Context context;
    private OnEventListener onEventListener;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private StorageReference pathReference_structure;
    private StorageReference pathReference_event;
    private long mLastClickTime = System.currentTimeMillis();
    private long lastPos = -1;
    private int pos;
    private final Drawable favorite_empty;
    private final Drawable favorite_red;
    private Event event;
    private String userID;
    private FirebaseFirestore db;
    private ArrayList<User> friendList = new ArrayList<>();
    private ArrayList<String> arrayListFriendID = new ArrayList<>();
    private int color_interested;
    private PopUpCalendarFromEvent popUpCalendarFromEvent;
    private SwipingStructureLayout swipingStructureLayout;
    private HashMap<String, User> map_friends;


    @Override
    public void onViewAttachedToWindow(@NonNull EventViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    public EventAdapter(Context ct, ArrayList<Event> list, OnEventListener onEventListener,
                        String userID, FirebaseFirestore db, ArrayList<User> friendList,
                        SwipingStructureLayout swipingStructureLayout,
                        PopUpCalendarFromEvent popUpCalendarFromEvent, HashMap<String, User> map_friends,
                        ArrayList<String> list_friendID) {
        this.onEventListener = onEventListener;
        this.liste = list;
        this.context = ct;
        this.db = db;
        this.userID = userID;
        this.friendList = friendList;
        this.popUpCalendarFromEvent = popUpCalendarFromEvent;
        this.swipingStructureLayout = swipingStructureLayout;
        this.map_friends = map_friends;
        this.arrayListFriendID = list_friendID;
        //Get the favorite icons from resources
        favorite_empty = ContextCompat.getDrawable(context, R.drawable.ic_favorite_black_24dp);
        favorite_red = ContextCompat.getDrawable(context, R.drawable.ic_favorite_red_24dp);
        color_interested = ContextCompat.getColor(context, R.color.colorGold);
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.event_layout, parent, false);
        return new EventViewHolder(view, onEventListener);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        //Set the variables
        event = liste.get(position);
        title = event.getEvent_name();
        societyname = event.getStructure_name();
        eventID = event.getEventID();
        proID = event.getUserID();
        liste_interested = event.getInterested();
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

        structure_description = event.getStructure_description();
        //Set the path to access pictures of the event and of the structure
        pathReference_event = storageReference.child("structures").child(proID).child("events").child(eventID).child("event.png");
        pathReference_structure = storageReference.child("structures").child(proID).child("logo").child("logo.png");
        //Glide is an async method to load email from firebase storage path or URL
        GlideApp.with(context).load(pathReference_event).diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().transition(withCrossFade()).into(holder.imageView_event);
        GlideApp.with(context).load(pathReference_structure).diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().transition(withCrossFade()).into(holder.imageView_structure);
        //Set the view
        holder.title.setText(title);
        holder.bandeau.setText(bandeau);
        holder.structure_name.setText(societyname);
        holder.structure_description.setText(structure_description);
        //Check if the event is already liked or not and set the favorite image on the right color
        if (event.getUserInterested()) {
            holder.imageView_favorite.setImageDrawable(favorite_red);
        } else {
            holder.imageView_favorite.setImageDrawable(favorite_empty);
        }
        //Set the color if friends are interested
        HashMap<String, Timestamp> hashMap_string_timestamp = event.getHashMap_users_timeStamp_interested();
        if (!hashMap_string_timestamp.isEmpty()) {
            //Set the popup window when long click on the cardView
            PopupEventFriendList popUpClass = new PopupEventFriendList();
            //popUpClass.showPopupWindow(view);
            holder.counter.setTextColor(color_interested);
            holder.counter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popUpClass.showPopupWindow(view, liste.get(holder.getAbsoluteAdapterPosition()), context);

                }
            });
        }
        //Click on the image of the structure to have the structure layout
        holder.constraintLayout_structure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!swipingStructureLayout.getStructureDisplayed()) {
                    get_structure(liste.get(holder.getAbsoluteAdapterPosition()).getUserID());
                }
            }
        });
        holder.imageView_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!swipingStructureLayout.getStructureDisplayed()) {
                    get_structure(liste.get(holder.getAbsoluteAdapterPosition()).getUserID());
                }
            }
        });

        holder.imageView_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                event = liste.get(holder.getAbsoluteAdapterPosition());
                if (event.getUserInterested()) {
                    holder.imageView_favorite.setImageDrawable(favorite_empty);
                    event.unLike_event(db, userID);
                    event.setUserInterested(false);
                } else {
                    if (!event.getOneDayEvent() || event.getPermanentEvent()) {
                        //The event is a long term event where the user has to choose a date, we create a custom popUp calendarView
                        popUpCalendarFromEvent.initCalendarPopUp(context, event, LocalDate.now(), holder.imageView_favorite);
                    } else {
                        holder.imageView_favorite.setImageDrawable(favorite_red);
                        event.setUserInterestedDate(DateTools.getStringDateMMYYYYFromTimeStamp(event.getTimestampEnding()));
                        event.setUserInterestedDay(DateTools.getDayFromTimeStamp(event.getTimestampEnding()));
                        event.like_event(db, userID, event.getTimestampEnding());
                        event.setUserInterested(true);
                    }
                }
            }
        });
        holder.imageView_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Des informations seront bientôt disponibles", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return liste.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ScrollingTextView bandeau;
        TextView title, societyname, date, counter, structure_name, structure_description;
        OnEventListener onEventListener;
        ImageView imageView_event, imageView_structure, imageView_favorite, imageView_info;
        ConstraintLayout constraintLayout, constraintLayout_structure;

        public EventViewHolder(@NonNull View eventview, OnEventListener onEventListener) {
            super(eventview);
            //Get the view from resources
            title = eventview.findViewById(R.id.textView_event_name);
            imageView_event = eventview.findViewById(R.id.imageView_event);
            imageView_structure = eventview.findViewById(R.id.imageView_structure);
            imageView_favorite = eventview.findViewById(R.id.imageView_favorite);
            imageView_info = eventview.findViewById(R.id.event_layout_info);
            bandeau = eventview.findViewById(R.id.textView_bandeau);
            constraintLayout = eventview.findViewById(R.id.constraintLayout);
            constraintLayout_structure = eventview.findViewById(R.id.event_layout_constraint_layout_structure);
            counter = eventview.findViewById(R.id.textView_counter);
            structure_name = eventview.findViewById(R.id.textView_structure_name);
            structure_description = eventview.findViewById(R.id.structure_description);
            //Set the on click listener
            this.onEventListener = onEventListener;
            eventview.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //requestFocus active the view and make the 'bandeau' scroll
            view.requestFocus();
            //detect double tap
            pos = getAbsoluteAdapterPosition();
            event = liste.get(pos);
            long currTime = System.currentTimeMillis();
            long deltaTime = currTime - mLastClickTime;

            onEventListener.onEventClick(pos, event.getEventID(), event.getUserID(), event);
            if (liste.get(pos).getInterested() != null) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                    }
                };
                Handler handler = new Handler();
                handler.postDelayed(runnable, ViewConfiguration.getDoubleTapTimeout());
                //System.out.println("2'' pos:" + pos + " deltaTime:" + deltaTime +" double tap time out:" + ViewConfiguration.getDoubleTapTimeout());
                if (currTime - mLastClickTime < ViewConfiguration.getDoubleTapTimeout() && lastPos == pos) {
                    //double tap
                    handler.removeCallbacksAndMessages(null);
                    /*
                    proID = event.getUserID();
                    eventID = event.getEventID();
                    if (event.getUserInterested()) {
                        imageView_favorite.setImageDrawable(favorite_empty);
                        event.unLike_event(db, userID);
                        event.setUserInterested(false);
                    } else {
                        if (!event.getOneDayEvent()) {
                            //The event is a long term event where the user has to choose a date, we create a custom popUp calendarView
                            popUpCalendarFromEvent.initCalendarPopUp(context, event, LocalDate.now(), imageView_favorite);
                        } else {
                            imageView_favorite.setImageDrawable(favorite_red);
                            event.setUserInterestedDate(DateTools.getStringDateMMYYYYFromTimeStamp(event.getTimestampEnding()));
                            event.setUserInterestedDay(DateTools.getDayFromTimeStamp(event.getTimestampEnding()));
                            event.like_event(db, userID, event.getTimestampEnding());
                            event.setUserInterested(true);
                        }
                    } */
                }
                mLastClickTime = currTime;
                lastPos = pos;
            } else {
                System.out.println("get(pos) is null");
            }
        }
    }

    public interface OnEventListener {
        void onEventClick(int position, String eventID, String proID, Event event);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void get_structure(String structureID) {
        System.out.println(structureID);
        db.collection("structures").document(structureID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Structure structure = documentSnapshot.toObject(Structure.class);
                find_structure_interested(structure, arrayListFriendID);

            }
        });
    }

    public void find_structure_interested(Structure structure, ArrayList<String> list_friendID) {
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
                            if (hashMap_interested_casted.containsKey(friendID) && hashMap_interested_casted.get(friendID) != null && (hashMap_interested_casted.get(friendID).getSeconds()) - (Timestamp.now().getSeconds()) >= -3600 * 29) {
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

                    } else {
                    }
                } else {
                }
            }
        });
    }


}