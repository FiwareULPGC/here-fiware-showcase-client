package fiware.smartparking.models;

import com.here.android.mpa.common.GeoCoordinate;

import java.util.ArrayList;

/**
 * Created by Ulpgc on 04/11/2015.
 */
public class Parking {

    public enum VehicleType {
        Car, Bicycle, Motorbike
    }

    public enum ParkingDisposition {
        Parallel, Perpendicular, Angle
    }


    private ArrayList<GeoCoordinate> location;
    private boolean metered;
    private int maximumAllowedDuration; //minutes
    private int totalSpotNumber;
    private int availableSpotNumber;
    private int extraSpotNumber; //for disabled, load-unload, etc
    private float pricePerMinute;
    //¿Enum currency?
    private ArrayList<VehicleType> allowedVehicles;
    private String openingTime; // ISO8601
    private String closingTime; // ISO8601
    private float probabilityOfSpotFinding;
    private ParkingDisposition parkingDisposition;

    //TODO: Constructors, getters, setters.
    public Parking() {}


}
