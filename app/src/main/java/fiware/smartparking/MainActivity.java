package fiware.smartparking;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.IconCategory;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.MapEngine;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.guidance.LaneInfo;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.routing.Maneuver;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteManager;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.ImageMedia;
import com.here.android.mpa.search.ResultListener;
import com.here.android.mpa.search.TextSuggestionRequest;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends Activity {
    // map embedded in the map fragment
    private Map map = null;
    // map fragment embedded in this activity
    private MapFragment mapFragment = null;
    private MapRoute route = null;

    // Oporto downtown
    private GeoCoordinate DEFAULT_COORDS;

    private MapMarker startMarker, endMarker;

    private NavigationManager navMan;
    private PositioningManager posMan;

    private static Route targetRoute;

    private TextView nextRoad, currentSpeed, ETA, distance;

    public static void setRoute(Route aRoute) {
        targetRoute = aRoute;
    }

    public void goHome(View v) {
        // Set the map center to Oporto center
        goTo(map, DEFAULT_COORDS, Map.Animation.LINEAR);
    }


    /**
     * Stops navigation manager.
     */
    private void stopNavigationManager() {
        if (navMan == null) {
            return;
        }

        if (navMan.getRunningState() != NavigationManager.NavigationState.IDLE) {
            navMan.stop();
        }
    }

    private void goTo(Map map, GeoCoordinate coordinates, Map.Animation animation) {
        map.setCenter(coordinates, animation);
        // Set the zoom level to the average between min and max
        map.setZoomLevel(map.getMaxZoomLevel() - 3);
        map.setTilt(map.getMaxTilt() / 2);
        map.setOrientation(0);
        map.setMapScheme(Map.Scheme.CARNAV_DAY);
    }

    private MapGesture.OnGestureListener gestureListener = new MapGesture.OnGestureListener() {
        @Override
        public void onPanStart() {

        }

        @Override
        public void onPanEnd() {

        }

        @Override
        public void onMultiFingerManipulationStart() {

        }

        @Override
        public void onMultiFingerManipulationEnd() {

        }

        @Override
        public boolean onMapObjectsSelected(List<ViewObject> list) {
            return false;
        }

        @Override
        public boolean onTapEvent(PointF pointF) {
            targetRoute = null;
            getDirections(null);
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(PointF pointF) {
            return false;
        }

        @Override
        public void onPinchLocked() {

        }

        @Override
        public boolean onPinchZoomEvent(float v, PointF pointF) {
            return false;
        }

        @Override
        public void onRotateLocked() {

        }

        @Override
        public boolean onRotateEvent(float v) {
            return false;
        }

        @Override
        public boolean onTiltEvent(float v) {
            return false;
        }

        @Override
        public boolean onLongPressEvent(PointF pointF) {
            return false;
        }

        @Override
        public void onLongPressRelease() {

        }

        @Override
        public boolean onTwoFingerTapEvent(PointF pointF) {
            return false;
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nextRoad = (TextView)findViewById(R.id.nextRoad);
        currentSpeed = (TextView)findViewById(R.id.currentSpeed);
        distance = (TextView)findViewById(R.id.distance);
        ETA = (TextView)findViewById(R.id.eta);

        // Search for the map fragment to finish setup by calling init().

        mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.mapfragment);
        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    mapFragment.getMapGesture().addOnGestureListener(gestureListener);
                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();

                    // Oporto downtown
                    DEFAULT_COORDS = new GeoCoordinate(41.162142, -8.621953);
                    goTo(mapFragment.getMap(), DEFAULT_COORDS, Map.Animation.NONE);

                    map.setExtrudedBuildingsVisible(true);
                    map.getPositionIndicator().setVisible(true);

                    posMan = PositioningManager.getInstance();
                    if (!posMan.isActive()) {
                        posMan.start(PositioningManager.LocationMethod.GPS_NETWORK);
                    }

                    if (targetRoute != null) {
                        doGetDirections();
                    }
                } else {
                    System.out.println("ERROR: Cannot initialize Map Fragment");
                }
            }
        });
    }

    @Override
    public void onPause() {
        detachNavigationListeners();

        if (navMan != null && navMan.getRunningState() == NavigationManager.NavigationState.RUNNING) {
            navMan.pause();

            hideWaypointerObjects();
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (navMan != null && navMan.getRunningState() == NavigationManager.NavigationState.PAUSED) {
            attachNavigationListeners();

            NavigationManager.Error error = navMan.resume();

            if (error != NavigationManager.Error.NONE) {
                Toast.makeText(getApplicationContext(),
                        "NavigationManager resume failed: " + error.toString(), Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    /**
     * Attaches listeners to navigation manager.
     */
    private void attachNavigationListeners() {
        if (navMan != null) {
            navMan.addPositionListener(
                    new WeakReference<NavigationManager.PositionListener>(m_navigationPositionListener));

            navMan.addNavigationManagerEventListener(
                    new WeakReference<NavigationManager.NavigationManagerEventListener>(m_navigationListener));
        }
    }

    /**
     * Detaches listeners from navigation manager.
     */
    private void detachNavigationListeners() {
        if (navMan != null) {
            navMan.removeNavigationManagerEventListener(m_navigationListener);
            navMan.removePositionListener(m_navigationPositionListener);
        }
    }

    public void stopSimulation(View v) {
        TextSuggestionRequest req = new TextSuggestionRequest("Rest");
        req.setSearchCenter(new GeoCoordinate(41.162142, -8.621953));
        req.execute(new ResultListener<List<String>>() {
            @Override
            public void onCompleted(List<String> strings, ErrorCode errorCode) {
                System.out.println("Ready ...");
            }
        });
        detachNavigationListeners();
        stopNavigationManager();
        hideWaypointerObjects();
    }


    public void pauseSimulation(View v) {
        Button b = (Button)v;

        if(navMan != null) {
           if (navMan.getRunningState() == NavigationManager.NavigationState.RUNNING) {
               b.setText("Resume");
               navMan.pause();
           }
           else if (navMan.getRunningState() == NavigationManager.NavigationState.PAUSED) {
               b.setText("Pause");
               navMan.resume();
           }
        }
    }

    // Functionality for taps of the "Get Directions" button
    public void getDirections(View view) {
        System.out.println("Get Directions");
        Intent intent = new Intent(this, RouteActivity.class);
        startActivity(intent);
    }

    private void doGetDirections() {
        hideWaypointerObjects();

        GeoCoordinate start = targetRoute.getStart();
        Image startImg = new Image();
        try {
            startImg.setImageResource(R.drawable.start);
        }
        catch(IOException e) {
            System.err.println("Cannot load image");
        }
        startMarker = new MapMarker(start, startImg);
        startMarker.setAnchorPoint(new PointF(startImg.getWidth()/2,startImg.getHeight()));
        map.addMapObject(startMarker);

        Image car = new Image();
        try {
            car.setImageResource(R.drawable.car);
        }
        catch(IOException e) {
            System.err.println("Cannot load image");
        }

        GeoCoordinate end = targetRoute.getDestination();

        Image endImg = new Image();
        try {
            endImg.setImageResource(R.drawable.end);
        }
        catch(IOException e) {
            System.err.println("Cannot load image");
        }
        endMarker = new MapMarker(end, endImg);
        endMarker.setAnchorPoint(new PointF(endImg.getWidth()/2,endImg.getHeight()));
        map.addMapObject(endMarker);

        MapRoute mapRoute = new MapRoute(targetRoute);
        map.addMapObject(mapRoute);
        startGuidance(targetRoute);
    }


    // Called on UI thread
    private final NavigationManager.PositionListener
                        m_navigationPositionListener = new NavigationManager.PositionListener() {
        @Override
        public void onPositionUpdated(final GeoPosition loc) {
            updateNavigationInfo(loc);
        }
    };

    private void updateNavigationInfo(final GeoPosition loc) {
        Maneuver nextManeuver = navMan.getNextManeuver();

        if(nextManeuver != null) {
            nextRoad.setText(nextManeuver.getNextRoadName());
        }

        // Update the average speed
        int avgSpeed = (int) loc.getSpeed();
        currentSpeed.setText(String.format("%d m/s", avgSpeed));

        // Update ETA
        SimpleDateFormat sdf = new SimpleDateFormat("k:mm", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));

        Date ETADate = navMan.getEta(true, Route.TrafficPenaltyMode.DISABLED);
        ETA.setText(sdf.format(ETADate));

        //Detecting destination proximity (+-250 mt Area);
        GeoBoundingBox gbb = new GeoBoundingBox(targetRoute.getDestination(),500,500);
        if (gbb.contains(loc.getCoordinate())) {
            map.setZoomLevel(map.getMaxZoomLevel()-3);
        }
    }

    private void hideWaypointerObjects() {
        if (map != null) {
            MapObject[] objects = {startMarker, endMarker};

            List<MapObject> objectList = Arrays.asList(objects);
            map.removeMapObjects(objectList);

             startMarker = endMarker = null;
        }

        if(route != null) {
            map.removeMapObject(route);
            route = null;
        }
    }

    // Called on UI thread
    private final NavigationManager.NavigationManagerEventListener m_navigationListener =
                                            new NavigationManager.NavigationManagerEventListener() {
        @Override
        public void onEnded(final NavigationManager.NavigationMode mode) {
            // NOTE: this method is called in both cases when destination
            // is reached and when NavigationManager is stopped.
            Toast.makeText(getApplicationContext(),
                                "Destination reached!", Toast.LENGTH_LONG).show();

            hideWaypointerObjects();

            detachNavigationListeners();

            navMan.setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
            navMan.setTrafficAvoidanceMode(NavigationManager.TrafficAvoidanceMode.DISABLE);
            navMan.setMap(null);
        }

        @Override
        public void onRouteUpdated(final Route updatedRoute) {
        }
    };

    /**
     *   Starts guidance simulation.
     *
     */
    private void startGuidance(Route route) {
        stopNavigationManager();

        if (navMan == null) {
            // Setup navigation manager
            navMan = NavigationManager.getInstance();
        }

        attachNavigationListeners();

        navMan.setMap(map);

        navMan.setMapUpdateMode(NavigationManager.MapUpdateMode.POSITION_ANIMATION);

        // Disable navigation sounds
        /*
          m_audioFlags = m_navigationManager.getEnabledAudioEvents();
          m_navigationManager.setEnabledAudioEvents(EnumSet.noneOf(NavigationManager.AudioEvent.class));
        */

        // Disable traffic avoidance mode as we use pedestrian guidance
        // navigator.setTrafficAvoidanceMode(TrafficAvoidanceMode.DISABLE);

        // Start navigation simulation
        NavigationManager.Error error = navMan.simulate(route, 14);
        if (error != NavigationManager.Error.NONE) {
            Toast.makeText(getApplicationContext(),
                     "Failed to start navigation. Error: " + error, Toast.LENGTH_LONG).show();
            navMan.setMap(null);
            return;
        }

        navMan.setNaturalGuidanceMode(
                    EnumSet.of(NavigationManager.NaturalGuidanceMode.JUNCTION));
    }
}