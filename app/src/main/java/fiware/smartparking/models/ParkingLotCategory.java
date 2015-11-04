package fiware.smartparking.models;

/**
 * Created by Ulpgc on 04/11/2015.
 */
public class ParkingLotCategory {

    private String name;
    private String description;

    public ParkingLotCategory(String name, String description){
        this.name = name;
        this.description = description;
    }

    public String getName () { return name;}
    public String getDescription () {return description;}

    public void setName (String name) { this.name = name;}
    public void setDescription (String description) { this.description = description;}

}
