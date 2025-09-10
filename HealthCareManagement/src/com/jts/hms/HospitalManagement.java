package com.jts.hms;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class HospitalManagement {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try (Connection conn = DatabaseService.getConnection()) 
        {
            Patient patient = new Patient(conn);
            Doctor doctor = new Doctor(conn);
            BookAppointment appointment = new BookAppointment(conn, patient, doctor);
            Bill bill = new Bill(conn);

            String role = HospitalLoginSystem.startLogin(conn, sc);

            if (role == null) 
            {
                System.out.println("Login failed.");
                return;
            }

            while (true) 
            {
                 if (role == null || role.equals("main")) 
                 {
                    role = HospitalLoginSystem.startLogin(conn, sc);
                    if (role == null) 
                    {
                        System.out.println("Login failed.");
                        return;
                    }
                 }
                switch (role) 
                {
                    case "patient":
                        int loggedInPatientId = HospitalLoginSystem.getLoggedInPatientId(); 
                        boolean backToMainFromPatient = false;
                        while (!backToMainFromPatient) {
                            System.out.println("\n=== Patient Portal ===");
                            System.out.println("1. View Doctors");
                            System.out.println("2. Book Appointment");
                            System.out.println("3. View Prescription");
                            System.out.println("4. View Bill");
                            System.out.println("5. Pay Bill");
                            System.out.println("6. View Upcoming Appointments");  
                            System.out.println("7. Back to Main Menu");
                            System.out.println("8. Exit");
                            int patientChoice = InputValidator.getInt(sc, "Enter your choice: ", 1, 8);


                            switch (patientChoice) {
                                case 1: doctor.viewDoctors(); break;
                                case 2: appointment.bookAppointment(); break;
                                case 3: doctor.viewPrescriptionByPatientId(); break;
                                case 4: bill.viewBills(); break;
                                case 5: bill.payBill(); break;
                                case 6: patient.viewUpcomingAppointments(loggedInPatientId); break; 
                                case 7: backToMainFromPatient = true; role = "main"; break; 
                                case 8:  return;
                                default: System.out.println("Invalid choice.");
                            }
                        }
                        break;
                    case "admin":
                        boolean backToMainFromAdmin = false;
                        while(!backToMainFromAdmin) {
                        System.out.println("\n=== Admin Portal ===");
                        System.out.println("1. View Patients");
                        System.out.println("2. View Doctors");
                        System.out.println("3. Analyse Reports");
                        System.out.println("4. Approve/Reject Users");
                        System.out.println("5. Back to Main Menu");
                        System.out.println("6. Exit");
                        int adminChoice = InputValidator.getInt(sc, "Enter your choice: ", 1, 6);

                        switch (adminChoice) 
                        {
                            case 1: patient.viewPatients(); break;
                            case 2: doctor.viewDoctors(); break;
                            case 3:
                                AdminReports reports = new AdminReports(conn);
                                reports.showReportsMenu();
                                break;
                            case 4:
                                HospitalLoginSystem loginSystem = new HospitalLoginSystem(conn);
                                loginSystem.approveUsers(sc);
                                break;
                            case 5: backToMainFromAdmin = true; role = "main"; break;
                            case 6: return;
                            default: System.out.println("Invalid choice.");
                        }
                    }
                        break;

                    case "doctor":
                        int loggedInDoctorId = HospitalLoginSystem.getLoggedInDoctorId(); 
                        Doctor loggedInDoctor = new Doctor(conn, loggedInDoctorId); // ✅ use doctorId
                        boolean backToMainFromDoctor = false;
                        while (!backToMainFromDoctor) 
                        {
                            System.out.println("\n=== Doctor Portal ===");
                            System.out.println("1. View Patients");
                            System.out.println("2. Add Prescription");
                            System.out.println("3. View Prescription");
                            System.out.println("4. View My Appointments");
                            System.out.println("5. Add Bill");
                            System.out.println("6. Back to Main Menu");
                            System.out.println("7. Exit");

                            int doctorChoice = InputValidator.getInt(sc, "Enter your choice: ", 1, 7);

                            switch (doctorChoice) 
                            {
                                case 1: patient.viewPatients(); break;
                                case 2: loggedInDoctor.addPrescriptionToPatient(); break;
                                case 3: loggedInDoctor.viewPrescriptionByPatientId(); break;
                                case 4: loggedInDoctor.viewAppointmentsByDate(); break; // ✅ doctor-specific
                                case 5: bill.addBill(true); break;
                                case 6: backToMainFromDoctor = true; role = "main"; break;
                                case 7: return;
                                default: System.out.println("Invalid choice.");
                            }

                        }
                        break;

                    default:
                        System.out.println("Unknown role. Exiting.");
                        return;
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found. " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } finally {
            sc.close();
        }
    }
}
