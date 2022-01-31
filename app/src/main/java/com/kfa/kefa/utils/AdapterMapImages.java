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

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.StorageReference;
import com.kfa.kefa.R;
import com.kfa.kefa.activities.MainActivity;
import com.kfa.kefa.activities.MapFragment;

import java.util.ArrayList;
import java.util.Map;

public class AdapterMapImages extends PagerAdapter {
    private Context context;
    private ArrayList<StorageReference> storageReferenceArrayList;
    private long mLastClickTime = System.currentTimeMillis();
    private String proID;
    private Structure structure;

    public AdapterMapImages(Context context, ArrayList<StorageReference > storageReferenceArrayList,String proID, Structure structure) {
        this.context = context;
        this.storageReferenceArrayList = storageReferenceArrayList;
        this.proID = proID;
        this.structure = structure;
    }

    public AdapterMapImages(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return storageReferenceArrayList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        TextView textView_structure;
        //View view = LayoutInflater.from(context).inflate(R.id.fragment_map_cardView ,container,false);
        CardView cardView = new CardView(context,null,R.style.cardview_light);
        cardView.setRadius(0);
        ImageView view = new ImageView(context);

        GlideApp.with(context).load(storageReferenceArrayList.get(position)).diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().transition(withCrossFade()).into(view);
        cardView.addView(view);
        container.addView(cardView,position);
        return cardView;
    }

    //Destroy item ?

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}

