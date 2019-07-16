public class TrackActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = "TrackActivity";

    private TextView mLatlngTxtVu, mAddressTxtVu;
    private TextView mToolbarTitle;

    // toolbar instance
    private androidx.appcompat.widget.Toolbar mToolbar;   // Toolbar instance

    private GoogleMap mGoogleMap;

    private Button mRouteStartBtn;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUserId;

    private String mChildUID, mChildName;

    private FirebaseDatabase database;
    private DatabaseReference mLocationDataRef, RootRef;

    Map<String, Marker> mNamedMarkers = new HashMap<String,Marker>();

    private String mParentlattitude, mParentlongitude;
/*
    String mChildLatitude , mChildLongitude ;
*/

    private Marker mChildMarker , mParentMarker;

    ChildEventListener markerUpdateListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            String key = dataSnapshot.getKey();

            Log.d(TAG, "Adding location for '" + key + "'");

            Double lng = dataSnapshot.child("lng").getValue(Double.class);
            Double lat = dataSnapshot.child("lat").getValue(Double.class);
            LatLng location = new LatLng(lat, lng);

            Marker marker = mNamedMarkers.get(key);

            if (marker == null) {
                MarkerOptions options = getMarkerOptions(key);
                marker = mGoogleMap.addMarker(options.position(location));
                mNamedMarkers.put(key, marker);
            } else {
                // This marker-already-exists section should never be called in this listener's normal use, but is here to handle edge cases quietly.
                // TODO: Confirm if marker title/snippet needs updating.
                marker.setPosition(location);
            }

        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
/*

            mChildLatitude = dataSnapshot.child("lat").getValue().toString();
            mChildLongitude = dataSnapshot.child("lng").getValue().toString();


            mChildMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(mChildLatitude), Double.parseDouble(mChildLongitude)))
                    .title("Child"));
*/


            String key = dataSnapshot.getKey();
            Log.d(TAG, "Location for '" + key + "' was updated.");

            Double lng = dataSnapshot.child("lang").getValue(Double.class);
            Double lat = dataSnapshot.child("lat").getValue(Double.class);
            LatLng location = new LatLng(lat, lng);

            Marker marker = mNamedMarkers.get(key);

            if (marker == null) {
                // This null-handling section should never be called in this listener's normal use, but is here to handle edge cases quietly.
                Log.d(TAG, "Expected existing marker for '" + key + "', but one was not found. Added now.");
                MarkerOptions options = getMarkerOptions(key); // TODO: Read data from database for this marker (e.g. Name, Driver, Vehicle type)
                marker = mGoogleMap.addMarker(options.position(location));
                mNamedMarkers.put(key, marker);
            } else {
                // TODO: Confirm if marker title/snippet needs updating.
                marker.setPosition(location);
            }

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

/*

            if (mChildMarker != null){
                mChildMarker.remove();
            }
*/

            String key = dataSnapshot.getKey();
            Log.d(TAG, "Location for '" + key + "' was removed.");

            Marker marker = mNamedMarkers.get(key);
            if (marker != null)
                marker.remove();

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.track_main_map);
        mapFragment.getMapAsync(this);


        mRouteStartBtn = (Button) findViewById(R.id.track_startRoute);

        setUpToolbar();

        setUpNavigationDrawer();

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            mCurrentUserId = mAuth.getCurrentUser();
        }

        mLocationDataRef = FirebaseDatabase.getInstance().getReference().child("Location");

        RootRef = FirebaseDatabase.getInstance().getReference();

        // getting intent extras here
        mChildUID = getIntent().getStringExtra("childId");
        mChildName = getIntent().getStringExtra("childName");
        mParentlattitude = getIntent().getStringExtra("parentLat");
        mParentlongitude = getIntent().getStringExtra("parentLng");

        Log.d(TAG, "onCreate: " +mParentlattitude +"  " + mParentlongitude);


        mRouteStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawRoute();
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;

        mParentMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(Double.parseDouble(mParentlattitude) , Double.parseDouble(mParentlongitude)))
                .title("Parent"));

        database = FirebaseDatabase.getInstance();
        mLocationDataRef  = database.getReference(mChildUID);

        mLocationDataRef.addChildEventListener(markerUpdateListener);

    }

    private void setUpToolbar() {

    //code written here is working fine
    }

    private void setUpNavigationDrawer() {
//code written here is working fine
    }


    private void drawRoute() {

        Log.d(TAG, "drawRoute: called");

        //Define list to get all latlng for the route
        List<LatLng> path = new ArrayList();

        //Execute Directions API request
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(getResources().getString(R.string.maps_browser_key))
                .build();
        DirectionsApiRequest req = DirectionsApi.getDirections(context, "26.8763267,75.7235405", "26.8800231,75.7481712");
        try {
            DirectionsResult res = req.await();

            mChildMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(26.8800231, 75.7481712))
                    .title("Child"));

            Log.d(TAG, "drawRoute: child marker set and adding polyline");

            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs != null) {
                    for (int i = 0; i < route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j = 0; j < leg.steps.length; j++) {
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length > 0) {
                                    for (int k = 0; k < step.steps.length; k++) {
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            zoomRoute(mGoogleMap , path);

        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

        //Draw the polyline
        if (path.size() > 0) {

            Log.d(TAG, "drawRoute: " + path);

            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLACK).width(8);
            mGoogleMap.addPolyline(opts);
        }

        LatLng n = new LatLng(Double.parseDouble(mParentlattitude), Double.parseDouble(mParentlongitude));

        boolean isPath = PolyUtil.isLocationOnPath(n, path, false, 100);

        Log.d(TAG, "drawRoute: " + isPath);

        /*if (isPath) {

            DatabaseReference notiData = RootRef.child("Notifications").child(mCurrentUserId.getUid()).push();
            String pushKey = notiData.getKey();


            String device_token = FirebaseInstanceId.getInstance().getToken();
            String type = "home";
            String name = mChildName;

            HashMap<String, String> notiMap = new HashMap<>();
            notiMap.put("deviceToken", device_token);
            notiMap.put("childName", name);
            notiMap.put("type", type);

            Map newMap = new HashMap();
            newMap.put("Notifications/" + mAuth.getCurrentUser().getUid() + "/" + pushKey + "/", notiMap);

            RootRef.setValue(newMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(TrackActivity.this, "Succedddddddd", Toast.LENGTH_SHORT).show();
                }
            });

        }*/

    }

    public void zoomRoute(GoogleMap googleMap, List<LatLng> lstLatLngRoute) {

        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 100;
        LatLngBounds latLngBounds = boundsBuilder.build();

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));
    }

    /**
     * Retrieves the marker data for the given key.
     * @param key  The ID of the marker
     * @return A MarkerOptions instance containing this marker's infoormation
     */
    private MarkerOptions getMarkerOptions(String key) {
        // TODO: Read data from database for the given marker (e.g. Name, Driver, Vehicle type)
        return new MarkerOptions().title("Location Placeholder").snippet("Update with marker info");
    }
}
