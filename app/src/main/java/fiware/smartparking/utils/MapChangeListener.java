package fiware.smartparking.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapCircle;
import com.here.android.mpa.mapping.MapContainer;
import com.here.android.mpa.mapping.MapLabeledMarker;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapPolygon;
import com.here.android.mpa.mapping.MapState;
import com.nokia.maps.MapPolygonImpl;

import java.util.ArrayList;
import java.util.List;

import fiware.smartparking.models.ParkingLot;
import fiware.smartparking.models.StreetParking;

/**
 * Created by Ulpgc on 11/11/2015.
 */
public class MapChangeListener implements Map.OnTransformListener {

    private ParkingDrawTask generalParkingOverlay;
    private ParkingDrawTask routeParkingOverlay;

    private boolean parkingOverlayActivationState;
    private Map associatedMap;

    private GeoBoundingBox destination;
    private boolean onRouteTo;
    private static int destinationAreaMeters = 250;

    private static final int minOffset = 10;

    public MapChangeListener (Map map, boolean overlayActiveOnInit, Context applicationContext) {
        generalParkingOverlay = new ParkingDrawTask(map,overlayActiveOnInit);
        routeParkingOverlay = null;
        associatedMap = map;
        destination = null;
        onRouteTo = false;

        parkingOverlayActivationState = overlayActiveOnInit;
        ParkingDrawTask.setApplicationContext(applicationContext);
    }

    public void setParkingOverlayActive(boolean active) {
        if (active && !onRouteTo && !parkingOverlayActivationState)
            generalParkingOverlay.setParkingOverlayActive(true);
        else if (!active && !onRouteTo && parkingOverlayActivationState)
            generalParkingOverlay.setParkingOverlayActive(false);

        parkingOverlayActivationState = active;

        if (parkingOverlayActivationState) {
            StreetParkingQueryJSONTask task =
                    new StreetParkingQueryJSONTask(generalParkingOverlay, associatedMap.getBoundingBox());
            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, (Void[]) null);

            ParkingLotQueryJSONTask lotParkingQuery =
                    new ParkingLotQueryJSONTask(generalParkingOverlay,
                            associatedMap.getBoundingBox());
            lotParkingQuery.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,(Void[]) null);
        }
    }

    public boolean isParkingOverlayActive() { return parkingOverlayActivationState;}

    public void startedRouteTo (GeoCoordinate destination){
        int mt = destinationAreaMeters * 2; //Radius * 2
        this.destination = new GeoBoundingBox(destination,mt,mt);
        onRouteTo = true;
        uncheckOverlays();
    }

    public void finishedRoute () {
        onRouteTo = false;
        destination = null;
        routeParkingOverlay.clearMarkers(true);
        routeParkingOverlay = null;
        checkOverlays();
    }

    @Override
    public void onMapTransformStart () {}

    @Override
    public void onMapTransformEnd (MapState mapState){
        int mt = destinationAreaMeters * 2 + minOffset; //Radius*2 + minimumOffset
        if (onRouteTo){
            if (destination.contains(mapState.getCenter()) && (routeParkingOverlay == null)) {
                routeParkingOverlay = new ParkingDrawTask(associatedMap,true);

                StreetParkingQueryJSONTask strParkingQuery =
                        new StreetParkingQueryJSONTask(routeParkingOverlay,
                                new GeoBoundingBox(destination.getCenter(), mt, mt));
                strParkingQuery.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, (Void[]) null);

                ParkingLotQueryJSONTask lotParkingQuery =
                        new ParkingLotQueryJSONTask(routeParkingOverlay,
                                new GeoBoundingBox(destination.getCenter(), mt, mt));
                lotParkingQuery.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, (Void[]) null);
            }
        }
        else if (parkingOverlayActivationState) {
            StreetParkingQueryJSONTask task = new StreetParkingQueryJSONTask(generalParkingOverlay,
                    associatedMap.getBoundingBox());
            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, (Void[]) null);
            ParkingLotQueryJSONTask lotParkingQuery =
                    new ParkingLotQueryJSONTask(generalParkingOverlay,
                            associatedMap.getBoundingBox());
            lotParkingQuery.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,(Void[]) null);
        }
    }

    private void uncheckOverlays (){
        if (parkingOverlayActivationState)
            generalParkingOverlay.setParkingOverlayActive(false);
    }

    private void checkOverlays (){
        if (parkingOverlayActivationState)
            generalParkingOverlay.setParkingOverlayActive(true);
    }

    public ParkingLot parkingLotSelected (List<ViewObject> list){
        ParkingLot res = null;

        for (int i=0;i<list.size();i++){
            try {
                MapMarker mp = (MapMarker) list.get(i);
                res = generalParkingOverlay.parkingLotSelected(mp.getCoordinate());
                if (res != null) break;

                if (routeParkingOverlay != null)
                    res = routeParkingOverlay.parkingLotSelected(mp.getCoordinate());
                if (res != null) break;
            }
            catch (Exception e){
                try {
                    MapPolygon polygon = (MapPolygon) list.get(i);
                    res = generalParkingOverlay.parkingLotSelected(polygon);
                    if (res != null) break;

                    if (routeParkingOverlay != null)
                        res = routeParkingOverlay.parkingLotSelected(polygon);
                    if (res != null) break;
                }
                catch (Exception err){
                    err.printStackTrace();
                }
            }
        }
        return res;
    }

    public StreetParking streetParkingSelected(List<ViewObject> list){
        StreetParking res = null;
        for (int i=0;i<list.size();i++){
            try {
                    MapMarker mp = (MapMarker) list.get(i);
                    res = generalParkingOverlay.streetParkingSelected(mp.getCoordinate());
                    if (res != null) return res;

                    if (routeParkingOverlay != null)
                        res = routeParkingOverlay.streetParkingSelected(mp.getCoordinate());
                    if (res != null) return res;
                }
            catch (Exception e) {
                try {
                    MapPolygon polygon = (MapPolygon) list.get(i);
                    res = generalParkingOverlay.streetParkingSelected(polygon);
                    if (res != null) return res;

                    if (routeParkingOverlay != null)
                        res = routeParkingOverlay.streetParkingSelected(polygon);
                    if (res != null) return res;

                }
                catch (Exception err){
                    err.printStackTrace();
                }
            }
        }
        return res;
    }

    public static void setDestinationAreaMeters (int meters){
        destinationAreaMeters = meters;
    }

    public static int getDestinationAreaMeters () { return destinationAreaMeters; }

}
