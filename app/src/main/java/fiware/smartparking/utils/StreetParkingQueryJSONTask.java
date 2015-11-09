package fiware.smartparking.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolyline;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import fiware.smartparking.models.Parking;
import fiware.smartparking.models.StreetParking;

/**
 * Created by Ulpgc on 07/11/2015.
 */
public class StreetParkingQueryJSONTask extends AsyncTask<Void, Void, String>
{
    public static String SERVER_ERROR = "Server Error";
    public static String UNSUPPORTED_ERROR = "Unsupported exception Error";
    public static String IO_ERROR = "IO Exception Error";

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

    protected String doInBackground(Void... paramss) {
        try {
            HttpClient httpClient =  new DefaultHttpClient();

            StringBuilder urlBuilder = new StringBuilder();

            urlBuilder.append("http://fiware-aveiro.citibrain.com:1026/v1/queryContext/");

            HttpPost request = new HttpPost(urlBuilder.toString());
            request.setHeader("Accept","application/json");
            request.setHeader("Content-Type","application/json");
            //TODO: filter by location
            //TODO: pagination
            //TODO: using non deprecated entities
            String requestBody = "{\"entities\": [{ \"type\" : \"StreetParking\",\"isPattern\": \"true\",\"id\": \".*\"}]}";
            try {
                JSONObject object = new JSONObject(requestBody);
                request.setEntity(new StringEntity(object.toString(), "UTF8"));
            }
            catch (Exception E){
                request.setEntity(new StringEntity("","UTF8"));
            }
            HttpResponse response = httpClient.execute(request);

            final int statusCode = response.getStatusLine().getStatusCode();
            final String jsonResponse = EntityUtils.toString(response.getEntity());

            if (statusCode != HttpStatus.SC_OK) {
                return SERVER_ERROR;
            } else {
                return jsonResponse;
            }
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return UNSUPPORTED_ERROR;
        } catch (IOException ex) {
            ex.printStackTrace();
            return IO_ERROR;
        }
    }

    @Override
    protected void onPostExecute(String jsonResponse) {
        if ( jsonResponse == SERVER_ERROR || jsonResponse == UNSUPPORTED_ERROR || jsonResponse == IO_ERROR) {
            Log.e("StrParkingQueryJSONTask", jsonResponse);
            return;
        }
        try {
            JSONObject queryResponse = new JSONObject(jsonResponse);
            ArrayList<StreetParking> parkingList = new ArrayList<StreetParking>();
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
            return new GeoCoordinate(Double.parseDouble(parts[1]),Double.parseDouble(parts[0]));
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


