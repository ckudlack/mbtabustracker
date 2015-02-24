package com.ckudlack.mbtabustracker.utils;

import android.database.Cursor;

import com.ckudlack.mbtabustracker.database.Schema;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MapUtils {

    public static LatLngBounds addStopMarkersToMap(Cursor c, GoogleMap map, List<Marker> currentlyVisibleMarkers) {
        map.clear();

        if (currentlyVisibleMarkers != null) {
            currentlyVisibleMarkers.clear();
        }

        LatLngBounds.Builder builder = LatLngBounds.builder();

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.width(20f);

        while (c.moveToNext()) {
            MarkerOptions markerOptions = new MarkerOptions();
            String latString = c.getString(c.getColumnIndex(Schema.StopsTable.STOP_LAT));
            String lngString = c.getString(c.getColumnIndex(Schema.StopsTable.STOP_LONG));
            String stopName = c.getString(c.getColumnIndex(Schema.StopsTable.STOP_NAME));

            LatLng pos = new LatLng(Double.parseDouble(latString), Double.parseDouble(lngString));

            markerOptions.position(pos);
            markerOptions.draggable(false);
            markerOptions.visible(true);
            markerOptions.title(stopName);

            builder.include(pos);

            polylineOptions.add(pos);
            polylineOptions.visible(true);

            Marker marker = map.addMarker(markerOptions);
            if (currentlyVisibleMarkers != null) {
                currentlyVisibleMarkers.add(marker);
            }
        }

        map.addPolyline(polylineOptions);

        return builder.build();
    }

}
