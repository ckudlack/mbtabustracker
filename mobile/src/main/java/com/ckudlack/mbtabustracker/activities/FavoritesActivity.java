package com.ckudlack.mbtabustracker.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ckudlack.mbtabustracker.Constants;
import com.ckudlack.mbtabustracker.R;
import com.ckudlack.mbtabustracker.adapters.FavoritesAdapter;
import com.ckudlack.mbtabustracker.models.Favorite;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FavoritesActivity extends ActionBarActivity {

    private static final int ADD_FAV_REQ_CODE = 324;

    private RecyclerView recyclerView;
    private FavoritesAdapter adapter;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

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

        List<Favorite> favoritesList = getFavoritesFromSharedPrefs();

        adapter = new FavoritesAdapter(favoritesList);
        recyclerView.setAdapter(adapter);
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
        List<Favorite> favorites = getFavoritesFromSharedPrefs();
        adapter.updateList(favorites);
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
}
