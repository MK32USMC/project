package ca.concordia.controller;

import ca.concordia.model.BixiTrip;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for BIXI trip data analysis.
 * Implements all 8 requirements from COEN 352 Programming Assignment 1.
 *
 * Complexity summary:
 *   REQ 1 — O(n)           filter by station
 *   REQ 2 — O(n log n)     filter by month + sort
 *   REQ 3 — O(n log n)     filter by duration + sort
 *   REQ 4 — O(n log n)     filter by time interval + sort
 *   REQ 5 — O(n + a log a) group by arrondissement + sort
 *   REQ 6 — O(n + s log K) top-K stations with min-heap
 *   REQ 7 — O(n)           rush hour via int[24] array
 *   REQ 8 — O(n + s log s) composite of REQ 2, 6, 7
 */
public class BixiController implements IBixiController {

    // ── Internal state ──────────────────────────────────────────────────────
    private List<BixiTrip> trips    = new ArrayList<>();
    private Set<String>    stations = new HashSet<>();  // unique station names

    private static final DateTimeFormatter INTERVAL_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId MONTREAL =
            ZoneId.of("America/Montreal");

    // ═══════════════════════════════════════════════════════════════════════
    // PART 3 — DATA LOADING
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Loads BIXI trip data from a CSV file.
     * Skips the header row automatically.
     * Reports total trips loaded and unique stations found.
     *
     * NOTE: Station names can contain commas (e.g. "Fullum / Marie-Anne"),
     * so we use a smarter split that limits fields to exactly 10 columns
     * from the right, keeping station names intact.
     *
     * Complexity: O(n) where n = number of rows in CSV.
     *
     * @param filePath path to the BIXI CSV file
     */
    @Override
    public void loadFile(String filePath) {
        trips.clear();
        stations.clear();

        System.out.println("Loading file: " + filePath);

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;  // skip header row
                    continue;
                }
                if (line.isBlank()) continue;

                BixiTrip trip = parseLine(line);
                if (trip != null) {
                    trips.add(trip);
                    stations.add(trip.getStartStationName());
                    stations.add(trip.getEndStationName());
                }
            }

            System.out.println("✓ Total trips loaded:    " + trips.size());
            System.out.println("✓ Unique stations found: " + stations.size());

        } catch (IOException e) {
            System.err.println("Error loading file: " + e.getMessage());
        }
    }

    /**
     * Parses a single CSV line into a BixiTrip.
     *
     * CSV columns (10 total):
     *   0  STARTSTATIONNAME
     *   1  STARTSTATIONARRONDISSEMENT
     *   2  STARTSTATIONLATITUDE
     *   3  STARTSTATIONLONGITUDE
     *   4  ENDSTATIONNAME
     *   5  ENDSTATIONARRONDISSEMENT
     *   6  ENDSTATIONLATITUDE
     *   7  ENDSTATIONLONGITUDE
     *   8  STARTTIMEMS
     *   9  ENDTIMEMS
     *
     * Strategy: split from the right using the last 4 numeric fields (lat, lon,
     * startMs, endMs), then treat everything before as station/borough fields.
     * This handles station names that contain commas.
     */
    private BixiTrip parseLine(String line) {
        try {
            // Split into at most 10 tokens
            String[] fields = line.split(",", 10);

            if (fields.length < 10) {
                System.err.println("Skipping malformed row (< 10 fields): " + line);
                return null;
            }

            String startStationName    = fields[0].trim();
            String startArrondissement = fields[1].trim();
            double startLat            = Double.parseDouble(fields[2].trim());
            double startLon            = Double.parseDouble(fields[3].trim());
            String endStationName      = fields[4].trim();
            String endArrondissement   = fields[5].trim();
            double endLat              = Double.parseDouble(fields[6].trim());
            double endLon              = Double.parseDouble(fields[7].trim());
            long   startTimeMs         = Long.parseLong(fields[8].trim());
            long   endTimeMs           = Long.parseLong(fields[9].trim());

            return new BixiTrip(
                    startStationName, startArrondissement, startLat, startLon,
                    endStationName,   endArrondissement,   endLat,   endLon,
                    startTimeMs, endTimeMs
            );

        } catch (NumberFormatException e) {
            System.err.println("Skipping row with bad number format: " + line);
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // REQ 1 (BASIC) — List all trips starting/ending at a given station
    // Complexity: O(n)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Returns all trips matching the station name and mode.
     *
     * @param stationName the station name to search for (case-insensitive)
     * @param mode        "start" — match start station only
     *                    "end"   — match end station only
     *                    "both"  — match either start or end station
     */
    @Override
    public Iterable<BixiTrip> getTripsByStation(String stationName, String mode) {
        String query = stationName.trim().toLowerCase();
        List<BixiTrip> result = new ArrayList<>();

        for (BixiTrip t : trips) {
            boolean matchStart = t.getStartStationName().toLowerCase().contains(query);
            boolean matchEnd   = t.getEndStationName().toLowerCase().contains(query);

            switch (mode.toLowerCase()) {
                case "start" -> { if (matchStart) result.add(t); }
                case "end"   -> { if (matchEnd)   result.add(t); }
                case "both"  -> { if (matchStart || matchEnd) result.add(t); }
                default -> System.err.println("Unknown mode: " + mode + ". Use start/end/both.");
            }
        }
        return result;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // REQ 2 (BASIC) — Find all trips made in a given month
    // Complexity: O(n log n)  — O(n) filter + O(k log k) sort
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Returns all trips whose start time falls in the given month,
     * sorted ascending by start time.
     *
     * @param month format "YYYY-MM", e.g. "2025-06"
     */
    @Override
    public Iterable<BixiTrip> getTripsByMonth(String month) {
        return trips.stream()
                .filter(t -> t.getStartYearMonth().equals(month.trim()))
                .sorted(Comparator.comparingLong(BixiTrip::getStartTimeMs))
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // REQ 3 (INTERMEDIATE) — List trips with duration > X minutes
    // Complexity: O(n log n)  — O(n) filter + O(k log k) sort
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Returns all trips whose duration exceeds the threshold,
     * sorted descending by duration (longest first).
     *
     * Duration = (ENDTIMEMS – STARTTIMEMS) / (1000 * 60)
     *
     * @param minDuration threshold in minutes (exclusive)
     */
    @Override
    public Iterable<BixiTrip> getTripsByDuration(float minDuration) {
        return trips.stream()
                .filter(t -> t.getDurationMinutes() > minDuration)
                .sorted(Comparator.comparingDouble(BixiTrip::getDurationMinutes).reversed())
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // REQ 4 (INTERMEDIATE) — Find trips within a start-time interval
    // Complexity: O(n log n)  — O(n) filter + O(k log k) sort
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Returns all trips whose start time falls within [startTime, finalTime],
     * sorted ascending by start time.
     *
     * @param startTime interval start in "YYYY-MM-DD HH:mm:ss" (Montreal time)
     * @param finalTime interval end   in "YYYY-MM-DD HH:mm:ss" (Montreal time)
     */
    @Override
    public Iterable<BixiTrip> getTripsByStartTime(String startTime, String finalTime) {
        long lo = parseIntervalMs(startTime.trim());
        long hi = parseIntervalMs(finalTime.trim());

        if (lo < 0 || hi < 0) {
            System.err.println("Invalid time format. Expected: yyyy-MM-dd HH:mm:ss");
            return Collections.emptyList();
        }

        return trips.stream()
                .filter(t -> t.getStartTimeMs() >= lo && t.getStartTimeMs() <= hi)
                .sorted(Comparator.comparingLong(BixiTrip::getStartTimeMs))
                .collect(Collectors.toList());
    }

    /**
     * Parses "yyyy-MM-dd HH:mm:ss" → epoch milliseconds in Montreal time.
     * Returns -1 on parse failure.
     */
    private long parseIntervalMs(String datetime) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(datetime, INTERVAL_FMT);
            return ldt.atZone(MONTREAL).toInstant().toEpochMilli();
        } catch (Exception e) {
            return -1;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // REQ 5 (INTERMEDIATE) — Top N arrondissements by departures
    // Complexity: O(n + a log a)  where a = number of distinct arrondissements
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Returns the top N arrondissements by number of trip departures.
     * Ties are broken alphabetically by arrondissement name.
     *
     * @param k how many arrondissements to return
     */
    @Override
    public Iterable<String> getTopArrondissements(int k) {
        // Count departures per arrondissement — O(n)
        Map<String, Integer> counts = new HashMap<>();
        for (BixiTrip t : trips) {
            counts.merge(t.getStartArrondissement(), 1, Integer::sum);
        }

        // Sort: count DESC, then name ASC — O(a log a)
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(k)
                .map(e -> e.getKey() + " — " + e.getValue() + " departures")
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // REQ 6 (ADVANCED) — Top K most-used starting stations in a period
    // Complexity: O(n + s log K)  where s = distinct stations in period
    //             Using a min-heap of size K beats full sort when K << s
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Returns the top K starting stations in the given time period,
     * sorted alphabetically by station name.
     *
     * @param k         number of top stations to return
     * @param startDate period start "YYYY-MM-DD HH:mm:ss"
     * @param endDate   period end   "YYYY-MM-DD HH:mm:ss"
     */
    @Override
    public Iterable<String> getTopStations(int k, String startDate, String endDate) {
        long lo = parseIntervalMs(startDate.trim());
        long hi = parseIntervalMs(endDate.trim());

        if (lo < 0 || hi < 0) {
            System.err.println("Invalid date format. Expected: yyyy-MM-dd HH:mm:ss");
            return Collections.emptyList();
        }

        // Count trips per starting station within the window — O(n)
        Map<String, Integer> counts = new HashMap<>();
        for (BixiTrip t : trips) {
            if (t.getStartTimeMs() >= lo && t.getStartTimeMs() <= hi) {
                counts.merge(t.getStartStationName(), 1, Integer::sum);
            }
        }

        // Min-heap of size K to find top-K — O(s log K)
        // Min-heap: smallest count at top; if new count > top, replace top
        PriorityQueue<Map.Entry<String, Integer>> minHeap =
                new PriorityQueue<>(k + 1, Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > k) {
                minHeap.poll();  // remove smallest
            }
        }

        // Collect and sort alphabetically by station name — O(K log K)
        return minHeap.stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + " — " + e.getValue() + " trips")
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // REQ 7 (ADVANCED) — Detect "rush hour" in a given month
    // Complexity: O(n)  — single pass + O(24) = O(1) to find max
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Finds the hour of day (0–23) with the highest AVERAGE number of
     * trips per day for the given month.
     *
     * Average = total trips in that hour / number of distinct days with trips.
     *
     * @param month month number 1–12
     * @return the rush hour (0–23)
     */
    @Override
    public int getRushHourOfMonth(int month) {
        long[]      hourTotals = new long[24];
        Set<Integer> days      = new HashSet<>();

        // Single pass — O(n)
        for (BixiTrip t : trips) {
            if (t.getStartMonthValue() == month) {
                hourTotals[t.getStartHour()]++;
                days.add(t.getStartDayOfMonth());
            }
        }

        if (days.isEmpty()) {
            System.out.println("No trips found for month " + month);
            return -1;
        }

        // Find the hour with max average — O(24) = O(1)
        int    rushHour  = 0;
        double maxAvg    = 0;
        int    numDays   = days.size();

        for (int h = 0; h < 24; h++) {
            double avg = (double) hourTotals[h] / numDays;
            if (avg > maxAvg) {
                maxAvg   = avg;
                rushHour = h;
            }
        }

        System.out.printf("Rush hour for month %d: %02d:00 — %.2f avg trips/day%n",
                month, rushHour, maxAvg);
        return rushHour;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // REQ 8 (ADVANCED) — Compare two months based on their statistics
    // Complexity: O(n + s log s)  — builds on REQ 2, 6, 7
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Compares two months side-by-side:
     *   - Total number of trips
     *   - K most-used START stations (with trip counts)
     *   - K most-used END stations   (with trip counts)
     *   - Rush hour + average trips/day during that hour
     *
     * @param month1 first month  (1–12)
     * @param month2 second month (1–12)
     * @param k      number of top stations to show
     */
    @Override
    public void compareMonths(int month1, int month2, int k) {
        MonthStats s1 = computeMonthStats(month1, k);
        MonthStats s2 = computeMonthStats(month2, k);

        String[] monthNames = { "", "January","February","March","April","May","June",
                "July","August","September","October","November","December" };

        System.out.println("\n" + "=".repeat(80));
        System.out.printf("%-38s | %-38s%n",
                "MONTH: " + monthNames[month1], "MONTH: " + monthNames[month2]);
        System.out.println("=".repeat(80));

        System.out.printf("%-38s | %-38s%n",
                "Total trips: " + s1.totalTrips,
                "Total trips: " + s2.totalTrips);

        System.out.println("-".repeat(80));
        System.out.printf("%-38s | %-38s%n",
                "Top " + k + " START stations:", "Top " + k + " START stations:");
        for (int i = 0; i < k; i++) {
            String left  = i < s1.topStartStations.size() ? s1.topStartStations.get(i) : "";
            String right = i < s2.topStartStations.size() ? s2.topStartStations.get(i) : "";
            System.out.printf("  %-36s |   %-36s%n", left, right);
        }

        System.out.println("-".repeat(80));
        System.out.printf("%-38s | %-38s%n",
                "Top " + k + " END stations:", "Top " + k + " END stations:");
        for (int i = 0; i < k; i++) {
            String left  = i < s1.topEndStations.size() ? s1.topEndStations.get(i) : "";
            String right = i < s2.topEndStations.size() ? s2.topEndStations.get(i) : "";
            System.out.printf("  %-36s |   %-36s%n", left, right);
        }

        System.out.println("-".repeat(80));
        System.out.printf("%-38s | %-38s%n",
                "Rush hour: " + s1.rushHour + ":00 (" + String.format("%.2f", s1.rushHourAvg) + " avg/day)",
                "Rush hour: " + s2.rushHour + ":00 (" + String.format("%.2f", s2.rushHourAvg) + " avg/day)");
        System.out.println("=".repeat(80));
    }

    /**
     * Computes all statistics for a given month.
     * This is the internal helper that powers REQ 8.
     */
    private MonthStats computeMonthStats(int month, int k) {
        long[]       hourTotals = new long[24];
        Set<Integer> days       = new HashSet<>();

        Map<String, Integer> startCounts = new HashMap<>();
        Map<String, Integer> endCounts   = new HashMap<>();
        int totalTrips = 0;

        // Single pass over all trips — O(n)
        for (BixiTrip t : trips) {
            if (t.getStartMonthValue() == month) {
                totalTrips++;
                startCounts.merge(t.getStartStationName(), 1, Integer::sum);
                endCounts.merge(t.getEndStationName(), 1, Integer::sum);
                hourTotals[t.getStartHour()]++;
                days.add(t.getStartDayOfMonth());
            }
        }

        // Top-K start stations — O(s log K)
        List<String> topStart = topKFromMap(startCounts, k);

        // Top-K end stations — O(s log K)
        List<String> topEnd = topKFromMap(endCounts, k);

        // Rush hour
        int rushHour = 0;
        double maxAvg = 0;
        int numDays = Math.max(days.size(), 1);
        for (int h = 0; h < 24; h++) {
            double avg = (double) hourTotals[h] / numDays;
            if (avg > maxAvg) { maxAvg = avg; rushHour = h; }
        }

        return new MonthStats(totalTrips, topStart, topEnd, rushHour, maxAvg);
    }

    /**
     * Returns top-K entries from a station-count map,
     * sorted alphabetically by station name.
     */
    private List<String> topKFromMap(Map<String, Integer> counts, int k) {
        PriorityQueue<Map.Entry<String, Integer>> minHeap =
                new PriorityQueue<>(k + 1, Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            minHeap.offer(e);
            if (minHeap.size() > k) minHeap.poll();
        }

        return minHeap.stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + " (" + e.getValue() + " trips)")
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // INNER CLASS — MonthStats (data carrier for REQ 8)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Holds all statistics for a single month.
     * Used internally by compareMonths() (REQ 8).
     */
    private static class MonthStats {
        final int          totalTrips;
        final List<String> topStartStations;
        final List<String> topEndStations;
        final int          rushHour;
        final double       rushHourAvg;

        MonthStats(int totalTrips,
                   List<String> topStartStations,
                   List<String> topEndStations,
                   int rushHour,
                   double rushHourAvg) {
            this.totalTrips       = totalTrips;
            this.topStartStations = topStartStations;
            this.topEndStations   = topEndStations;
            this.rushHour         = rushHour;
            this.rushHourAvg      = rushHourAvg;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UTILITY — access loaded data
    // ═══════════════════════════════════════════════════════════════════════

    /** Returns all loaded trips (useful for testing). */
    public List<BixiTrip> getAllTrips() {
        return Collections.unmodifiableList(trips);
    }

    /** Returns all unique station names loaded. */
    public Set<String> getAllStations() {
        return Collections.unmodifiableSet(stations);
    }

    /** Returns total number of trips loaded. */
    public int getTripCount() {
        return trips.size();
    }

    /** Returns total number of unique stations loaded. */
    public int getStationCount() {
        return stations.size();
    }
}
