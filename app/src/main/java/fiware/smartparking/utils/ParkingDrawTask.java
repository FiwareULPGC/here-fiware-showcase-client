package fiware.smartparking.utils;

import android.graphics.Color;
import android.util.Log;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.IconCategory;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapCircle;
import com.here.android.mpa.mapping.MapContainer;
import com.here.android.mpa.mapping.MapLabeledMarker;
import com.here.android.mpa.mapping.MapOverlayType;
import com.here.android.mpa.mapping.MapPolygon;

import java.util.ArrayList;

import fiware.smartparking.R;
import fiware.smartparking.models.Parking;
import fiware.smartparking.models.ParkingAccess;
import fiware.smartparking.models.ParkingLot;
import fiware.smartparking.models.ParkingLotCategory;
import fiware.smartparking.models.StreetParking;

/**
 * Created by Ulpgc on 04/11/2015.
 */
public class ParkingDrawTask {

    private Map map;
    private MapContainer lotContainer,streetContainer;
    private ArrayList<StreetParking> lastStrParkings;
    private ArrayList<ParkingLot> lastLotParkings;
    private static Image parkingIcon;
    private boolean status;
    // Necessary since MapPolygon has no useful getters and we need to compare directly
    // between objects.
    private class MapPolygonWrapper {
        public MapPolygon mapPolygon;
        public int streetParkingIndex;

        MapPolygonWrapper(MapPolygon mapPolygon, int index){
            this.mapPolygon = mapPolygon;
            streetParkingIndex = index;
        }
    }
    private ArrayList<MapPolygonWrapper> lastStrPolygons;

    public ParkingDrawTask(Map map, boolean activeOnInit){

        this.map = map;
        lotContainer = new MapContainer();
        streetContainer =  new MapContainer();
        lastStrParkings = new ArrayList<>();
        lastLotParkings = new ArrayList<>();
        lastStrPolygons = new ArrayList<>();
        status = activeOnInit;
        if (status) {
            this.map.addMapObject(lotContainer);
            this.map.addMapObject(streetContainer);
        }

        if (parkingIcon == null){
            parkingIcon = new Image();
            try {
                parkingIcon.setImageResource(R.mipmap.parking);
            }
            catch (Exception e) { parkingIcon = null; }
        }
    }

    public static ArrayList<ParkingLot> getMockLotParkingList(){

        //Oporto mock example
        //GeoCoordinate center = new GeoCoordinate(41.161642, -8.621453);
        //Aveiro mock example
        //GeoCoordinate center = new GeoCoordinate(40.629793,-8.641643);
        GeoCoordinate center = new GeoCoordinate(40.637796, -8.635491);

        ArrayList<ParkingLotCategory> categories = new ArrayList<>();
        categories.add(new ParkingLotCategory("Aparcamiento","Subterráneo"));

        ArrayList<ParkingAccess> entrances = new ArrayList<>();
        ArrayList<ParkingAccess> exits = new ArrayList<>();

        ArrayList<Parking.VehicleType> allowedVehicles = new ArrayList<>();
        allowedVehicles.add(Parking.VehicleType.Car);
        allowedVehicles.add(Parking.VehicleType.Motorbike);
        allowedVehicles.add(Parking.VehicleType.Bicycle);

        ArrayList<ParkingLot> lotParkings = new ArrayList<>();
        lotParkings.add(new ParkingLot(
                new Parking(center, null, false, 60, 200, 32, 10, 0.15f, "08:00", "20:00", 0.86f,
                        allowedVehicles, Parking.ParkingDisposition.Perpendicular,"12/11/2015 00:00:00"),
                categories,
                100,
                22,
                entrances,
                exits,
                4.4f,
                2.1f,
                3.8f
        ));
        return lotParkings;
    }

    public void drawStreetParkings(ArrayList<StreetParking> streetParkings){
        if (streetParkings.size() != 0){
            clearStreetMarkers(streetParkings);
        }
        for (int i=0;i<streetParkings.size();i++) {
            StreetParking streetParking = streetParkings.get(i);
            MapLabeledMarker strParkingMarker = new MapLabeledMarker(streetParking.getCenter() );

            if (parkingIcon == null)
                strParkingMarker.setIcon(IconCategory.PARKING_AREA);
            else
                strParkingMarker.setIcon(parkingIcon);

            strParkingMarker.setReserveOverlayType(MapOverlayType.FOREGROUND_OVERLAY);
            strParkingMarker.setLabelText(map.getMapDisplayLanguage(),
                    Integer.toString(streetParking.getAvailableSpotNumber()));
            strParkingMarker.setFontScalingFactor(1.5f);
            streetContainer.addMapObject(strParkingMarker);

            for (int j=0;j<streetParking.getParkingPolygons();j++) {
                MapPolygon streetPolygon = new MapPolygon(streetParking.getParkingAreaPolygonAt(j));
                streetPolygon.setLineColor(Color.parseColor("#FF0000FF"));
                streetPolygon.setFillColor(Color.parseColor("#770000FF"));
                streetContainer.addMapObject(streetPolygon);
                lastStrPolygons.add(new MapPolygonWrapper(streetPolygon,i));
            }
        }
    }

    public void drawParkingLots(ArrayList<ParkingLot> lotParkings){
        if (lotParkings.size() != 0) clearLotMarkers(lotParkings);
        for (int i=0; i<lotParkings.size();i++){
            ParkingLot lotParking = lotParkings.get(i);

            MapLabeledMarker lotParkingMarker = new MapLabeledMarker(lotParking.getCenter());

            if (parkingIcon == null)
                lotParkingMarker.setIcon(IconCategory.PARKING_AREA);
            else
                lotParkingMarker.setIcon(parkingIcon);

            lotParkingMarker.setReserveOverlayType(MapOverlayType.FOREGROUND_OVERLAY);
            lotParkingMarker.setFontScalingFactor(1.5f);
            lotParkingMarker = lotParkingMarker.setLabelText(map.getMapDisplayLanguage(),
                    Integer.toString(lotParking.getAvailableSpotNumber()));
            lotContainer.addMapObject(lotParkingMarker);

            //Creating a default circle with 10 meters radius
            MapCircle circle = new MapCircle(10,lotParking.getCenter());
            circle.setLineColor(Color.parseColor("#FF0000FF"));
            circle.setFillColor(Color.parseColor("#770000FF"));
            lotContainer.addMapObject(circle);
        }
    }

    public void clearMarkers(boolean shouldDestroy){
        clearLotMarkers();
        clearStreetMarkers();
        if (shouldDestroy) {
            map.removeMapObject(lotContainer);
            map.removeMapObject(streetContainer);
            lotContainer = null;
            streetContainer = null;
            status = false;
        }
    }

    public void setParkingOverlayActive (boolean active){
        status = active;
        if (active){
            map.addMapObject(lotContainer);
            map.addMapObject(streetContainer);
        }
        else {
            map.removeMapObject(lotContainer);
            map.removeMapObject(streetContainer);
        }
    }

    private void clearLotMarkers(){
        lotContainer.removeAllMapObjects();
        lastLotParkings.clear();
    }

    private void clearStreetMarkers(){
        streetContainer.removeAllMapObjects();
        lastStrParkings.clear();
        lastStrPolygons.clear();
    }

    private void clearLotMarkers(ArrayList<ParkingLot> lotParkings){
        clearLotMarkers();
        lastLotParkings.addAll(lotParkings);
    }
    private void clearStreetMarkers(ArrayList<StreetParking> streetParkings){
        clearStreetMarkers();
        lastStrParkings.addAll(streetParkings);
    }

    public StreetParking streetParkingSelected (GeoCoordinate geo){
        if (status)
            for (int i=0;i<lastStrParkings.size();i++){
                if ((lastStrParkings.get(i).getCenter().getLatitude() == geo.getLatitude())
                    && (lastStrParkings.get(i).getCenter().getLongitude() == geo.getLongitude()))
                    return lastStrParkings.get(i);
            }
        return null;
    }

    public StreetParking streetParkingSelected (MapPolygon polygon){
        if (status){
            for (int i=0;i<lastStrPolygons.size();i++){
                if (lastStrPolygons.get(i).mapPolygon.equals(polygon)){
                    return lastStrParkings.get(lastStrPolygons.get(i).streetParkingIndex);
                }
            }
        }
        return null;
    }

    public ParkingLot parkingLotSelected (GeoCoordinate geo){
        if (status)
            for (int i=0;i<lastLotParkings.size();i++){
                if ((lastLotParkings.get(i).getCenter().getLatitude() == geo.getLatitude())
                        && (lastLotParkings.get(i).getCenter().getLongitude() == geo.getLongitude()))
                    return lastLotParkings.get(i);
            }
        return null;
    }

}
