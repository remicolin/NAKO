package com.kfa.kefa.utils;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class Utils {


    public static HashMap<String, Timestamp> getOnlyTimestamp(Map<String, Object> hashMap){
        HashMap <String,Timestamp>timestampDays = new HashMap<>();
        for (HashMap.Entry<String, Object> set :
                hashMap.entrySet()) {
            try {
                timestampDays.put(set.getKey(),(Timestamp) hashMap.get(set.getKey()));

            }
            catch (ClassCastException e){
                System.out.println("exeption casting structure");
            }

        }
        return  timestampDays;
    }

}
