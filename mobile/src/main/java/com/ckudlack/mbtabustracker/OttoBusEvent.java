package com.ckudlack.mbtabustracker;

import com.ckudlack.mbtabustracker.models.AllRoutesWrapper;
import com.ckudlack.mbtabustracker.models.Stop;
import com.ckudlack.mbtabustracker.models.StopPredictionWrapper;

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
        private List<Stop> stops;

        public StopsPersistedEvent(String routeId, List<Stop> stops) {
            this.routeId = routeId;
            this.stops = stops;
        }

        public String getRouteId() {
            return routeId;
        }

        public List<Stop> getStops() {
            return stops;
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
        StopPredictionWrapper predictionWrapper;


        public PredictionsByStopReturnEvent(StopPredictionWrapper predictionWrapper) {
            this.predictionWrapper = predictionWrapper;
        }

        public StopPredictionWrapper getPredictionWrapper() {
            return predictionWrapper;
        }
    }
}
