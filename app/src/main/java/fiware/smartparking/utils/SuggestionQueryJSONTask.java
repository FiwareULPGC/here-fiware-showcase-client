package fiware.smartparking.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Ulpgc on 03/11/2015.
 *
 * This class makes requests for the following pattern of Nokia Here Rest Api:
 * http://places.cit.api.here.com/places/v1/suggest?at=<lat>,<lon>&app_id=<app-id>
 * &app_code=<app-code>&accept=application/json&pretty&q=<Query>&size=<size>
 *
 */
public class SuggestionQueryJSONTask extends AsyncTask<Void, Void, String> {

    private double lat,lon;
    private int resultsAccepted;
    private String query;
    private Boolean pendingRequest;
    private AutoCompleteTextView view;
    private Context ctx;
    private ArrayAdapter<String> adapter;

    public static String SERVER_ERROR = "Server Error";
    private static String NO_RESULTS_FOUND = "No results found";
    private static String TAG = "SuggestionQueryError";

    public SuggestionQueryJSONTask(double lat, double lon, String query, Boolean pendingRequest,
                            AutoCompleteTextView view, ArrayAdapter<String> adapter, Context activityContext) {
        this(lat,lon,query,pendingRequest,20,view,adapter,activityContext);
    }

    public SuggestionQueryJSONTask(double lat, double lon, String query, Boolean pendingRequest,
                            int resultsAccepted, AutoCompleteTextView view, ArrayAdapter<String> adapter, Context activityContext){
        this.lat = lat;
        this.lon = lon;
        this.query = query;
        this.view = view;
        this.resultsAccepted = resultsAccepted;
        this.adapter = adapter;
        this.pendingRequest = pendingRequest;
        this.ctx  = activityContext;
    }

    @Override
    protected void onPreExecute() {
    }

    protected String doInBackground (Void... paramss){
        try {
            String urlBuilder = "http://places.cit.api.here.com/places/v1/suggest?at=";
            urlBuilder = urlBuilder.concat(Double.toString(lat));
            urlBuilder = urlBuilder.concat(",");
            urlBuilder = urlBuilder.concat(Double.toString(lon));
            urlBuilder = urlBuilder.concat("&q=");
            urlBuilder = urlBuilder.concat(Uri.encode(query));
            urlBuilder = urlBuilder.concat("&size=");
            urlBuilder = urlBuilder.concat(Integer.toString(resultsAccepted));
            //TODO: Change here app_id and app_code to use the proper ones.
            urlBuilder = urlBuilder.concat("&app_id=DemoAppId01082013GAL&app_code=AJKnXv84fjrb0KIHawS0Tg&accept=application/json&pretty");

            URL url = new URL(urlBuilder);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(500);
            conn.setRequestMethod("GET");

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
        if ( jsonResponse.contentEquals(SERVER_ERROR)){
            Log.e(TAG,jsonResponse);
            return;
        }
        try {
            String[] stringList = new String[resultsAccepted];
            JSONObject response = new JSONObject(jsonResponse);
            JSONArray suggestions = response.getJSONArray("suggestions");
            int limit = Math.min(suggestions.length(),resultsAccepted);
            for (int i = 0; i<limit;i++){
                stringList[i] = suggestions.getString(i);
            }
            pendingRequest = false;
            //if (limit > 0) {
                adapter = new ArrayAdapter<>(ctx,
                        android.R.layout.simple_dropdown_item_1line, stringList);
                view.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            //}
            //else Log.e (TAG,NO_RESULTS_FOUND);
        }
        catch (Exception E){E.printStackTrace();}
    }
}
