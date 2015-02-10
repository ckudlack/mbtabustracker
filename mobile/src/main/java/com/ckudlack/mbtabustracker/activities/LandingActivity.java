package com.ckudlack.mbtabustracker.activities;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.ckudlack.mbtabustracker.Constants;
import com.ckudlack.mbtabustracker.OttoBusEvent;
import com.ckudlack.mbtabustracker.R;
import com.ckudlack.mbtabustracker.adapters.RoutesAdapter;
import com.ckudlack.mbtabustracker.adapters.StopsAdapter;
import com.ckudlack.mbtabustracker.application.MbtaBusTrackerApplication;
import com.ckudlack.mbtabustracker.async.PersistRoutesInDbTask;
import com.ckudlack.mbtabustracker.async.PersistStopsInDbTask;
import com.ckudlack.mbtabustracker.database.DBAdapter;
import com.ckudlack.mbtabustracker.database.Schema;
import com.ckudlack.mbtabustracker.models.AllRoutesWrapper;
import com.ckudlack.mbtabustracker.models.Direction2;
import com.ckudlack.mbtabustracker.models.FeedInfo;
import com.ckudlack.mbtabustracker.models.Mode;
import com.ckudlack.mbtabustracker.models.Route;
import com.ckudlack.mbtabustracker.models.Stop;
import com.ckudlack.mbtabustracker.models.StopsByRouteWrapper;
import com.ckudlack.mbtabustracker.net.RetrofitManager;
import com.ckudlack.mbtabustracker.utils.IoUtils;
import com.squareup.otto.Subscribe;

import java.io.File;
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
    private Cursor cursor;
    private Spinner routesSpinner;
    private Spinner stopsSpinner;
    private DBAdapter dbAdapter;

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

        Button b = (Button) findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllRoutes();
            }
        });

        routesSpinner = (Spinner) findViewById(R.id.routes_spinner);
        routesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    return;
                }

                Cursor c = routesAdapter.getCursor();
                c.moveToPosition(position);
                String routeId = cursor.getString(c.getColumnIndex(Schema.RoutesTable.ROUTE_ID));
                Route route = new Route();
                route.buildFromCursor(c, dbAdapter);
                if (route.getStopIds() == null) {
                    getStopsForRoute(routeId);
                } else {
                    stopReturned(new OttoBusEvent.StopsPersistCompletedEvent(route.getRouteId()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        stopsSpinner = (Spinner) findViewById(R.id.stops_spinner);

        setRoutesSpinner();
    }

    private void setRoutesSpinner() {
        cursor = MbtaBusTrackerApplication.getDbAdapter().db.query(Schema.RoutesTable.TABLE_NAME, Schema.RoutesTable.ALL_COLUMNS, null, null, null, null, null);
        if (cursor.getCount() > 0) {
            routesAdapter = new RoutesAdapter(this, cursor);
        }

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

                List<String> stopIds = new ArrayList<>();
                StringBuilder sb = new StringBuilder();
                for (Stop s : stops) {
                    stopIds.add(s.getStopId());
                    sb.append(s.getStopId());
                    sb.append(" ");
                }

                String idsString = sb.toString().trim();

                ContentValues contentValues = new ContentValues();
                contentValues.put(Schema.RoutesTable.STOP_IDS_LIST, idsString);
                dbAdapter.db.update(Schema.RoutesTable.TABLE_NAME, contentValues, Schema.RoutesTable.ROUTE_ID + " = " + routeId, null);
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
    public void stopReturned(OttoBusEvent.StopsPersistCompletedEvent event) {
        //TODO: Fix query
        cursor = MbtaBusTrackerApplication.getDbAdapter().db.query(Schema.StopsTable.TABLE_NAME, Schema.StopsTable.ALL_COLUMNS, Schema.StopsTable.STOP_ID + " = " + event.getRouteId(), null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            StopsAdapter stopsAdapter = new StopsAdapter(this, cursor);
            stopsSpinner.setAdapter(stopsAdapter);
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
