package fiware.smartparking.models;

/**
 * Created by Ulpgc on 04/11/2015.
 */
public class StreetParking extends Parking {
    private boolean spotDelimited;

    public StreetParking (Parking baseParking, boolean spotDelimited) {
        super(baseParking.getLocation(),baseParking.isMetered(),baseParking.getMaximumAllowedDuration(),
                baseParking.getTotalSpotNumber(),baseParking.getAvailableSpotNumber(),baseParking.getExtraSpotNumber(),
                baseParking.getPricePerMinute(), baseParking.getOpeningTime(),baseParking.getClosingTime(),
                baseParking.getProbabilityOfSpotFinding(),baseParking.getParkingDisposition());
        this.spotDelimited = spotDelimited;
    }

    public boolean isSpotDelimited() {return spotDelimited;}
    public void setSpotDelimited(boolean spotDelimited) {this.spotDelimited = spotDelimited;}

}
