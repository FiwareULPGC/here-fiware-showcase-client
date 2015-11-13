package fiware.smartparking.utils;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import fiware.smartparking.R;
import fiware.smartparking.models.ParkingLot;
import fiware.smartparking.models.StreetParking;

/**
 * Created by Ulpgc on 12/11/2015.
 */
public class DialogUtils {

    private final static int MAX_AREA_METERS = 10000;

    public static void openDialog(final Context ctx, final MapChangeListener changeListener) {
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Dialog dialog = new Dialog(ctx);

        final LinearLayout vi = (LinearLayout) inflater.inflate(R.layout.dialog_layout, null);

        Button ok = (Button) vi.findViewById(R.id.settings_ok);
        Button cancel = (Button) vi.findViewById(R.id.settings_cancel);
        final EditText editText = (EditText) vi.findViewById(R.id.settings_radius);
        final CheckBox checkbox = (CheckBox) vi.findViewById(R.id.settings_checkbox);

        checkbox.setChecked(changeListener.isParkingOverlayActive());

        editText.setText(Integer.toString(MapChangeListener.getDestinationAreaMeters()));
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean showMetersErrorMessage = false;
                if (changeListener != null) {
                    changeListener.setParkingOverlayActive(checkbox.isChecked());
                    try {
                        int meters = Integer.parseInt(editText.getText().toString());
                        if (meters > 0 && meters < MAX_AREA_METERS)
                            MapChangeListener.setDestinationAreaMeters(meters);
                        else showMetersErrorMessage = true;
                    } catch (Exception e) {
                        showMetersErrorMessage = true;
                    }
                    dialog.dismiss();
                    if (showMetersErrorMessage)
                        Toast.makeText(ctx,"Radius should be a number between 1 and "+Integer.toString(MAX_AREA_METERS),
                                Toast.LENGTH_SHORT).show();

                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }

        });
        dialog.setContentView(vi);
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void openPopupInfo(Context ctx, StreetParking parking){
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Dialog dialog = new Dialog(ctx);

        final ScrollView vi = (ScrollView) inflater.inflate(R.layout.popup_layout, null);
        Button ok = (Button) vi.findViewById(R.id.popup_ok);
        TextView textView = (TextView) vi.findViewById(R.id.popup_info);

        textView.setText(parking.description());
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setContentView(vi);
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void openPopupInfo(Context ctx, ParkingLot parking){
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Dialog dialog = new Dialog(ctx);

        final ScrollView vi = (ScrollView) inflater.inflate(R.layout.popup_layout, null);
        Button ok = (Button) vi.findViewById(R.id.popup_ok);
        TextView textView = (TextView) vi.findViewById(R.id.popup_info);

        textView.setText(parking.description());
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setContentView(vi);
        dialog.setCancelable(false);
        dialog.show();
    }
}