package com.kfa.kefa.utils.user_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.User;
import com.kfa.kefa.utils.calendar.DateTools;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;

public class FriendInterestedAdapter extends RecyclerView.Adapter<FriendInterestedAdapter.FriendViewHolder> {
    private ArrayList<User> list;
    private Context context;
    private User friendUser;
    private int pos;
    private String userID;
    private HashMap<String,Timestamp> hashMap;


    public FriendInterestedAdapter(Context context , ArrayList<User> list, HashMap<String,Timestamp> hashMap){
        this.list = list;
        this.context = context;
        this.hashMap = hashMap;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.friend_interested_layout,parent,false);


        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
    User friendUser = list.get(position);
    System.out.println(friendUser.getUserID());
    holder.textView_userTAG.setText(friendUser.getUserTAG());
    holder.textView_name.setText(friendUser.getName());
    if (hashMap!=null) {
        holder.textView_date.setText(DateTools.getBandeau(hashMap.get(friendUser.getUserID())));
    }
    else{
        holder.textView_date.setVisibility(View.GONE);
    }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder{
        private TextView textView_userTAG,textView_name,textView_date;
        public FriendViewHolder(@NonNull View friendView) {
            super(friendView);
            textView_name = friendView.findViewById(R.id.textView_friendName);
            textView_userTAG = friendView.findViewById(R.id.textView_friendUserTAG);
            textView_date = friendView.findViewById(R.id.friend_interested_layout_textView_date);
        }
    }
}
