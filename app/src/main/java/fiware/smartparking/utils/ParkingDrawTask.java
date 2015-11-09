package fiware.smartparking.utils;

import android.graphics.Color;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolyline;
import com.here.android.mpa.common.IconCategory;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapContainer;
import com.here.android.mpa.mapping.MapLabeledMarker;
import com.here.android.mpa.mapping.MapPolyline;

import java.util.ArrayList;

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

    public ParkingDrawTask(Map map){

        this.map = map;
        container = new MapContainer();
        this.map.addMapObject(container);
    }

    public static ArrayList<ParkingLot> getMockLotParkingList(){

        //Oporto mock example
        //GeoCoordinate center = new GeoCoordinate(41.161642, -8.621453);
        //Aveiro mock example
        GeoCoordinate center = new GeoCoordinate(40.629793,-8.641643);

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
        GeoCoordinate center = new GeoCoordinate(41.162642,-8.621953);

        ArrayList<GeoCoordinate> geoCoords = new ArrayList<>();
        geoCoords.add(new GeoCoordinate(41.162642, -8.622453));
        geoCoords.add(new GeoCoordinate(41.162642, -8.621453));

        ArrayList<GeoPolyline> geoPolylines = new ArrayList<>();
        geoPolylines.add(new GeoPolyline(geoCoords));

        ArrayList<Parking.VehicleType> allowedVehicles = new ArrayList<>();
        allowedVehicles.add(Parking.VehicleType.Car);
        allowedVehicles.add(Parking.VehicleType.Motorbike);
        allowedVehicles.add(Parking.VehicleType.Bicycle);

        ArrayList<StreetParking> strParkings = new ArrayList<>();

        strParkings.add(new StreetParking(
                new Parking(center, geoPolylines, false, 60, 80, 10, 10, 0.05f, "08:00", "20:00", 0.86f,
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
            if (gbb.contains(streetParking.getCenter())){

                MapLabeledMarker strParkingMarker = new MapLabeledMarker(streetParking.getCenter() );
                strParkingMarker.setIcon(IconCategory.PARKING_AREA);
                strParkingMarker.setLabelText(map.getMapDisplayLanguage(),
                        Integer.toString(streetParking.getAvailableSpotNumber()));
                container.addMapObject(strParkingMarker);

                for (int j=0;j<streetParking.getParkingPolylines();j++) {
                    MapPolyline streetPolyline = new MapPolyline(streetParking.getParkingAreaPolylineAt(j));
                    streetPolyline.setLineColor(Color.RED);
                    container.addMapObject(streetPolyline);
                }
            }
        }
    }

    public void drawParkingLots(GeoBoundingBox gbb, ArrayList<ParkingLot> lotParkings){
        for (int i=0; i<lotParkings.size();i++){
            ParkingLot lotParking = lotParkings.get(i);
            if (gbb.contains(lotParking.getCenter())) {
                MapLabeledMarker lotParkingMarker = new MapLabeledMarker(lotParking.getCenter());
                lotParkingMarker.setIcon(IconCategory.PARKING_AREA);
                lotParkingMarker = lotParkingMarker.setLabelText(map.getMapDisplayLanguage(),
                        Integer.toString(lotParking.getAvailableSpotNumber()));
                container.addMapObject(lotParkingMarker);
            }
        }
    }

    public void clearMarkers(){
        map.removeMapObject(container);
        container.removeAllMapObjects();
        container = null;
    }
}
