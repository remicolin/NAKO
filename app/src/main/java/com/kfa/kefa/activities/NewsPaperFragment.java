package com.kfa.kefa.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.Event;
import com.kfa.kefa.utils.EventAdapter;
import com.kfa.kefa.utils.Structure;
import com.kfa.kefa.utils.SwipingStructureLayout;
import com.kfa.kefa.utils.User;
import com.kfa.kefa.utils.Utils;
import com.kfa.kefa.utils.ViewPagerAdapterEvent;
import com.kfa.kefa.utils.calendar.DateTools;
import com.kfa.kefa.utils.calendar.calendarfromevent.PopUpCalendarFromEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsPaperFragment extends Fragment implements  EventAdapter.OnEventListener {
    private FirebaseFirestore db;
    private Event event;
    private Structure structure;
    private Context mContext;
    private RecyclerView recycler_event;
    private ArrayList<Event> list;
    private ArrayList<Structure> liste_structure;
    private Handler handler;
    private long mLastClickTime,lastPos;
    private final String KEY_RECYCLER_STATE = "recycler_state";
    private static Bundle mBundleRecyclerViewState;
    public static int i;
    private String userID;
    private FirebaseAuth firebaseAuth;
    private ArrayList<User> friendList = new ArrayList<>();
    private ArrayList<String> list_friendID = new ArrayList<>();
    private User user;
    private View  calendarPopUp;
    private EventAdapter eventAdapter;
    private SwipingStructureLayout swipingStructureLayout;
    private StorageReference storageReference;
    private PopUpCalendarFromEvent popUpCalendarFromEvent;
    private HashMap<String,User> map_friends = new HashMap<>();
    private int iterator_events,iterator_permanent_events;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        list = new ArrayList<Event>();
        handler =new Handler();
        long mLastClickTime = System.currentTimeMillis();
        long lastPos = -1;
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        userID = firebaseAuth.getCurrentUser().getUid();



    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment1, container, false);

        // Get data from Intent
        user = (User) this.getArguments().getParcelable("user");
        friendList = this.getArguments().getParcelableArrayList("friendList");
        assert friendList != null;
        for(User friend : friendList){
            list_friendID.add(friend.getUserID());
            map_friends.put(friend.getUserID(),friend);
        }
        //Get views from ressources
        recycler_event = view.findViewById(R.id.recycle_events);
        ConstraintLayout constraintLayout = view.findViewById(R.id.fragment1_constraintLayout);
        //Setup the structure layout
        popUpCalendarFromEvent = new PopUpCalendarFromEvent(mContext,constraintLayout,inflater,userID,db);
        //
        swipingStructureLayout = new SwipingStructureLayout(mContext,constraintLayout,container,inflater,db,storageReference,userID,friendList,null);
        swipingStructureLayout.initLayout();
        //Setup the popUp Calendar Layout
        //Find events
        //find_events_by_city();
        find_events();
        return view;
    }



    public void find_events_by_city() {
        final String city = "TOULOUSE";
        LocalDate localDate_now = LocalDate.now();
        db.collectionGroup("events")
                .whereEqualTo("city", city).whereGreaterThan("timestampEnding",DateTools.getTimeStampFromLocalDate(localDate_now)).orderBy("timestampEnding", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int size = task.getResult().size();
                            System.out.println("size:" + size);
                            int i = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                assert document != null;
                                    i+=1;
                                    event = document.toObject(Event.class);

                                    if (event.getEventID() != null && event.getUserID() != null){
                                        System.out.println(" Heloooooooo 1111");
                                        find_interested(event, i == size);}
                                    else if (i ==size){
                                        System.out.println("Heloooooooo is not successful");
                                        updateRecyclerEvents();
                                    }
                            }
                        } else {
                            System.out.println("Task is not successful");
                            updateRecyclerEvents();
                        }
                    }
                });
    }

    public void find_interested(Event event, boolean bl){

        HashMap<String, Object> list_friend_interested = new HashMap();
        db.collection("structures").document(event.getUserID()).collection("interested").document(event.getEventID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        // Document found in the offline cache
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()){
                            HashMap<String, Timestamp> map_friend_interestedTimestamp = new HashMap();
                            Map<String, Object> hashMap_interested = new HashMap<>();
                            hashMap_interested = document.getData();
                            //Cast to have only timestamp
                            HashMap<String, Timestamp> hashMap_interested_casted = Utils.getOnlyTimestamp(hashMap_interested);
                            //ArrayList which contains User of friends interested by the structure
                            ArrayList<User> list_friends_interested = new ArrayList<>();
                            for (String friendID : list_friendID){
                                if (hashMap_interested_casted.containsKey(friendID) && hashMap_interested_casted.get(friendID) != null &&hashMap_interested_casted.get(friendID).compareTo(Timestamp.now()) +3600*12>0  ){
                                    map_friend_interestedTimestamp.put(friendID,hashMap_interested_casted.get(friendID));
                                    list_friends_interested.add(map_friends.get(friendID));
                                }
                            }
                            event.setUserInterested(hashMap_interested.containsKey(userID) && (hashMap_interested.get(userID)) != null);
                            event.setList_users_interested(list_friends_interested);
                            event.setHashMap_users_timeStamp_interested(map_friend_interestedTimestamp);
                            list.add(event);
                            System.out.println("Evènement suivant ajouté:" + event.getEventID());

                            if (bl){
                                updateRecyclerEvents();
                            }
                        }
                        else{
                            System.out.println("Evènement suivant non ajouté:" + event.getEventID());
                            updateRecyclerEvents();
                        }
                    } else {
                        updateRecyclerEvents();
                    }
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("Fragement Paused");
        mBundleRecyclerViewState = new Bundle();
        if (recycler_event != null && recycler_event.getLayoutManager()!= null){
        Parcelable listState = recycler_event.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);}
    }

    @Override
    public void onResume() {
        super.onResume();
        // restore RecyclerView state
        if (mBundleRecyclerViewState != null) {
            System.out.println("mbundle not null");

            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            if (recycler_event.getLayoutManager() != null) {
                System.out.println("layoutmanager not null");
                System.out.println(listState);

                recycler_event.getLayoutManager().onRestoreInstanceState(listState);
            }
        }
        else {

        }
    }

    private void updateRecyclerEvents() {
        eventAdapter = new EventAdapter(mContext, list, this,userID,db,friendList,
                swipingStructureLayout,popUpCalendarFromEvent,map_friends,list_friendID);
        recycler_event.setAdapter(eventAdapter);
        recycler_event.setLayoutManager(new LinearLayoutManager(mContext));

    }


    @Override
    public void onEventClick(int position, String eventID, String proID, Event event) {
    }
    private  void onItemDoubleClick(int position, String eventID, String proID){
        //Toast.makeText(getActivity(),"double click" + position,Toast.LENGTH_SHORT).show();


    }







    public void find_events() {
        ArrayList<Event> eventArrayList = new ArrayList<>();
        ArrayList<String> list_friendID = new ArrayList<>();
        LocalDate localDate_now = LocalDate.now();
        for (User user : friendList) {
            list_friendID.add(user.getUserID());
        }
        db.collectionGroup("events").whereGreaterThan("timestampEnding", DateTools.getTimeStampFromLocalDate(localDate_now)).orderBy("timestampEnding", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int size = task.getResult().size();
                    if (size > 0) {
                        iterator_events = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            find_event_interested(event, list_friendID, size);

                        }
                    } else {
                        find_permanents_events();
                    }
                } else {
                }
            }
        });

    }
    public void find_event_interested(Event event, ArrayList<String> list_friendID, int size) {
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
                        list.add(event);
                        iterator_events = iterator_events + 1;
                        if (iterator_events == size) {
                            find_permanents_events();

                        }
                    } else {

                    }
                } else {

                }
            }
        });
    }

    public void find_permanents_events() {
        ArrayList<String> list_friendID = new ArrayList<>();
        for (User user : friendList) {
            list_friendID.add(user.getUserID());
        }
        db.collectionGroup("events").whereEqualTo("permanentEvent", true).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int size = task.getResult().size();
                    System.out.println("size of permanent events:" + size);
                    if (size > 0) {
                        iterator_permanent_events = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            find_permanents_event_interested(event, list_friendID, size);
                        }
                    } else {
                        updateRecyclerEvents();

                    }
                } else {

                }
            }
        });


    }
    public void find_permanents_event_interested(Event event, ArrayList<String> list_friendID, int size) {
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
                        list.add(event);
                        iterator_permanent_events = iterator_permanent_events + 1;
                        System.out.println("$$ iterator permanent events:" +iterator_permanent_events );
                        if (iterator_permanent_events == size) {
                            updateRecyclerEvents();
                        }
                    } else {
                        if (iterator_permanent_events == size) {
                            updateRecyclerEvents();
                        }
                    }
                } else {


                }
            }
        });
    }



    /*
    Old function
    public void find_events() {

        // Find cities within 50km of London
        final GeoLocation center = new GeoLocation(43.601570, 1.443310);
        final double radiusInM = 5 * 1000;

        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = db.collectionGroup("events")
                    .orderBy("geohash")
                    .startAt(b.startHash)
                    .endAt(b.endHash);
            tasks.add(q.get());
        }

        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> t) {

                        for (Task<QuerySnapshot> task : tasks) {
                            QuerySnapshot snap = task.getResult();
                            assert snap != null;
                            int   size = snap.getDocuments().size();
                            int i = 0;
                            for (DocumentSnapshot doc : snap.getDocuments()) {
                                i+=1;
                                event = doc.toObject(Event.class);
                                double lat = doc.getDouble("lat");
                                double lng = doc.getDouble("lng");
                                // We have to filter out a few false positives due to GeoHash (especially near the poles)
                                // accuracy, but most will match
                                GeoLocation docLocation = new GeoLocation(lat, lng);
                                double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                                if (distanceInM <= radiusInM) {
                                    find_interested(event, i == size);
                                }
                            }
                        }
                    }
                });
    } */

}   // Each item in 'bounds' represents a startAt/endAt pair. We have to issue