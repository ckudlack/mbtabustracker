package com.ckudlack.mbtabustracker.activities;

import android.app.Fragment;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ckudlack.mbtabustracker.Constants;
import com.ckudlack.mbtabustracker.OttoBusEvent;
import com.ckudlack.mbtabustracker.R;
import com.ckudlack.mbtabustracker.application.MbtaBusTrackerApplication;
import com.ckudlack.mbtabustracker.database.DBAdapter;
import com.ckudlack.mbtabustracker.database.Schema;
import com.ckudlack.mbtabustracker.models.Direction;
import com.ckudlack.mbtabustracker.models.RouteScheduleWrapper;
import com.ckudlack.mbtabustracker.models.RouteStop;
import com.ckudlack.mbtabustracker.models.StopTime;
import com.ckudlack.mbtabustracker.models.Trip;
import com.ckudlack.mbtabustracker.net.RetrofitManager;
import com.ckudlack.mbtabustracker.utils.MapUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RouteMapActivity extends ActionBarActivity {

    private Fragment mapFragment;
    private GoogleMap map;

    private DBAdapter dbAdapter;
    private List<Marker> currentlyVisibleMarkers = new ArrayList<>();
    private String routeId;
    private String direction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_map);

        mapFragment = getFragmentManager().findFragmentById(R.id.route_map);
        map = ((MapFragment) mapFragment).getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);

        routeId = getIntent().getStringExtra(Constants.ROUTE_ID_KEY);
        direction = getIntent().getStringExtra(Constants.DIRECTION_KEY);

        dbAdapter = MbtaBusTrackerApplication.getDbAdapter();

        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                getScheduledStops();
            }
        });
    }

    private void getScheduledStops() {
        RetrofitManager.getRealtimeService().getScheduleByRoute(RetrofitManager.API_KEY, RetrofitManager.FORMAT, routeId, direction, new Callback<RouteScheduleWrapper>() {
            @Override
            public void success(RouteScheduleWrapper routeScheduleWrapper, Response response) {
                Direction direction = routeScheduleWrapper.getDirection().get(0);
                Trip trip = direction.getTrip().get(0);
                List<StopTime> stopTimeList = trip.getStopTimeList();
                MbtaBusTrackerApplication.bus.post(new OttoBusEvent.StopTimesReturnedEvent(stopTimeList));
            }

            @Override
            public void failure(RetrofitError error) {
                MbtaBusTrackerApplication.bus.post(new OttoBusEvent.RetrofitFailureEvent(error));
            }
        });
    }

    @Subscribe
    public void stopTimeReturned(OttoBusEvent.StopTimesReturnedEvent event) {
        List<RouteStop> routeStops = dbAdapter.getRouteStops(Schema.RouteStopsTable.ROUTE_ID, routeId);

        StringBuilder dbIdsListBuilder = new StringBuilder();
        dbIdsListBuilder.append("(");
        for (int i = 0; i < routeStops.size(); i++) {
            dbIdsListBuilder.append(routeStops.get(i).getStopDbId());
            if (i >= routeStops.size() - 1) {
                break;
            }

            dbIdsListBuilder.append(",");
        }
        dbIdsListBuilder.append(")");

        List<StopTime> stopTimes = event.getStopTimes();

        StringBuilder stopIdsListBuilder = new StringBuilder();
        stopIdsListBuilder.append("(");
        for (int i = 0; i < stopTimes.size(); i++) {
            stopIdsListBuilder.append(stopTimes.get(i).getStopId());
            if (i >= stopTimes.size() - 1) {
                break;
            }

            stopIdsListBuilder.append(",");
        }
        stopIdsListBuilder.append(")");

        Cursor cursor = dbAdapter.db.query(Schema.StopsTable.TABLE_NAME, Schema.StopsTable.ALL_COLUMNS, Schema.StopsTable.ID_COL + " IN " + dbIdsListBuilder.toString() + " AND " + Schema.StopsTable.STOP_DIRECTION + " = \'" + direction + "\'" + " AND " + Schema.StopsTable.STOP_ID + " IN " + stopIdsListBuilder.toString(), null, null, null, Schema.StopsTable.STOP_ORDER);

        if (cursor.getCount() > 0) {
            LatLngBounds bounds = MapUtils.addStopMarkersToMap(cursor, map, currentlyVisibleMarkers);
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80));
        }

        String order = getIntent().getStringExtra(Constants.STOP_KEY);
        Marker marker = currentlyVisibleMarkers.get(Integer.parseInt(order) - 1);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16f));
        marker.showInfoWindow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MbtaBusTrackerApplication.bus.register(this);
    }

    @Override
    protected void onPause() {
        MbtaBusTrackerApplication.bus.unregister(this);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_route_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
