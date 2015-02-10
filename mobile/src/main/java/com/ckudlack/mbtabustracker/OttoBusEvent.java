package com.ckudlack.mbtabustracker;

import com.ckudlack.mbtabustracker.models.AllRoutesWrapper;

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

    public static class StopsPersistCompletedEvent extends OttoBusEvent {
        private String routeId;

        public StopsPersistCompletedEvent(String routeId) {
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
}
