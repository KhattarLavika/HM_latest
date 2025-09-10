package com.jts.hms;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Scanner;

public class BookAppointment {

    private static final int MAX_APPOINTMENTS_PER_SLOT = 3;

    private Connection connection;
    private Patient patient;
    private Doctor doctor;
    private Scanner scanner = new Scanner(System.in);

    public BookAppointment(Connection connection, Patient patient, Doctor doctor) {
        this.connection = connection;
        this.patient = patient;
        this.doctor = doctor;
    }

    public void bookAppointment() throws SQLException {

        // ✅ Get logged-in patient ID from HospitalLoginSystem
        int patientId = HospitalLoginSystem.getLoggedInPatientId();
        if (patientId <= 0) {
            System.out.println("No logged-in patient found. Please log in first.");
            return;
        }

        // ✅ Verify patient exists in patients table
        if (!patient.getPatientById(patientId)) {
            System.out.println("Invalid patient ID (patient record not found).");
            return;
        }

        // Ask patient to choose doctor
        int doctorId = InputValidator.getInt(scanner, "Enter Doctor ID: ", 1, Integer.MAX_VALUE);

        // ✅ Validate doctor existence
        if (!doctor.getDoctorById(doctorId)) {
            System.out.println("Invalid doctor ID.");
            return;
        }

        Date appointmentDate;
        Time appointmentTime;

        // ✅ Get valid appointment date
        while (true) {
            appointmentDate = Date.valueOf(InputValidator.getDate(scanner, "Enter appointment date (yyyy-mm-dd): "));
            Date today = new Date(System.currentTimeMillis());
            if (appointmentDate.before(today)) {
                System.out.println("You cannot book an appointment for a past date.");
            } else {
                break;
            }
        }

        // ✅ Get valid appointment time
        appointmentTime = InputValidator.getTime(scanner, "Enter appointment time (HH:mm): ");

        // ✅ Check slot availability
        while (!checkAvailability(doctorId, appointmentDate, appointmentTime)) {
            System.out.println("This time slot is fully booked.");
            showAvailableSlots(doctorId, appointmentDate);
            System.out.println("Please choose another time slot.");
            appointmentTime = InputValidator.getTime(scanner, "Enter appointment time (HH:mm): ");
        }

        // ✅ Prevent duplicate booking by same patient for same date & time
        String duplicateCheckQuery = "SELECT COUNT(*) FROM appointments WHERE patient_id = ? AND appointment_date = ? AND appointment_time = ?";
        try (PreparedStatement psCheck = connection.prepareStatement(duplicateCheckQuery)) {
            psCheck.setInt(1, patientId);
            psCheck.setDate(2, appointmentDate);
            psCheck.setTime(3, appointmentTime);

            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("You already have an appointment at this date and time.");
                    return;
                }
            }
        }

        // ✅ Insert appointment
        String query = "INSERT INTO appointments(patient_id, doctor_id, appointment_date, appointment_time) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setDate(3, appointmentDate);
            ps.setTime(4, appointmentTime);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Appointment booked successfully.");
            } else {
                System.out.println("Failed to book appointment.");
            }
        }
    }

    private boolean checkAvailability(int doctorId, Date appointmentDate, Time appointmentTime) throws SQLException {
        int hour = appointmentTime.toLocalTime().getHour();
        Time slotStart = Time.valueOf(String.format("%02d:00:00", hour));
        Time slotEnd = Time.valueOf(String.format("%02d:00:00", hour + 1));

        String query = "SELECT COUNT(*) FROM appointments " +
                       "WHERE doctor_id = ? AND appointment_date = ? " +
                       "AND appointment_time >= ? AND appointment_time < ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, doctorId);
            ps.setDate(2, appointmentDate);
            ps.setTime(3, slotStart);
            ps.setTime(4, slotEnd);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) < MAX_APPOINTMENTS_PER_SLOT;
                }
            }
        }
        return false;
    }

    private void showAvailableSlots(int doctorId, Date appointmentDate) throws SQLException {
        System.out.println("Available Slots for Doctor ID " + doctorId + " on " + appointmentDate + ":");
        for (int hour = 9; hour < 17; hour++) {
            Time slotStart = Time.valueOf(String.format("%02d:00:00", hour));
            Time slotEnd = Time.valueOf(String.format("%02d:00:00", hour + 1));

            String query = "SELECT COUNT(*) FROM appointments " +
                           "WHERE doctor_id = ? AND appointment_date = ? " +
                           "AND appointment_time >= ? AND appointment_time < ?";

            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, doctorId);
                ps.setDate(2, appointmentDate);
                ps.setTime(3, slotStart);
                ps.setTime(4, slotEnd);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) < MAX_APPOINTMENTS_PER_SLOT) {
                        System.out.printf(" Slot: %02d:00 - %02d:00 (%d/%d booked)%n", hour, hour + 1,
                                          rs.getInt(1), MAX_APPOINTMENTS_PER_SLOT);
                    }
                }
            }
        }
    }
}
