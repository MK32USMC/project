package ca.concordia.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class BixiTrip {

    private String startStationName;
    private String startArrondissement;
    private String endStationName;

    private long startTimeMs;
    private long endTimeMs;

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId MONTREAL =
            ZoneId.of("America/Montreal");

    public BixiTrip(String startStationName,
                    String startArrondissement,
                    String endStationName,
                    long startTimeMs,
                    long endTimeMs) {

        this.startStationName    = startStationName;
        this.startArrondissement = startArrondissement;
        this.endStationName      = endStationName;
        this.startTimeMs         = startTimeMs;
        this.endTimeMs           = endTimeMs;
    }

    /** REQ 3 - Gets duration in minutes**/
    public double getDurationMinutes() {
        return (endTimeMs - startTimeMs) / (1000.0 * 60);
    }

    /** Used for month and hour filtering **/
    public LocalDateTime getStartLocalDateTime() {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(startTimeMs), MONTREAL);
    }

    public LocalDateTime getEndLocalDateTime() {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(endTimeMs), MONTREAL);
    }

    public String getStartTimeFormatted() {
        return getStartLocalDateTime().format(DISPLAY_FMT);
    }

    public String getEndTimeFormatted() {
        return getEndLocalDateTime().format(DISPLAY_FMT);
    }

    /** REQ 2 - used for month filtering **/
    public String getStartYearMonth() {
        LocalDateTime ldt = getStartLocalDateTime();
        return String.format("%04d-%02d",
                ldt.getYear(), ldt.getMonthValue());
    }

    /** REQ 7 - used in rush hour detection **/
    public int getStartHour() {
        return getStartLocalDateTime().getHour();
    }

    public int getStartDayOfMonth() {
        return getStartLocalDateTime().getDayOfMonth();
    }

   /** Used in REQ 7 and 8 **/
    public int getStartMonthValue() {
        return getStartLocalDateTime().getMonthValue();
    }

    public String getStartStationName()    { return startStationName; }
    public String getStartArrondissement() { return startArrondissement; }
    public String getEndStationName()      { return endStationName; }
    public long   getStartTimeMs()         { return startTimeMs; }
    public long   getEndTimeMs()           { return endTimeMs; }

    /** cleans up string output **/

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
