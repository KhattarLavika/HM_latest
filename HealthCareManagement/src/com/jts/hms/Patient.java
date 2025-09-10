package com.jts.hms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;


public class Patient 
{
	private Connection conn;
	Scanner sc = new Scanner(System.in);

	public Patient(Connection conn) 
	{
		this.conn = conn;
	}

	public void addPatient() throws SQLException 
	{
		String name;
		do {
			System.out.print("Enter Patient Name: ");
			name = sc.nextLine().trim();
		} while (!validateName(name));

		System.out.print("Enter Patient Age: ");
		int age = InputValidator.getInt(sc, "Enter Patient Age: ", 0, 120);

		System.out.print("Enter Patient Gender: ");
		String gender = InputValidator.getGender(sc, "Enter Patient Gender: ");

		String query = "insert into patients(name, age, gender) values (?, ?, ?)";

		try (PreparedStatement ps = conn.prepareStatement(query)) 
		{
			ps.setString(1, name);
			ps.setInt(2, age);
			ps.setString(3, gender);

			if (ps.executeUpdate() > 0) 
			{
				System.out.println("Patients details add successfully.");
			} 
			else 
			{
				System.out.println("Failed to add Patients details.");
			}
		}
	}

	//For all to view patients and
	public void viewPatients() throws SQLException 
	{
		String query = "select * from patients where status ='approved'";

		try (PreparedStatement ps = conn.prepareStatement(query)) 
		{
			try (ResultSet rs = ps.executeQuery()) 
			{
				System.out.println("Patients details : ");

				while (rs.next()) 
				{
					int id = rs.getInt("id");
					String name = rs.getString("name");
					int age = rs.getInt("age");
					String gender = rs.getString("gender");

					System.out.println();
					System.out.println("Patient id :" + id);
					System.out.println("Patient Name :" + name);
					System.out.println("Patient Age :" + age);
					System.out.println("Patient Gender :" + gender);
					System.out.println("--------------------------------");
				}
			}
		}
	}
	
	//For admin...manage all requests
	public boolean getPatientById(int id) throws SQLException 
	{
		String query = "select count(1) from patients where id = ?";
		
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
	private boolean validateName(String name) 
	{
        if (name == null || name.isEmpty()) 
		{
            System.out.println("Name cannot be empty. Please try again.");
            return false;
        }
        if (!name.matches("^[A-Za-z ]{2,50}$")) 
		{
            System.out.println("Invalid name. Only letters and spaces allowed (2-50 chars).");
            return false;
        }
        return true;
    }
	public void viewUpcomingAppointments(int patientId) throws SQLException 
	{
		String query =
            "SELECT a.ID, d.NAME AS DOCTOR_NAME, d.DEPT, " +
            "       a.APPOINTMENT_DATE, a.APPOINTMENT_TIME " +
            "FROM appointments a " +
            "JOIN doctors d ON a.DOCTOR_ID = d.ID " +
            "WHERE a.PATIENT_ID = ? " +
            "AND a.APPOINTMENT_DATE >= TRUNC(SYSDATE) " + // only today or future
            "ORDER BY a.APPOINTMENT_DATE, a.APPOINTMENT_TIME";
		
		try (PreparedStatement ps = conn.prepareStatement(query)) 
		{
            ps.setInt(1, patientId);

			try (ResultSet rs = ps.executeQuery()) 
			{
                System.out.println("\nUpcoming Appointments for Patient ID " + patientId + ":");

                boolean found = false;
                while (rs.next()) 
				{
                    found = true;
                    int appointmentId = rs.getInt("ID");
                    String doctorName = rs.getString("DOCTOR_NAME");
                    String department = rs.getString("DEPT");
                    Date date = rs.getDate("APPOINTMENT_DATE");
                    Timestamp time = rs.getTimestamp("APPOINTMENT_TIME");

					SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
					SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

					String formattedDate = (date != null) ? dateFormat.format(date) : "N/A";
					String formattedTime = (time != null) ? timeFormat.format(time) : "N/A";


                    System.out.println("Appointment ID: " + appointmentId);
                    System.out.println("Doctor: " + doctorName + " (" + department + ")");
                    System.out.println("Date: " + formattedDate);
                    System.out.println("Time: " + formattedTime);
                    System.out.println("--------------------------------");
                }

                if (!found) 
				{
                    System.out.println("No upcoming appointments found.");
                }
            }
        }
	}

	

}
