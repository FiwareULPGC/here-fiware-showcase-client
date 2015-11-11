package fiware.smartparking.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolygon;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import fiware.smartparking.models.Parking;
import fiware.smartparking.models.StreetParking;

/**
 * Created by Ulpgc on 07/11/2015.
 */
public class StreetParkingQueryJSONTask extends AsyncTask<Void, Void, String>
{
    public static String TAG = "StrParkingQueryJSONTask";
    public static String SERVER_ERROR = "Server Error";
    public static String QUERY_TYPE_ERROR = "Found a non street parking element";
    public static String QUERY_NO_VALUE_ERROR = "0 street parkings are found";

    private ParkingDrawTask drawTask = null;
    private MapChangeListener changeListener = null;
    private GeoBoundingBox gbb;

    public StreetParkingQueryJSONTask (ParkingDrawTask drawTask, GeoBoundingBox gbb){
        this.drawTask = drawTask;
        this.gbb = gbb;
    }

    public StreetParkingQueryJSONTask (MapChangeListener changeListener, GeoBoundingBox gbb){
        this.changeListener = changeListener;
        this.gbb = gbb;
    }

    @Override
    protected void onPreExecute() {

    }

    protected String doInBackground (Void... paramss){
        try {
            String urlBuilder = "http://fiware-aveiro.citibrain.com:1026/v1/queryContext/";

            URL url = new URL(urlBuilder);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept","application/json");

            byte[] outputInBytes = getRequestBody().getBytes("UTF-8");
            OutputStream os = conn.getOutputStream();
            os.write(outputInBytes);
            os.close();

            InputStream is = conn.getInputStream();
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);

            conn.disconnect();

            return responseStrBuilder.toString();
        }
        catch (Exception e) { e.printStackTrace(); return SERVER_ERROR; }
    }

    @Override
    protected void onPostExecute(String jsonResponse) {
        if (jsonResponse.contentEquals(SERVER_ERROR)) {
            Log.e(TAG, jsonResponse);
            return;
        }
        try {
            JSONObject queryResponse = new JSONObject(jsonResponse);
            ArrayList<StreetParking> parkingList = new ArrayList<>();
            JSONArray contextResponses = queryResponse.getJSONArray("contextResponses");
            for (int i=0; i<contextResponses.length();i++){
                JSONObject contextElement = contextResponses.getJSONObject(i).getJSONObject("contextElement");
                parkingList.add(parseStreetElement(contextElement));
            }
            if (drawTask != null) {
                TextToSpeechUtils.setStreetParkingList(parkingList);
                drawTask.drawStreetParkings(gbb, parkingList);
            }
            if (changeListener != null){
                changeListener.resetOverlays();
                changeListener.drawStreetParkings(gbb, parkingList);
            }
        }
        catch (Exception E){
            Log.e(TAG,QUERY_NO_VALUE_ERROR);
        }
    }

    ////////////////////////////////

    private StreetParking parseStreetElement(JSONObject ctxElement){
        try {
            if (!ctxElement.getString("type").contentEquals("StreetParking")) {
                Log.e(TAG, QUERY_TYPE_ERROR);
                return null;
            }

            JSONArray attributes = ctxElement.getJSONArray("attributes");

            //Street parking properties to retrieve
            GeoCoordinate centerCoords = null;
            ArrayList<GeoPolygon> location = null;
            ArrayList<Parking.VehicleType> allowedVehicles = null;
            int availableSpotNumber = 0, totalSpotNumber = 0, extraSpotNumber = 0, maximumAllowedDuration = 0;
            boolean isMetered = false, spotDelimited = false;
            String openingTime = "", closingTime = "";
            float pricePerMinute = 0.0f, spotFindingProbability = 0.0f;
            Parking.ParkingDisposition parkingDisposition = null;

            for (int i=0; i<attributes.length();i++){
                JSONObject element = attributes.getJSONObject(i);
                if (element.getString("name").contentEquals("allowedVehicles"))
                    allowedVehicles = retrieveAllowedVehiclesList(element);
                else if (element.getString("name").contentEquals("availableSpotNumber"))
                    availableSpotNumber = Integer.parseInt(element.getString("value"));
                else if (element.getString("name").contentEquals("capacity"))
                    totalSpotNumber = Integer.parseInt(element.getString("value"));
                else if (element.getString("name").contentEquals("center")
                        || element.getString("name").contentEquals("coordinates"))
                    centerCoords = retrieveCenterCoords(element);
                else if (element.getString("name").contentEquals("location"))
                    location = retrieveParkingArea(element);
                else if (element.getString("name").contentEquals("parking_disposition"))
                    parkingDisposition = retrieveParkingDisposition(element);
            }
            //Creating a parking
            Parking parking = new Parking(
                    centerCoords,
                    location,
                    isMetered,
                    maximumAllowedDuration,
                    totalSpotNumber,
                    availableSpotNumber,
                    extraSpotNumber,
                    pricePerMinute,
                    openingTime,
                    closingTime,
                    spotFindingProbability,
                    allowedVehicles,
                    parkingDisposition
            );
            return new StreetParking(parking,spotDelimited);
        }
        catch (Exception E) {
            E.printStackTrace();
            return null;
        }
    }

    private ArrayList<Parking.VehicleType> retrieveAllowedVehiclesList(JSONObject element){
        ArrayList<Parking.VehicleType> allowedVehicles = new ArrayList<>();
        try {
            JSONArray value = element.getJSONArray("value");
            for (int i=0;i<value.length();i++){
                if (value.getString(i).contentEquals("cars"))
                    allowedVehicles.add(Parking.VehicleType.Car);
                else if (value.getString(i).contentEquals("motorcycles"))
                    allowedVehicles.add(Parking.VehicleType.Motorbike);
                else if (value.getString(i).contentEquals("bicycles"))
                    allowedVehicles.add(Parking.VehicleType.Bicycle);
            }
        }
        catch (Exception e) {e.printStackTrace();}
        return allowedVehicles;
    }

    private Parking.ParkingDisposition retrieveParkingDisposition(JSONObject element){
        try {
            if (element.getString("value").contentEquals("Perpendicular"))
                return Parking.ParkingDisposition.Perpendicular;
            else if (element.getString("value").contentEquals("Parallel"))
                return Parking.ParkingDisposition.Parallel;
            else if (element.getString("value").contentEquals("Angle"))
                return Parking.ParkingDisposition.Angle;
            else
                return null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private GeoCoordinate retrieveCenterCoords(JSONObject element){
        try {
            String value = element.getString("value");
            String[] parts = value.split(",");
            //return new GeoCoordinate(Double.parseDouble(parts[1]),Double.parseDouble(parts[0]));
            return new GeoCoordinate(Double.parseDouble(parts[0]),Double.parseDouble(parts[1]));
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<GeoPolygon> retrieveParkingArea(JSONObject element){
        ArrayList<GeoPolygon> parkingArea = new ArrayList<>();
        try {
            JSONArray multipolygonData = element.getJSONArray("value");
            for (int i=0; i< multipolygonData.length(); i++){
                ArrayList<GeoCoordinate> line = new ArrayList<>();
                JSONArray lineData = multipolygonData.getJSONArray(i).getJSONArray(0);
                for (int j=0; j<lineData.length()-1; j++){
                    line.add(new GeoCoordinate(
                            lineData.getJSONArray(j).getDouble(0),
                            lineData.getJSONArray(j).getDouble(1)
                    ));
                }
                parkingArea.add(new GeoPolygon(line));
            }
        }
        catch (Exception e) { e.printStackTrace(); }
        return parkingArea;
    }

    private String getRequestBody(){
        GeoCoordinate corner1 = new GeoCoordinate(gbb.getTopLeft().getLatitude(), gbb.getBottomRight().getLongitude());
        GeoCoordinate corner2 = new GeoCoordinate(gbb.getBottomRight().getLatitude(),gbb.getTopLeft().getLongitude());
        //TODO: pagination

        String rqBody = "{\"entities\": [{ \"type\" : \"StreetParking\",\"isPattern\": \"true\",\"id\": \".*\"}],";
        rqBody = rqBody.concat("\"restriction\" : {\"scopes\" : [{\"type\":\"FIWARE::Location\"," );
        rqBody = rqBody.concat("\"value\":{\"polygon\" : { \"vertices\" : [");

        rqBody = rqBody.concat("{\"latitude\":\"").concat(Double.toString(gbb.getTopLeft().getLatitude()));
        rqBody = rqBody.concat("\",\"longitude\":\"").concat(Double.toString(gbb.getTopLeft().getLongitude()));
        rqBody = rqBody.concat("\"},");
        rqBody = rqBody.concat("{\"latitude\":\"").concat(Double.toString(corner1.getLatitude()));
        rqBody = rqBody.concat("\",\"longitude\":\"").concat(Double.toString(corner1.getLongitude()));
        rqBody = rqBody.concat("\"},");
        rqBody = rqBody.concat("{\"latitude\":\"").concat(Double.toString(gbb.getBottomRight().getLatitude()));
        rqBody = rqBody.concat("\",\"longitude\":\"").concat(Double.toString(gbb.getBottomRight().getLongitude()));
        rqBody = rqBody.concat("\"},");
        rqBody = rqBody.concat("{\"latitude\":\"").concat(Double.toString(corner2.getLatitude()));
        rqBody = rqBody.concat("\",\"longitude\":\"").concat(Double.toString(corner2.getLongitude()));
        rqBody = rqBody.concat("\"}]}}}]}}");

        return rqBody;
    }
}


