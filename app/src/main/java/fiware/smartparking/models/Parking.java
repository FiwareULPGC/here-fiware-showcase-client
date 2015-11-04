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

    public Parking(ArrayList<GeoCoordinate> location, boolean metered, int maximumAllowedDuration, int totalSpotNumber,
                   int availableSpotNumber,int extraSpotNumber, float pricePerMinute, String openingTime,
                   String closingTime, float probabilityOfSpotFinding, ParkingDisposition disposition) {


        ArrayList<VehicleType> vehicleTypes = new ArrayList<VehicleType>();
        vehicleTypes.add(VehicleType.Bicycle);
        vehicleTypes.add(VehicleType.Car);
        vehicleTypes.add(VehicleType.Motorbike);

        this.location = location;
        this.maximumAllowedDuration = maximumAllowedDuration;
        this.totalSpotNumber = totalSpotNumber;
        this.extraSpotNumber = extraSpotNumber;
        this.availableSpotNumber = availableSpotNumber;
        this.pricePerMinute = pricePerMinute;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.probabilityOfSpotFinding = probabilityOfSpotFinding;
        this.allowedVehicles = vehicleTypes;
        this.parkingDisposition = disposition;
        this.metered = metered;

    }

    public GeoCoordinate getLocationAt (int index) { return this.location.get(index); }
    public int getLocationSize() {return this.location.size();}
    public ArrayList<GeoCoordinate> getLocation() {return this.location;}

    public int getMaximumAllowedDuration() { return maximumAllowedDuration;}
    public int getTotalSpotNumber() { return totalSpotNumber; }
    public int getAvailableSpotNumber() { return  availableSpotNumber; }
    public int getExtraSpotNumber() { return  extraSpotNumber;}
    public float getPricePerMinute() {return pricePerMinute;}
    public float getProbabilityOfSpotFinding() {return  probabilityOfSpotFinding;}
    public String getOpeningTime() {return openingTime;}
    public String getClosingTime() {return closingTime;}
    public ParkingDisposition getParkingDisposition() {return parkingDisposition;}
    public boolean isMetered() {return metered;}

    public VehicleType getVehicleTypeAt (int index) {return this.allowedVehicles.get(index);}
    public int getVehicleTypesSize() {return this.allowedVehicles.size();}

    public void clearLocations() {this.location.clear();}
    public void addLocation(GeoCoordinate location) {this.location.add(location);}

    public void setMaximumAllowedDuration(int maximumAllowedDuration) {
        this.maximumAllowedDuration = maximumAllowedDuration;
    }
    public void setTotalSpotNumber(int totalSpotNumber){this.totalSpotNumber = totalSpotNumber;}
    public void setAvailableSpotNumber(int availableSpotNumber) {
        this.availableSpotNumber = availableSpotNumber;
    }
    public void setExtraSpotNumber(int extraSpotNumber) {this.extraSpotNumber = extraSpotNumber;}
    public void setPricePerMinute(float pricePerMinute) {this.pricePerMinute = pricePerMinute;}
    public void setProbabilityOfSpotFinding(float probabilityOfSpotFinding){
        this.probabilityOfSpotFinding = probabilityOfSpotFinding;
    }
    public void setOpeningTime(String openingTime) {this.openingTime = openingTime;}
    public void setClosingTime(String closingTime) {this.closingTime = closingTime;}
    public void setParkingDisposition(ParkingDisposition parkingDisposition){
        this.parkingDisposition = parkingDisposition;
    }
    public void setMetered (boolean metered) {this.metered = metered;}
    public void clearAllowedVehicles(){this.allowedVehicles.clear();}
    public void addAllowedVehicle(VehicleType vehicleType) {allowedVehicles.add(vehicleType);}

}
