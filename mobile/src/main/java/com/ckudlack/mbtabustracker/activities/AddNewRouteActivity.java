package com.ckudlack.mbtabustracker.activities;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.ckudlack.mbtabustracker.Constants;
import com.ckudlack.mbtabustracker.OttoBusEvent;
import com.ckudlack.mbtabustracker.R;
import com.ckudlack.mbtabustracker.adapters.RoutesAdapter;
import com.ckudlack.mbtabustracker.adapters.StopsAdapter;
import com.ckudlack.mbtabustracker.application.MbtaBusTrackerApplication;
import com.ckudlack.mbtabustracker.async.PersistRouteStopsInDbTask;
import com.ckudlack.mbtabustracker.async.PersistRoutesInDbTask;
import com.ckudlack.mbtabustracker.async.PersistStopsInDbTask;
import com.ckudlack.mbtabustracker.database.DBAdapter;
import com.ckudlack.mbtabustracker.database.Schema;
import com.ckudlack.mbtabustracker.models.AllRoutesWrapper;
import com.ckudlack.mbtabustracker.models.Direction;
import com.ckudlack.mbtabustracker.models.Direction2;
import com.ckudlack.mbtabustracker.models.Favorite;
import com.ckudlack.mbtabustracker.models.FeedInfo;
import com.ckudlack.mbtabustracker.models.Mode;
import com.ckudlack.mbtabustracker.models.Route;
import com.ckudlack.mbtabustracker.models.RouteScheduleWrapper;
import com.ckudlack.mbtabustracker.models.RouteStop;
import com.ckudlack.mbtabustracker.models.Stop;
import com.ckudlack.mbtabustracker.models.StopTime;
import com.ckudlack.mbtabustracker.models.StopsByRouteWrapper;
import com.ckudlack.mbtabustracker.models.Trip;
import com.ckudlack.mbtabustracker.net.RetrofitManager;
import com.ckudlack.mbtabustracker.utils.IoUtils;
import com.ckudlack.mbtabustracker.utils.MapUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;


public class AddNewRouteActivity extends ActionBarActivity {

    private RoutesAdapter routesAdapter;
    private StopsAdapter stopsAdapter;

    private Cursor cursor;
    private DBAdapter dbAdapter;

    private Spinner routesSpinner;
    private Spinner stopsSpinner;
    private Switch directionSwitch;
    private Button addButton;

    private Route currentRoute;
    private Stop currentStop;

    private Fragment mapFragment;
    private GoogleMap map;

    private List<Marker> currentlyVisibleMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_route);

        dbAdapter = MbtaBusTrackerApplication.getDbAdapter();

        RetrofitManager.getFeedService().getUpdateDate(new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                Timber.d("Success!");
                MbtaBusTrackerApplication.bus.post(new OttoBusEvent.FeedInfoReturnedEvent(response));
            }

            @Override
            public void failure(RetrofitError error) {
                MbtaBusTrackerApplication.bus.post(new OttoBusEvent.RetrofitFailureEvent(error));
            }
        });

        routesSpinner = (Spinner) findViewById(R.id.routes_spinner);
        routesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = routesAdapter.getCursor();
                c.moveToPosition(position);
                Route route = new Route();
                route.buildFromCursor(c, dbAdapter);

                currentRoute = route;

                String routeId = route.getRouteId();

                List<RouteStop> routeStops = dbAdapter.getRouteStops(Schema.RouteStopsTable.ROUTE_ID, routeId);

                if (routeStops == null) {
                    getStopsForRoute(routeId);
                } else {
                    getScheduledStops();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        stopsSpinner = (Spinner) findViewById(R.id.stops_spinner);
        stopsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = stopsAdapter.getCursor();
                c.moveToPosition(position);
                Stop stop = new Stop();
                stop.buildFromCursor(c, dbAdapter);

                currentStop = stop;

                Marker marker = currentlyVisibleMarkers.get(position);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16f));
                marker.showInfoWindow();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setRoutesSpinner();

        directionSwitch = (Switch) findViewById(R.id.direction_switch);
        directionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (currentRoute != null) {
                    List<RouteStop> routeStops = dbAdapter.getRouteStops(Schema.RouteStopsTable.ROUTE_ID, currentRoute.getRouteId());
                    getScheduledStops();
                }
            }
        });

        addButton = (Button) findViewById(R.id.go_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStopToFavorites();
            }
        });

        mapFragment = getFragmentManager().findFragmentById(R.id.route_map);
        map = ((MapFragment) mapFragment).getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);

        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                //TODO Something here
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                int markerIndex = currentlyVisibleMarkers.indexOf(marker);
                stopsSpinner.setSelection(markerIndex);
                return false;
            }
        });

    }

    private void getScheduledStops() {
        RetrofitManager.getRealtimeService().getScheduleByRoute(RetrofitManager.API_KEY, RetrofitManager.FORMAT, currentRoute.getRouteId(), getDirectionString(), new Callback<RouteScheduleWrapper>() {
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

    private void addStopToFavorites() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> stringSet;
        stringSet = sharedPreferences.getStringSet(Constants.FAVORITES_KEY, null);

        Favorite favorite = new Favorite();
        favorite.setStopId(currentStop.getStopId());
        favorite.setStopName(currentStop.getStopName());
        favorite.setRouteId(currentRoute.getRouteId());
        favorite.setRouteName(currentRoute.getRouteName());
        favorite.setDirectionName(directionSwitch.isChecked() ? "Inbound" : "Outbound");
        favorite.setDirectionId(getDirectionString());
        favorite.setOrder(currentStop.getStopOrder());

        Gson gson = new Gson();
        String fav = gson.toJson(favorite, Favorite.class);

        List<String> strings = new ArrayList<>();

        if (stringSet == null || stringSet.size() == 0) {
            strings.add(fav);
        } else {
            for (String aStringSet : stringSet) {
                strings.add(aStringSet);
            }
            strings.add(fav);
        }

        sharedPreferences.edit().putStringSet(Constants.FAVORITES_KEY, new HashSet<>(strings)).apply();
        Toast.makeText(this, getString(R.string.route_added), Toast.LENGTH_LONG).show();
    }

    private String getDirectionString() {
        return directionSwitch.isChecked() ? "1" : "0";
    }

    private void setRoutesSpinner() {
        cursor = MbtaBusTrackerApplication.getDbAdapter().db.query(Schema.RoutesTable.TABLE_NAME, Schema.RoutesTable.ALL_COLUMNS, null, null, null, null, null);
        if (cursor.getCount() > 0) {
            if (routesAdapter == null) {
                routesAdapter = new RoutesAdapter(this, cursor);
                routesSpinner.setAdapter(routesAdapter);
            } else {
                routesAdapter.loadNewCursor(cursor);
            }
        }
    }

    private void getAllRoutes() {
        RetrofitManager.getRealtimeService().getAllRoutes(RetrofitManager.API_KEY, RetrofitManager.FORMAT, new Callback<AllRoutesWrapper>() {
            @Override
            public void success(AllRoutesWrapper allRoutesWrapper, Response response) {
                Timber.d("Success!");
                MbtaBusTrackerApplication.bus.post(new OttoBusEvent.RoutesReturnedEvent(allRoutesWrapper));
            }

            @Override
            public void failure(RetrofitError error) {
                Timber.e("Failed", error);
                MbtaBusTrackerApplication.bus.post(new OttoBusEvent.RetrofitFailureEvent(error));
            }
        });
    }

    private void getStopsForRoute(final String routeId) {
        RetrofitManager.getRealtimeService().getStopsByRoute(RetrofitManager.API_KEY, RetrofitManager.FORMAT, routeId, new Callback<StopsByRouteWrapper>() {
            @Override
            public void success(StopsByRouteWrapper stopsByRouteWrapper, Response response) {
                Timber.d("Success!");

                List<Direction2> directions = stopsByRouteWrapper.getDirection();
                List<Stop> stops = directions.get(directionSwitch.isChecked() ? 1 : 0).getStop();

                PersistStopsInDbTask persistStopsInDbTask = new PersistStopsInDbTask(routeId, getDirectionString());
                persistStopsInDbTask.execute(stops);
            }

            @Override
            public void failure(RetrofitError error) {
                MbtaBusTrackerApplication.bus.post(new OttoBusEvent.RetrofitFailureEvent(error));
            }
        });
    }

    private LatLngBounds addStopMarkersToMap(Cursor c) {
        map.clear();
        currentlyVisibleMarkers.clear();

        LatLngBounds.Builder builder = LatLngBounds.builder();

        PolylineOptions polylineOptions = new PolylineOptions();

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
            currentlyVisibleMarkers.add(marker);
        }

        map.addPolyline(polylineOptions);

        return builder.build();
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

    @Subscribe
    public void allRoutesReturned(OttoBusEvent.RoutesReturnedEvent event) {
        List<Mode> modes = event.getWrapper().getMode();
        for (Mode mode : modes) {
            if (mode.getRouteType().equals(Constants.ROUTE_TYPE_BUS)) {
                List<Route> routes = mode.getRoute();
                //noinspection unchecked
                new PersistRoutesInDbTask().execute(routes);
            }
        }
    }

    @Subscribe
    public void feedInfoReturned(OttoBusEvent.FeedInfoReturnedEvent event) {
        Response response = event.getResponse();

        InputStream in = null;
        try {
            in = response.getBody().in();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String s = null;
        try {
            s = IoUtils.toString(in);
        } catch (IOException e) {
            Timber.e(e, "failed");
        }

        if (s != null) {
            //TODO: Make this more flexible, in case MBTA changes the text file format

            String[] halves = s.split("\\r\\n");
            String values = halves[1];
            String[] data = values.split(",");
            int lastIndex = data.length - 1;

            String startDate = data[lastIndex - 3];
            String endDate = data[lastIndex - 2];
            String version = data[lastIndex - 1];

            FeedInfo feedInfo = new FeedInfo(startDate, endDate, version);
            FeedInfo localFeedInfo = dbAdapter.getFeedInfo();

            if (localFeedInfo.getStartDate() != null && localFeedInfo.getEndDate() != null && (feedInfo.getStartDate().getTimeInMillis() > localFeedInfo.getStartDate().getTimeInMillis())) {
                dbAdapter.acquire(feedInfo);
                feedInfo.persistToDatabase();
                getAllRoutes();
            } else if (localFeedInfo.getStartDate() == null && localFeedInfo.getEndDate() == null) {
                dbAdapter.acquire(feedInfo);
                feedInfo.persistToDatabase();
                getAllRoutes();
            }
        }
    }

    @Subscribe
    public void dbPersistCompleted(OttoBusEvent.DbPersistCompletedEvent event) {
        setRoutesSpinner();
    }

    @Subscribe
    public void stopReturned(OttoBusEvent.StopsPersistedEvent event) {
        List<RouteStop> routeStops = new ArrayList<>();


        for (Long id : event.getIds()) {
            RouteStop rs = new RouteStop(event.getRouteId(), id, getDirectionString());
            routeStops.add(rs);
        }


        //TODO: Get stop from DB instead

        PersistRouteStopsInDbTask persistRouteStopsInDbTask = new PersistRouteStopsInDbTask(event.getRouteId(), getDirectionString());
        persistRouteStopsInDbTask.execute(routeStops);
    }

    @Subscribe
    public void routeStopReturned(OttoBusEvent.RouteStopsPersistedEvent event) {
        List<RouteStop> routeStops = dbAdapter.getRouteStops(Schema.RouteStopsTable.ROUTE_ID, event.getRouteId());
        getScheduledStops();
    }

    @Subscribe
    public void stopTimesReturned(OttoBusEvent.StopTimesReturnedEvent event) {
        List<RouteStop> routeStops = dbAdapter.getRouteStops(Schema.RouteStopsTable.ROUTE_ID, currentRoute.getRouteId());

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

        StringBuilder dbStopIdsListBuilder = new StringBuilder();
        dbStopIdsListBuilder.append("(");
        for (int i = 0; i < stopTimes.size(); i++) {
            dbStopIdsListBuilder.append(stopTimes.get(i).getStopId());
            if (i >= stopTimes.size() - 1) {
                break;
            }

            dbStopIdsListBuilder.append(",");
        }
        dbStopIdsListBuilder.append(")");

        cursor = dbAdapter.db.query(Schema.StopsTable.TABLE_NAME, Schema.StopsTable.ALL_COLUMNS, Schema.StopsTable.ID_COL + " IN " + dbIdsListBuilder.toString() + " AND " + Schema.StopsTable.STOP_DIRECTION + " = " + (directionSwitch.isChecked() ? "\'1\'" : "\'0\'") + " AND " + Schema.StopsTable.STOP_ID + " IN " + dbStopIdsListBuilder.toString(), null, null, null, Schema.StopsTable.STOP_ORDER);

        if (cursor.getCount() > 0) {
            LatLngBounds bounds = MapUtils.addStopMarkersToMap(cursor, map, currentlyVisibleMarkers);
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80));

            cursor.moveToFirst();
            if (stopsAdapter == null) {
                stopsAdapter = new StopsAdapter(this, cursor);
                stopsSpinner.setAdapter(stopsAdapter);
            } else {
                stopsAdapter.loadNewCursor(cursor);
            }
            stopsSpinner.setSelection(0);
        } else {
            getStopsForRoute(currentRoute.getRouteId());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_landing, menu);
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
