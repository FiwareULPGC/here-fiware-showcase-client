package fiware.smartparking.models;

import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Ulpgc on 04/11/2015.
 */
public class ParkingLot extends Parking {

    private ArrayList<ParkingLotCategory> category;
    private int totalStoryNumber;
    private int firstAvailableStory;
    private ArrayList<ParkingAccess> entrances;
    private ArrayList<ParkingAccess> exits;
    private float averageSpotWidth;
    private float averageSpotLength;
    private float userRating; //¿Number?

    public ParkingLot (Parking baseParking, ArrayList<ParkingLotCategory> category, int totalStoryNumber,
                       int firstAvailableStory, ArrayList<ParkingAccess> entrances, ArrayList<ParkingAccess> exits,
                       float averageSpotWidth, float averageSpotLength, float userRating){
        super(baseParking.getLocation(),baseParking.isMetered(),baseParking.getMaximumAllowedDuration(),
                baseParking.getTotalSpotNumber(),baseParking.getAvailableSpotNumber(),baseParking.getExtraSpotNumber(),
                baseParking.getPricePerMinute(), baseParking.getOpeningTime(),baseParking.getClosingTime(),
                baseParking.getProbabilityOfSpotFinding(),baseParking.getParkingDisposition());

        this.category = category;
        this.totalStoryNumber = totalStoryNumber;
        this.firstAvailableStory = firstAvailableStory;
        this.entrances = entrances;
        this.exits = exits;
        this.averageSpotLength = averageSpotLength;
        this.averageSpotWidth = averageSpotWidth;
        this.userRating = userRating;
    }

    public ParkingLotCategory getCategoryAt(int index) { return category.get(index); }
    public int getCategorySize() { return category.size();}
    public ArrayList<ParkingLotCategory> getCategory() { return category;}

    public int getTotalStoryNumber() {return totalStoryNumber;}
    public int getFirstAvailableStory() {return firstAvailableStory;}
    public ArrayList<ParkingAccess> getExits() {return exits;}
    public ParkingAccess getExitAt(int index) {return exits.get(index);}
    public int getExitsSize() {return exits.size();}
    public ArrayList<ParkingAccess> getEntrances() {return entrances;}
    public ParkingAccess getEntrancesAt(int index){return entrances.get(index);}
    public int getEntrancesSize() {return entrances.size();}
    public float getAverageSpotWidth() { return averageSpotWidth;}
    public float getAverageSpotLength() {return averageSpotLength;}
    public float getUserRating() {return userRating;}

    public void addCategory(ParkingLotCategory category) { this.category.add(category);}
    public void clearCategory() {this.category.clear();}
    public void addEntrance(ParkingAccess entrance) {entrances.add(entrance);}
    public void clearEntrances() {entrances.clear();}
    public void addExit(ParkingAccess exit) {exits.add(exit);}
    public void clearExits() {exits.clear();}

    public void setTotalStoryNumber(int totalStoryNumber) {this.totalStoryNumber = totalStoryNumber;}
    public void setFirstAvailableStory(int firstAvailableStory){this.firstAvailableStory = firstAvailableStory;}
    public void setAverageSpotWidth(float averageSpotWidth) {this.averageSpotWidth = averageSpotWidth;}
    public void setAverageSpotLength(float averageSpotLength) {this.averageSpotLength = averageSpotLength;}
    public void setUserRating(float userRating) {this.userRating = userRating;}
}