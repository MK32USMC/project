package ca.concordia.controller;

import ca.concordia.model.BixiTrip;


public interface IBixiController {

    /** REQ 1 - Trip List */
    Iterable<BixiTrip> getTripsByStation(String stationName, String mode);

    /** REQ 2 - Trips by Month */
    Iterable<BixiTrip> getTripsByMonth(String month);

    /** REQ 3 - Lists trips over a given duration */
    Iterable<BixiTrip> getTripsByDuration(float minDuration);

    /** REQ 4 - Lists trips by start time within a given interval */
    Iterable<BixiTrip> getTripsByStartTime(String startTime, String finalTime);

    /** REQ 5 - Lists top Arrondissements of desired number */
    Iterable<String> getTopArrondissements(int k);

    /** REQ 6 - Lists top stations in desired time perios and of desired number */
    Iterable<String> getTopStations(int k, String startDate, String endDate);

    /** REQ 7 - Figures out Rush Hour of a given month */
    String getRushHourOfMonth(int month);

    /** REQ 8 - Compares two months */
    void compareMonths(int month1, int month2, int k);

    /** Loads CSV file at filepath, and counts trips and stations */
    void loadFile(String filePath);
}
