package com.ckudlack.mbtabustracker.net;

import com.ckudlack.mbtabustracker.Constants;
import com.ckudlack.mbtabustracker.models.AllRoutesWrapper;
import com.ckudlack.mbtabustracker.models.RouteScheduleWrapper;
import com.ckudlack.mbtabustracker.models.StopPredictionWrapper;
import com.ckudlack.mbtabustracker.models.StopsByRouteWrapper;
import com.ckudlack.mbtabustracker.models.StopsNearMeWrapper;
import com.ckudlack.mbtabustracker.models.VehicleInfoWrapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;
import timber.log.Timber;

public class RetrofitManager {

    private static final Gson gson = new GsonBuilder().create();
    public static final String API_KEY = "a3pkaUJwEk-9u0nvkL_Byw";
    public static final String FORMAT = "json";

    private static RestAdapter MBTA_RT_REST_ADAPTER = new RestAdapter.Builder()
            .setEndpoint(Constants.MBTA_URL)
            .setConverter(new GsonConverter(gson))
            .setLog(new RestAdapter.Log() {
                @Override
                public void log(String message) {
                    Timber.d(message);
                }
            })
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .build();

    private static RestAdapter MBTA_FEED_INFO_REST_ADAPTER = new RestAdapter.Builder()
            .setEndpoint("http://www.mbta.com/uploadedfiles")
            .setConverter(new GsonConverter(gson))
            .setLog(new RestAdapter.Log() {
                @Override
                public void log(String message) {
                    Timber.d(message);
                }
            })
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .build();

    private static final MbtaService REALTIME_SERVICE = MBTA_RT_REST_ADAPTER.create(MbtaService.class);

    private static final MbtaFeedService FEED_SERVICE = MBTA_FEED_INFO_REST_ADAPTER.create(MbtaFeedService.class);

    public static MbtaService getRealtimeService() {
        return REALTIME_SERVICE;
    }

    public static MbtaFeedService getFeedService() {
        return FEED_SERVICE;
    }

    public interface MbtaService {

        @GET("/predictionsbystop")
        void getPredictionsByStop(@Query("api_key") String apiKey, @Query("format") String format, @Query("stop") String stopId, Callback<StopPredictionWrapper> callback);

        @GET("/stopsbyroute")
        void getStopsByRoute(@Query("api_key") String apiKey, @Query("format") String format, @Query("route") String routeId, Callback<StopsByRouteWrapper> callback);

        @GET("/routes")
        void getAllRoutes(@Query("api_key") String apiKey, @Query("format") String format, Callback<AllRoutesWrapper> callback);

        @GET("/stopsbylocation")
        void getStopsByLocation(@Query("api_key") String apiKey, @Query("format") String format, @Query("lat") float latitude, @Query("lon") float longitude, Callback<StopsNearMeWrapper> callback);

        @GET("/scheduleByRoute")
        void getScheduleByRoute(@Query("api_key") String apiKey, @Query("format") String format, @Query("route") String routeId, @Query("direction") String direction, Callback<RouteScheduleWrapper> callback);

        @GET("/vehiclesByRoute")
        void getVehiclesByRoute(@Query("api_key") String apiKey, @Query("format") String format, @Query("route") String routeId, Callback<VehicleInfoWrapper> callback);

    }

    public interface MbtaFeedService {

        @Headers({"Content-Type: text/plain"})
        @GET("/feed_info.txt")
        void getUpdateDate(Callback<Response> callback);
    }
}
