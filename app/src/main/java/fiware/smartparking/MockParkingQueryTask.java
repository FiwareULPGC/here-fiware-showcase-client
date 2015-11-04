package fiware.smartparking;

import android.util.Log;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;

import java.util.ArrayList;

import fiware.smartparking.models.Parking;
import fiware.smartparking.models.ParkingAccess;
import fiware.smartparking.models.ParkingLot;
import fiware.smartparking.models.ParkingLotCategory;
import fiware.smartparking.models.StreetParking;

/**
 * Created by Ulpgc on 04/11/2015.
 */
public class MockParkingQueryTask {

    StreetParking streetParking;
    ParkingLot lotParking;

    MapMarker strParkingMarker, lotParkingMarker;

    public MockParkingQueryTask(){

        ArrayList<GeoCoordinate> geoCoords = new ArrayList<GeoCoordinate>();
        geoCoords.add(new GeoCoordinate(41.162642, -8.622453));
        ArrayList<GeoCoordinate> geoCoords2 = new ArrayList<GeoCoordinate>();
        geoCoords2.add(new GeoCoordinate(41.161642, -8.621453));

        ArrayList<ParkingLotCategory> categories = new ArrayList<ParkingLotCategory>();
        categories.add(new ParkingLotCategory("Aparcamiento","Subterráneo"));

        ArrayList<ParkingAccess> entrances = new ArrayList<ParkingAccess>();
        ArrayList<ParkingAccess> exits = new ArrayList<ParkingAccess>();

        streetParking = new StreetParking(
                new Parking(geoCoords,false,60,80,10,10,0.05f,"08:00","20:00",0.86f, Parking.ParkingDisposition.Parallel),
                false
        );
        lotParking = new ParkingLot(
                new Parking(geoCoords2,false,60,200,32,10,0.15f,"08:00","20:00",0.86f, Parking.ParkingDisposition.Perpendicular),
                categories,
                100,
                22,
                entrances,
                exits,
                4.4f,
                2.1f,
                3.8f
        );
        strParkingMarker = null;
        lotParkingMarker = null;

    }

    public void queryAndDrawParking(Map map, GeoBoundingBox gbb){
        if (gbb.contains(streetParking.getLocationAt(0))){
            strParkingMarker = new MapMarker();
            strParkingMarker.setCoordinate(streetParking.getLocationAt(0));
            strParkingMarker.setTitle("Available parking places: "+Double.toString(streetParking.getAvailableSpotNumber()));
            map.addMapObject(strParkingMarker);
            Log.e("Parking", "street parking found");
        }
        else Log.e("Parking","No street parking found");

        if (gbb.contains(lotParking.getLocationAt(0))){
            lotParkingMarker = new MapMarker();
            lotParkingMarker.setCoordinate(lotParking.getLocationAt(0));
            lotParkingMarker.setTitle("Available parking places: "+Double.toString(lotParking.getAvailableSpotNumber()));
            map.addMapObject(lotParkingMarker);
            Log.e("Parking", "lot parking found");
        }
        else Log.e("Parking", "No lot parking found");    }

    public void clearMarkers(Map map){
        if (strParkingMarker != null){
            map.removeMapObject(strParkingMarker);
            strParkingMarker = null;
        }
        if (lotParkingMarker != null){
            map.removeMapObject(lotParkingMarker);
            lotParkingMarker = null;
        }
    }

    public void hideAllInfo(){
        strParkingMarker.hideInfoBubble();
        lotParkingMarker.hideInfoBubble();
    }



    public void changeInfoState(ViewObject view){
        if (view.getBaseType() == ViewObject.Type.USER_OBJECT) {
            try {
                MapMarker markerObject = (MapMarker) view;
                if (markerObject.isInfoBubbleVisible())
                    markerObject.hideInfoBubble();
                else
                    markerObject.showInfoBubble();
            }
            catch (Exception e) {}
        }
    }


}
