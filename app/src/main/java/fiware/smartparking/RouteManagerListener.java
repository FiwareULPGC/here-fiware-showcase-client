package fiware.smartparking;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.routing.RouteManager;
import com.here.android.mpa.routing.RouteResult;

import java.util.List;

/**
 * Created by jmcf on 22/10/15.
 */
public class RouteManagerListener implements RouteManager.Listener {
    private MapRoute mapRoute;
    private MainActivity callback;
    private Map map;

    public RouteManagerListener(Map map, MainActivity callback) {
        this.map = map;
        this.callback = callback;
    }

    public void onProgress(int progress) {

    }

    @Override
    public void onCalculateRouteFinished(RouteManager.Error errorCode, List<RouteResult> result) {
        if (errorCode == RouteManager.Error.NONE && result.get(0).getRoute() != null) {
            // callback.routeReady(result.get(0).getRoute());
            /*
            // create a map route object and place it on the map
            mapRoute = new MapRoute(result.get(0).getRoute());
            map.addMapObject(mapRoute);

            // Get the bounding box containing the route and zoom in
            GeoBoundingBox gbb = result.get(0).getRoute().getBoundingBox();
            map.zoomTo(gbb, Map.Animation.NONE,  Map.MOVE_PRESERVE_ORIENTATION);
            */
        }
    }
}
