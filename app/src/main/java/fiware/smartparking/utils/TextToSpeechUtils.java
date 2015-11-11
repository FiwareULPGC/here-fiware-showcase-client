package fiware.smartparking.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.routing.Maneuver;

import java.util.ArrayList;
import java.util.Locale;

import fiware.smartparking.models.Parking;
import fiware.smartparking.models.ParkingLot;
import fiware.smartparking.models.StreetParking;

/**
 * Created by Ulpgc on 08/11/2015.
 */
public class TextToSpeechUtils {

    private static boolean followManeuver = false;
    private static TextToSpeech tts = null;

    private static ArrayList<StreetParking> streetParkingList;
    private static ArrayList<ParkingLot> lotParkingList;
    private static int lastStreetParking = -1, lastLotParking = -1;

    public static final int MY_DATA_CHECK_CODE = 0;

    public static void checkTTSIntent(Activity activity){
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        activity.startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
    }

    public static void shouldFollowManeuver(boolean follow){
        followManeuver = follow;
    }

    public static void shouldCreateTTS(Context ctx1, android.speech.tts.TextToSpeech.OnInitListener ctx2){
        if (tts == null){
            tts = new TextToSpeech(ctx1,ctx2);
            streetParkingList = new ArrayList<>();
            lotParkingList = new ArrayList<>();
        }
    }

    public static void shouldShutdownTTS(){
        if (tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
            streetParkingList.clear();
            lotParkingList.clear();
            streetParkingList = null;
            lotParkingList = null;
        }
    }

    public static void setStreetParkingList(ArrayList<StreetParking> parkingList){
        if (streetParkingList != null) {
            streetParkingList.clear();
            streetParkingList.addAll(parkingList);
        }
    }

    public static void setLotParkingList(ArrayList<ParkingLot> parkingList){
        if (lotParkingList != null) {
            lotParkingList.clear();
            lotParkingList.addAll(parkingList);
        }
    }

    public static void setLanguage(Locale language){
        if (tts != null)
            tts.setLanguage(language);
    }

    @SuppressWarnings("deprecation")
    public static void indicateParkingProximity(GeoCoordinate loc, float meters){
        if (tts == null) return;
        try {
            boolean parkingFound = false;
            for (int i=0;i<streetParkingList.size();i++) {
                GeoBoundingBox parkingGbb = new GeoBoundingBox(streetParkingList.get(i).getCenter(),meters*2,meters*2);
                if (parkingGbb.contains(loc)){
                    parkingFound = true;
                    if (lastStreetParking != i) {
                        lastStreetParking = i;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            tts.speak(retrieveParkingInstruction(streetParkingList.get(i), meters), TextToSpeech.QUEUE_FLUSH, null, "MessageId");
                        else
                            tts.speak(retrieveParkingInstruction(streetParkingList.get(i), meters), TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
            if (!parkingFound) lastStreetParking = -1;
            parkingFound = false;
            for (int i=0;i<lotParkingList.size();i++) {
                GeoBoundingBox parkingGbb = new GeoBoundingBox(lotParkingList.get(i).getCenter(),meters*2,meters*2);
                if (parkingGbb.contains(loc)){
                    parkingFound = true;
                    if (lastLotParking != i) {
                        lastLotParking = i;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            tts.speak(retrieveParkingInstruction(lotParkingList.get(i), meters), TextToSpeech.QUEUE_FLUSH, null, "MessageId");
                        else
                            tts.speak(retrieveParkingInstruction(lotParkingList.get(i), meters), TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
            if (!parkingFound) lastLotParking = -1;
        }
        catch (Exception E) {E.printStackTrace();}
    }

    @SuppressWarnings("deprecation")
    public static void indicateManeuverProximity(Maneuver nextManeuver,
                                                 GeoCoordinate loc, float meters){

        if (tts == null) return;
        try {
            //End should be warned in the end.
            GeoBoundingBox maneuverGbb;
            if (nextManeuver.getAction() == Maneuver.Action.END)
                maneuverGbb = new GeoBoundingBox(nextManeuver.getCoordinate(),20,20);
            else
                maneuverGbb = new GeoBoundingBox(nextManeuver.getCoordinate(),meters*2,meters*2);

            if (maneuverGbb.contains(loc)) {
                if (followManeuver) {

                    followManeuver = false;
                    String instruction = "";
                    switch(nextManeuver.getAction()) {
                        case JUNCTION:
                        case ROUNDABOUT: {
                            instruction = retrieveTurnInstruction(nextManeuver.getTurn(),meters);
                            break;
                        }
                        case END: {
                            instruction = "Destination reached!";
                            break;
                        }
                    }
                    if (tts != null){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            tts.speak(instruction, TextToSpeech.QUEUE_FLUSH, null, "MessageId");
                        else
                            tts.speak(instruction, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
       }
       catch (Exception e) {e.printStackTrace();}

    }

    private static String retrieveTurnInstruction(Maneuver.Turn turn, float meters) {
        StringBuilder sb = new StringBuilder();
        switch (turn) {
            case QUITE_LEFT:
                sb.append("Turn left at ");
                sb.append((int)meters);
                sb.append(" meters");
                return sb.toString();
            case QUITE_RIGHT:
                sb.append("Turn right at ");
                sb.append((int)meters);
                sb.append(" meters");
                return sb.toString();
            case ROUNDABOUT_1:
                return "Leave the roundabout at the first exit";
            case ROUNDABOUT_2:
                return "Leave the roundabout at the second exit";
            case ROUNDABOUT_3:
                return "Leave the roundabout at the third exit";
            case ROUNDABOUT_4:
                return "Leave the roundabout at the fourth exit";
            case ROUNDABOUT_5:
                return "Leave the roundabout at the fifth exit";
            case ROUNDABOUT_6:
                return "Leave the roundabout at the sixth exit";
            case ROUNDABOUT_7:
                return "Leave the roundabout at the seventh exit";
            case ROUNDABOUT_8:
                return "Leave the roundabout at the eighth exit";
            case ROUNDABOUT_9:
                return "Leave the roundabout at the ninth exit";
            case ROUNDABOUT_10:
                return "Leave the roundabout at the tenth exit";
            case ROUNDABOUT_11:
                return "Leave the roundabout at the eleventh exit";
            case ROUNDABOUT_12:
                return "Leave the roundabout at the twelfth exit";
            default:
                return "Undetermined turn at fifty meters";
        }
    }

    private static String retrieveParkingInstruction (Parking parking, float meters){
        if (parking.getAvailableSpotNumber() == 0) return "";
        String sb = "Parking with ";
        sb = sb.concat(Integer.toString(parking.getAvailableSpotNumber()));
        sb = sb.concat(" free spots within ");
        sb = sb.concat(Integer.toString((int)meters));
        sb = sb.concat(" meters");
        return sb;
    }
}
