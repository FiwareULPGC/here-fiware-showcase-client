package fiware.smartparking.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolyline;

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
    public static String SERVER_ERROR = "Server Error";

    public static String QUERY_TYPE_ERROR = "Found a non street parking element";

    private ParkingDrawTask drawTask;
    private GeoBoundingBox gbb;

    public StreetParkingQueryJSONTask (ParkingDrawTask drawTask, GeoBoundingBox gbb){
        this.drawTask = drawTask;
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

            //TODO: filter by location, pagination
            String requestBody = "{\"entities\": [{ \"type\" : \"StreetParking\",\"isPattern\": \"true\",\"id\": \".*\"}]}";

            byte[] outputInBytes = requestBody.getBytes("UTF-8");
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
        if (jsonResponse.contentEquals(SERVER_ERROR)) { // || jsonResponse == UNSUPPORTED_ERROR || jsonResponse == IO_ERROR) {
            Log.e("StrParkingQueryJSONTask", jsonResponse);
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
            TextToSpeechUtils.setStreetParkingList(parkingList);
            drawTask.drawStreetParkings(gbb,parkingList);
        }
        catch (Exception E){
            E.printStackTrace();
        }
    }

    ////////////////////////////////

    private StreetParking parseStreetElement(JSONObject ctxElement){
        try {
            if (!ctxElement.getString("type").contentEquals("StreetParking")) {
                Log.e("QUERY ELEMENT ERROR", QUERY_TYPE_ERROR);
                return null;
            }

            JSONArray attributes = ctxElement.getJSONArray("attributes");

            //Street parking properties to retrieve
            GeoCoordinate centerCoords = null;
            ArrayList<GeoPolyline> location = null;
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

    private ArrayList<GeoPolyline> retrieveParkingArea(JSONObject element){
        ArrayList<GeoPolyline> parkingArea = new ArrayList<>();
        try {
            JSONArray multipolygonData = element.getJSONArray("value");
            for (int i=0; i< multipolygonData.length(); i++){
                ArrayList<GeoCoordinate> line = new ArrayList<>();
                JSONArray lineData = multipolygonData.getJSONArray(i).getJSONArray(0);
                for (int j=0; j<lineData.length(); j++){
                    line.add(new GeoCoordinate(
                            lineData.getJSONArray(j).getDouble(0),
                            lineData.getJSONArray(j).getDouble(1)
                    ));
                }
                parkingArea.add(new GeoPolyline(line));
            }
        }
        catch (Exception e) { e.printStackTrace(); }
        return parkingArea;
    }
}


