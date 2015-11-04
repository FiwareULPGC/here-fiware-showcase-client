package fiware.smartparking.models;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.search.Category;

import java.util.ArrayList;

/**
 * Created by Ulpgc on 04/11/2015.
 */
public class ParkingAccess {

    public enum ParkingAccessCategory {
        Exit, Entrance, EmergencyExit, Pedestrian
    }

    public enum ParkingAccessCharacteristics {
        Ramp, Curve
    }

    private GeoCoordinate location;
    private ParkingAccessCategory category;
    private float width;
    private float height;
    private float slope;
    private ArrayList<ParkingAccessCharacteristics> characteristics;

    public ParkingAccess (GeoCoordinate location, ParkingAccessCategory category, float width,
                          float height, float slope){
        this (location, category, width, height, slope, new ArrayList<ParkingAccessCharacteristics>());
    }

    public ParkingAccess (GeoCoordinate location, ParkingAccessCategory category, float width,
                          float height, float slope,
                          ArrayList<ParkingAccessCharacteristics> characteristics){
        this.location = location;
        this.category = category;
        this.width = width;
        this.height = height;
        this.slope = slope;
        this.characteristics = characteristics;
    }

    public GeoCoordinate getLocation() { return location; }
    public ParkingAccessCategory getCategory() { return category;}
    public float getWidth() { return width;}
    public float getHeight() { return height;}
    public float getSlope() { return slope;}
    public ParkingAccessCharacteristics getCharacteristics(int index){ return characteristics.get(index);}
    public int getNumberOfCharacteristics () { return characteristics.size();}

    public void setLocation(GeoCoordinate location) {this.location = location;}
    public void setCategory(ParkingAccessCategory category) {this.category = category;}
    public void setWidth(float width) { this.width = width;}
    public void setHeight(float height) {this.height = height;}
    public void setSlope(float slope) {this.slope = slope; }
    public void addCharacteristics (ParkingAccessCharacteristics characteristics){
        this.characteristics.add(characteristics);
    }
    public void clearCharacteristics (){
        this.characteristics.clear();
    }
}
