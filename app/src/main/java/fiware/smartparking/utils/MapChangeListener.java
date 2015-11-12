package fiware.smartparking.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapContainer;
import com.here.android.mpa.mapping.MapLabeledMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapState;

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
    private static boolean shouldRepeat = false;

    public MapChangeListener (Map map, boolean overlayActiveOnInit) {
        generalParkingOverlay = new ParkingDrawTask(map,overlayActiveOnInit);
        routeParkingOverlay = null;
        associatedMap = map;
        destination = null;
        onRouteTo = false;

        parkingOverlayActivationState = overlayActiveOnInit;
    }

    public void setParkingOverlayActive(boolean active) {
        if (active && !onRouteTo && !parkingOverlayActivationState)
            generalParkingOverlay.setParkingOverlayActive(true);
        else if (!active && !onRouteTo && parkingOverlayActivationState)
            generalParkingOverlay.setParkingOverlayActive(false);

        parkingOverlayActivationState = active;

        if (parkingOverlayActivationState) {
            StreetParkingQueryJSONTask task =
                    new StreetParkingQueryJSONTask(generalParkingOverlay, associatedMap.getBoundingBox(),true);
            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, (Void[]) null);
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
            if (destination.contains(mapState.getCenter()) && (routeParkingOverlay == null || shouldRepeat)) {
                routeParkingOverlay = new ParkingDrawTask(associatedMap,true);

                StreetParkingQueryJSONTask strParkingQuery =
                        new StreetParkingQueryJSONTask(routeParkingOverlay,
                                new GeoBoundingBox(destination.getCenter(), mt, mt),true);
                strParkingQuery.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, (Void[]) null);

                //MockLotParkingList. Should be substituted with a LotParking request.
                ArrayList<ParkingLot> mock = ParkingDrawTask.getMockLotParkingList() ;
                routeParkingOverlay.drawParkingLots(mock);
                TextToSpeechUtils.setLotParkingList(mock);
            }
        }
        else if (parkingOverlayActivationState) {
            StreetParkingQueryJSONTask task = new StreetParkingQueryJSONTask(generalParkingOverlay,
                    associatedMap.getBoundingBox(),false);
            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, (Void[]) null);
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
        try {
            for (int i=0;i<list.size();i++){
                MapLabeledMarker mp = (MapLabeledMarker) list.get(i);
                res = generalParkingOverlay.parkingLotSelected(mp.getCoordinate());
                if (res != null) break;

                if (routeParkingOverlay != null)
                    res = routeParkingOverlay.parkingLotSelected(mp.getCoordinate());
                if (res != null) break;

            }
        }
        finally { return res; }
    }

    public StreetParking streetParkingSelected(List<ViewObject> list){
        StreetParking res = null;
        for (int i=0;i<list.size();i++){
            try {
                    MapLabeledMarker mp = (MapLabeledMarker) list.get(i);
                    res = generalParkingOverlay.streetParkingSelected(mp.getCoordinate());
                    if (res != null) return res;

                    if (routeParkingOverlay != null)
                        res = routeParkingOverlay.streetParkingSelected(mp.getCoordinate());
                    if (res != null) return res;
                }
            catch (Exception e) {e.printStackTrace();}
        }
        return res;
    }


    public static void shouldRepeat(boolean repeat){
        shouldRepeat = repeat;
    }

    public static void setDestinationAreaMeters (int meters){
        destinationAreaMeters = meters;
    }

    public static int getDestinationAreaMeters () { return destinationAreaMeters; }

}
