package com.kfa.kefa.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kfa.kefa.utils.SwipingStructureLayout;
import com.kfa.kefa.utils.calendar.DateTools;
import com.kfa.kefa.utils.Event;
import com.kfa.kefa.utils.User;
import com.kfa.kefa.utils.calendar.CalendarAdapter;

import com.kfa.kefa.R;
import com.kfa.kefa.utils.calendar.CalendarItem;
import com.kfa.kefa.utils.calendar.calendarfromevent.PopUpCalendarFromEvent;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CalendarFragment extends Fragment implements CalendarAdapter.OnItemListener {
    private RecyclerView calendarRecyclerView;
    private TextView monthYearText;
    private LocalDate selectedDate;
    private Context mContext;
    private Button button_previousMonth,button_nextMonth;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private String userID;
    private View structureLayout;
    private ArrayList<User> friendList = new ArrayList<>();
    private ArrayList<Event> eventArrayList = new ArrayList<>();
    private ArrayList<CalendarItem> calendarItemArrayList = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private User user;
    private ArrayList<String> list_friendID = new ArrayList<>();
    private int dayOfWeek;
    private int daysInMonth;
    private HashMap<Integer,ArrayList<Event>> theHashmap = new HashMap<>();
    private View eventPopUpView;
    private ConstraintLayout constraintLayout;
    private PopUpCalendarFromEvent popUpCalendarFromEvent;
    private Boolean isTheFragmentInCreation=true;
    private SwipingStructureLayout swipingStructureLayout;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Init Firebase
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        userID = firebaseAuth.getCurrentUser().getUid();
        //Get data from Intent
        user = (User) this.getArguments().getParcelable("user");
        friendList = this.getArguments().getParcelableArrayList("friendList");
        assert friendList != null;
        for(User friend : friendList){
            list_friendID.add(friend.getUserID());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        monthYearText = view.findViewById(R.id.monthYearTV);
        button_previousMonth = view.findViewById(R.id.fragment_calendar_previous);
        button_nextMonth = view.findViewById(R.id.fragment_calendar_next);
        // Init the structure Layout
        constraintLayout = view.findViewById(R.id.fragment2_constraintLayout);
        structureLayout = (View) inflater.inflate(R.layout.structure_layout, container,false);
        constraintLayout.addView(structureLayout);
        structureLayout.setTranslationY(-3000);
        //Init CalendarPopUpLayout
        popUpCalendarFromEvent = new PopUpCalendarFromEvent(mContext,constraintLayout,inflater,userID,db);
        //Init the event layout
        eventPopUpView = (View) inflater.inflate(R.layout.popup_events_calendar,constraintLayout,false);
        constraintLayout.addView(eventPopUpView);
        eventPopUpView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventPopUpView.setVisibility(View.GONE);
            }
        });
        eventPopUpView.setVisibility(View.GONE);
        //Get the date
        selectedDate = LocalDate.now();
        monthYearText.setText(monthYearFromDate(selectedDate));
        //Search events in the current month
        daysInMonthArray(selectedDate);

        //Set the click listener on the previous and next month
        button_previousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousMonthAction(calendarRecyclerView);
            }
        });
        button_nextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextMonthAction(calendarRecyclerView);
            }
        });

        swipingStructureLayout = new SwipingStructureLayout(mContext,constraintLayout,container,inflater,db,storageReference,userID,friendList,null);
        swipingStructureLayout.initLayout();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isTheFragmentInCreation){
            isTheFragmentInCreation = false;
        }
        else{
        calendarItemArrayList = new ArrayList<>();
        monthYearText.setText(monthYearFromDate(selectedDate));
        daysInMonthArray(selectedDate);}
    }

    private void daysInMonthArray(LocalDate date)
    {
        YearMonth yearMonth = YearMonth.from(date);
        daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        eventArrayList = new ArrayList<>();
        for(int i = 1; i <= daysInMonth; i++)
        {       theHashmap.put(i,new ArrayList<>());}
        getFromUserIDAndDate(userID,db,selectedDate);
    }

    public void getFromUserIDAndDate(String userID,FirebaseFirestore db,LocalDate localDate){
        String date = DateTools.dateMMYYYY(localDate);
        Query query = db.collection("users").document(userID).collection("events_interested").whereEqualTo("userInterestedDate",date);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                eventArrayList = new ArrayList<Event>();
                if (task.isSuccessful())
                {
                    //System.out.println("task is succesful:"+date +"//" + i);
                    if (task.getResult() != null)
                    {
                        int u = task.getResult().size();
                        System.out.println("size!!!"+ u);
                        int v = 0;
                        if (u==0){
                            setMonthView();
                        }
                        else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                v += 1;
                                Event event = document.toObject(Event.class);
                                find_interested(event, u == v);
                            /*
                            if (theHashmap.get(event.getUserInterestedDay())!=null){
                                theHashmap.get(event.getUserInterestedDay()).add(event);
                            } */
                            }
                        }
                    }
                    else{
                        setMonthView();
                    }
                }
                else
                {
                    setMonthView();
                }
            }
        });
    }

    public void find_interested(Event event, boolean bl){

        HashMap<String, Object> list_friend_interested = new HashMap();
        //For temporary events
        if (!event.getStructureEvent()) {
            db.collection("structures").document(event.getUserID()).collection("interested").document(event.getEventID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> hashMap_interested = new HashMap<>();
                            hashMap_interested = document.getData();
                            ArrayList<String> arrayList_interested = new ArrayList<>();
                            for (String friendID : list_friendID) {
                                assert hashMap_interested != null;
                                if (hashMap_interested.containsKey(friendID) && hashMap_interested.get(friendID) != null) {
                                    list_friend_interested.put(friendID, hashMap_interested.get(friendID));
                                }
                            }
                            assert hashMap_interested != null;
                            event.setUserInterested(hashMap_interested.containsKey(userID) && (hashMap_interested.get(userID)) != null);
                            event.setInterested(list_friend_interested);
                            if (theHashmap.get(event.getUserInterestedDay()) != null) {
                                theHashmap.get(event.getUserInterestedDay()).add(event);
                            }

                            if (bl) {
                                setMonthView();
                            }
                        } else {
                            if (bl) {
                                setMonthView();
                            }
                        }
                    } else {
                        if (bl) {
                            setMonthView();
                        }
                    }
                }
            });
        }
        //For structure permanent event
        else {
            db.collection("structures").document(event.getUserID()).collection("structure_interested").document(event.getEventID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> hashMap_interested = new HashMap<>();
                            hashMap_interested = document.getData();
                            ArrayList<String> arrayList_interested = new ArrayList<>();
                            for (String friendID : list_friendID) {
                                assert hashMap_interested != null;
                                if (hashMap_interested.containsKey(friendID) && hashMap_interested.get(friendID) != null) {
                                    list_friend_interested.put(friendID, hashMap_interested.get(friendID));
                                }
                            }
                            assert hashMap_interested != null;
                            event.setUserInterested(hashMap_interested.containsKey(userID) && (hashMap_interested.get(userID)) != null);
                            event.setInterested(list_friend_interested);
                            if (theHashmap.get(event.getUserInterestedDay()) != null) {
                                theHashmap.get(event.getUserInterestedDay()).add(event);
                            }

                            if (bl) {
                                setMonthView();
                            }
                        } else {
                            if (bl) {
                                setMonthView();
                            }
                        }
                    } else {
                        if (bl) {
                            setMonthView();
                        }
                    }
                }
            });
        }
    }

    private void setMonthView()
    {
        //Format calendar
        addBlankCaseToCalendarAndOrderThem();
        //Init the CalendarView
        CalendarAdapter calendarAdapter = new CalendarAdapter(mContext,calendarItemArrayList,
                this,userID,db,friendList,structureLayout,eventPopUpView, popUpCalendarFromEvent,swipingStructureLayout);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }



    private String monthYearFromDate(LocalDate date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    public void addBlankCaseToCalendarAndOrderThem(){
        for (int i = 1;i<=daysInMonth;i+=1){
            calendarItemArrayList.add(new CalendarItem(i,theHashmap.get(i)));
        }
        for (int i =  0;i<dayOfWeek;i+=1){
            if (dayOfWeek != 7){
                calendarItemArrayList.add(0,new CalendarItem(0,new ArrayList<>()));}
        }

    }


    //Click on the previous and next month
    public void previousMonthAction(View view)
    {
        calendarItemArrayList = new ArrayList<>();
        selectedDate = selectedDate.minusMonths(1);
        monthYearText.setText(monthYearFromDate(selectedDate));
        daysInMonthArray(selectedDate);
    }
    public void nextMonthAction(View view)
    {
        calendarItemArrayList = new ArrayList<>();
        selectedDate = selectedDate.plusMonths(1);
        monthYearText.setText(monthYearFromDate(selectedDate));
        daysInMonthArray(selectedDate);
    }

    @Override
    public void onItemClick(int position, String dayText) {

    }




    public void getFromTimeStamp(Timestamp now,FirebaseFirestore db){
        Query query = db.collectionGroup("events").whereGreaterThan("timestampEnding",now);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                eventArrayList = new ArrayList<Event>();
                if (task.isSuccessful())
                {
                    if (task.getResult() != null)
                    {
                        System.out.println("Hello Query Collection"+task.getResult().size());
                        for (QueryDocumentSnapshot document : task.getResult())
                        {
                            Event event = document.toObject(Event.class);
                            eventArrayList.add(event);
                        }
                        if (eventArrayList.size() != 0)
                        {
                        }
                        else
                        {
                        }
                    }
                }
                else
                {
                    System.out.println("task is not succesful");
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    //Get application context
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
    }
