package com.ckudlack.mbtabustracker.database;

public class Schema {

    public static class Table {
        public static final String ID_COL = "_id";
        protected static final String CREATE_TERMINATOR = ");";

        static String buildCreateSQL(String tableName, String createText) {
            return "create table " + tableName + " (" +
                    ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    createText +
                    CREATE_TERMINATOR;
        }
    }

    public static class FeedInfoTable extends Table {
        public static final String TABLE_NAME = "feed_info";
        public static final String START_DATE = "feed_start_date";
        public static final String END_DATE = "feed_end_date";
        public static final String VERSION = "feed_version";

        public static final String CREATE_TABLE_SQL =
                buildCreateSQL(TABLE_NAME,
                        START_DATE + " text not null, " +
                                END_DATE + " text not null, " +
                                VERSION + " text not null"
                );

        public static final String[] ALL_COLUMNS = new String[]{
                ID_COL, START_DATE, END_DATE, VERSION
        };
    }

    public static class RoutesTable extends Table {
        public static final String TABLE_NAME = "routes";
        public static final String ROUTE_ID = "route_id";
        public static final String ROUTE_NAME = "route_name";

        public static final String CREATE_TABLE_SQL =
                buildCreateSQL(TABLE_NAME,
                        ROUTE_ID + " text not null, " +
                                ROUTE_NAME + " text not null"
                );

        public static final String[] ALL_COLUMNS = new String[]{
                ID_COL, ROUTE_ID, ROUTE_NAME
        };
    }

    public static class RouteStopsTable extends Table {
        public static final String TABLE_NAME = "route_stops";
        public static final String ROUTE_ID = "route_id";
        public static final String STOP_ID = "stop_id";
        public static final String DIRECTION = "direction";

        public static final String CREATE_TABLE_SQL =
                buildCreateSQL(TABLE_NAME,
                        ROUTE_ID + " text not null, " +
                                STOP_ID + " text not null, " +
                                DIRECTION + " text not null"
                );

        public static final String[] ALL_COLUMNS = new String[]{
                ID_COL, ROUTE_ID, STOP_ID, DIRECTION
        };
    }

    public static class DistancesTable extends Table {
        public static final String TABLE_NAME = "distances";
        public static final String STOP_DEPART = "stop_depart";
        public static final String STOP_ARRIVE = "stop_arrive";
        public static final String DISTANCE = "distance";
        public static final String ROUTE_ID = "route_id";

        public static final String CREATE_TABLE_SQL =
                buildCreateSQL(TABLE_NAME,
                        ROUTE_ID + " text not null, " +
                                STOP_DEPART + " text not null, " +
                                STOP_ARRIVE + " text not null, " +
                                DISTANCE + " double not null"
                );

        public static final String[] ALL_COLUMNS = new String[]{
                ID_COL, ROUTE_ID, STOP_ARRIVE, STOP_DEPART, DISTANCE
        };
    }

    public static class ShapesTable extends Table {
        public static final String TABLE_NAME = "shapes";
        public static final String SHAPE_ID = "shape_id";
        public static final String LATITUDE = "lat";
        public static final String LONGITUDE = "long";

        public static final String CREATE_TABLE_SQL =
                buildCreateSQL(TABLE_NAME,
                        SHAPE_ID + " long not null, " +
                                LATITUDE + " double not null, " +
                                LONGITUDE + " double not null"
                );

        public static final String[] ALL_COLUMNS = new String[]{
                ID_COL, SHAPE_ID, LATITUDE, LONGITUDE
        };
    }

    public static class StopsTable extends Table {
        public static final String TABLE_NAME = "stops";
        public static final String STOP_ID = "stop_id";
        public static final String STOP_NAME = "stop_name";
        public static final String STOP_LONG = "stop_long";
        public static final String STOP_LAT = "stop_lat";
        public static final String STOP_ORDER = "stop_order";
        public static final String STOP_PARENT_STATION = "stop_parent_station";
        public static final String STOP_PARENT_STATION_NAME = "stop_parent_station_name";
        public static final String STOP_DIRECTION = "stop_direction";

        public static final String CREATE_TABLE_SQL =
                buildCreateSQL(TABLE_NAME,
                        STOP_ID + " text not null, " +
                                STOP_NAME + " text not null, " +
                                STOP_PARENT_STATION + " text not null, " +
                                STOP_PARENT_STATION_NAME + " text not null, " +
                                STOP_DIRECTION + " text not null, " +
                                STOP_ORDER + " integer not null, " +
                                STOP_LAT + " double not null, " +
                                STOP_LONG + " double not null"
                );

        public static final String[] ALL_COLUMNS = new String[]{
                ID_COL, STOP_ID, STOP_NAME, STOP_LAT, STOP_LONG, STOP_ORDER, STOP_PARENT_STATION, STOP_PARENT_STATION_NAME, STOP_DIRECTION
        };
    }

    public static class FavoritesTable extends Table {
        public static final String TABLE_NAME = "favorites";
        public static final String STOP_ID = "stop_id";
        public static final String ROUTE_ID = "route_id";
        public static final String DIRECTION_ID = "direction_id";
        public static final String STOP_NAME = "stop_name";
        public static final String ROUTE_NAME = "route_name";
        public static final String STOP_ORDER = "stop_order";
        public static final String STOP_LONG = "stop_long";
        public static final String STOP_LAT = "stop_lat";
        public static final String PREDICTIONS = "predictions";
        public static final String DIRECTION_NAME = "stop_direction";

        public static final String CREATE_TABLE_SQL =
                buildCreateSQL(TABLE_NAME,
                        STOP_ID + " text not null, " +
                                STOP_NAME + " text not null, " +
                                DIRECTION_NAME + " text not null, " +
                                ROUTE_ID + " text not null, " +
                                DIRECTION_ID + " text not null, " +
                                ROUTE_NAME + " text not null, " +
                                STOP_LAT + " double not null, " +
                                STOP_ORDER + " text not null, " +
                                PREDICTIONS + " text, " +
                                STOP_LONG + " double not null"
                );

        public static final String[] ALL_COLUMNS = new String[]{
                ID_COL, STOP_ID, STOP_NAME, STOP_LAT, STOP_LONG, ROUTE_ID, ROUTE_NAME, DIRECTION_ID, DIRECTION_NAME, PREDICTIONS, STOP_ORDER
        };
    }

    public static class TripsTable extends Table {
        public static final String TABLE_NAME = "trips";
        public static final String ROUTE_ID = "route_id";
        public static final String TRIP_ID = "trip_id";
        public static final String SERVICE_ID = "service_id";
        public static final String DIRECTION_ID = "direction_id";
        public static final String TRAIN_NUMBER = "train_number";
        public static final String SHAPE_ID = "shape_id";

        public static final String CREATE_TABLE_SQL =
                buildCreateSQL(TABLE_NAME,
                        ROUTE_ID + " text not null, " +
                                TRIP_ID + " text not null, " +
                                SERVICE_ID + " text not null, " +
                                DIRECTION_ID + " integer not null, " +
                                TRAIN_NUMBER + " integer not null, " +
                                SHAPE_ID + " long not null"
                );

        public static final String[] ALL_COLUMNS = new String[]{
                ID_COL, ROUTE_ID, TRIP_ID, SERVICE_ID, DIRECTION_ID, TRAIN_NUMBER, SHAPE_ID
        };
    }

    public static class StopTimesTable extends Table {
        public static final String TABLE_NAME = "stop_times";
        public static final String ROUTE_ID = "route_id";
        public static final String TRIP_ID = "trip_id";
        public static final String STOP_ID = "stop_id";
        public static final String ARRIVAL_TIME = "arrival_time";
        public static final String DEPARTURE_TIME = "departure_time";
        public static final String STOP_SEQUENCE = "stop_sequence";
        public static final String DAY = "day";
        public static final String DIRECTION = "direction";

        public static final String CREATE_TABLE_SQL =
                buildCreateSQL(TABLE_NAME,
                        ROUTE_ID + " text not null, " +
                                TRIP_ID + " text not null, " +
                                STOP_ID + " text not null, " +
                                ARRIVAL_TIME + " text not null, " +
                                DEPARTURE_TIME + " text not null, " +
                                STOP_SEQUENCE + " integer not null, " +
                                DAY + " text not null, " +
                                DIRECTION + " integer not null"
                );

        public static final String[] ALL_COLUMNS = new String[]{
                ID_COL, ROUTE_ID, TRIP_ID, STOP_ID, ARRIVAL_TIME, DEPARTURE_TIME, STOP_SEQUENCE, DAY, DIRECTION
        };

    }
}