package com.kfa.kefa.utils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.calendar.DateTools;
import com.kfa.kefa.utils.calendar.calendarfromevent.PopUpCalendarFromEvent;
import com.kfa.kefa.utils.calendar.calendarfromstructure.PopUpCalendarFromStructure;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SwipingStructureLayout {
    private Context context;
    private ViewGroup rootView;
    private LayoutInflater inflater;
    private FirebaseFirestore db;
    private View structureLayout;
    private ViewPager viewPager_structure, viewPager_events;
    private TextView structure_name, structure_description, structure_k;
    private ImageView imageView_favorite, imageView_goUp, imageView_logo, imageView_info;
    private Drawable go_empty, go_full;
    private int color_interested, color_lightShadow;
    private int height;
    private StorageReference storageReference;
    private String userID;
    private PopUpCalendarFromStructure popUpCalendarFromStructure;
    private PopUpCalendarFromEvent popUpCalendarFromEvent;
    private Boolean isStructureDisplayed = false;
    private ArrayList<User> friendList;
    private ConstraintLayout constraintLayout;
    private int lastPos = -1;
    private CardView cardView_structure_layout, cardView_gps;
    private int count = 0;
    private HashMap<String, User> map_friends = new HashMap<>();
    private int iterator_events, iterator_premanent_events = 0;


    public SwipingStructureLayout(Context context, ConstraintLayout constraintLayout,
                                  ViewGroup rootView, LayoutInflater inflater,
                                  FirebaseFirestore db, StorageReference storageReference,
                                  String userID, ArrayList<User> friendList, @Nullable CardView cardview_gps) {
        this.context = context;
        this.constraintLayout = constraintLayout;
        this.rootView = rootView;
        this.inflater = inflater;
        this.db = db;
        this.storageReference = storageReference;
        this.userID = userID;
        this.friendList = friendList;
        for (User user : friendList) {
            map_friends.put(user.getUserID(), user);
        }
        this.go_empty = ContextCompat.getDrawable(context, R.drawable.ic_sharp_celebration_24);
        this.go_full = ContextCompat.getDrawable(context, R.drawable.ic_sharp_celebration_24_gold);
        this.color_interested = ContextCompat.getColor(context, R.color.colorGold);
        this.color_lightShadow = ContextCompat.getColor(context, R.color.colorLightShadow);
        this.cardView_gps = cardview_gps;
    }

    public void initLayout() {
        structureLayout = (View) inflater.inflate(R.layout.structure_layout, constraintLayout, false);
        viewPager_structure = structureLayout.findViewById(R.id.structure_layout_viewPager_structure);
        imageView_favorite = structureLayout.findViewById(R.id.structure_layout_imageView_favorite_structure);
        structure_name = structureLayout.findViewById(R.id.structure_layout_textView_name_structure);
        structure_description = structureLayout.findViewById(R.id.structure_layout_textView_description);
        viewPager_events = structureLayout.findViewById(R.id.structure_layout_viewPager_events);
        imageView_goUp = structureLayout.findViewById(R.id.imageView_goUp);
        structure_k = structureLayout.findViewById(R.id.textView_k);
        imageView_logo = structureLayout.findViewById(R.id.structure_layout_image_view_structure);
        imageView_info = structureLayout.findViewById(R.id.imageView_info);
        cardView_structure_layout = structureLayout.findViewById(R.id.cardView_structure_layout);
        TabLayout tabLayout = (TabLayout) structureLayout.findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(viewPager_structure, true);
        constraintLayout.addView(structureLayout);
        this.height = structureLayout.getHeight();
        structureLayout.setTranslationY(-10000);

        //Set the scrollUp
        imageView_goUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translateUp();
            }
        });
        //Logo Info
        imageView_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Des informations seront bientôt disponibles", Toast.LENGTH_LONG).show();
            }
        });


        structureLayout.requestFocus();
        //Init the popUp view
        this.popUpCalendarFromStructure = new PopUpCalendarFromStructure(context, constraintLayout, inflater, userID, db);
        this.popUpCalendarFromEvent = new PopUpCalendarFromEvent(context, constraintLayout, inflater, userID, db);

    }


    public void initEvents() {

    }

    public void translateUp() {
        // cardView_structure_layout.setRadius(70);
        if (cardView_gps != null) {
            cardView_gps.setVisibility(View.VISIBLE);
        }
        structureLayout.animate().translationY(-structureLayout.getHeight()).start();
        isStructureDisplayed = false;

    }

    public void translateDown() {
        structureLayout.animate().translationY(0).start();
        isStructureDisplayed = true;
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        //  cardView_structure_layout.setRadius(0);
                    }
                },
                430
        );
    }

    public void set_layout_with_structure(Structure structure) {
        String structureID = structure.getUserID();
        StorageReference pathReference_structure_logo = storageReference.child("structures").child(structureID).child("logo").child("logo.png");
        StorageReference pathReference_structure_picture2 = storageReference.child("structures").child(structureID).child("pictures").child("structure1.png");
        StorageReference pathReference_structure_picture3 = storageReference.child("structures").child(structureID).child("pictures").child("structure2.png");


        //Set the logo
        GlideApp.with(context).load(pathReference_structure_logo).diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().transition(withCrossFade()).into(imageView_logo);

        //Put them in a list
        ArrayList<StorageReference> storageReferenceArrayList = new ArrayList<>();
        storageReferenceArrayList.add(pathReference_structure_picture2);
        storageReferenceArrayList.add(pathReference_structure_picture3);
        //Set the images
        AdapterMapImages adapterMapImages = new AdapterMapImages(context, storageReferenceArrayList, structureID, structure);
        viewPager_structure.setAdapter(adapterMapImages);
        //Change the color of the structure cardView if friends are interested and set long click listener to see them
        PopupStructureFriendList popupStructureFriendList = new PopupStructureFriendList();
        if (structure.getList_users_interested().size() > 0) {
            structure_k.setTextColor(color_interested);
            structure_k.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupStructureFriendList.showPopupWindow(view, structure, context);
                }
            });
        }
        if (structure.getList_users_interested().size() == 0) {
            structure_k.setTextColor(color_lightShadow);
            structure_k.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
        if (structure.getUserInterested()) {
            imageView_favorite.setImageDrawable(go_full);
        } else {
            imageView_favorite.setImageDrawable(go_empty);
        }
        imageView_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pressStructureFavorite(structure);
            }
        });

        // Set the Layout of the Structure

        structure_name.setText(structure.getStructure_name());
        structure_description.setText(structure.getDescription());
        find_events(structureID);
    }

    private void pressStructureFavorite(Structure structure) {
        //double tap
        String proID = structure.getUserID();
        if (structure.getUserInterested()) {
            imageView_favorite.setImageDrawable(go_empty);
            structure.unLike_structure(db, userID);
            structure.setUserInterested(false);
        } else {
            //The event is a long term event where the user has to choose a date, we create a custom popUp calendarView
            popUpCalendarFromStructure.initCalendarPopUp(context, structure, LocalDate.now(), imageView_favorite);
        }
    }

    public void find_events(String structureID) {
        ArrayList<Event> eventArrayList = new ArrayList<>();
        ArrayList<String> list_friendID = new ArrayList<>();
        LocalDate localDate_now = LocalDate.now();
        for (User user : friendList) {
            list_friendID.add(user.getUserID());
        }
        db.collection("structures").document(structureID).collection("events").whereGreaterThan("timestampEnding", DateTools.getTimeStampFromLocalDate(localDate_now)).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int size = task.getResult().size();
                    if (size > 0) {
                        System.out.println("togo 1" + size);
                        viewPager_events.setVisibility(View.VISIBLE);
                        count = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            find_event_interested(event, list_friendID, size, eventArrayList);
                        /*
                        eventArrayList.add(event);
                        adapterEvent = new AdapterEvent(context,eventArrayList,"fef",db);
                        viewPager_events.setAdapter(adapterEvent);
                        viewPager_events.setPadding(100,0,100,0); */
                        }
                    } else {
                        viewPager_events.setVisibility(View.INVISIBLE);
                        find_permanents_events(structureID, eventArrayList);
                    }
                } else {
                }
            }
        });


    }
    public void find_event_interested(Event event, ArrayList<String> list_friendID, int size, ArrayList<Event> eventArrayList) {
        db.collection("structures").document(event.getUserID()).collection("interested").document(event.getEventID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // Document found in the offline cache
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        HashMap<String, Timestamp> map_friend_interestedTimestamp = new HashMap();
                        Map<String, Object> hashMap_interested = new HashMap<>();
                        hashMap_interested = document.getData();
                        //Cast to have only timestamp
                        HashMap<String, Timestamp> hashMap_interested_casted = Utils.getOnlyTimestamp(hashMap_interested);
                        //ArrayList which contains User of friends interested by the structure
                        ArrayList<User> list_friends_interested = new ArrayList<>();
                        for (String friendID : list_friendID) {
                            if (hashMap_interested_casted.containsKey(friendID) && hashMap_interested_casted.get(friendID) != null && hashMap_interested_casted.get(friendID).compareTo(Timestamp.now()) + 3600 * 12 > 0) {
                                map_friend_interestedTimestamp.put(friendID, hashMap_interested_casted.get(friendID));
                                list_friends_interested.add(map_friends.get(friendID));
                            }
                        }
                        event.setUserInterested(hashMap_interested.containsKey(userID) && (hashMap_interested.get(userID)) != null);
                        event.setList_users_interested(list_friends_interested);
                        event.setHashMap_users_timeStamp_interested(map_friend_interestedTimestamp);
                        eventArrayList.add(event);
                        count = count + 1;
                        if (count == size) {
                            System.out.println("togo 2" + size);
                            find_permanents_events(event.getUserID(), eventArrayList);
                            /*
                            ViewPagerAdapterEvent viewPagerAdapterEvent = new ViewPagerAdapterEvent(context,eventArrayList,db,friendList,userID, popUpCalendarFromEvent);
                            viewPagerAdapterEvent.notifyDataSetChanged();
                            viewPager_events.setAdapter(viewPagerAdapterEvent);
                            viewPager_events.setPadding(70,0,70,0);*/

                        }
                    } else { /*
                        ViewPagerAdapterEvent viewPagerAdapterEvent = new ViewPagerAdapterEvent(context,eventArrayList,db,friendList,userID, popUpCalendarFromEvent);
                        viewPagerAdapterEvent.notifyDataSetChanged();
                        viewPager_events.setAdapter(viewPagerAdapterEvent);
                        viewPager_events.setPadding(70,0,70,0); */

                    }
                } else {
                    /*
                    ViewPagerAdapterEvent viewPagerAdapterEvent = new ViewPagerAdapterEvent(context,eventArrayList,db,friendList,userID, popUpCalendarFromEvent);
                    viewPagerAdapterEvent.notifyDataSetChanged();
                    viewPager_events.setAdapter(viewPagerAdapterEvent);
                    viewPager_events.setPadding(70,0,70,0); */


                }
            }
        });
    }

    public void find_permanents_events(String structureID, ArrayList<Event> eventArrayList) {
        ArrayList<String> list_friendID = new ArrayList<>();
        LocalDate localDate_now = LocalDate.now();
        for (User user : friendList) {
            list_friendID.add(user.getUserID());
        }
        db.collection("structures").document(structureID).collection("events").whereEqualTo("permanentEvent", true).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int size = task.getResult().size();
                    System.out.println("size of permanent events:" + size);
                    if (size > 0) {
                        viewPager_events.setVisibility(View.VISIBLE);
                        iterator_premanent_events = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            find_permanents_event_interested(event, list_friendID, size, eventArrayList);
                        /*
                        eventArrayList.add(event);
                        adapterEvent = new AdapterEvent(context,eventArrayList,"fef",db);
                        viewPager_events.setAdapter(adapterEvent);
                        viewPager_events.setPadding(100,0,100,0); */
                        }
                    } else {
                        ViewPagerAdapterEvent viewPagerAdapterEvent = new ViewPagerAdapterEvent(context, eventArrayList, db, friendList, userID, popUpCalendarFromEvent);
                        viewPagerAdapterEvent.notifyDataSetChanged();
                        viewPager_events.setAdapter(viewPagerAdapterEvent);
                        viewPager_events.setPadding(70, 0, 70, 0);
                    }
                } else {
                }
            }
        });


    }
    public void find_permanents_event_interested(Event event, ArrayList<String> list_friendID, int size, ArrayList<Event> eventArrayList) {
        db.collection("structures").document(event.getUserID()).collection("interested").document(event.getEventID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // Document found in the offline cache
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        HashMap<String, Timestamp> map_friend_interestedTimestamp = new HashMap();
                        Map<String, Object> hashMap_interested = new HashMap<>();
                        hashMap_interested = document.getData();
                        //Cast to have only timestamp
                        HashMap<String, Timestamp> hashMap_interested_casted = Utils.getOnlyTimestamp(hashMap_interested);
                        //ArrayList which contains User of friends interested by the structure
                        ArrayList<User> list_friends_interested = new ArrayList<>();
                        for (String friendID : list_friendID) {
                            if (hashMap_interested_casted.containsKey(friendID) && hashMap_interested_casted.get(friendID) != null && hashMap_interested_casted.get(friendID).compareTo(Timestamp.now()) + 3600 * 12 > 0) {
                                map_friend_interestedTimestamp.put(friendID, hashMap_interested_casted.get(friendID));
                                list_friends_interested.add(map_friends.get(friendID));
                            }
                        }
                        event.setUserInterested(hashMap_interested.containsKey(userID) && (hashMap_interested.get(userID)) != null);
                        event.setList_users_interested(list_friends_interested);
                        event.setHashMap_users_timeStamp_interested(map_friend_interestedTimestamp);
                        eventArrayList.add(event);
                        iterator_premanent_events = iterator_premanent_events + 1;
                        System.out.println("$$ iterator permanent events:" +iterator_premanent_events );
                        if (iterator_premanent_events == size) {
                            System.out.println("$$$ iterator permanent events:" +iterator_premanent_events );
                            System.out.println("$$$$ size of the list:" + eventArrayList.size() );
                            ViewPagerAdapterEvent viewPagerAdapterEvent = new ViewPagerAdapterEvent(context, eventArrayList, db, friendList, userID, popUpCalendarFromEvent);
                            //viewPagerAdapterEvent.notifyDataSetChanged();
                            viewPager_events.setAdapter(viewPagerAdapterEvent);
                            viewPager_events.setPadding(70, 0, 70, 0);

                        }
                    } else { /*
                        ViewPagerAdapterEvent viewPagerAdapterEvent = new ViewPagerAdapterEvent(context,eventArrayList,db,friendList,userID, popUpCalendarFromEvent);
                        viewPagerAdapterEvent.notifyDataSetChanged();
                        viewPager_events.setAdapter(viewPagerAdapterEvent);
                        viewPager_events.setPadding(70,0,70,0); */

                    }
                } else {
                    /*
                    ViewPagerAdapterEvent viewPagerAdapterEvent = new ViewPagerAdapterEvent(context,eventArrayList,db,friendList,userID, popUpCalendarFromEvent);
                    viewPagerAdapterEvent.notifyDataSetChanged();
                    viewPager_events.setAdapter(viewPagerAdapterEvent);
                    viewPager_events.setPadding(70,0,70,0); */


                }
            }
        });
    }

    public Boolean getStructureDisplayed() {
        return isStructureDisplayed;
    }

    public void setStructureDisplayed(Boolean structureDisplayed) {
        isStructureDisplayed = structureDisplayed;
    }

    // Active when the image of the layout change
    private final ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //refresh the page only once
            if (position != lastPos) {
                viewPager_events.requestFocus(position);
                lastPos = position;
            }
        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}
