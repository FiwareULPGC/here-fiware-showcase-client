package fiware.smartparking.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolygon;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import fiware.smartparking.models.Parking;
import fiware.smartparking.models.ParkingAccess;
import fiware.smartparking.models.ParkingLot;
import fiware.smartparking.models.ParkingLotCategory;
import fiware.smartparking.models.StreetParking;

/**
 * Created by Ulpgc on 07/11/2015.
 */
public class ParkingLotQueryJSONTask extends AsyncTask<Void, Void, String> {
    public static String TAG = "ParkingLotQueryJSONTask";
    public static String SERVER_ERROR = "Server Error";
    public static String QUERY_TYPE_ERROR = "Found a non parking lot element";
    public static String QUERY_NO_VALUE_ERROR = "0 street parkings are found";

    private ParkingDrawTask drawTask;
    private GeoBoundingBox gbb;

    public ParkingLotQueryJSONTask(ParkingDrawTask drawTask, GeoBoundingBox gbb) {
        this.drawTask = drawTask;
        this.gbb = gbb;
    }

    @Override
    protected void onPreExecute() {
    }

    protected String doInBackground(Void... paramss) {
        String str = makeRequest("http://fiware-aveiro.citibrain.com:1026/v1/queryContext/", getRequestBody());
        if (str == null) return SERVER_ERROR;
        else return str;
    }

    public static String makeRequest(String uri, String data) {
        HttpURLConnection urlConnection;
        String result = null;
        try {
            //Connect
            urlConnection = (HttpURLConnection) ((new URL(uri).openConnection()));
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Connection","close");
            urlConnection.setRequestMethod("POST");
            urlConnection.connect();

            //Write
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(data);
            writer.close();
            outputStream.close();

            //Read
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            bufferedReader.close();
            result = sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String jsonResponse) {
        if (jsonResponse.contentEquals(SERVER_ERROR)) {
            Log.e(TAG, jsonResponse);
            return;
        }
        try {
            JSONObject queryResponse = new JSONObject(jsonResponse);
            ArrayList<ParkingLot> parkingList = new ArrayList<>();
            JSONArray contextResponses = queryResponse.getJSONArray("contextResponses");
            for (int i = 0; i < contextResponses.length(); i++) {
                JSONObject contextElement = contextResponses.getJSONObject(i).getJSONObject("contextElement");
                parkingList.add(parseLotElement(contextElement));
            }
            if (drawTask != null) {
                TextToSpeechUtils.setLotParkingList(parkingList);
                drawTask.drawParkingLots(parkingList);
            }
        } catch (Exception E) {
            Log.e(TAG, QUERY_NO_VALUE_ERROR);
            Log.e(QUERY_NO_VALUE_ERROR, jsonResponse);
            Log.e("REQUESTED", getRequestBody());
        }
    }

    ////////////////////////////////

    private ParkingLot parseLotElement(JSONObject ctxElement) {
        try {
            if (!ctxElement.getString("type").contentEquals("ParkingLot")) {
                Log.e(TAG, QUERY_TYPE_ERROR);
                return null;
            }

            JSONArray attributes = ctxElement.getJSONArray("attributes");

            //Street parking properties to retrieve
            GeoCoordinate centerCoords = null;
            ArrayList<GeoPolygon> location = null;
            ArrayList<Parking.VehicleType> allowedVehicles = null;
            int availableSpotNumber = 0, totalSpotNumber = 0, extraSpotNumber = 0, maximumAllowedDuration = 0;
            boolean isMetered = false;
            String openingTime = "", closingTime = "", lastUpdated = "";
            float pricePerMinute = 0.0f, spotFindingProbability = 0.0f;
            Parking.ParkingDisposition parkingDisposition = null;

            for (int i = 0; i < attributes.length(); i++) {
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
                else if (element.getString("name").contentEquals("lastUpdated"))
                    lastUpdated = element.getString("value");
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
                    parkingDisposition,
                    lastUpdated
            );

            //Parking Lot special variables = default
            ArrayList<ParkingLotCategory> categories = null;
            int totalStoryNumber = 0, firstAvailableStory = 0;
            float averageSpotWidth = 0, averageSpotLength = 0, userRating = 0;
            ArrayList<ParkingAccess> entrances = null, exits = null;

            return new ParkingLot(parking, categories,totalStoryNumber,firstAvailableStory,entrances,
                    exits,averageSpotWidth,averageSpotLength,userRating);
        } catch (Exception E) {
            E.printStackTrace();
            return null;
        }
    }

    private ArrayList<Parking.VehicleType> retrieveAllowedVehiclesList(JSONObject element) {
        ArrayList<Parking.VehicleType> allowedVehicles = new ArrayList<>();
        try {
            JSONArray value = element.getJSONArray("value");
            for (int i = 0; i < value.length(); i++) {
                if (value.getString(i).contentEquals("cars"))
                    allowedVehicles.add(Parking.VehicleType.Car);
                else if (value.getString(i).contentEquals("motorcycles"))
                    allowedVehicles.add(Parking.VehicleType.Motorbike);
                else if (value.getString(i).contentEquals("bicycles"))
                    allowedVehicles.add(Parking.VehicleType.Bicycle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allowedVehicles;
    }

    private Parking.ParkingDisposition retrieveParkingDisposition(JSONObject element) {
        try {
            if (element.getString("value").contentEquals("Perpendicular"))
                return Parking.ParkingDisposition.Perpendicular;
            else if (element.getString("value").contentEquals("Parallel"))
                return Parking.ParkingDisposition.Parallel;
            else if (element.getString("value").contentEquals("Angle"))
                return Parking.ParkingDisposition.Angle;
            else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private GeoCoordinate retrieveCenterCoords(JSONObject element) {
        try {
            String value = element.getString("value");
            String[] parts = value.split(",");
            return new GeoCoordinate(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<GeoPolygon> retrieveParkingArea(JSONObject element) {
        ArrayList<GeoPolygon> parkingArea = new ArrayList<>();
        try {
            JSONArray multipolygonData = element.getJSONArray("value");
            for (int i = 0; i < multipolygonData.length(); i++) {
                ArrayList<GeoCoordinate> line = new ArrayList<>();
                JSONArray lineData = multipolygonData.getJSONArray(i).getJSONArray(0);
                for (int j = 0; j < lineData.length() - 1; j++) {
                    line.add(new GeoCoordinate(
                            lineData.getJSONArray(j).getDouble(0),
                            lineData.getJSONArray(j).getDouble(1)
                    ));
                }
                parkingArea.add(new GeoPolygon(line));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parkingArea;
    }

    private String getRequestBody() {
        GeoCoordinate corner1 = new GeoCoordinate(gbb.getTopLeft().getLatitude(), gbb.getBottomRight().getLongitude());
        GeoCoordinate corner2 = new GeoCoordinate(gbb.getBottomRight().getLatitude(), gbb.getTopLeft().getLongitude());

        DecimalFormat df = new DecimalFormat();
        df.setMaximumIntegerDigits(3);
        df.setMaximumFractionDigits(13);
        df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));

        String rqBody = "{\"entities\":[{\"type\":\"ParkingLot\",\"isPattern\":\"true\",\"id\":\".*\"}],";
        rqBody = rqBody.concat("\"restriction\":{\"scopes\":[{\"type\":\"FIWARE::Location\",");
        rqBody = rqBody.concat("\"value\":{\"polygon\":{\"vertices\":[");

        rqBody = rqBody.concat("{\"latitude\":\"").concat(df.format(gbb.getTopLeft().getLatitude()));
        rqBody = rqBody.concat("\",\"longitude\":\"").concat(df.format(gbb.getTopLeft().getLongitude()));
        rqBody = rqBody.concat("\"},");
        rqBody = rqBody.concat("{\"latitude\":\"").concat(df.format(corner1.getLatitude()));
        rqBody = rqBody.concat("\",\"longitude\":\"").concat(df.format(corner1.getLongitude()));
        rqBody = rqBody.concat("\"},");
        rqBody = rqBody.concat("{\"latitude\":\"").concat(df.format(gbb.getBottomRight().getLatitude()));
        rqBody = rqBody.concat("\",\"longitude\":\"").concat(df.format(gbb.getBottomRight().getLongitude()));
        rqBody = rqBody.concat("\"},");
        rqBody = rqBody.concat("{\"latitude\":\"").concat(df.format(corner2.getLatitude()));
        rqBody = rqBody.concat("\",\"longitude\":\"").concat(df.format(corner2.getLongitude()));
        rqBody = rqBody.concat("\"}]}}}]}}\n");

        return rqBody;
    }

}

