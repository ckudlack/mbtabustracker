package com.ckudlack.mbtabustracker.utils;

import com.ckudlack.mbtabustracker.models.RouteStop;
import com.ckudlack.mbtabustracker.models.StopTime;

import java.util.List;

public class StringUtils {

    public static String buildDbIdList(List<RouteStop> routeStops) {
        StringBuilder dbIdsListBuilder = new StringBuilder();
        dbIdsListBuilder.append("(");
        for (int i = 0; i < routeStops.size(); i++) {
            dbIdsListBuilder.append(routeStops.get(i).getStopDbId());
            if (i >= routeStops.size() - 1) {
                break;
            }

            dbIdsListBuilder.append(",");
        }
        dbIdsListBuilder.append(")");
        return dbIdsListBuilder.toString();
    }

    public static String buildStopIdList(List<StopTime> stopTimes) {
        StringBuilder stopIdsListBuilder = new StringBuilder();
        stopIdsListBuilder.append("(");
        for (int i = 0; i < stopTimes.size(); i++) {
            stopIdsListBuilder.append(stopTimes.get(i).getStopId());
            if (i >= stopTimes.size() - 1) {
                break;
            }

            stopIdsListBuilder.append(",");
        }
        stopIdsListBuilder.append(")");
        return stopIdsListBuilder.toString();
    }
}
