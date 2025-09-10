package com.jts.hms;

import java.sql.*;
import java.util.*;

public class Bill {
    private Connection conn;
    private Scanner sc = new Scanner(System.in);

    // Static test catalog
    private static final Map<String, Double> TEST_CATALOG = new HashMap<>();
    static {
        TEST_CATALOG.put("Blood Test", 500.0);
        TEST_CATALOG.put("X-Ray", 800.0);
        TEST_CATALOG.put("MRI", 2500.0);
    }

    public Bill(Connection conn) {
        this.conn = conn;
    }

    // Add a new bill
    public void addBill(boolean isDoctor) throws SQLException {
        int patientId;
        if (isDoctor) {
            // Doctor enters patient ID manually
            patientId = InputValidator.getInt(sc, "Enter Patient ID: ", 1, Integer.MAX_VALUE);

            // ✅ validate patient
            if (!patientExists(patientId)) {
                System.out.println("Invalid Patient ID. Patient does not exist.");
                return;
            }
        } else {
            // Patient: use logged-in patient ID
            patientId = HospitalLoginSystem.getLoggedInPatientId();
            if (patientId <= 0 || !patientExists(patientId)) {
                System.out.println("No valid logged-in patient found.");
                return;
            }
        }

        int doctorId = InputValidator.getInt(sc, "Enter Doctor ID: ", 1, Integer.MAX_VALUE);

        // ✅ validate doctor
        if (!doctorExists(doctorId)) {
            System.out.println("Invalid Doctor ID. Doctor does not exist.");
            return;
        }

        double consultationFee = getConsultationFee(doctorId);
        if (consultationFee == -1) {
            System.out.println("Doctor has no consultation fee set.");
            return;
        }

        System.out.println("Consultation Fee: " + consultationFee);

        List<String> selectedTests = new ArrayList<>();
        double testTotal = 0.0;

        System.out.println("\nSelect tests performed (type 'yes' to include):");
        for (Map.Entry<String, Double> entry : TEST_CATALOG.entrySet()) {
            boolean includeTest = InputValidator.getYesNo(sc,
                    entry.getKey() + " (" + entry.getValue().intValue() + "): ");
            if (includeTest) {
                selectedTests.add(entry.getKey());
                testTotal += entry.getValue();
            }
        }

        double totalAmount = consultationFee + testTotal;

        System.out.println("\nSelected Tests:");
        for (String test : selectedTests) {
            System.out.println("- " + test + " (" + TEST_CATALOG.get(test) + ")");
        }
        System.out.println("Total Test Charges: " + testTotal);
        System.out.println("Final Bill Amount: " + totalAmount);

        java.sql.Date billDate = new java.sql.Date(System.currentTimeMillis());

        String query = "INSERT INTO bills(patient_id, doctor_id, amount, bill_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setDouble(3, totalAmount);
            ps.setDate(4, billDate);

            if (ps.executeUpdate() > 0) {
                System.out.println("Bill added successfully.");
            } else {
                System.out.println("Failed to add bill.");
            }
        }
    }

    // View bills for logged-in patient
    public void viewBills() throws SQLException {
        int patientId = HospitalLoginSystem.getLoggedInPatientId();
        if (patientId <= 0) {
            System.out.println("No logged-in patient found.");
            return;
        }

        String query = "SELECT b.id, b.amount, b.bill_date, d.name AS doctor_name " +
                       "FROM bills b " +
                       "JOIN doctors d ON b.doctor_id = d.id " +
                       "WHERE b.patient_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("Bill Details:");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.println("Bill ID: " + rs.getInt("id"));
                    System.out.println("Doctor Name: " + rs.getString("doctor_name"));
                    System.out.println("Amount: " + rs.getDouble("amount"));
                    System.out.println("Date: " + rs.getDate("bill_date"));
                    System.out.println("---------------------------");
                }
                if (!found) {
                    System.out.println("No bills found for this patient.");
                }
            }
        }
    }

    // Pay bill (only for logged-in patient)
    public void payBill() throws SQLException {
        int patientId = HospitalLoginSystem.getLoggedInPatientId();
        if (patientId <= 0) {
            System.out.println("No logged-in patient found.");
            return;
        }

        int billId = InputValidator.getInt(sc, "Enter Bill ID to pay: ", 1, Integer.MAX_VALUE);

        // Check if bill exists and belongs to logged-in patient
        String query = "SELECT amount FROM bills WHERE id = ? AND patient_id = ?";
        double billAmount = -1;
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, billId);
            ps.setInt(2, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    billAmount = rs.getDouble("amount");
                } else {
                    System.out.println("Bill ID not found for this patient.");
                    return;
                }
            }
        }

        System.out.println("Bill Amount: " + billAmount);
        double payment = InputValidator.getDouble(sc, "Enter payment amount: ", 0, Double.MAX_VALUE);

        if (Double.compare(payment, billAmount) == 0) {
            System.out.println("Bill paid successfully.");
        } else {
            System.out.println("Payment amount does not match the bill amount. Bill not paid.");
        }
    }

    // Helper: Get consultation fee
    private double getConsultationFee(int doctorId) throws SQLException {
        String query = "SELECT consultation_fee FROM doctors WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("consultation_fee");
                }
            }
        }
        return -1;
    }

    // Helper: Check if patient exists
    private boolean patientExists(int patientId) throws SQLException {
        String query = "SELECT COUNT(1) FROM patients WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Helper: Check if doctor exists
    private boolean doctorExists(int doctorId) throws SQLException {
        String query = "SELECT COUNT(1) FROM doctors WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Helper: Check if bill exists
    private boolean billExists(int billId) throws SQLException 
    {
        String query = "SELECT COUNT(1) FROM bills WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
