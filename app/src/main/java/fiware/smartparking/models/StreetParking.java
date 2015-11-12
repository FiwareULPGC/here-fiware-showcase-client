package fiware.smartparking.models;

/**
 * Created by Ulpgc on 04/11/2015.
 */
public class StreetParking extends Parking {
    private boolean spotDelimited;

    public StreetParking (Parking baseParking, boolean spotDelimited) {
        super(baseParking.getCenter(),baseParking.getParkingArea(),baseParking.isMetered(),baseParking.getMaximumAllowedDuration(),
                baseParking.getTotalSpotNumber(),baseParking.getAvailableSpotNumber(),baseParking.getExtraSpotNumber(),
                baseParking.getPricePerMinute(), baseParking.getOpeningTime(),baseParking.getClosingTime(),
                baseParking.getProbabilityOfSpotFinding(),baseParking.getAllowedVehicles(),baseParking.getParkingDisposition(),baseParking.getLastUpdated());
        this.spotDelimited = spotDelimited;
    }

    public boolean isSpotDelimited() {return spotDelimited;}
    public void setSpotDelimited(boolean spotDelimited) {this.spotDelimited = spotDelimited;}

    public String description (){
        String res = "Parking information: \n";
        res = res.concat("\nTotal spots: ").concat(Integer.toString(this.getTotalSpotNumber()));
        res = res.concat("\nAvailable spots: ").concat(Integer.toString(this.getAvailableSpotNumber()));
        res = res.concat("\nAllowed vehicles: ").concat(this.getAllowedVehiclesDescription());
        res = res.concat("\nParking disposition: ").concat(this.getParkingDisposition().name());
        res = res.concat("\nLast updated: ").concat(this.getLastUpdated());
        return res;
    }

}
