package com.ckudlack.mbtabustracker.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
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
import com.ckudlack.mbtabustracker.models.Direction;
import com.ckudlack.mbtabustracker.models.Favorite;
import com.ckudlack.mbtabustracker.models.Mode;
import com.ckudlack.mbtabustracker.models.Route;
import com.ckudlack.mbtabustracker.models.StopPredictionWrapper;
import com.ckudlack.mbtabustracker.models.Trip;
import com.ckudlack.mbtabustracker.net.RetrofitManager;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class FavoritesActivity extends ActionBarActivity implements FavoritesAdapter.ItemClickedCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int ADD_FAV_REQ_CODE = 324;

    private RecyclerView recyclerView;
    private FavoritesAdapter adapter;
    private LinearLayoutManager layoutManager;
    private List<Favorite> favoritesList;
    private Timer timer = new Timer();

    protected LatLng position;

    private GoogleApiClient locationClient;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        setupLocationServices();

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

        favoritesList = getFavoritesFromSharedPrefs();

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

    private void getPredictionsForFavStop(final List<Favorite> favoritesList) {
        if (favoritesList.size() == 0) {
            adapter.updateList(this.favoritesList);
            return;
        }

        final Favorite favorite = favoritesList.get(0);
        RetrofitManager.getRealtimeService().getPredictionsByStop(RetrofitManager.API_KEY, RetrofitManager.FORMAT, favorite.getStopId(), new Callback<StopPredictionWrapper>() {
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

    private List<Favorite> getFavoritesFromSharedPrefs() {
        List<Favorite> favoritesList = new ArrayList<>();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> favorites = sharedPrefs.getStringSet(Constants.FAVORITES_KEY, null);
        if (favorites != null) {
            Iterator<String> iterator = favorites.iterator();

            while ((iterator.hasNext())) {
                String fav = iterator.next();
                Gson gson = new Gson();

                Favorite favorite = gson.fromJson(fav, Favorite.class);
                favoritesList.add(favorite);
            }
        }
        return favoritesList;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        favoritesList = getFavoritesFromSharedPrefs();
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
    protected void onStart() {
        super.onStart();
        locationClient.connect();
    }

    @Override
    protected void onDestroy() {
        locationClient.disconnect();
        super.onDestroy();
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
        Intent intent = new Intent(this, RouteMapActivity.class);
        intent.putExtra(Constants.ROUTE_ID_KEY, favorite.getRouteId());
        intent.putExtra(Constants.DIRECTION_KEY, favorite.getDirectionId());
        intent.putExtra(Constants.STOP_KEY, favorite.getOrder());
        intent.putExtra(Constants.STOP_NAME_KEY, favorite.getStopName());
        startActivity(intent);
    }

    @Override
    public void onConnected(Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2 * 60 * 1000); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(
                locationClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        position = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
