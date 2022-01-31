package com.kfa.kefa.utils.calendar;

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

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.Utils;
import com.kfa.kefa.utils.ViewPagerAdapterEvent;
import com.kfa.kefa.utils.AdapterMapImages;
import com.kfa.kefa.utils.Event;
import com.kfa.kefa.utils.GlideApp;
import com.kfa.kefa.utils.PopupEventFriendList;
import com.kfa.kefa.utils.Structure;
import com.kfa.kefa.utils.User;
import com.kfa.kefa.utils.calendar.calendarfromevent.PopUpCalendarFromEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventAdapterCalendar extends RecyclerView.Adapter<EventAdapterCalendar.EventViewHolder> {
    private String title, societyname, eventID, proID, date, bandeau,structure_description;
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
    private final Drawable favorite_black;
    private final Drawable favorite_red;
    private Event event;
    private String userID;
    private FirebaseFirestore db ;
    private ArrayList<User> friendList = new ArrayList<>();
    private ArrayList<String> arrayListFriendID = new ArrayList<>();
    private int color_interested;
    private boolean isStructureLayoutDisplayed;
    private View structureLayout;
    private ViewPager viewPager_structure,viewPager_events;
    private TextView textView_structure;
    private ImageView imageView_logo;
    private ArrayList<StorageReference> storageReferenceArrayList;
    private ArrayList<Event> eventArrayList_structure;
    private ViewPagerAdapterEvent viewPagerAdapterEvent;
    private View eventPopUpView;
    private PopUpCalendarFromEvent popUpCalendarFromEvent;


    @Override
    public void onViewAttachedToWindow(@NonNull EventViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        //holder.bandeau.requestFocus();
    }

    public EventAdapterCalendar(Context ct, ArrayList<Event> list, String userID, FirebaseFirestore db,
                                ArrayList<User> friendList, View structureLayout, View eventPopUpView, PopUpCalendarFromEvent popUpCalendarFromEvent) {
        this.liste = list;
        this.context = ct;
        this.db = db;
        this.userID = userID;
        this.friendList = friendList;
        this.isStructureLayoutDisplayed = false;
        this.structureLayout = structureLayout;
        this.viewPager_structure = structureLayout.findViewById(R.id.structure_layout_viewPager_structure);
        this.textView_structure = structureLayout.findViewById(R.id.structure_layout_textView_name_structure);
        this.viewPager_events = structureLayout.findViewById(R.id.structure_layout_viewPager_events);
        this.eventPopUpView = eventPopUpView;
        this.popUpCalendarFromEvent = popUpCalendarFromEvent;
        //Get the favorite icons from resources
        favorite_black = ContextCompat.getDrawable(context, R.drawable.ic_favorite_black_24dp);
        favorite_red = ContextCompat.getDrawable(context, R.drawable.ic_favorite_red_24dp);
        color_interested = ContextCompat.getColor(context, R.color.colorGold);
        //Setting up the pageAdapter for the events
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
        date = DateTools.getBandeau(event.getTimestampEnding());
        liste_interested = event.getInterested();
        bandeau = societyname + " · " + date + " · " + "Toulouse";
        structure_description = event.getStructure_description();

        //Set the path to access pictures of the event and of the structure
        pathReference_structure = storageReference.child("structures").child(event.getUserID()).child("logo").child("logo.png");
        if (!event.getStructureEvent()){
            pathReference_event = storageReference.child("structures").child(proID).child("events").child(eventID).child("event.png");
        }
        else{
            pathReference_event = storageReference.child("structures").child(proID).child("pictures").child("structure1.png");
        }

        //Glide is an async method to load email from firebase storage path or URL
        GlideApp.with(context).load(pathReference_event).diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().transition(withCrossFade()).into(holder.imageView_event);
        GlideApp.with(context).load(pathReference_structure).diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().transition(withCrossFade()).into(holder.imageView_structure);
        //Set the view
        holder.title.setText(title);
        holder.bandeau.setText(bandeau);
        holder.structure_name.setText(societyname);
        holder.structure_description.setText(structure_description);
        //Check if the event is already liked or not and set the favorite image on the right color
       // System.out.println(event.getInterested().get(userID));
        if (event.getUserInterested()) {
            holder.imageView_favorite.setImageDrawable(favorite_red);
        } else {
            holder.imageView_favorite.setImageDrawable(favorite_black);
        }
        //Set the color if friends are interested

        if (!event.getInterested().isEmpty()) {
            holder.counter.setTextColor(color_interested);
            //Set the popup window when long click on the cardView
            holder.counter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupEventFriendList popUpClass = new PopupEventFriendList();
                    //popUpClass.showPopupWindow(view);
                    ArrayList<User> arrayList = new ArrayList<>();
                    HashMap<String, Timestamp> stringTimestampHashMap = new HashMap<>();
                    HashMap<String,Timestamp> hashMapInterestedCasted = Utils.getOnlyTimestamp(event.getInterested());
                    for (User friend : friendList) {
                        if (event.getInterested().containsKey(friend.getUserID())) {
                            arrayList.add(friend);
                            if (hashMapInterestedCasted.containsKey(friend.getUserID()) && hashMapInterestedCasted.get(friend.getUserID()) != null)
                                stringTimestampHashMap.put(friend.getUserID(),hashMapInterestedCasted.get(friend.getUserID()));

                        }
                    }
                    event.setList_users_interested(arrayList);
                    event.setHashMap_users_timeStamp_interested(stringTimestampHashMap);
                    popUpClass.showPopupWindow(view, event, context);
                }
            });
        }

        //Click on the image of the structure to have the structure layout
        /*
        holder.imageView_structure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isStructureLayoutDisplayed) {
                    //structureLayout.setVisibility(View.VISIBLE);
                    eventPopUpView.setVisibility(View.GONE);
                    structureLayout.animate().translationY(0).start();
                    isStructureLayoutDisplayed = true;
                    get_structure(liste.get(holder.getAbsoluteAdapterPosition()).getUserID());
                }
            }
        });

        structureLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStructureLayoutDisplayed){
                    structureLayout.animate().translationY(-structureLayout.getHeight()).start();
                    isStructureLayoutDisplayed = false;}
            }
        }); */
}
    @Override
    public int getItemCount() {
        return liste.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title, societyname, date, bandeau,counter,structure_name,structure_description;
        OnEventListener onEventListener;
        ImageView imageView_event, imageView_structure, imageView_favorite;
        ConstraintLayout constraintLayout;
        public EventViewHolder(@NonNull View eventview, OnEventListener onEventListener) {
            super(eventview);
            //Get the view from resources
            title = eventview.findViewById(R.id.textView_event_name);
            imageView_event = eventview.findViewById(R.id.imageView_event);
            imageView_structure = eventview.findViewById(R.id.imageView_structure);
            imageView_favorite = eventview.findViewById(R.id.imageView_favorite);
            bandeau = eventview.findViewById(R.id.textView_bandeau);
            constraintLayout = eventview.findViewById(R.id.constraintLayout);
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
            long deltaTime= currTime - mLastClickTime;
            System.out.println("pos:" + pos + " deltaTime:" + deltaTime +" double tap time out:" + ViewConfiguration.getDoubleTapTimeout());
            if (liste.get(pos).getInterested() != null) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                    }
                };
                Handler handler = new Handler();
                handler.postDelayed(runnable, ViewConfiguration.getDoubleTapTimeout());
                if (currTime - mLastClickTime < ViewConfiguration.getDoubleTapTimeout() && lastPos == pos) {
                    //double tap
                    handler.removeCallbacksAndMessages(null);
                    proID = event.getUserID();
                    eventID = event.getEventID();
                    System.out.println(event.getInterested().get(userID));
                    if (event.getInterested().get(userID) != null &&  event.getInterested().get(userID) != null) {
                        imageView_favorite.setImageDrawable(favorite_black);
                        event.unLike_event(db,userID);
                        event.getInterested().put(userID, null);
                    } else {
                        //imageView_favorite.setImageDrawable(favorite_red);
                        //event.like_event(db,userID);
                        //event.getInterested().put(userID,  DateTools.nowDDMMYYYY());
                    }
                }
                mLastClickTime = currTime;
                lastPos = pos;
            }
            else{System.out.println("get(pos) is null");}
        }
    }

    public interface OnEventListener {
        void onEventClick(int position, String eventID, String proID, Event event);
    }
    public boolean isStructureLayoutDisplayed() {
        return isStructureLayoutDisplayed;
    }
    public void setStructureLayoutDisplayed(boolean structureLayoutDisplayed) {
        isStructureLayoutDisplayed = structureLayoutDisplayed;
    }

    public void get_structure(String structureID) {
        System.out.println(structureID);
        db.collection("structures").document(structureID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Structure structure = documentSnapshot.toObject(Structure.class);
                StorageReference pathReference_structure_picture = storageReference.child("structures").child(structureID).child("structure.png");
                StorageReference pathReference_structure_picture2 = storageReference.child("structures").child(structureID).child("structure2.png");
                StorageReference pathReference_structure_picture3 = storageReference.child("structures").child(structureID).child("structure3.png");
                //Put them in a list
                ArrayList<StorageReference> storageReferenceArrayList = new ArrayList<>();
                storageReferenceArrayList.add(pathReference_structure_picture2);
                storageReferenceArrayList.add(pathReference_structure_picture3);
                //Set the images
                AdapterMapImages adapterMapImages = new AdapterMapImages(context, storageReferenceArrayList, structureID, structure);
                viewPager_structure.setAdapter(adapterMapImages);
                GlideApp.with(context).load(pathReference_structure_picture).diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().transition(withCrossFade()).into(imageView_logo);
                textView_structure.setText(structure.getStructure_name());
                get_events(structureID);
            }
        });
    }
    public void get_events(String structureID){
        storageReferenceArrayList = new ArrayList<>();
        eventArrayList_structure = new ArrayList<>();
        ArrayList<String> list_friendID = new ArrayList<>();
        assert friendList != null;
        for(User friend : friendList){
            list_friendID.add(friend.getUserID());
        }
        db.collection("structures").document(structureID).collection("events").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int size = task.getResult().size();
                    int i =0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        i+=1;
                        Event event = document.toObject(Event.class);
                        find_interested(event,list_friendID,i==size);
                    }
                }
                else {
                }
            }
        });
    }
    public void find_interested(Event event, ArrayList<String> list_friendID, boolean bl){
        HashMap<String, Object> list_friend_interested = new HashMap();
        db.collection("structures").document(event.getUserID()).collection("interested").document(event.getEventID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // Document found in the offline cache
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        Map<String, Object> hashMap_interested = new HashMap<>();
                        hashMap_interested = document.getData();
                        ArrayList<String> arrayList_interested = new ArrayList<>();
                        for (String friendID : list_friendID){
                            assert hashMap_interested != null;
                            if (hashMap_interested.containsKey(friendID)){
                                list_friend_interested.put(friendID,hashMap_interested.get(friendID));
                            }
                        }
                        assert hashMap_interested != null;
                        event.setUserInterested(hashMap_interested.containsKey(userID) &&  hashMap_interested.get(userID) != null);
                        event.setInterested(list_friend_interested);
                        eventArrayList_structure.add(event);
                        if (bl){
                            viewPagerAdapterEvent = new ViewPagerAdapterEvent(context,eventArrayList_structure,db,friendList,userID, popUpCalendarFromEvent);
                            viewPager_events.setAdapter(viewPagerAdapterEvent);
                            viewPager_events.setPadding(100,0,100,0);
                        }
                    }
                    else{
                        viewPagerAdapterEvent = new ViewPagerAdapterEvent(context,eventArrayList_structure,db,friendList,userID, popUpCalendarFromEvent);
                        viewPager_events.setAdapter(viewPagerAdapterEvent);
                        viewPager_events.setPadding(100,0,100,0);
                    }
                } else {
                    viewPagerAdapterEvent = new ViewPagerAdapterEvent(context,eventArrayList_structure,db,friendList,userID, popUpCalendarFromEvent);
                    viewPager_events.setAdapter(viewPagerAdapterEvent);
                    viewPager_events.setPadding(100,0,100,0);
                }
            }
        });
    }
}