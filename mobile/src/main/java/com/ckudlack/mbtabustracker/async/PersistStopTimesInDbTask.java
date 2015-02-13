package com.ckudlack.mbtabustracker.async;

import android.os.AsyncTask;

import com.ckudlack.mbtabustracker.models.Trip;

import java.util.List;

public class PersistStopTimesInDbTask extends AsyncTask<List<Trip>, Void, Void> {

    @Override
    protected Void doInBackground(List<Trip>... params) {
        return null;
    }
}
