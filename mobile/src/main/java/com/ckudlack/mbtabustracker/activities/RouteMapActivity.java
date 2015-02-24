package com.ckudlack.mbtabustracker.activities;

import android.app.Fragment;
import android.database.Cursor;
import android.graphics.Color;
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
import com.ckudlack.mbtabustracker.models.Vehicle;
import com.ckudlack.mbtabustracker.models.VehicleInfoWrapper;
import com.ckudlack.mbtabustracker.net.RetrofitManager;
import com.ckudlack.mbtabustracker.utils.MapUtils;
import com.ckudlack.mbtabustracker.utils.StringUtils;
import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class RouteMapActivity extends ActionBarActivity implements RoutingListener {

    private Fragment mapFragment;
    private GoogleMap map;

    private DBAdapter dbAdapter;
    private List<Marker> currentlyVisibleMarkers = new ArrayList<>();
    private List<Marker> busMarkers = new ArrayList<>();
    private String routeId;
    private String direction;
    private String order;

    private Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_map);

        mapFragment = getFragmentManager().findFragmentById(R.id.route_map);
        map = ((MapFragment) mapFragment).getMap();
        map.setMyLocationEnabled(true);

        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setMyLocationButtonEnabled(true);

        routeId = getIntent().getStringExtra(Constants.ROUTE_ID_KEY);
        direction = getIntent().getStringExtra(Constants.DIRECTION_KEY);
        order = getIntent().getStringExtra(Constants.STOP_KEY);

        dbAdapter = MbtaBusTrackerApplication.getDbAdapter();

        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                getScheduledStops();
            }
        });

        getSupportActionBar().setTitle(getIntent().getStringExtra(Constants.STOP_NAME_KEY));
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

    private void getVehicleLocations() {
        RetrofitManager.getRealtimeService().getVehiclesByRoute(RetrofitManager.API_KEY, RetrofitManager.FORMAT, routeId, new Callback<VehicleInfoWrapper>() {
            @Override
            public void success(VehicleInfoWrapper vehicleInfoWrapper, Response response) {
                List<Direction> directions = vehicleInfoWrapper.getDirection();
                for (Direction d : directions) {
                    if (d.getDirectionId().equals(direction)) {
                        MbtaBusTrackerApplication.bus.post(new OttoBusEvent.VehicleInfoReturnedEvent(d.getTrip()));
                        return;
                    }
                }
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

        String dbIdsList = StringUtils.buildDbIdList(routeStops);

        List<StopTime> stopTimes = event.getStopTimes();

        String dbStopIdsList = StringUtils.buildStopIdList(stopTimes);

        Cursor cursor = dbAdapter.db.query(Schema.StopsTable.TABLE_NAME, Schema.StopsTable.ALL_COLUMNS, Schema.StopsTable.ID_COL + " IN " + dbIdsList + " AND " + Schema.StopsTable.STOP_DIRECTION + " = \'" + direction + "\'" + " AND " + Schema.StopsTable.STOP_ID + " IN " + dbStopIdsList, null, null, null, Schema.StopsTable.STOP_ORDER);

        if (cursor.getCount() > 0) {
            LatLngBounds bounds = MapUtils.addStopMarkersToMap(cursor, map, currentlyVisibleMarkers, this);
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80));
        }

        zoomToFavoritedStop();
        getVehicleLocations();

        Routing routing = new Routing(Routing.TravelMode.WALKING);
        routing.registerListener(this);
        Marker marker = currentlyVisibleMarkers.get(Integer.parseInt(order) - 1);
        routing.execute(new LatLng(42.3670981, -71.0800857), marker.getPosition());
    }

    private void zoomToFavoritedStop() {
        Marker marker = currentlyVisibleMarkers.get(Integer.parseInt(order) - 1);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16f));
        marker.showInfoWindow();
    }

    @Subscribe
    public void vehicleInfoReturned(OttoBusEvent.VehicleInfoReturnedEvent event) {
        for (Marker m : busMarkers) {
            m.remove();
        }

        busMarkers.clear();

        List<Trip> trips = event.getTrips();

        for (Trip t : trips) {
            Vehicle vehicle = t.getVehicle();

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(Double.parseDouble(vehicle.getVehicleLat()), Double.parseDouble(vehicle.getVehicleLon())));
            markerOptions.visible(true);
            markerOptions.draggable(false);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.bus_small);

            markerOptions.icon(icon);
            Marker marker = map.addMarker(markerOptions);
            busMarkers.add(marker);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MbtaBusTrackerApplication.bus.register(this);
        timer = new Timer();
        scheduleBusLocationUpdate();
    }

    private void scheduleBusLocationUpdate() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getVehicleLocations();
                    }
                });
            }
        }, 3000, 15000);
    }

    @Override
    protected void onPause() {
        timer.cancel();
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
            zoomToFavoritedStop();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRoutingFailure() {
        Timber.e("Failed");
    }

    @Override
    public void onRoutingStart() {
        Timber.d("Start");
    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
        Timber.d("Success");
        PolylineOptions polyoptions = new PolylineOptions();
        polyoptions.color(Color.BLUE);
        polyoptions.width(10);
        polyoptions.addAll(mPolyOptions.getPoints());
        map.addPolyline(polyoptions);
    }
}
