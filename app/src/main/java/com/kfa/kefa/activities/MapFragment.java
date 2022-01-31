package com.kfa.kefa.activities;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.Structure;
import com.kfa.kefa.utils.SwipingStructureLayout;
import com.kfa.kefa.utils.User;
import com.kfa.kefa.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Activity mActivity;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location lastLocation;
    private Marker lastMarker;
    private Map<Object, String> hashmap_marker_structure = new HashMap<>();
    private FirebaseFirestore db;
    private Structure structure;
    private Map<String, Structure> liste_structure = new HashMap<>();
    private StorageReference storageReference;
    private long timestamp;
    //List of the link to display on the card
    private int lastPos = -1;
    private User user;
    private ArrayList<User> friendList;
    private ArrayList<String> list_friendID = new ArrayList<>();
    private String userID;
    private FirebaseAuth firebaseAuth;
    private boolean isStructureDisplayed = false;
    private MapView mapView = null;
    private boolean intercepMove = false;
    private HashMap<String, User> map_friends = new HashMap<>();
    private SwipingStructureLayout swipingStructureLayout;
    private CardView cardView_gps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Connect to Firebase
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        //Get data from Intent
        user = this.getArguments().getParcelable("user");
        friendList = this.getArguments().getParcelableArrayList("friendList");
        userID = firebaseAuth.getCurrentUser().getUid();


        assert friendList != null;
        for (User friend : friendList) {
            list_friendID.add(friend.getUserID());
            map_friends.put(friend.getUserID(), friend);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        cardView_gps = view.findViewById(R.id.cardView_gps);
        mapView = (MapView) view.findViewById(R.id.map);
        MapsInitializer.initialize(mActivity);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        mapView.requestDisallowInterceptTouchEvent(true);
        //Init the structure Layout
        ConstraintLayout constraintLayout = view.findViewById(R.id.constraint_layout_map);
        swipingStructureLayout = new SwipingStructureLayout(mActivity, constraintLayout, container, inflater, db, storageReference, userID, friendList, cardView_gps);
        swipingStructureLayout.initLayout();

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mActivity, R.raw.map_style));
        //Set the max zoom and min zoom
        mMap.setMinZoomPreference(14);
        mMap.setMaxZoomPreference(20);
        //Disable compass and rotate gesture
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        /*
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                // userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                // mMap.clear();
                //mMap.addMarker(new MarkerOptions().position(userLatLng).title("Your Location"));
                // mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        */
        //askLocationPermission();

        LatLng userLatLng = new LatLng(43.601570, 1.443310);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
        find_structures();
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                timestamp = System.currentTimeMillis();

                if (!isStructureDisplayed) {
                    get_structure(marker);
                    cardView_gps.setVisibility(View.GONE);
                    swipingStructureLayout.translateDown();
                } else {
                    get_structure(marker);
                }
                lastMarker = marker;

                return false;
            }
        });

        cardView_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng toulouseLatLng = new LatLng(43.601570, 1.443310);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(toulouseLatLng));
            }
        });

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                mapView.getParent().requestDisallowInterceptTouchEvent(true);
            }
        });

    }

    // Find structures near your place
    public void find_structures() {

        // Find structures within 5m of Toulouse
        final GeoLocation center = new GeoLocation(43.601570, 1.443310);
        final double radiusInM = 5 * 1000;


        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = db.collectionGroup("structures")
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
                            for (DocumentSnapshot doc : snap.getDocuments()) {
                                structure = doc.toObject(Structure.class);
                                double lat = doc.getDouble("lat");
                                double lng = doc.getDouble("lng");

                                // We have to filter out a few false positives due to GeoHash (especially near the poles)
                                // accuracy, but most will match
                                GeoLocation docLocation = new GeoLocation(lat, lng);
                                double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                                if (distanceInM <= radiusInM) {
                                    if (true) {
                                        find_structure_interested(structure, list_friendID);
                                    } else {
                                        System.out.println("structure not complete");
                                    }
                                }
                            }
                        }
                        if (liste_structure != null) {
                            System.out.println(hashmap_marker_structure.size());
                        } else {
                            System.out.println("no structures found");
                        }
                    }
                });
    }

    //Find the people interested by the structure and set the marker on the green color if so
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


                        liste_structure.put(structure.getUserID(), structure);
                        add_structure_to_map(structure);

                    } else {
                    }
                } else {
                }
            }
        });
    }

    // Add the structures to the map
    private void add_structure_to_map(Structure structure) {
        LatLng structure_location = new LatLng(structure.getLat(), structure.getLng());
        if (structure.getList_users_interested().size() > 0) {
            Marker marker_structure = mMap.addMarker(new MarkerOptions().position(structure_location).icon(BitmapFromVector(mActivity, R.drawable.ic_baseline_marker_gold_24)));
            hashmap_marker_structure.put(marker_structure, structure.getUserID());
            liste_structure.put(structure.getUserID(), structure);
        } else {

            Marker marker_structure = mMap.addMarker(new MarkerOptions().position(structure_location).icon(BitmapFromVector(mActivity, R.drawable.ic_baseline_marker_24)));

            hashmap_marker_structure.put(marker_structure, structure.getUserID());
            liste_structure.put(structure.getUserID(), structure);
        }
    }

    // Get the structure matching to the marker clicked
    public void get_structure(Marker marker) {
        String structureID = hashmap_marker_structure.get(marker);
        structure = liste_structure.get(structureID);
        swipingStructureLayout.set_layout_with_structure(structure);

    }

    // Get the location of the user
    /*
    private void askLocationPermission() {
        Dexter.withContext(getActivity()).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (lastLocation == null) {
                    System.out.println("lastlocation null");
                    LatLng userLatLng = new LatLng(43.601570, 1.443310);
                    //mMap.addMarker(new MarkerOptions().position(userLatLng));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
                } else {
                    //LatLng userLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    LatLng userLatLng = new LatLng(43.601570, 1.443310);

                    //Marker newMarker = mMap.addMarker(new MarkerOptions().position(userLatLng));;
                    //marker_structure.put(newMarker,"USERID");
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));

                }
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    } */

    //Change he marker of Gmaps
    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // Get the application context
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}


