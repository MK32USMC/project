package ca.concordia.controller;

import ca.concordia.model.BixiTrip;

/**
 * Interface defining all BIXI data analysis operations.
 * COEN 352 — Programming Assignment 1
 *
 * Requirements coverage:
 *   REQ 1 (basic)        → getTripsByStation
 *   REQ 2 (basic)        → getTripsByMonth
 *   REQ 3 (intermediate) → getTripsByDuration
 *   REQ 4 (intermediate) → getTripsByStartTime
 *   REQ 5 (intermediate) → getTopArrondissements
 *   REQ 6 (advanced)     → getTopStations
 *   REQ 7 (advanced)     → getRushHourOfMonth
 *   REQ 8 (advanced)     → compareMonths
 */
public interface IBixiController {

    /**
     * REQ 1 (basic) — List all trips starting/ending at a given station.
     *
     * @param stationName the station name to search (case-insensitive)
     * @param mode        "start" | "end" | "both"
     * @return all matching trip records
     */
    Iterable<BixiTrip> getTripsByStation(String stationName, String mode);

    /**
     * REQ 2 (basic) — Find all trips made in a given month.
     * Results are sorted ascending by start time.
     *
     * @param month format "YYYY-MM" (e.g. "2025-06")
     * @return all trips whose STARTTIMEMS falls in that month
     */
    Iterable<BixiTrip> getTripsByMonth(String month);

    /**
     * REQ 3 (intermediate) — List trips with a duration longer than X minutes.
     * Duration = (ENDTIMEMS - STARTTIMEMS) / (1000 * 60).
     * Results are sorted descending by duration (longest first).
     *
     * @param minDuration threshold in minutes (exclusive)
     * @return all trips whose duration exceeds minDuration
     */
    Iterable<BixiTrip> getTripsByDuration(float minDuration);

    /**
     * REQ 4 (intermediate) — Find all trips within a start-time interval.
     * Results are sorted ascending by start time.
     *
     * @param startTime interval start in "YYYY-MM-DD HH:mm:ss" (Montreal time)
     * @param finalTime interval end   in "YYYY-MM-DD HH:mm:ss" (Montreal time)
     * @return all trips whose STARTTIMEMS falls within [startTime, finalTime]
     */
    Iterable<BixiTrip> getTripsByStartTime(String startTime, String finalTime);

    /**
     * REQ 5 (intermediate) — List the top N arrondissements by number of departures.
     * Ties are broken alphabetically by arrondissement name.
     *
     * @param k number of top arrondissements to return
     * @return formatted strings: "Arrondissement Name - N departures", ordered highest first
     */
    Iterable<String> getTopArrondissements(int k);

    /**
     * REQ 6 (advanced) — Find the top K most frequently used starting stations
     * in a given time period.
     * Results are sorted alphabetically by station name.
     *
     * @param k         number of top stations to return
     * @param startDate period start in "YYYY-MM-DD HH:mm:ss"
     * @param endDate   period end   in "YYYY-MM-DD HH:mm:ss"
     * @return formatted strings: "Station Name - N trips", sorted alphabetically
     */
    Iterable<String> getTopStations(int k, String startDate, String endDate);

    /**
     * REQ 7 (advanced) — Detect the "rush hour" in a given month.
     * Rush hour = the hour of day (0-23) with the highest average number
     * of trips per day across all days in that month.
     *
     * @param month month number 1-12
     * @return the rush hour (0-23), or -1 if no data found for that month
     */
    int getRushHourOfMonth(int month);

    /**
     * REQ 8 (advanced) — Compare two months based on their statistics.
     * Prints a side-by-side comparison of:
     *   - Total number of trips
     *   - K most-used START stations (with trip counts)
     *   - K most-used END stations   (with trip counts)
     *   - Rush hour + average trips/day during that hour
     *
     * @param month1 first month  (1-12)
     * @param month2 second month (1-12)
     * @param k      number of top stations to include in the comparison
     */
    void compareMonths(int month1, int month2, int k);

    /**
     * Loads BIXI trip data from a CSV file.
     * Reports total trips loaded and unique stations found.
     *
     * @param filePath path to the BIXI CSV file
     */
    void loadFile(String filePath);
}
