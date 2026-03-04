package ca.concordia.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single BIXI bike trip record loaded from the CSV data file.
 * All fields map directly to CSV columns. Time values are stored as Unix
 * timestamps in milliseconds (as provided by BIXI open data).
 */
public class BixiTrip {

    // ── Fields (match CSV column order) ────────────────────────────────────
    private String startStationName;
    private String startArrondissement;
    private double startLatitude;
    private double startLongitude;

    private String endStationName;
    private String endArrondissement;
    private double endLatitude;
    private double endLongitude;

    private long startTimeMs;
    private long endTimeMs;

    // ── Formatter for human-readable output ────────────────────────────────
    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId MONTREAL =
            ZoneId.of("America/Montreal");

    // ── Constructor ─────────────────────────────────────────────────────────

    /**
     * Full constructor — use this when parsing a CSV row.
     */
    public BixiTrip(String startStationName,
                    String startArrondissement,
                    double startLatitude,
                    double startLongitude,
                    String endStationName,
                    String endArrondissement,
                    double endLatitude,
                    double endLongitude,
                    long startTimeMs,
                    long endTimeMs) {

        this.startStationName    = startStationName;
        this.startArrondissement = startArrondissement;
        this.startLatitude       = startLatitude;
        this.startLongitude      = startLongitude;
        this.endStationName      = endStationName;
        this.endArrondissement   = endArrondissement;
        this.endLatitude         = endLatitude;
        this.endLongitude        = endLongitude;
        this.startTimeMs         = startTimeMs;
        this.endTimeMs           = endTimeMs;
    }

    // ── Computed helpers ────────────────────────────────────────────────────

    /**
     * REQ 3: Duration in minutes.
     * Formula: (ENDTIMEMS – STARTTIMEMS) / (1000 * 60)
     */
    public double getDurationMinutes() {
        return (endTimeMs - startTimeMs) / (1000.0 * 60);
    }

    /**
     * Returns the start time as a LocalDateTime in Montreal time.
     * Used for month/hour filtering (REQ 2, 4, 7).
     */
    public LocalDateTime getStartLocalDateTime() {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(startTimeMs), MONTREAL);
    }

    /**
     * Returns the end time as a LocalDateTime in Montreal time.
     */
    public LocalDateTime getEndLocalDateTime() {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(endTimeMs), MONTREAL);
    }

    /**
     * Returns start time formatted as "yyyy-MM-dd HH:mm:ss" for display.
     */
    public String getStartTimeFormatted() {
        return getStartLocalDateTime().format(DISPLAY_FMT);
    }

    /**
     * Returns end time formatted as "yyyy-MM-dd HH:mm:ss" for display.
     */
    public String getEndTimeFormatted() {
        return getEndLocalDateTime().format(DISPLAY_FMT);
    }

    /**
     * Returns "YYYY-MM" string from the start timestamp.
     * Used for REQ 2 month filtering.
     */
    public String getStartYearMonth() {
        LocalDateTime ldt = getStartLocalDateTime();
        return String.format("%04d-%02d",
                ldt.getYear(), ldt.getMonthValue());
    }

    /**
     * Returns the hour of day (0–23) from the start timestamp.
     * Used for REQ 7 rush-hour detection.
     */
    public int getStartHour() {
        return getStartLocalDateTime().getHour();
    }

    /**
     * Returns the day-of-month (1–31) from the start timestamp.
     * Used for REQ 7 average-per-day calculation.
     */
    public int getStartDayOfMonth() {
        return getStartLocalDateTime().getDayOfMonth();
    }

    /**
     * Returns the month number (1–12) from the start timestamp.
     * Used for REQ 7 and REQ 8.
     */
    public int getStartMonthValue() {
        return getStartLocalDateTime().getMonthValue();
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    public String getStartStationName()    { return startStationName; }
    public String getStartArrondissement() { return startArrondissement; }
    public double getStartLatitude()       { return startLatitude; }
    public double getStartLongitude()      { return startLongitude; }
    public String getEndStationName()      { return endStationName; }
    public String getEndArrondissement()   { return endArrondissement; }
    public double getEndLatitude()         { return endLatitude; }
    public double getEndLongitude()        { return endLongitude; }
    public long   getStartTimeMs()         { return startTimeMs; }
    public long   getEndTimeMs()           { return endTimeMs; }

    // ── toString ────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
                "%-45s -> %-45s | Start: %s | End: %s | Duration: %.1f min",
                startStationName,
                endStationName,
                getStartTimeFormatted(),
                getEndTimeFormatted(),
                getDurationMinutes()
        );
    }
}
