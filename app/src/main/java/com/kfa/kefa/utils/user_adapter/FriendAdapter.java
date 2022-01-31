package com.kfa.kefa.utils.user_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.User;

import java.util.ArrayList;


public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {
    private ArrayList<User> list;
    private Context context;
    private User friendUser;
    private OnUserListener onUserListener;
    private int pos;
    private String userID;
    private boolean bl;
    private FirebaseFirestore db;
    private User user;


    public FriendAdapter(Context context,OnUserListener onUserListener, User user ,ArrayList<User> list, boolean bl, FirebaseFirestore db){
        this.list = list;
        this.context = context;
        this.onUserListener = onUserListener;
        this.userID = user.getUserID();
        this.bl = bl;
        this.db = db;
        this.user = user;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.friend_request_layout,parent,false);


        return new FriendViewHolder(view,onUserListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
    friendUser = list.get(position);
    holder.textView_userTAG.setText(friendUser.getUserTAG());
    holder.textView_name.setText(friendUser.getName());
    holder.button_valid_request.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            friendUser = list.get(holder.getAbsoluteAdapterPosition());
            user.accept_friend(context,db,userID,friendUser,user);
            holder.itemView.setVisibility(View.GONE);
        }
    });
    holder.button_delete_request.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            friendUser = list.get(holder.getAbsoluteAdapterPosition());
            user.delete_friend_request(context,db,userID,friendUser,user);
            holder.itemView.setVisibility(View.GONE);
        }
    });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView textView_userTAG,textView_name;
        private OnUserListener onUserListener;
        private CardView button_valid_request,button_delete_request;
        public FriendViewHolder(@NonNull View friendView, OnUserListener onUserListener) {
            super(friendView);
            textView_name = friendView.findViewById(R.id.textView_friendName);
            textView_userTAG = friendView.findViewById(R.id.textView_friendUserTAG);
            button_valid_request = friendView.findViewById(R.id.friend_request_layout_button_valid_request);
            button_delete_request = friendView.findViewById(R.id.friend_request_layout_button_delete_request);
            if (bl){
                button_valid_request.setVisibility(View.GONE);
                button_delete_request.setVisibility(View.GONE);
            }
            this.onUserListener = onUserListener;
        }

        @Override
        public void onClick(View view) {
            pos = getAbsoluteAdapterPosition();
            onUserListener.onUserClick(pos,userID,list.get(pos).getUserID());
        }
    }
    public interface OnUserListener{
        void onUserClick(int position, String userID, String friendID);
    }
}
