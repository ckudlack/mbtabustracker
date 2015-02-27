package com.ckudlack.mbtabustracker.activities;

import android.app.Fragment;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.ckudlack.mbtabustracker.Constants;
import com.ckudlack.mbtabustracker.OttoBusEvent;
import com.ckudlack.mbtabustracker.R;
import com.ckudlack.mbtabustracker.application.MbtaBusTrackerApplication;
import com.ckudlack.mbtabustracker.database.DBAdapter;
import com.ckudlack.mbtabustracker.database.Schema;
import com.ckudlack.mbtabustracker.models.Direction;
import com.ckudlack.mbtabustracker.models.Mode;
import com.ckudlack.mbtabustracker.models.RouteScheduleWrapper;
import com.ckudlack.mbtabustracker.models.RouteStop;
import com.ckudlack.mbtabustracker.models.StopPredictionWrapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class RouteMapActivity extends LocationActivity implements RoutingListener {

    private Fragment mapFragment;
    private GoogleMap map;
    private TextView busPredView;
    private TextView timetoDestView;

    private DBAdapter dbAdapter;
    private HashMap<String, Marker> currentlyVisibleMarkers = new HashMap<>();
    private List<Marker> busMarkers = new ArrayList<>();
    private String routeId;
    private String direction;
    private String stopId;

    private Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_map);

        mapFragment = getFragmentManager().findFragmentById(R.id.route_map);
        map = ((MapFragment) mapFragment).getMap();
        map.setMyLocationEnabled(true);

        busPredView = (TextView) findViewById(R.id.next_bus_pred);
        timetoDestView = (TextView) findViewById(R.id.time_to_stop);

        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setMyLocationButtonEnabled(true);

        routeId = getIntent().getStringExtra(Constants.ROUTE_ID_KEY);
        direction = getIntent().getStringExtra(Constants.DIRECTION_KEY);
        stopId = getIntent().getStringExtra(Constants.STOP_KEY);

        dbAdapter = MbtaBusTrackerApplication.getDbAdapter();

        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                getScheduledStops();
            }
        });

        getSupportActionBar().setTitle(getIntent().getStringExtra(Constants.STOP_NAME_KEY));

        RetrofitManager.getRealtimeService().getPredictionsByStop(stopId, new Callback<StopPredictionWrapper>() {
            @Override
            public void success(StopPredictionWrapper stopPredictionWrapper, Response response) {
                Timber.d("Success!");
                MbtaBusTrackerApplication.bus.post(new OttoBusEvent.PredictionsByStopReturnEvent(stopPredictionWrapper, null));
            }

            @Override
            public void failure(RetrofitError error) {
                MbtaBusTrackerApplication.bus.post(new OttoBusEvent.RetrofitFailureEvent(error));
            }
        });
    }

    private void getScheduledStops() {
        RetrofitManager.getRealtimeService().getScheduleByRoute(routeId, direction, new Callback<RouteScheduleWrapper>() {
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
        RetrofitManager.getRealtimeService().getVehiclesByRoute(routeId, new Callback<VehicleInfoWrapper>() {
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

        RetrofitManager.getRealtimeService().getPredictionsByStop(stopId, new Callback<StopPredictionWrapper>() {
            @Override
            public void success(StopPredictionWrapper stopPredictionWrapper, Response response) {
                Timber.d("Success!");
                MbtaBusTrackerApplication.bus.post(new OttoBusEvent.PredictionsByStopReturnEvent(stopPredictionWrapper, null));
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
        Marker marker = currentlyVisibleMarkers.get(stopId);
        routing.execute(new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude()), marker.getPosition());
    }

    private void zoomToFavoritedStop() {
        Marker marker = currentlyVisibleMarkers.get(stopId);
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

    @Subscribe
    public void predictionsByStopReturned(OttoBusEvent.PredictionsByStopReturnEvent event) {
        List<Mode> modes = event.getPredictionWrapper().getMode();
        Mode busMode = null;
        for (Mode m : modes) {
            if (m.getRouteType().equals(Constants.ROUTE_TYPE_BUS)) {
                busMode = m;
                break;
            }
        }

        //TODO: Make this more modular, so it uses similar code to FavoritesActivity
        if (busMode != null) {
            List<com.ckudlack.mbtabustracker.models.Route> routes = busMode.getRoute();
            for (com.ckudlack.mbtabustracker.models.Route r : routes) {
                if (r.getRouteId().equals(routeId)) {
                    Direction direction;
                    if (r.getDirection().size() > 1) {
                        direction = r.getDirection().get(Integer.parseInt(this.direction));
                    } else {
                        direction = r.getDirection().get(0);
                    }

                    List<Trip> trips = direction.getTrip();
                    StringBuilder sb = new StringBuilder();
                    Trip t = trips.get(0);

                    int timeRemaining = (int) (Integer.parseInt(t.getPreAway()) / 60f);
                    sb.append(String.valueOf(timeRemaining));
                    sb.append(" mins");

                    busPredView.setText(getString(R.string.next_bus_in) + " " + sb.toString());
                    break;
                }
            }
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
        polyoptions.color(Color.RED);
        polyoptions.width(10);
        polyoptions.addAll(mPolyOptions.getPoints());
        map.addPolyline(polyoptions);

        timetoDestView.setText(getString(R.string.time_to_dest) + " " + route.getDurationText());
    }
}
