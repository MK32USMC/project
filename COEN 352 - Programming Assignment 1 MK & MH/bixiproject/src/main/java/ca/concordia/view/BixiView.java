package ca.concordia.view;

import ca.concordia.controller.BixiController;
import ca.concordia.controller.IBixiController;
import ca.concordia.model.BixiTrip;

import java.util.Scanner;

public class BixiView {

    private final IBixiController controller;
    private final Scanner         scanner;

    public BixiView() {
        this.controller = new BixiController();
        this.scanner    = new Scanner(System.in);
    }

    // Entry point
    public void start() {
        System.out.println("+--------------------------+");
        System.out.println("|     BIXI Data Analyzer   |");
        System.out.println("+--------------------------+");

        System.out.print("\nEnter path to BIXI CSV file: ");
        String filePath = scanner.nextLine().trim();
        controller.loadFile(filePath);

        boolean running = true;
        while (running) {
            showMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1"  -> handleReq1();
                case "2"  -> handleReq2();
                case "3"  -> handleReq3();
                case "4"  -> handleReq4();
                case "5"  -> handleReq5();
                case "6"  -> handleReq6();
                case "7"  -> handleReq7();
                case "8"  -> handleReq8();
                case "0"  -> running = false;
                default   -> System.out.println("Invalid option. Try again.");
            }
        }
        System.out.println("Goodbye!");
        scanner.close();
    }

    private void showMenu() {
        System.out.println("\n--------------------------------------");
        System.out.println("  [1]  REQ 1 - Trips by station");
        System.out.println("  [2]  REQ 2 - Trips by month");
        System.out.println("  [3]  REQ 3 - Trips by duration");
        System.out.println("  [4]  REQ 4 - Trips in time interval");
        System.out.println("  [5]  REQ 5 - Top N arrondissements");
        System.out.println("  [6]  REQ 6 - Top K stations in period");
        System.out.println("  [7]  REQ 7 - Rush hour by month");
        System.out.println("  [8]  REQ 8 - Compare two months");
        System.out.println("  [0]  Exit");
        System.out.println("--------------------------------------");
        System.out.print("Choice: ");
    }

    // REQ 1
    private void handleReq1() {
        System.out.print("Station name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Mode (start / end / both): ");
        String mode = scanner.nextLine().trim();

        Iterable<BixiTrip> results = controller.getTripsByStation(name, mode);
        int count = printTrips(results);
        System.out.println("-> " + count + " trip(s) found.");
    }

    // REQ 2
    private void handleReq2() {
        System.out.print("Month (YYYY-MM): ");
        String month = scanner.nextLine().trim();

        Iterable<BixiTrip> results = controller.getTripsByMonth(month);
        int count = printTrips(results);
        System.out.println("-> " + count + " trip(s) found.");
    }

    // REQ 3
    private void handleReq3() {
        System.out.print("Minimum duration (minutes): ");
        String input = scanner.nextLine().trim();
        try {
            float minDur = Float.parseFloat(input);
            Iterable<BixiTrip> results = controller.getTripsByDuration(minDur);
            int count = printTrips(results);
            System.out.println("-> " + count + " trip(s) longer than " + minDur + " min.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number: " + input);
        }
    }

    // REQ 4
    private void handleReq4() {
        System.out.print("Start datetime (yyyy-MM-dd HH:mm:ss): ");
        String start = scanner.nextLine().trim();
        System.out.print("End   datetime (yyyy-MM-dd HH:mm:ss): ");
        String end   = scanner.nextLine().trim();

        Iterable<BixiTrip> results = controller.getTripsByStartTime(start, end);
        int count = printTrips(results);
        System.out.println("-> " + count + " trip(s) in interval.");
    }

    // REQ 5
    private void handleReq5() {
        System.out.print("Number of top arrondissements (N): ");
        String input = scanner.nextLine().trim();
        try {
            int n = Integer.parseInt(input);
            Iterable<String> results = controller.getTopArrondissements(n);
            System.out.println("\nTop " + n + " arrondissements:");
            int rank = 1;
            for (String a : results) {
                System.out.printf("  %2d. %s%n", rank++, a);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number: " + input);
        }
    }

    // REQ 6
    private void handleReq6() {
        System.out.print("K (number of top stations): ");
        String kInput = scanner.nextLine().trim();
        System.out.print("Period start (yyyy-MM-dd HH:mm:ss): ");
        String start  = scanner.nextLine().trim();
        System.out.print("Period end   (yyyy-MM-dd HH:mm:ss): ");
        String end    = scanner.nextLine().trim();

        try {
            int k = Integer.parseInt(kInput);
            Iterable<String> results = controller.getTopStations(k, start, end);
            System.out.println("\nTop " + k + " starting stations (alphabetical):");
            int rank = 1;
            for (String s : results) {
                System.out.printf("  %2d. %s%n", rank++, s);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number: " + kInput);
        }
    }

    // REQ 7
    private void handleReq7() {
        System.out.print("Month number (1-12): ");
        String input = scanner.nextLine().trim();
        try {
            int month = Integer.parseInt(input);
            if (month < 1 || month > 12) {
                System.out.println("Month must be between 1 and 12.");
                return;
            }
            String result = controller.getRushHourOfMonth(month);
            System.out.println("\n" + result);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number: " + input);
        }
    }

    // REQ 8
    private void handleReq8() {
        System.out.print("First month  (1-12): ");
        String m1Input = scanner.nextLine().trim();
        System.out.print("Second month (1-12): ");
        String m2Input = scanner.nextLine().trim();
        System.out.print("K (top stations): ");
        String kInput  = scanner.nextLine().trim();

        try {
            int m1 = Integer.parseInt(m1Input);
            int m2 = Integer.parseInt(m2Input);
            int k  = Integer.parseInt(kInput);

            if (m1 < 1 || m1 > 12 || m2 < 1 || m2 > 12) {
                System.out.println("Month must be between 1 and 12.");
                return;
            }

            controller.compareMonths(m1, m2, k);

        } catch (NumberFormatException e) {
            System.out.println("Invalid input - please enter integers only.");
        }
    }

    /**
     * Prints all trips in the iterable and returns the count.
     */
    private int printTrips(Iterable<BixiTrip> trips) {
        int count = 0;
        System.out.println();
        for (BixiTrip t : trips) {
            System.out.println("  " + t);
            count++;
        }
        return count;
    }
}
