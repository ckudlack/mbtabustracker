package com.ckudlack.mbtabustracker.activities;

import android.database.Cursor;
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
import android.widget.TextView;

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
import com.ckudlack.mbtabustracker.models.FeedInfo;
import com.ckudlack.mbtabustracker.models.Mode;
import com.ckudlack.mbtabustracker.models.Route;
import com.ckudlack.mbtabustracker.models.RouteStop;
import com.ckudlack.mbtabustracker.models.Stop;
import com.ckudlack.mbtabustracker.models.StopPredictionWrapper;
import com.ckudlack.mbtabustracker.models.StopsByRouteWrapper;
import com.ckudlack.mbtabustracker.models.Trip;
import com.ckudlack.mbtabustracker.net.RetrofitManager;
import com.ckudlack.mbtabustracker.utils.IoUtils;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;


public class LandingActivity extends ActionBarActivity {

    private RoutesAdapter routesAdapter;
    private StopsAdapter stopsAdapter;

    private Cursor cursor;
    private DBAdapter dbAdapter;

    private Spinner routesSpinner;
    private Spinner stopsSpinner;
    private Switch directionSwitch;
    private Button goButton;
    private TextView tripInfo;

    private Route currentRoute;
    private Stop currentStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

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

        tripInfo = (TextView) findViewById(R.id.trip_info);

        routesSpinner = (Spinner) findViewById(R.id.routes_spinner);
        routesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    return;
                }

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
                    getStopsFromForeignKey(routeStops);
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
                if (position == 0) {
                    return;
                }

                Cursor c = stopsAdapter.getCursor();
                c.moveToPosition(position);
                Stop stop = new Stop();
                stop.buildFromCursor(c, dbAdapter);

                currentStop = stop;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setRoutesSpinner();

        directionSwitch = (Switch) findViewById(R.id.direction_switch);
        directionSwitch.setEnabled(false);

        directionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });


        goButton = (Button) findViewById(R.id.go_button);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RetrofitManager.getRealtimeService().getPredictionsByStop(RetrofitManager.API_KEY, RetrofitManager.FORMAT, currentStop.getStopId(), new Callback<StopPredictionWrapper>() {
                    @Override
                    public void success(StopPredictionWrapper stopPredictionWrapper, Response response) {
                        Timber.d("Success");
                        MbtaBusTrackerApplication.bus.post(new OttoBusEvent.PredictionsByStopReturnEvent(stopPredictionWrapper));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        MbtaBusTrackerApplication.bus.post(new OttoBusEvent.RetrofitFailureEvent(error));
                    }
                });
            }
        });
    }

    private void getStopsFromForeignKey(List<RouteStop> routeStops) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < routeStops.size(); i++) {
            sb.append(routeStops.get(i).getStopId());
            if (i >= routeStops.size() - 1) {
                break;
            }

            sb.append(",");
        }
        sb.append(")");

        //TODO: Make order depend on inbound / outbound
        cursor = dbAdapter.db.query(Schema.StopsTable.TABLE_NAME, Schema.StopsTable.ALL_COLUMNS, Schema.StopsTable.STOP_ID + " IN " + sb.toString(), null, null, null, Schema.StopsTable.STOP_ORDER);

        //TODO: Update cursor in StopsAdapter instead of creating new instance each time
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            stopsAdapter = new StopsAdapter(this, cursor);
            stopsSpinner.setAdapter(stopsAdapter);
        }
    }

    private void setRoutesSpinner() {
        cursor = MbtaBusTrackerApplication.getDbAdapter().db.query(Schema.RoutesTable.TABLE_NAME, Schema.RoutesTable.ALL_COLUMNS, null, null, null, null, null);
        if (cursor.getCount() > 0) {
            routesAdapter = new RoutesAdapter(this, cursor);
        }

        //TODO: Update cursor in RoutesAdapter instead of creating new instance each time
        if (routesAdapter != null) {
            routesSpinner.setAdapter(routesAdapter);
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
                List<Stop> stops = directions.get(0).getStop();

                PersistStopsInDbTask persistStopsInDbTask = new PersistStopsInDbTask(routeId);
                persistStopsInDbTask.execute(stops);
            }

            @Override
            public void failure(RetrofitError error) {
                MbtaBusTrackerApplication.bus.post(new OttoBusEvent.RetrofitFailureEvent(error));
            }
        });
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

        for (Stop s : event.getStops()) {
            RouteStop rs = new RouteStop(event.getRouteId(), s.getStopId());
            routeStops.add(rs);
        }

        PersistRouteStopsInDbTask persistRouteStopsInDbTask = new PersistRouteStopsInDbTask(event.getRouteId());
        persistRouteStopsInDbTask.execute(routeStops);
    }

    @Subscribe
    public void routeStopReturned(OttoBusEvent.RouteStopsPersistedEvent event) {
        List<RouteStop> routeStops = dbAdapter.getRouteStops(Schema.RouteStopsTable.ROUTE_ID, event.getRouteId());
        getStopsFromForeignKey(routeStops);
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

        if (busMode != null) {
            List<Route> routes = busMode.getRoute();
            for (Route r : routes) {
                if (r.getRouteId().equals(currentRoute.getRouteId())) {
                    Direction direction = r.getDirection().get(directionSwitch.isChecked() ? 1 : 0);
                    List<Trip> trips = direction.getTrip();
                    StringBuilder sb = new StringBuilder();
                    for (Trip t : trips) {
                        int timeRemaining = (int) (Integer.parseInt(t.getPreAway()) / 60f);
                        sb.append(String.valueOf(timeRemaining));
                        sb.append(" mins, ");
                    }
                    sb.delete(sb.length() - 2, sb.length());

                    tripInfo.setText(sb.toString());
                    break;
                }
            }
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
