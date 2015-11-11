package fiware.smartparking.utils;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.IconCategory;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapContainer;
import com.here.android.mpa.mapping.MapLabeledMarker;
import com.here.android.mpa.mapping.MapPolygon;
import com.here.android.mpa.mapping.MapState;

import java.util.ArrayList;

import fiware.smartparking.R;
import fiware.smartparking.models.StreetParking;

/**
 * Created by Ulpgc on 11/11/2015.
 */
public class MapChangeListener implements Map.OnTransformListener {

    MapContainer generalParkingOverlay;
    ParkingDrawTask routeParkingOverlay;

    boolean parkingOverlayActivationState;
    Map associatedMap;
    Image parkingIcon;

    GeoCoordinate destination;
    boolean onRouteTo;
    int destinationAreaMeters;


    public MapChangeListener (Map map, boolean overlayActiveOnInit, int destinationAreaMeters) {
        generalParkingOverlay = new MapContainer();
        associatedMap = map;
        destination = null;
        onRouteTo = false;
        this.destinationAreaMeters = destinationAreaMeters;

        parkingIcon = new Image();
        try {
            parkingIcon.setImageResource(R.mipmap.parking);
        }
        catch (Exception e) { parkingIcon = null; }

        if (overlayActiveOnInit)
            associatedMap.addMapObject(generalParkingOverlay);

        parkingOverlayActivationState = overlayActiveOnInit;
    }

    public void setParkingOverlayActive(boolean active) {
        if (active && !onRouteTo && !parkingOverlayActivationState)
            associatedMap.addMapObject(generalParkingOverlay);
        else if (!active && !onRouteTo && parkingOverlayActivationState)
            associatedMap.removeMapObject(generalParkingOverlay);

        parkingOverlayActivationState = active;

        if (parkingOverlayActivationState) {
            //At the time of activation, this should be called!
            StreetParkingQueryJSONTask task = new StreetParkingQueryJSONTask(this, associatedMap.getBoundingBox());
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        }
    }

    public boolean isParkingOverlayActive() { return parkingOverlayActivationState;}

    public void startedRouteTo (GeoCoordinate destination){
        this.destination = destination;
        onRouteTo = true;
        //uncheckOverlays ();
    }

    public void finishedRoute () {
        onRouteTo = false;
        destination = null;
        //checkOverlays();
    }

    public void resetOverlays () {
        if (parkingOverlayActivationState)
            generalParkingOverlay.removeAllMapObjects();
    }

    public void drawStreetParkings (GeoBoundingBox gbb, ArrayList<StreetParking> streetParkingArrayList){
        if (parkingOverlayActivationState){
            for (int i=0;i<streetParkingArrayList.size();i++) {
                StreetParking streetParking = streetParkingArrayList.get(i);
                if (gbb.contains(streetParking.getCenter())){
                    MapLabeledMarker strParkingMarker = new MapLabeledMarker(streetParking.getCenter() );

                    if (parkingIcon == null)
                        strParkingMarker.setIcon(IconCategory.PARKING_AREA);
                    else
                        strParkingMarker.setIcon(parkingIcon);


                    strParkingMarker.setLabelText(associatedMap.getMapDisplayLanguage(),
                            Integer.toString(streetParking.getAvailableSpotNumber()));
                    strParkingMarker.setFontScalingFactor(1.5f);
                    generalParkingOverlay.addMapObject(strParkingMarker);


                    for (int j=0;j<streetParking.getParkingPolygons();j++) {
                        MapPolygon streetPolygon = new MapPolygon(streetParking.getParkingAreaPolygonAt(j));
                        streetPolygon.setLineColor(Color.parseColor("#FF0000FF"));
                        streetPolygon.setFillColor(Color.parseColor("#770000FF"));
                        generalParkingOverlay.addMapObject(streetPolygon);
                    }
                }
            }
        }

    }


    @Override
    public void onMapTransformStart () {}

    @Override
    public void onMapTransformEnd (MapState mapState){
        if (onRouteTo){

        }
        if (parkingOverlayActivationState) {
            StreetParkingQueryJSONTask task = new StreetParkingQueryJSONTask(this, associatedMap.getBoundingBox());
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        }
    }

}
