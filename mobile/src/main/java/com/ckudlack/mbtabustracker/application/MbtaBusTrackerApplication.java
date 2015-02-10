package com.ckudlack.mbtabustracker.application;

import android.app.Application;
import android.text.TextUtils;
import android.widget.Toast;

import com.ckudlack.mbtabustracker.BuildConfig;
import com.ckudlack.mbtabustracker.OttoBusEvent;
import com.ckudlack.mbtabustracker.database.DBAdapter;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import retrofit.client.Response;
import timber.log.Timber;

public class MbtaBusTrackerApplication extends Application {

    public static DBAdapter dbAdapter;
    public static Bus bus = new Bus();
    public static GlobalOttoListener globalOttoListener;
    private MbtaBusTrackerApplication self;

    @Override
    public void onCreate() {
        super.onCreate();
        self = this;

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        bus = new Bus();
        globalOttoListener = new GlobalOttoListener();
        dbAdapter = new DBAdapter(this);
//        dbAdapter.clear();
    }

    public static DBAdapter getDbAdapter() {
        return dbAdapter;
    }

    private class GlobalOttoListener {
        public GlobalOttoListener() {
            bus.register(this);
        }

        @Subscribe
        public void onRetrofitError(OttoBusEvent.RetrofitFailureEvent event) {
            Timber.e("GLOBAL RETROFIT ERROR");
            String message = event.getError().getMessage();
            if (event.getError().isNetworkError()) {
                Toast.makeText(self, "Network error, please check connection", Toast.LENGTH_SHORT).show();
            } else if (event.getError().getResponse() != null) {
                if (event.getError().getResponse().getStatus() == 500) {
                    Toast.makeText(self, "Internal Server Error", Toast.LENGTH_SHORT).show();
                }
            } else {
                Response response = event.getError().getResponse();
                if (response != null) {
                    Toast.makeText(self, "HTTP " + response.getStatus() + " Error", Toast.LENGTH_SHORT).show();
                }
            }
            if (!TextUtils.isEmpty(message)) {
                Timber.e(message);
            }
        }
    }
}
