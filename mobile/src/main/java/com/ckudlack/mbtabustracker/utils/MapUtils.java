package com.ckudlack.mbtabustracker.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import com.ckudlack.mbtabustracker.R;
import com.ckudlack.mbtabustracker.database.Schema;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;

public class MapUtils {

    public static LatLngBounds addStopMarkersToMap(Cursor c, GoogleMap map, HashMap<String, Marker> currentlyVisibleMarkers, Context context) {
        map.clear();

        if (currentlyVisibleMarkers != null) {
            currentlyVisibleMarkers.clear();
        }

        LatLngBounds.Builder builder = LatLngBounds.builder();

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.width(15f);

        Bitmap ob = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_action_place);
        Bitmap obm = Bitmap.createBitmap(ob.getWidth(), ob.getHeight(), ob.getConfig());
        Canvas canvas = new Canvas(obm);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.marker_color), PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(ob, 0f, 0f, paint);

        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(obm);

        while (c.moveToNext()) {
            MarkerOptions markerOptions = new MarkerOptions();
            String latString = c.getString(c.getColumnIndex(Schema.StopsTable.STOP_LAT));
            String lngString = c.getString(c.getColumnIndex(Schema.StopsTable.STOP_LONG));
            String stopName = c.getString(c.getColumnIndex(Schema.StopsTable.STOP_NAME));
            String stopId = c.getString(c.getColumnIndex(Schema.StopsTable.STOP_ID));

            LatLng pos = new LatLng(Double.parseDouble(latString), Double.parseDouble(lngString));

            markerOptions.position(pos);
            markerOptions.draggable(false);
            markerOptions.visible(true);
            markerOptions.title(stopName);
            markerOptions.icon(bitmapDescriptor);

            builder.include(pos);

            polylineOptions.add(pos);
            polylineOptions.visible(true);

            Marker marker = map.addMarker(markerOptions);
            if (currentlyVisibleMarkers != null) {
                currentlyVisibleMarkers.put(stopId, marker);
            }
        }

        map.addPolyline(polylineOptions);

        return builder.build();
    }

}
