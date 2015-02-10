package com.ckudlack.mbtabustracker.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.ckudlack.mbtabustracker.database.DBAdapter;
import com.ckudlack.mbtabustracker.database.DatabaseObject;
import com.ckudlack.mbtabustracker.database.Schema;

import java.util.GregorianCalendar;

public class FeedInfo extends DatabaseObject {

    private String feedStartDateString;
    private String feedEndDateString;
    private String feedVersion;

    private GregorianCalendar startDate;
    private GregorianCalendar endDate;

    public FeedInfo() {
    }

    public FeedInfo(String feedStartDateString, String feedEndDateString, String feedVersion) {
        this.feedStartDateString = feedStartDateString;
        this.feedEndDateString = feedEndDateString;
        this.feedVersion = feedVersion;

        createDateObjects(feedStartDateString, feedEndDateString);
    }

    public String getFeedStartDateString() {
        return feedStartDateString;
    }

    public void setFeedStartDateString(String feedStartDateString) {
        this.feedStartDateString = feedStartDateString;
    }

    public String getFeedEndDateString() {
        return feedEndDateString;
    }

    public void setFeedEndDateString(String feedEndDateString) {
        this.feedEndDateString = feedEndDateString;
    }

    public String getFeedVersion() {
        return feedVersion;
    }

    public void setFeedVersion(String feedVersion) {
        this.feedVersion = feedVersion;
    }

    public GregorianCalendar getStartDate() {
        return startDate;
    }

    public GregorianCalendar getEndDate() {
        return endDate;
    }

    @Override
    protected String getTableName() {
        return Schema.FeedInfoTable.TABLE_NAME;
    }

    @Override
    protected void buildSubclassFromCursor(Cursor cursor, DBAdapter dbAdapter) {
        setFeedStartDateString(cursor.getString(cursor.getColumnIndex(Schema.FeedInfoTable.START_DATE)));
        setFeedEndDateString(cursor.getString(cursor.getColumnIndex(Schema.FeedInfoTable.END_DATE)));
        setFeedVersion(cursor.getString(cursor.getColumnIndex(Schema.FeedInfoTable.VERSION)));

        createDateObjects(feedStartDateString, feedEndDateString);
    }

    @Override
    public void fillInContentValues(ContentValues values, DBAdapter dbAdapter) {
        contentValuesHelper(values);
    }

    public void contentValuesHelper(ContentValues values) {
        values.put(Schema.FeedInfoTable.START_DATE, feedStartDateString);
        values.put(Schema.FeedInfoTable.END_DATE, feedEndDateString);
        values.put(Schema.FeedInfoTable.VERSION, feedVersion);
    }

    private void createDateObjects(String feedStartDateString, String feedEndDateString) {
        startDate = calculateDate(feedStartDateString);
        endDate = calculateDate(feedEndDateString);
    }

    private GregorianCalendar calculateDate(String dateString) {
        int year = Integer.parseInt(dateString.substring(1, 5));
        int month = Integer.parseInt(dateString.substring(5, 7));
        int day = Integer.parseInt(dateString.substring(7, 9));

        return new GregorianCalendar(year, month, day);
    }
}
