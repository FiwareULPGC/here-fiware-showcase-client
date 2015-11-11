package fiware.smartparking;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.MapEngine;
import com.here.android.mpa.routing.RouteManager;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.GeocodeRequest;
import com.here.android.mpa.search.ResultListener;
import com.here.android.mpa.search.Location;
import com.here.android.mpa.search.TextSuggestionRequest;

import java.util.ArrayList;
import java.util.List;

import fiware.smartparking.utils.SuggestionQueryJSONTask;

/**
 * Created by jmcf on 23/10/15.
 */
public class RouteActivity extends AppCompatActivity {
    private AutoCompleteTextView origin;
    private AutoCompleteTextView destination;
    private Button routeButton;

    private ArrayAdapter<String> originAdapter;
    private ArrayAdapter<String> destinationAdapter;
    private List<String> optionList1 = new ArrayList<String>();
    private List<String> optionList2 = new ArrayList<String>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        routeButton = (Button)findViewById(R.id.routeButton);

        getSupportActionBar().setTitle("Calculate Route");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        origin = (AutoCompleteTextView)findViewById(R.id.editText);
        originAdapter = new ArrayAdapter<String>(RouteActivity.this,
               android.R.layout.simple_dropdown_item_1line, optionList1);
        origin.setAdapter(originAdapter);
        origin.addTextChangedListener(new MyTextWatcher(origin, originAdapter));

        destination = (AutoCompleteTextView)findViewById(R.id.editText2);
        destinationAdapter = new ArrayAdapter<String>(RouteActivity.this,
                android.R.layout.simple_dropdown_item_1line, optionList2);
        destination.setAdapter(destinationAdapter);
        destination.addTextChangedListener(new MyTextWatcher(destination, destinationAdapter));
    }

    private void doCalculateRoute(GeoCoordinate start, GeoCoordinate end) {
        // Initialize RouteManager
        RouteManager routeManager = new RouteManager();

        // 3. Select routing options via RoutingMode
        RoutePlan routePlan = new RoutePlan();
        RouteOptions routeOptions = new RouteOptions();
        routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        routePlan.setRouteOptions(routeOptions);

        routePlan.addWaypoint(start);
        routePlan.addWaypoint(end);

        // Retrieve Routing information via RouteManagerListener
        RouteManager.Error error =
                routeManager.calculateRoute(routePlan, new RouteManager.Listener() {
                    @Override
                    public void onCalculateRouteFinished(RouteManager.Error errorCode, List<RouteResult> result) {
                        if (errorCode == RouteManager.Error.NONE && result.get(0).getRoute() != null) {
                            MainActivity.setRoute(result.get(0).getRoute());
                            Intent intent = new Intent(getApplicationContext(),
                                                                                MainActivity.class);
                            startActivity(intent);
                            RouteActivity.this.onBackPressed();
                            return;
                        }
                    }

                    public void onProgress(int progress) {

                    }
                });

        if (error != RouteManager.Error.NONE) {
            System.out.println("Error while obtaining route");
        }
    }

    public void calculateRoute(View v) {
        ProgressDialog progress = ProgressDialog.show(this, "Route Calculation",
                                                                "We are calculating a route", true);

        GeocodeRequest req1 = new GeocodeRequest(origin.getText().toString());
        //req1.setSearchArea(new GeoCoordinate(41.162142, -8.621953), 10000);
        req1.setSearchArea(new GeoCoordinate(40.637296, -8.635791), 10000);
        req1.execute(new ResultListener<List<Location>>() {
            public void onCompleted(List<Location> data, ErrorCode error) {
                if(error == ErrorCode.NONE) {
                    if (data.size() > 0) {
                        final GeoCoordinate geoOrigin = data.get(0).getCoordinate();

                        GeocodeRequest req2 = new GeocodeRequest(destination.getText().toString());
                        //req2.setSearchArea(new GeoCoordinate(41.162142, -8.621953), 10000);
                        req2.setSearchArea(new GeoCoordinate(40.637296, -8.635791), 10000);
                        req2.execute(new ResultListener<List<Location>>() {
                            public void onCompleted(List<Location> data, ErrorCode error) {
                                if (error == ErrorCode.NONE) {
                                    if (data == null || data.size() == 0)
                                        Log.e("Placed not located", "near sector");
                                    GeoCoordinate geoDestination = data.get(0).getCoordinate();
                                    doCalculateRoute(geoOrigin, geoDestination);
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "Error while Geocoding locations", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                    else {
                       Log.e("Error at","Preparing route");
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Error while Geocoding locations", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private class MyTextWatcher implements TextWatcher {
        private AutoCompleteTextView view;
        private ArrayAdapter<String> adapter;
        // Previous text
        private CharSequence prevText = "";
        private Boolean pendingRequest = false;

        public MyTextWatcher(AutoCompleteTextView v, ArrayAdapter<String> a) {
            view = v;
            adapter = a;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(origin.getText().length() > 0 && destination.getText().length() > 0) {
                routeButton.setEnabled(true);
            }
            else {
                routeButton.setEnabled(false);
            }

            // Nothing is done if text refines previous text
            if(pendingRequest || start == prevText.length() &&
                    s.length() > 4 && adapter.getCount() > 0) {
                prevText = new StringBuilder(s);
                return;
            }

            // Starting with 4 chars is when we query
            if(s.length() >= 4 && prevText.length() < 4 ||
                    (s.length() >=4 && s.toString().indexOf(prevText.toString()) == -1 &&
                            prevText.toString().indexOf(s.toString()) == -1)) {

             /*TextSuggestionRequest req = new TextSuggestionRequest(s.toString()).setSearchCenter(new GeoCoordinate(40.629793, -8.641633));//(41.162142, -8.621953));
                pendingRequest = true;

               req.execute(new ResultListener<List<String>>() {
                    @Override
                    public void onCompleted(List<String> strings, ErrorCode errorCode) {
                        if (errorCode == ErrorCode.NONE) {
                            pendingRequest = false;
                            adapter = new ArrayAdapter<String>(RouteActivity.this,
                                    android.R.layout.simple_dropdown_item_1line, strings);
                            view.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                        else {
                            Log.e("onCompleteErrorCode", errorCode.name());
                            if (strings == null) Log.e("Strings:","NULL");
                        }
                    }
                });*/


                //Oporto-based suggestions
                //SuggestionQueryJSONTask task = new SuggestionQueryJSONTask(41.162142, -8.621953,s.toString(),pendingRequest,view,adapter,RouteActivity.this);
                //Aveiro-based suggestions
                //40.629793,-8.641633
                SuggestionQueryJSONTask task = new SuggestionQueryJSONTask(40.637296, -8.635791,s.toString(),pendingRequest,view,adapter,RouteActivity.this);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
            }
            else if(prevText.length() >= 4 && s.length() < 4){
                adapter = new ArrayAdapter<String>(RouteActivity.this,
                        android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
                view.setAdapter(adapter);
                view.dismissDropDown();
                adapter.notifyDataSetChanged();
            }

            prevText = new StringBuilder(s);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
