package fiware.smartparking.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
    public static String UNSUPPORTED_ERROR = "Unsupported exception Error";
    public static String IO_ERROR = "IO Exception Error";


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

    protected String doInBackground(Void... paramss) {
        try {
            HttpClient httpClient =  new DefaultHttpClient();

            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append("http://places.cit.api.here.com/places/v1/suggest?");
            urlBuilder.append("at="+Double.toString(lat)+","+Double.toString(lon));
            urlBuilder.append("&q="+ Uri.encode(query));
            urlBuilder.append("&size="+Integer.toString(resultsAccepted));
            //TODO: Change here app_id and app_code to use the proper ones.
            urlBuilder.append("&app_id=DemoAppId01082013GAL&app_code=AJKnXv84fjrb0KIHawS0Tg&accept=application/json&pretty");

            HttpGet request = new HttpGet(urlBuilder.toString());

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
            Log.e("SuggestionQueryError",jsonResponse);
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
            adapter = new ArrayAdapter<String>(ctx,
                    android.R.layout.simple_dropdown_item_1line, stringList);
            view.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        catch (Exception E){}
    }
}
