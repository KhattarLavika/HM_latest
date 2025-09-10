package com.jts.hms;

import java.sql.*;
import java.util.*;

public class HospitalLoginSystem {

    private Connection conn;

    public HospitalLoginSystem(Connection connection) {
        this.conn = connection;
    }

    // Entry point for HospitalManagement
    public static String startLogin(Connection connection, Scanner scanner) throws SQLException {
        HospitalLoginSystem loginSystem = new HospitalLoginSystem(connection);
        return loginSystem.start(scanner);
    }

    // ================= MAIN LOGIN MENU =================
    public String start(Scanner scanner) throws SQLException {
        while (true) { // keep showing until valid choice
            System.out.println("\n\n");
            System.out.println("===============================================================");
            System.out.println("        HOSPITAL MANAGEMENT SYSTEM LOGIN PORTAL");
            System.out.println("===============================================================");
            System.out.println("1. Admin Login");
            System.out.println("2. Doctor");
            System.out.println("3. Patient");

            int roleChoice = InputValidator.getInt(scanner, "Enter choice (1/2/3): ", 1, 3);

            switch (roleChoice) {
                case 1: return loginAsAdmin(scanner);
                case 2: return doctorMenu(scanner);
                case 3: return patientMenu(scanner);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // ================= ADMIN LOGIN =================
    private String loginAsAdmin(Scanner scanner) {
        int attempts = 0;
        while (attempts < 3) {
            System.out.print("Enter Admin Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Enter Admin Password: ");
            String password = scanner.nextLine().trim();

            if (username.equalsIgnoreCase("admin") && password.equals("admin123")) {
                System.out.println("Admin login successful.");
                return "admin";
            } else {
                attempts++;
                System.out.println("Invalid admin credentials. Attempts left: " + (3 - attempts));
            }
        }
        System.out.println("Too many failed login attempts. Returning to main menu.");
        return null;
    }

    // ================= DOCTOR LOGIN/SIGNUP =================
    private static int loggedInDoctorId = 0;

    private String doctorMenu(Scanner scanner) throws SQLException {
        while (true) {
            System.out.println("\nDoctor Portal:");
            System.out.println("1. Login");
            System.out.println("2. Signup");
            int choice = InputValidator.getInt(scanner, "Enter choice: ", 1, 2);

            switch (choice) {
                case 1: return loginAsDoctor(scanner);
                case 2:
                    signupDoctor(scanner);
                    return "main"; // needs admin approval
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private String loginAsDoctor(Scanner scanner) throws SQLException {
        int attempts = 0;
        while (attempts < 3) {
            System.out.print("Enter Doctor Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Enter Doctor Password: ");
            String password = scanner.nextLine().trim();

            String query = "SELECT id, status FROM doctors WHERE LOWER(username)=LOWER(?) AND password=?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, username);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String status = rs.getString("status");
                        if ("approved".equalsIgnoreCase(status)) {
                            loggedInDoctorId = rs.getInt("id");
                            System.out.println("Doctor login successful. (Doctor ID = " + loggedInDoctorId + ")");
                            return "doctor";
                        } else {
                            System.out.println("Doctor account is pending approval by admin.");
                            return "pending";
                        }
                    }
                }
            }

            attempts++;
            System.out.println("Invalid doctor credentials. Attempts left: " + (3 - attempts));
        }
        System.out.println("Too many failed login attempts. Returning to main menu.");
        return null;
    }

    public static int getLoggedInDoctorId() {
        return loggedInDoctorId;
    }

    private void signupDoctor(Scanner scanner) throws SQLException {
        String name;
        do {
            System.out.print("Enter Doctor Name: ");
            name = scanner.nextLine().trim();
        } while (!validateName(name));
        String specialization = InputValidator.getString(scanner, "Enter Department: ");
        String phone = InputValidator.getPhoneNumber(scanner, "Enter Phone Number: ");
        int consultation_fee = InputValidator.getInt(scanner, "Enter consultation fee: ");
        String username = InputValidator.getString(scanner, "Enter Username: ");
        String password = InputValidator.getString(scanner, "Enter Password: ");

        String checkQuery = "SELECT id FROM doctors WHERE name=? AND phone_no=?";
        try (PreparedStatement checkPs = conn.prepareStatement(checkQuery)) {
            checkPs.setString(1, name);
            checkPs.setString(2, phone);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    System.out.println("A doctor with this name and phone number already exists!");
                    return;
                }
            }
        }

        String query = "INSERT INTO doctors (name, dept, phone_no, consultation_fee, username, password, status) " +
                       "VALUES (?, ?, ?, ?, ?, ?, 'pending')";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);
            ps.setString(2, specialization);
            ps.setString(3, phone);
            ps.setInt(4, consultation_fee);
            ps.setString(5, username.toLowerCase());
            ps.setString(6, password);

            if (ps.executeUpdate() > 0) {
                System.out.println("Doctor registered successfully. Awaiting admin approval.");
            } else {
                System.out.println("Doctor registration failed.");
            }
        }
    }

    private boolean validateName(String name) {
        if (name == null || name.isEmpty()) {
            System.out.println(" Name cannot be empty. Please try again.");
            return false;
        }
        if (!name.matches("^[A-Za-z ]{2,50}$")) {
            System.out.println(" Invalid name. Only letters and spaces allowed (2-50 chars).");
            return false;
        }
        return true;
    }

    // ================= PATIENT LOGIN/SIGNUP =================
    private static int loggedInPatientId = 0;

    private String patientMenu(Scanner scanner) throws SQLException {
        while (true) {
            System.out.println("\nPatient Portal:");
            System.out.println("1. Login");
            System.out.println("2. Signup");
            int choice = InputValidator.getInt(scanner, "Enter choice: ", 1, 2);

            switch (choice) {
                case 1: return loginAsPatient(scanner);
                case 2:
                    signupPatient(scanner);
                    return "main"; // needs admin approval
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private String loginAsPatient(Scanner scanner) throws SQLException {
        int attempts = 0;
        while (attempts < 3) {
            System.out.print("Enter Patient Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Enter Patient Password: ");
            String password = scanner.nextLine().trim();

            String query = "SELECT id, status FROM patients WHERE LOWER(username)=LOWER(?) AND password=?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, username);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String status = rs.getString("status");
                        if ("approved".equalsIgnoreCase(status)) {
                            loggedInPatientId = rs.getInt("id");
                            System.out.println("Patient login successful.");
                            return "patient";
                        } else {
                            System.out.println("Patient account is pending approval by admin.");
                            return "pending";
                        }
                    }
                }
            }

            attempts++;
            System.out.println("Invalid patient credentials. Attempts left: " + (3 - attempts));
        }
        System.out.println("Too many failed login attempts. Returning to main menu.");
        return null;
    }

    public static int getLoggedInPatientId() {
        return loggedInPatientId;
    }

    private void signupPatient(Scanner scanner) throws SQLException {
        String name;
        do {
            System.out.print("Enter Patient Name: ");
            name = scanner.nextLine().trim();
        } while (!validateName(name));
        int age = InputValidator.getInt(scanner, "Enter Age: ", 0, 120);
        String gender = InputValidator.getGender(scanner, "Enter Gender: ");
        String username = InputValidator.getString(scanner, "Enter Username: ");
        String password = InputValidator.getString(scanner, "Enter Password: ");

        String checkQuery = "SELECT id FROM patients WHERE name=? AND age=? AND gender=?";
        try (PreparedStatement checkPs = conn.prepareStatement(checkQuery)) {
            checkPs.setString(1, name);
            checkPs.setInt(2, age);
            checkPs.setString(3, gender);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    System.out.println("A patient with the same details already exists!");
                    return;
                }
            }
        }

        String query = "INSERT INTO patients (name, age, gender, username, password, status) " +
                       "VALUES (?, ?, ?, ?, ?, 'pending')";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, gender);
            ps.setString(4, username.toLowerCase());
            ps.setString(5, password);

            if (ps.executeUpdate() > 0) {
                System.out.println("Patient registered successfully. Awaiting admin approval.");
            } else {
                System.out.println("Patient registration failed.");
            }
        }
    }

    // ================= ADMIN APPROVAL =================
    public void approveUsers(Scanner scanner) throws SQLException {
        while (true) {
            System.out.println("\n--- Approve/Reject Pending Users ---");
            System.out.println("1. View Pending Patients");
            System.out.println("2. View Pending Doctors");
            System.out.println("3. Back");

            int choice = InputValidator.getInt(scanner, "Enter choice: ", 1, 3);

            switch (choice) {
                case 1: handleApproval(scanner, "patients"); break;
                case 2: handleApproval(scanner, "doctors"); break;
                case 3: return;
                default: System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void handleApproval(Scanner scanner, String table) throws SQLException {
        String idColumn = "id";
        String query = "SELECT " + idColumn + ", name, username FROM " + table + " WHERE status='pending'";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            boolean hasPending = false;
            Set<Integer> pendingIds = new HashSet<>();

            System.out.println("\n--- Pending " + table + " ---");
            while (rs.next()) {
                hasPending = true;
                int id = rs.getInt(idColumn);
                String name = rs.getString("name");
                String username = rs.getString("username");
                pendingIds.add(id);
                System.out.println(id + " | " + name + " | " + username);
                System.out.println("--------------------------------");
            }

            if (!hasPending) {
                System.out.println("No pending " + table + " found.");
                return;
            }

            int id = InputValidator.getInt(scanner, "Enter ID to approve/reject (0 to cancel): ");
            if (id == 0) return;

            if (!pendingIds.contains(id)) {
                System.out.println("Invalid ID. Please select a valid pending " + table + ".");
                return;
            }

            System.out.print("Approve or Reject? (a/r): ");
            String decision = scanner.nextLine().trim().toLowerCase();

            String update = "UPDATE " + table + " SET status=? WHERE " + idColumn + "=?";
            try (PreparedStatement updatePs = conn.prepareStatement(update)) {
                if (decision.equals("a")) {
                    updatePs.setString(1, "approved");
                } else if (decision.equals("r")) {
                    updatePs.setString(1, "rejected");
                } else {
                    System.out.println("Invalid input. Skipping...");
                    return;
                }
                updatePs.setInt(2, id);

                int rows = updatePs.executeUpdate();
                if (rows > 0) {
                    System.out.println("User " + (decision.equals("a") ? "approved." : "rejected."));
                }
            }
        }
    }
}
