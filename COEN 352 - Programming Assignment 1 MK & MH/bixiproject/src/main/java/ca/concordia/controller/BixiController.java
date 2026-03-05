package ca.concordia.controller;

import ca.concordia.model.BixiTrip;
import ca.concordia.util.MergeSort;
import ca.concordia.util.SimpleList;
import ca.concordia.util.SimpleMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class BixiController implements IBixiController {

    private SimpleList<BixiTrip> trips          = new SimpleList<>();
    private int                  uniqueStations = 0;

    private static final DateTimeFormatter INTERVAL_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId MONTREAL = ZoneId.of("America/Montreal");

    @Override
    public void loadFile(String filePath) {
        trips = new SimpleList<>();
        System.out.println("Loading file: " + filePath);

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String  line;
            boolean isHeader  = true;
            SimpleMap stationSeen = new SimpleMap();

            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                if (line.isBlank()) continue;
                BixiTrip trip = parseLine(line);
                if (trip != null) {
                    trips.add(trip);
                    stationSeen.increment(trip.getStartStationName());
                    stationSeen.increment(trip.getEndStationName());
                }
            }
            uniqueStations = stationSeen.size();
            System.out.println("Total trips loaded:    " + trips.size());
            System.out.println("Unique stations found: " + uniqueStations);

        } catch (IOException e) {
            System.err.println("Error loading file: " + e.getMessage());
        }
    }

    private BixiTrip parseLine(String line) {
        try {
            String[] f = splitCsvLine(line, 10);
            if (f == null) return null;
            return new BixiTrip(
                    f[0].trim(),
                    f[1].trim(),
                    f[4].trim(),
                    Long.parseLong(f[8].trim()),
                    Long.parseLong(f[9].trim())
            );
        } catch (NumberFormatException e) { return null; }
    }

    private String[] splitCsvLine(String line, int expected) {
        String[] out = new String[expected];
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        int field = 0;
        int len = line.length();

        for (int i = 0; i < len; i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < len && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                if (field >= expected) return null;
                out[field++] = sb.toString();
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        if (field >= expected) return null;
        out[field++] = sb.toString();
        return field == expected ? out : null;
    }

    /** REQ 1 **/

    @Override
    public Iterable<BixiTrip> getTripsByStation(String stationName, String mode) {
        String query = stationName.trim().toLowerCase();
        SimpleList<BixiTrip> result = new SimpleList<>();
        for (BixiTrip t : trips) {
            boolean ms = t.getStartStationName().toLowerCase().contains(query);
            boolean me = t.getEndStationName().toLowerCase().contains(query);
            switch (mode.toLowerCase()) {
                case "start": if (ms)      result.add(t); break;
                case "end":   if (me)      result.add(t); break;
                case "both":  if (ms || me) result.add(t); break;
                default: System.err.println("Invalid mode: " + mode);
            }
        }
        return result;
    }

    /** REQ 2 **/

    @Override
    public Iterable<BixiTrip> getTripsByMonth(String month) {
        SimpleList<BixiTrip> result = new SimpleList<>();
        String m = month.trim();
        for (BixiTrip t : trips) {
            if (t.getStartYearMonth().equals(m)) result.add(t);
        }
        MergeSort.sortByStartTime(result);
        return result;
    }

    /** REQ 3 **/

    @Override
    public Iterable<BixiTrip> getTripsByDuration(float minDuration) {
        SimpleList<BixiTrip> result = new SimpleList<>();
        for (BixiTrip t : trips) {
            if (t.getDurationMinutes() > minDuration) result.add(t);
        }
        MergeSort.sortByDurationDesc(result);
        return result;
    }

    /** REQ 4 **/

    @Override
    public Iterable<BixiTrip> getTripsByStartTime(String startTime, String finalTime) {
        long lo = parseIntervalMs(startTime.trim());
        long hi = parseIntervalMs(finalTime.trim());
        if (lo < 0 || hi < 0) {
            System.err.println("Invalid, expected: yyyy-MM-dd HH:mm:ss");
            return new SimpleList<>();
        }
        if (lo > hi) {
            System.err.println("Invalid, start must be <= end");
            return new SimpleList<>();
        }
        SimpleList<BixiTrip> result = new SimpleList<>();
        for (BixiTrip t : trips) {
            if (t.getStartTimeMs() >= lo && t.getStartTimeMs() <= hi) result.add(t);
        }
        MergeSort.sortByStartTime(result);
        return result;
    }

    private long parseIntervalMs(String dt) {
        try {
            return LocalDateTime.parse(dt, INTERVAL_FMT)
                    .atZone(MONTREAL).toInstant().toEpochMilli();
        } catch (Exception e) { return -1; }
    }

    /** REQ 5 **/

    @Override
    public Iterable<String> getTopArrondissements(int k) {
        if (k <= 0) return new SimpleList<>();
        SimpleMap counts = new SimpleMap();
        for (BixiTrip t : trips) counts.increment(t.getStartArrondissement());

        SimpleList<String> keyList = counts.keys();
        int a = keyList.size();
        String[] keys   = new String[a];
        int[]    values = new int[a];
        for (int i = 0; i < a; i++) {
            keys[i]   = keyList.get(i);
            values[i] = counts.get(keys[i]);
        }

        MergeSort.sortByCountDesc(keys, values, a);

        int limit = Math.min(k, a);
        SimpleList<String> result = new SimpleList<>(limit);
        for (int i = 0; i < limit; i++) {
            result.add(keys[i] + " - " + values[i] + " departures");
        }
        return result;
    }

    /** REQ 6 **/

    @Override
    public Iterable<String> getTopStations(int k, String startDate, String endDate) {
        if (k <= 0) return new SimpleList<>();
        long lo = parseIntervalMs(startDate.trim());
        long hi = parseIntervalMs(endDate.trim());
        if (lo < 0 || hi < 0) {
            System.err.println("Invalid, expected: yyyy-MM-dd HH:mm:ss");
            return new SimpleList<>();
        }
        if (lo > hi) {
            System.err.println("Invalid, start must be <= end");
            return new SimpleList<>();
        }

        SimpleMap counts = new SimpleMap();
        for (BixiTrip t : trips) {
            if (t.getStartTimeMs() >= lo && t.getStartTimeMs() <= hi) {
                String name = t.getStartStationName();
                counts.increment(name);
            }
        }

        SimpleList<String> allKeys = counts.keys();
        int s = allKeys.size();
        String[] keys = new String[s];
        int[]    vals = new int[s];
        for (int i = 0; i < s; i++) {
            keys[i] = allKeys.get(i);
            vals[i] = counts.get(keys[i]);
        }
        MergeSort.sortByCountDesc(keys, vals, s);

        int limit = Math.min(k, s);
        String[] topKeys = new String[limit];
        int[]    topVals = new int[limit];
        for (int i = 0; i < limit; i++) {
            topKeys[i] = keys[i];
            topVals[i] = vals[i];
        }
        MergeSort.sortStringsAlphaWithCounts(topKeys, topVals, limit);

        SimpleList<String> result = new SimpleList<>(limit);
        for (int i = 0; i < limit; i++) {
            result.add(topKeys[i] + " - " + topVals[i] + " trips");
        }
        return result;
    }

    /** REQ 7 **/

    @Override
    public String getRushHourOfMonth(int month) {
        long[]    hourTotals  = new long[24];
        boolean[] daysSeen    = new boolean[32];
        int       distinctDays = 0;

        for (BixiTrip t : trips) {
            if (t.getStartMonthValue() == month) {
                hourTotals[t.getStartHour()]++;
                int day = t.getStartDayOfMonth();
                if (!daysSeen[day]) { daysSeen[day] = true; distinctDays++; }
            }
        }

        if (distinctDays == 0) return "Rush hour: N/A (no data)";

        int rushHour = 0; double maxAvg = 0;
        for (int h = 0; h < 24; h++) {
            double avg = (double) hourTotals[h] / distinctDays;
            if (avg > maxAvg) { maxAvg = avg; rushHour = h; }
        }
        return String.format("Rush hour: %02d:00 - %02d:00 | Total trips: %d | Avg per day: %.2f",
                rushHour, rushHour + 1, hourTotals[rushHour], maxAvg);
    }

    /** REQ 8 **/

    @Override
    public void compareMonths(int month1, int month2, int k) {
        MonthStats s1 = computeMonthStats(month1, k);
        MonthStats s2 = computeMonthStats(month2, k);
        String[] mn = {"","January","February","March","April","May","June",
                "July","August","September","October","November","December"};

        System.out.println("\n" + "=".repeat(80));
        System.out.printf("%-38s | %-38s%n", "MONTH: " + mn[month1], "MONTH: " + mn[month2]);
        System.out.println("=".repeat(80));
        System.out.printf("%-38s | %-38s%n", "Total trips: "+s1.totalTrips, "Total trips: "+s2.totalTrips);

        System.out.println("-".repeat(80));
        System.out.printf("%-38s | %-38s%n", "Top "+k+" START stations:", "Top "+k+" START stations:");
        for (int i = 0; i < k; i++) {
            String l = i < s1.topStart.size() ? s1.topStart.get(i) : "";
            String r = i < s2.topStart.size() ? s2.topStart.get(i) : "";
            System.out.printf("  %-36s |   %-36s%n", l, r);
        }

        System.out.println("-".repeat(80));
        System.out.printf("%-38s | %-38s%n", "Top "+k+" END stations:", "Top "+k+" END stations:");
        for (int i = 0; i < k; i++) {
            String l = i < s1.topEnd.size() ? s1.topEnd.get(i) : "";
            String r = i < s2.topEnd.size() ? s2.topEnd.get(i) : "";
            System.out.printf("  %-36s |   %-36s%n", l, r);
        }

        System.out.println("-".repeat(80));
        System.out.printf("%-38s | %-38s%n",
                "Rush hour: "+s1.rushHour+":00 ("+String.format("%.2f",s1.rushAvg)+" avg/day)",
                "Rush hour: "+s2.rushHour+":00 ("+String.format("%.2f",s2.rushAvg)+" avg/day)");
        System.out.println("=".repeat(80));
    }

    private MonthStats computeMonthStats(int month, int k) {
        SimpleMap startCounts = new SimpleMap();
        SimpleMap endCounts   = new SimpleMap();
        long[]    hourTotals  = new long[24];
        boolean[] daysSeen    = new boolean[32];
        int distinctDays = 0, totalTrips = 0;

        for (BixiTrip t : trips) {
            if (t.getStartMonthValue() == month) {
                totalTrips++;
                startCounts.increment(t.getStartStationName());
                endCounts.increment(t.getEndStationName());
                hourTotals[t.getStartHour()]++;
                int day = t.getStartDayOfMonth();
                if (!daysSeen[day]) { daysSeen[day] = true; distinctDays++; }
            }
        }

        SimpleList<String> topStart = topKStations(startCounts, k);
        SimpleList<String> topEnd   = topKStations(endCounts, k);

        int rushHour = 0; double maxAvg = 0;
        int nd = Math.max(distinctDays, 1);
        for (int h = 0; h < 24; h++) {
            double avg = (double) hourTotals[h] / nd;
            if (avg > maxAvg) { maxAvg = avg; rushHour = h; }
        }
        return new MonthStats(totalTrips, topStart, topEnd, rushHour, maxAvg);
    }

    private SimpleList<String> topKStations(SimpleMap counts, int k) {
        if (k <= 0) return new SimpleList<>();
        SimpleList<String> allKeys = counts.keys();
        int s = allKeys.size();
        String[] keys = new String[s];
        int[]    vals = new int[s];
        for (int i = 0; i < s; i++) {
            keys[i] = allKeys.get(i);
            vals[i] = counts.get(keys[i]);
        }
        MergeSort.sortByCountDesc(keys, vals, s);

        int limit = Math.min(k, s);
        String[] topKeys = new String[limit];
        int[]    topVals = new int[limit];
        for (int i = 0; i < limit; i++) {
            topKeys[i] = keys[i];
            topVals[i] = vals[i];
        }
        MergeSort.sortStringsAlphaWithCounts(topKeys, topVals, limit);

        SimpleList<String> result = new SimpleList<>(limit);
        for (int i = 0; i < limit; i++) result.add(topKeys[i] + " (" + topVals[i] + " trips)");
        return result;
    }

    private static class MonthStats {
        int totalTrips; SimpleList<String> topStart, topEnd; int rushHour; double rushAvg;
        MonthStats(int t, SimpleList<String> ts, SimpleList<String> te, int rh, double ra) {
            totalTrips=t; topStart=ts; topEnd=te; rushHour=rh; rushAvg=ra;
        }
    }

    public SimpleList<BixiTrip> getAllTrips() { return trips; }
    public int getTripCount()                 { return trips.size(); }
    public int getStationCount()              { return uniqueStations; }
}
