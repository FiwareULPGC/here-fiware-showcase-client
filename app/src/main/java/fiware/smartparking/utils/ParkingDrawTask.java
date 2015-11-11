package fiware.smartparking.utils;

import android.graphics.Color;
import android.util.Log;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolygon;
import com.here.android.mpa.common.GeoPolyline;
import com.here.android.mpa.common.IconCategory;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapCircle;
import com.here.android.mpa.mapping.MapContainer;
import com.here.android.mpa.mapping.MapLabeledMarker;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapOverlayType;
import com.here.android.mpa.mapping.MapPolygon;
import com.here.android.mpa.mapping.MapPolyline;

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

    Map map;
    MapContainer container;
    Image parkingIcon;

    public ParkingDrawTask(Map map){

        this.map = map;
        container = new MapContainer();
        this.map.addMapObject(container);

        parkingIcon = new Image();
        try {
            parkingIcon.setImageResource(R.mipmap.parking);
        }
        catch (Exception e) { parkingIcon = null; }
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
                        allowedVehicles, Parking.ParkingDisposition.Perpendicular),
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

    public static ArrayList<StreetParking> getMockStreetParkingList(){
       // GeoCoordinate center = new GeoCoordinate(40.637296, -8.635791);
        GeoCoordinate center = new GeoCoordinate(40.638296, -8.636791);


        ArrayList<GeoCoordinate> geoCoords = new ArrayList<>();
        /*geoCoords.add(new GeoCoordinate(41.162642, -8.622453));
        geoCoords.add(new GeoCoordinate(41.162642, -8.621453));*/
        geoCoords.add(new GeoCoordinate(40.637796, -8.636791));
        geoCoords.add(new GeoCoordinate(40.638796, -8.636791));
        geoCoords.add(new GeoCoordinate(40.638796, -8.635791));
        geoCoords.add(new GeoCoordinate(40.637796, -8.635791));

        ArrayList<GeoPolygon> geoPolygons = new ArrayList<>();
        geoPolygons.add(new GeoPolygon(geoCoords));

        ArrayList<Parking.VehicleType> allowedVehicles = new ArrayList<>();
        allowedVehicles.add(Parking.VehicleType.Car);
        allowedVehicles.add(Parking.VehicleType.Motorbike);
        allowedVehicles.add(Parking.VehicleType.Bicycle);

        ArrayList<StreetParking> strParkings = new ArrayList<>();

        strParkings.add(new StreetParking(
                new Parking(center, geoPolygons, false, 60, 80, 10, 10, 0.05f, "08:00", "20:00", 0.86f,
                        allowedVehicles, Parking.ParkingDisposition.Parallel), false
        ));

        return strParkings;
    }

    public void drawParkings (GeoBoundingBox gbb, ArrayList<StreetParking> streetParkings,
                              ArrayList<ParkingLot> lotParkings){
        if (streetParkings == null || lotParkings == null) return;

        drawStreetParkings(gbb, streetParkings);
        drawParkingLots(gbb,lotParkings);
    }

    public void drawStreetParkings(GeoBoundingBox gbb, ArrayList<StreetParking> streetParkings){
        for (int i=0;i<streetParkings.size();i++) {
            StreetParking streetParking = streetParkings.get(i);
            MapLabeledMarker strParkingMarker = new MapLabeledMarker(streetParking.getCenter() );

            if (parkingIcon == null)
                strParkingMarker.setIcon(IconCategory.PARKING_AREA);
            else
                strParkingMarker.setIcon(parkingIcon);


            strParkingMarker.setLabelText(map.getMapDisplayLanguage(),
                    Integer.toString(streetParking.getAvailableSpotNumber()));
            strParkingMarker.setFontScalingFactor(1.5f);
            container.addMapObject(strParkingMarker);


            for (int j=0;j<streetParking.getParkingPolygons();j++) {
                MapPolygon streetPolygon = new MapPolygon(streetParking.getParkingAreaPolygonAt(j));
                streetPolygon.setLineColor(Color.parseColor("#FF0000FF"));//(Color.GREEN);
                streetPolygon.setFillColor(Color.parseColor("#770000FF"));
                container.addMapObject(streetPolygon);
            }
        }
    }

    public void drawParkingLots(GeoBoundingBox gbb, ArrayList<ParkingLot> lotParkings){
        for (int i=0; i<lotParkings.size();i++){
            ParkingLot lotParking = lotParkings.get(i);
            if (gbb.contains(lotParking.getCenter())) {
                MapLabeledMarker lotParkingMarker = new MapLabeledMarker(lotParking.getCenter());

                if (parkingIcon == null)
                    lotParkingMarker.setIcon(IconCategory.PARKING_AREA);
                else
                  lotParkingMarker.setIcon(parkingIcon);

                lotParkingMarker.setFontScalingFactor(1.5f);
                lotParkingMarker = lotParkingMarker.setLabelText(map.getMapDisplayLanguage(),
                        Integer.toString(lotParking.getAvailableSpotNumber()));
                container.addMapObject(lotParkingMarker);

                //Creating a default circle with 10 meters radius
                MapCircle circle = new MapCircle(10,lotParking.getCenter());
                circle.setLineColor(Color.parseColor("#FF0000FF"));//(Color.GREEN);
                circle.setFillColor(Color.parseColor("#770000FF"));
                container.addMapObject(circle);


            }
        }
    }

    public void clearMarkers(){
        map.removeMapObject(container);
        container.removeAllMapObjects();
        container = null;
    }
}
