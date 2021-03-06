package com.ckudlack.mbtabustracker;

import com.ckudlack.mbtabustracker.models.AllRoutesWrapper;
import com.ckudlack.mbtabustracker.models.Favorite;
import com.ckudlack.mbtabustracker.models.StopPredictionWrapper;
import com.ckudlack.mbtabustracker.models.StopTime;
import com.ckudlack.mbtabustracker.models.Trip;

import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class OttoBusEvent {

    public static class RoutesReturnedEvent extends OttoBusEvent {
        private AllRoutesWrapper wrapper;

        public RoutesReturnedEvent(AllRoutesWrapper routes) {
            this.wrapper = routes;
        }

        public AllRoutesWrapper getWrapper() {
            return wrapper;
        }
    }

    public static class FeedInfoReturnedEvent extends OttoBusEvent {
        private Response response;

        public FeedInfoReturnedEvent(Response response) {
            this.response = response;
        }

        public Response getResponse() {
            return response;
        }
    }

    public static class DbPersistCompletedEvent extends OttoBusEvent {
        public DbPersistCompletedEvent() {
        }
    }

    public static class StopsPersistedEvent extends OttoBusEvent {
        private String routeId;
        private List<Long> ids;

        public StopsPersistedEvent(String routeId, List<Long> ids) {
            this.routeId = routeId;
            this.ids = ids;
        }

        public String getRouteId() {
            return routeId;
        }

        public List<Long> getIds() {
            return ids;
        }
    }

    public static class RouteStopsPersistedEvent extends OttoBusEvent {
        private String routeId;

        public RouteStopsPersistedEvent(String routeId) {
            this.routeId = routeId;
        }

        public String getRouteId() {
            return routeId;
        }
    }

    public static class RetrofitFailureEvent extends OttoBusEvent {
        private RetrofitError error;

        public RetrofitFailureEvent(RetrofitError error) {
            this.error = error;
        }

        public RetrofitError getError() {
            return error;
        }
    }

    public static class PredictionsByStopReturnEvent extends OttoBusEvent {
        private StopPredictionWrapper predictionWrapper;
        private Favorite favorite;

        public PredictionsByStopReturnEvent(StopPredictionWrapper predictionWrapper, Favorite favorite) {
            this.predictionWrapper = predictionWrapper;
            this.favorite = favorite;
        }

        public StopPredictionWrapper getPredictionWrapper() {
            return predictionWrapper;
        }

        public Favorite getFavorite() {
            return favorite;
        }
    }

    public static class StopTimesReturnedEvent extends OttoBusEvent {
        private List<StopTime> stopTimes;

        public StopTimesReturnedEvent(List<StopTime> stopTimes) {
            this.stopTimes = stopTimes;
        }

        public List<StopTime> getStopTimes() {
            return stopTimes;
        }
    }

    public static class VehicleInfoReturnedEvent extends OttoBusEvent {
        List<Trip> trips;

        public VehicleInfoReturnedEvent(List<Trip> trips) {
            this.trips = trips;
        }

        public List<Trip> getTrips() {
            return trips;
        }
    }
}
