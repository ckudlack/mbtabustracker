package com.ckudlack.mbtabustracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ckudlack.mbtabustracker.Constants;
import com.ckudlack.mbtabustracker.OttoBusEvent;
import com.ckudlack.mbtabustracker.R;
import com.ckudlack.mbtabustracker.SpacesItemDecoration;
import com.ckudlack.mbtabustracker.adapters.FavoritesAdapter;
import com.ckudlack.mbtabustracker.application.MbtaBusTrackerApplication;
import com.ckudlack.mbtabustracker.database.DBAdapter;
import com.ckudlack.mbtabustracker.models.Direction;
import com.ckudlack.mbtabustracker.models.Favorite;
import com.ckudlack.mbtabustracker.models.Mode;
import com.ckudlack.mbtabustracker.models.Route;
import com.ckudlack.mbtabustracker.models.StopPredictionWrapper;
import com.ckudlack.mbtabustracker.models.Trip;
import com.ckudlack.mbtabustracker.net.RetrofitManager;
import com.directions.route.RoutingListener;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class FavoritesActivity extends LocationActivity implements FavoritesAdapter.ItemClickedCallback,
        RoutingListener {

    private static final int ADD_FAV_REQ_CODE = 324;

    private RecyclerView recyclerView;
    private FavoritesAdapter adapter;
    private LinearLayoutManager layoutManager;
    private List<Favorite> favoritesList;
    private Timer timer = new Timer();
    private DBAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        setupLocationServices();
        dbAdapter = MbtaBusTrackerApplication.getDbAdapter();

        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.floating_button);
        button.setSize(FloatingActionButton.SIZE_NORMAL);
        button.setColorNormalResId(android.R.color.holo_blue_dark);
        button.setColorPressedResId(android.R.color.holo_blue_light);
        button.setIcon(R.mipmap.ic_action_star);
        button.setStrokeVisible(false);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FavoritesActivity.this, AddNewRouteActivity.class);
                startActivityForResult(intent, ADD_FAV_REQ_CODE);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SpacesItemDecoration(50));

        favoritesList = dbAdapter.getFavorites();

        adapter = new FavoritesAdapter(favoritesList, this);
        recyclerView.setAdapter(adapter);

        getPredictionsForFavStop(favoritesList);
    }

    private void setupLocationServices() {
        locationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        locationClient.connect();
    }

    /*private void buildNotification(Favorite favorite) {
        int notificationId = 123;
// Build intent for notification content
*//*        Intent viewIntent = new Intent(this, ViewEventActivity.class);
        viewIntent.putExtra(EXTRA_EVENT_ID, eventId);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);*//*

        Intent notificationIntent = new Intent(this, WearDisplayActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
        wearableExtender.setDisplayIntent(notificationPendingIntent);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(
                                getResources(), R.mipmap.ic_launcher))
                        .setContentTitle("Bus Tracker")
                        .setContentText("Event Text")
                        .extend(wearableExtender);
//                        .setContentIntent(viewPendingIntent);


// Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

// Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
    }*/

    private void getPredictionsForFavStop(final List<Favorite> favoritesList) {
        if (favoritesList.size() == 0) {
            adapter.updateList(this.favoritesList);
            return;
        }

        final Favorite favorite = favoritesList.get(0);
        RetrofitManager.getRealtimeService().getPredictionsByStop(favorite.getStopId(), new Callback<StopPredictionWrapper>() {
            @Override
            public void success(StopPredictionWrapper stopPredictionWrapper, Response response) {
                Timber.d("Success!");
                MbtaBusTrackerApplication.bus.post(new OttoBusEvent.PredictionsByStopReturnEvent(stopPredictionWrapper, favoritesList.get(0)));
                getPredictionsForFavStop(favoritesList.subList(1, favoritesList.size()));
            }

            @Override
            public void failure(RetrofitError error) {
                MbtaBusTrackerApplication.bus.post(new OttoBusEvent.RetrofitFailureEvent(error));
                getPredictionsForFavStop(favoritesList.subList(1, favoritesList.size()));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        favoritesList = dbAdapter.getFavorites();
        getPredictionsForFavStop(favoritesList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MbtaBusTrackerApplication.bus.register(this);
        timer = new Timer();
        scheduleTimer();
    }

    private void scheduleTimer() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getPredictionsForFavStop(favoritesList);
                    }
                });
            }
        }, 5000, 20000);
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
        getMenuInflater().inflate(R.menu.menu_favorites, menu);
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
                if (r.getRouteId().equals(event.getFavorite().getRouteId())) {
                    Direction direction;
                    if (r.getDirection().size() > 1) {
                        direction = r.getDirection().get(Integer.parseInt(event.getFavorite().getDirectionId()));
                    } else {
                        direction = r.getDirection().get(0);
                    }

                    List<Trip> trips = direction.getTrip();
                    StringBuilder sb = new StringBuilder();
                    for (Trip t : trips) {
                        int timeRemaining = (int) (Integer.parseInt(t.getPreAway()) / 60f);
                        sb.append(String.valueOf(timeRemaining));
                        sb.append(" mins, ");
                    }
                    sb.delete(sb.length() - 2, sb.length());

                    int index = favoritesList.indexOf(event.getFavorite());
                    favoritesList.get(index).setPredictions(sb.toString());

                    break;
                }
            }
        }
    }

    @Override
    public void onListItemClicked(int position) {
        Favorite favorite = favoritesList.get(position);

//        buildNotification(favorite);


        Intent intent = new Intent(this, RouteMapActivity.class);
        intent.putExtra(Constants.ROUTE_ID_KEY, favorite.getRouteId());
        intent.putExtra(Constants.DIRECTION_KEY, favorite.getDirectionId());
        intent.putExtra(Constants.ORDER_KEY, favorite.getOrder());
        intent.putExtra(Constants.STOP_NAME_KEY, favorite.getStopName());
        intent.putExtra(Constants.STOP_KEY, favorite.getStopId());
        startActivity(intent);
    }

    @Override
    public void onCloseButtonClicked(int position) {
        adapter.notifyItemRemoved(position);
        favoritesList.get(position).deleteFromDatabase();
        favoritesList = dbAdapter.getFavorites(); //reload them

        //TODO: Fix concurrent modification exception when predictions are being updated and a favorite is removed
    }

    @Override
    public void onRoutingFailure() {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, com.directions.route.Route route) {
        /*String stopName = favoritesList.get(1).getStopName();
        stopName += " (" + route.getDurationText() + ")";
        favoritesList.get(1).setStopName(stopName);
        adapter.updateList(this.favoritesList);*/
    }
}
