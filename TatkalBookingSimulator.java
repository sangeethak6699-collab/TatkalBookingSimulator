/**
 * IRCTC Tatkal Ticket Booking Simulator
 * 
 * Features:
 * - Multithreading & Synchronization (Handling race conditions for concurrent bookings)
 * - Seat categories: Confirmed, RAC (Reservation Against Cancellation), Waiting List (WL)
 * - Queue-based Waiting List management
 * - Custom Exceptions (SeatNotAvailableException)
 * - PNR Generation and Booking Reports
 * - Interactive Graphical UI (Swing GUI included)
 */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

// Custom Exception for seat availability issues
class SeatNotAvailableException extends Exception {
    public SeatNotAvailableException(String message) {
        super(message);
    }
}

// Seat Class
class Seat {
    private int seatNumber;
    private String category; // "Confirmed", "RAC", "WL"
    private boolean isBooked;

    public Seat(int seatNumber, String category) {
        this.seatNumber = seatNumber;
        this.category = category;
        this.isBooked = false;
    }

    public int getSeatNumber() { return seatNumber; }
    public String getCategory() { return category; }
    public boolean isBooked() { return isBooked; }
    public void setBooked(boolean booked) { isBooked = booked; }
}

// Ticket Class
class Ticket {
    private String pnr;
    private String passengerName;
    private String trainName;
    private int seatNumber;
    private String status; // CONFIRMED, RAC, WAITING_LIST

    public Ticket(String pnr, String passengerName, String trainName, int seatNumber, String status) {
        this.pnr = pnr;
        this.passengerName = passengerName;
        this.trainName = trainName;
        this.seatNumber = seatNumber;
        this.status = status;
    }

    public String getPnr() { return pnr; }
    public String getPassengerName() { return passengerName; }
    public String getTrainName() { return trainName; }
    public int getSeatNumber() { return seatNumber; }
    public String getStatus() { return status; }
}

// Booking Engine with Synchronization
class BookingEngine {
    private String trainName;
    private int totalConfirmedSeats;
    private int totalRACSeats;
    private int totalWLSeats;

    private int bookedConfirmed = 0;
    private int bookedRAC = 0;
    private int bookedWL = 0;

    private Queue<Ticket> waitingListQueue = new ConcurrentLinkedQueue<>();
    private List<Ticket> allBookings = new ArrayList<>();
    private static final AtomicInteger pnrCounter = new AtomicInteger(10000000);

    public BookingEngine(String trainName, int totalConfirmedSeats, int totalRACSeats, int totalWLSeats) {
        this.trainName = trainName;
        this.totalConfirmedSeats = totalConfirmedSeats;
        this.totalRACSeats = totalRACSeats;
        this.totalWLSeats = totalWLSeats;
    }

    // Synchronized method to prevent race conditions during Tatkal rush
    public synchronized Ticket bookTicket(String passengerName) throws SeatNotAvailableException {
        String pnr = "PNR" + pnrCounter.incrementAndGet();
        Ticket ticket;

        if (bookedConfirmed < totalConfirmedSeats) {
            bookedConfirmed++;
            ticket = new Ticket(pnr, passengerName, trainName, bookedConfirmed, "CONFIRMED");
        } else if (bookedRAC < totalRACSeats) {
            bookedRAC++;
            ticket = new Ticket(pnr, passengerName, trainName, totalConfirmedSeats + bookedRAC, "RAC");
        } else if (bookedWL < totalWLSeats) {
            bookedWL++;
            ticket = new Ticket(pnr, passengerName, trainName, -1, "WAITING_LIST (WL-" + bookedWL + ")");
            waitingListQueue.add(ticket);
        } else {
            throw new SeatNotAvailableException("No seats or waiting list available for passenger: " + passengerName);
        }

        allBookings.add(ticket);
        return ticket;
    }

    public synchronized List<Ticket> getAllBookings() {
        return new ArrayList<>(allBookings);
    }
}

// GUI Interface for Tatkal Simulator
public class TatkalBookingSimulator extends JFrame {
    private BookingEngine bookingEngine;
    private DefaultTableModel tableModel;
    private JTextArea logArea;
    private JTextField nameField;

    public TatkalBookingSimulator() {
        bookingEngine = new BookingEngine("12626 - KERALA EXPRESS (TATKAL)", 5, 2, 2);

        setTitle("IRCTC Tatkal Ticket Booking Simulator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        JLabel titleLabel = new JLabel("IRCTC Tatkal Ticket Booking Simulator", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 51, 102));
        JLabel infoLabel = new JLabel("Quota: 5 Confirmed | 2 RAC | 2 Waiting List", JLabel.CENTER);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        headerPanel.add(titleLabel);
        headerPanel.add(infoLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Center Table
        String[] columnNames = {"PNR", "Passenger Name", "Train", "Seat No", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Controls & Simulation Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Passenger Name:"));
        nameField = new JTextField(15);
        inputPanel.add(nameField);

        JButton bookBtn = new JButton("Book Single Ticket");
        JButton simulateRushBtn = new JButton("Simulate Tatkal Rush (10 Concurrent Users)");
        inputPanel.add(bookBtn);
        inputPanel.add(simulateRushBtn);

        bottomPanel.add(inputPanel, BorderLayout.NORTH);

        logArea = new JTextArea(6, 50);
        logArea.setEditable(false);
        bottomPanel.add(new JScrollPane(logArea), BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // Action Listeners
        bookBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                try {
                    Ticket t = bookingEngine.bookTicket(name);
                    tableModel.addRow(new Object[]{t.getPnr(), t.getPassengerName(), t.getTrainName(), t.getSeatNumber() > 0 ? t.getSeatNumber() : "N/A", t.getStatus()});
                    logArea.append("[SUCCESS] Booked for " + name + " -> Status: " + t.getStatus() + "\n");
                    nameField.setText("");
                } catch (SeatNotAvailableException ex) {
                    logArea.append("[FAILED] " + ex.getMessage() + "\n");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a passenger name.");
            }
        });

        simulateRushBtn.addActionListener(e -> simulateTatkalRush());
    }

    private void simulateTatkalRush() {
        logArea.append("--- STARTING TATKAL RUSH SIMULATION (Concurrent Threads) ---\n");
        String[] sampleNames = {"Alice", "Bob", "Charlie", "David", "Emma", "Frank", "Grace", "Hank", "Ivy", "Jack"};

        for (String name : sampleNames) {
            new Thread(() -> {
                try {
                    Thread.sleep((long) (Math.random() * 500));
                    Ticket t = bookingEngine.bookTicket(name);
                    SwingUtilities.invokeLater(() -> {
                        tableModel.addRow(new Object[]{t.getPnr(), t.getPassengerName(), t.getTrainName(), t.getSeatNumber() > 0 ? t.getSeatNumber() : "N/A", t.getStatus()});
                        logArea.append("[RUSH SUCCESS] " + name + " -> Status: " + t.getStatus() + "\n");
                    });
                } catch (SeatNotAvailableException ex) {
                    SwingUtilities.invokeLater(() -> logArea.append("[RUSH FAILED] " + ex.getMessage() + "\n"));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TatkalBookingSimulator gui = new TatkalBookingSimulator();
            gui.setVisible(true);
        });
    }
}
