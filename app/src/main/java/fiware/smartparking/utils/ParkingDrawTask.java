package fiware.smartparking.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.IconCategory;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapCircle;
import com.here.android.mpa.mapping.MapContainer;
import com.here.android.mpa.mapping.MapLabeledMarker;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapOverlayType;
import com.here.android.mpa.mapping.MapPolygon;

import java.util.ArrayList;

import fiware.smartparking.R;
import fiware.smartparking.models.Parking;
import fiware.smartparking.models.ParkingAccess;
import fiware.smartparking.models.ParkingLot;
import fiware.smartparking.models.ParkingLotCategory;
import fiware.smartparking.models.StreetParking;

/**
 * Created by Ulpgc on 04/11/2015.
 */
public class ParkingDrawTask {

    private Map map;
    private MapContainer lotContainer,streetContainer;
    private ArrayList<StreetParking> lastStrParkings;
    private ArrayList<ParkingLot> lastLotParkings;
    private static Bitmap iconBitmap;
    private static Context ctx;

    private boolean status;
    // Necessary since MapPolygon has no useful getters and we need to compare directly
    // between objects.
    private class MapPolygonWrapper {
        public MapPolygon mapPolygon;
        public int streetParkingIndex;

        MapPolygonWrapper(MapPolygon mapPolygon, int index){
            this.mapPolygon = mapPolygon;
            streetParkingIndex = index;
        }
    }
    private ArrayList<MapPolygonWrapper> lastStrPolygons, lastLotPolygons;

    public ParkingDrawTask(Map map, boolean activeOnInit){

        this.map = map;
        lotContainer = new MapContainer();
        streetContainer =  new MapContainer();
        streetContainer.setOverlayType(MapOverlayType.FOREGROUND_OVERLAY);
        lastStrParkings = new ArrayList<>();
        lastLotParkings = new ArrayList<>();
        lastStrPolygons = new ArrayList<>();
        lastLotPolygons = new ArrayList<>();
        status = activeOnInit;
        if (status) {
            this.map.addMapObject(lotContainer);
            this.map.addMapObject(streetContainer);
        }
    }

    public void drawStreetParkings(ArrayList<StreetParking> streetParkings){
        if (streetParkings.size() != 0){
            clearStreetMarkers(streetParkings);
        }
        for (int i=0;i<streetParkings.size();i++) {
            StreetParking streetParking = streetParkings.get(i);

            MapMarker mapMarker = new MapMarker(streetParking.getCenter(), createLabeledIcon(
                    Integer.toString(streetParking.getAvailableSpotNumber()), 16, Color.BLACK));
            mapMarker.setOverlayType(MapOverlayType.FOREGROUND_OVERLAY);
            streetContainer.addMapObject(mapMarker);

            for (int j=0;j<streetParking.getParkingPolygons();j++) {
                MapPolygon streetPolygon = new MapPolygon(streetParking.getParkingAreaPolygonAt(j));
                streetPolygon.setLineColor(Color.parseColor("#FF0000FF"));
                streetPolygon.setFillColor(Color.parseColor("#770000FF"));
                streetContainer.addMapObject(streetPolygon);
                lastStrPolygons.add(new MapPolygonWrapper(streetPolygon,i));
            }
        }
    }

    public void drawParkingLots(ArrayList<ParkingLot> lotParkings){
        if (lotParkings.size() != 0) clearLotMarkers(lotParkings);
        for (int i=0; i<lotParkings.size();i++){
            ParkingLot lotParking = lotParkings.get(i);

            MapMarker mapMarker = new MapMarker(lotParking.getCenter(), createLabeledIcon(
                    Integer.toString(lotParking.getAvailableSpotNumber()), 16, Color.BLACK));
            mapMarker.setOverlayType(MapOverlayType.FOREGROUND_OVERLAY);
            lotContainer.addMapObject(mapMarker);

            for (int j=0;j<lotParking.getParkingPolygons();j++) {
                MapPolygon lotPolygon = new MapPolygon(lotParking.getParkingAreaPolygonAt(j));
                lotPolygon.setLineColor(Color.parseColor("#FF0000FF"));
                lotPolygon.setFillColor(Color.parseColor("#770000FF"));
                lotContainer.addMapObject(lotPolygon);
                lastLotPolygons.add(new MapPolygonWrapper(lotPolygon,i));
            }
        }
    }

    public void clearMarkers(boolean shouldDestroy){
        clearLotMarkers();
        clearStreetMarkers();
        if (shouldDestroy) {
            map.removeMapObject(lotContainer);
            map.removeMapObject(streetContainer);
            lotContainer = null;
            streetContainer = null;
            status = false;
        }
    }

    public void setParkingOverlayActive (boolean active){
        status = active;
        if (active){
            map.addMapObject(lotContainer);
            map.addMapObject(streetContainer);
        }
        else {
            map.removeMapObject(lotContainer);
            map.removeMapObject(streetContainer);
        }
    }

    private void clearLotMarkers(){
        lotContainer.removeAllMapObjects();
        lastLotParkings.clear();
        lastLotPolygons.clear();
    }

    private void clearStreetMarkers(){
        streetContainer.removeAllMapObjects();
        lastStrParkings.clear();
        lastStrPolygons.clear();
    }

    private void clearLotMarkers(ArrayList<ParkingLot> lotParkings){
        clearLotMarkers();
        lastLotParkings.addAll(lotParkings);
    }
    private void clearStreetMarkers(ArrayList<StreetParking> streetParkings){
        clearStreetMarkers();
        lastStrParkings.addAll(streetParkings);
    }

    public StreetParking streetParkingSelected (GeoCoordinate geo){
        if (status)
            for (int i=0;i<lastStrParkings.size();i++){
                if ((lastStrParkings.get(i).getCenter().getLatitude() == geo.getLatitude())
                    && (lastStrParkings.get(i).getCenter().getLongitude() == geo.getLongitude()))
                    return lastStrParkings.get(i);
            }
        return null;
    }

    public StreetParking streetParkingSelected (MapPolygon polygon){
        if (status){
            for (int i=0;i<lastStrPolygons.size();i++){
                if (lastStrPolygons.get(i).mapPolygon.equals(polygon)){
                    return lastStrParkings.get(lastStrPolygons.get(i).streetParkingIndex);
                }
            }
        }
        return null;
    }

    public ParkingLot parkingLotSelected (GeoCoordinate geo){
        if (status)
            for (int i=0;i<lastLotParkings.size();i++){
                if ((lastLotParkings.get(i).getCenter().getLatitude() == geo.getLatitude())
                        && (lastLotParkings.get(i).getCenter().getLongitude() == geo.getLongitude()))
                    return lastLotParkings.get(i);
            }
        return null;
    }

    public ParkingLot parkingLotSelected (MapPolygon polygon){
        if (status){
            for (int i=0; i<lastLotPolygons.size();i++){
                if (lastLotPolygons.get(i).mapPolygon.equals(polygon)){
                    return lastLotParkings.get(lastLotPolygons.get(i).streetParkingIndex );
                }
            }
        }
        return null;
    }

    private Image createLabeledIcon(String text, float textSize, int textColor){
        try {
            if (iconBitmap == null)
                iconBitmap = BitmapFactory.decodeResource(ctx.getResources(),R.mipmap.parking);
            Paint paint = createPaint(textSize,textColor);
            float baseline = -paint.ascent();
            int textWidth = (int) (paint.measureText(text) + 0.5f);
            int textHeight = (int) (baseline + paint.descent() + 0.5f);

            int width = Math.max(iconBitmap.getWidth(), textWidth);
            int height = iconBitmap.getHeight() + textHeight;
            Bitmap resBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
            Canvas resCanvas = new Canvas(resBitmap);
            resCanvas.drawBitmap(iconBitmap, calculateLeft(width,iconBitmap.getWidth()), 0, null);
            resCanvas.drawText(text, calculateLeft(width,textWidth), baseline+iconBitmap.getHeight(), paint);
            Image resImage = new Image();
            resImage.setBitmap(resBitmap);
            return resImage;
        }
        catch (Exception e) {e.printStackTrace();}
        return null;
    }

    private int calculateLeft (int globalWidth, int elementWidth){
        return (globalWidth - elementWidth)/2;
    }

    public static void setApplicationContext(Context appContext){
        ctx = appContext;
    }

    private float dipToPixels(float dip){
        final float scale = ctx.getResources().getDisplayMetrics().density;
        return (dip * scale);
    }

    private Paint createPaint(float textSize,int textColor){
        Paint paint = new Paint();
        paint.setTextSize(dipToPixels(textSize));
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        return paint;
    }

}
