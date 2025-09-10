package com.jts.hms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Doctor 
{
    private Connection conn;
    private int doctorId;   // logged-in doctor’s ID
    Scanner sc = new Scanner(System.in);

    public Doctor(Connection conn) 
    {
        this.conn = conn;
    }

    // constructor for doctor login
    public Doctor(Connection conn, int doctorId) 
    {
        this.conn = conn;
        this.doctorId = doctorId;
    }

    // Add new doctor (admin only)
        private boolean validateName(String name) 
        {
        if (name == null || name.isEmpty()) 
        {
            System.out.println(" Name cannot be empty. Please try again.");
            return false;
        }
        if (!name.matches("^[A-Za-z ]{2,50}$")) {
            System.out.println(" Invalid name. Only letters and spaces allowed (2-50 chars).");
            return false;
        }
        return true;
    }
    public void addDoctor() throws SQLException 
    {
        String name;
		do {
			System.out.print("Enter Doctor Name: ");
			name = sc.nextLine().trim();
		} while (!validateName(name));

        System.out.print("Enter Department: ");
        String dept = InputValidator.getString(sc, "Enter Department: ");

        String query = "INSERT INTO doctors(NAME, DEPT) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(query)) 
        {
            ps.setString(1, name);
            ps.setString(2, dept);

            if (ps.executeUpdate() > 0) 
            {
                System.out.println("Doctor details added successfully.");
            } 
            else 
            {
                System.out.println("Failed to add Doctor details.");
            }
        }
    }

    // View all doctors
    public void viewDoctors() throws SQLException 
    {
        String query = "SELECT * FROM doctors where status ='approved'";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("Doctor details:");

                while (rs.next()) {
                    int id = rs.getInt("ID");
                    String name = rs.getString("NAME");
                    String dept = rs.getString("DEPT");

                    System.out.println("\nDoctor ID: " + id);
                    System.out.println("Doctor Name: " + name);
                    System.out.println("Doctor Department: " + dept + "\n");
                    System.out.println("--------------------------------");

                }
            }
        }
    }

    // Check if doctor exists by ID
    public boolean getDoctorById(int id) throws SQLException 
    {
        String query = "SELECT COUNT(1) FROM doctors WHERE ID = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) 
        {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) 
            {
                if (rs.next()) 
                {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Add prescription for a patient
    public void addPrescriptionToPatient() throws SQLException 
    {
        int patientId = InputValidator.getInt(sc, "Enter Patient ID: ");
        String prescription = InputValidator.getString(sc, "Enter Prescription Details: ");

        String query = "UPDATE patients SET PRESCRIPTION = ? WHERE ID = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) 
        {
            ps.setString(1, prescription);
            ps.setInt(2, patientId);

            if (ps.executeUpdate() > 0) 
            {
                System.out.println("Prescription added successfully.");
            } 
            else 
            {
                System.out.println("Failed to add prescription. Patient ID may not exist.");
            }
        }
    }

    // View prescription of a patient
    public void viewPrescriptionByPatientId() throws SQLException 
    {
        int patientId = InputValidator.getInt(sc, "Enter Patient ID: ");

        String query = "SELECT NAME, PRESCRIPTION FROM patients WHERE ID = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("NAME");
                    String prescription = rs.getString("PRESCRIPTION");

                    System.out.println("Patient Name: " + name);
                    System.out.println("Prescription: " + (prescription != null ? prescription : "No prescription added."));
                } else {
                    System.out.println("Patient not found.");
                }
            }
        }
    }

    // View appointments for logged-in doctor by date
    public void viewAppointmentsByDate() 
    {
        if (doctorId == 0) 
        {
            System.out.println("This action is only for logged-in doctors.");
            return;
        }
        LocalDate appointmentDate;
        while (true) 
        {
            System.out.print("Enter Appointment Date (YYYY-MM-DD): ");
            String dateStr = sc.nextLine().trim();
            try 
            {
                appointmentDate = LocalDate.parse(dateStr);
                break;
            } 
            catch (DateTimeParseException e) 
            {
                System.out.println("Invalid date format. Please enter in YYYY-MM-DD format.");
            }
        }
        String date = appointmentDate.toString();
        

        String query = "SELECT a.ID, p.NAME AS PATIENT_NAME, a.APPOINTMENT_TIME " +
                       "FROM appointments a " +
                       "JOIN patients p ON a.PATIENT_ID = p.ID " +
                       "WHERE a.DOCTOR_ID = ? AND a.APPOINTMENT_DATE = TO_DATE(?, 'YYYY-MM-DD') " +
                       "ORDER BY a.APPOINTMENT_TIME";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, doctorId);
            ps.setString(2, date);

            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\nAppointments for Doctor ID " + doctorId + " on " + date + ":");

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    int appointmentId = rs.getInt("ID");
                    String patientName = rs.getString("PATIENT_NAME");
                    java.sql.Time time = rs.getTime("APPOINTMENT_TIME");
                    String appointmentTime = time.toLocalTime().toString().substring(0,5);

                    System.out.println("Appointment ID: " + appointmentId);
                    System.out.println("Patient Name: " + patientName);
                    System.out.println("Appointment Time: " + appointmentTime);
                    System.out.println("--------------------------------");
                }

                if (!found) {
                    System.out.println("No appointments found for this date.");
                }
            }
        } 
        catch (SQLException e) 
        {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
