package com.jts.hms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class AdminReports 
{
    private Connection conn;
    private Scanner sc= new Scanner(System.in);

    public AdminReports(Connection connection)
    {
        this.conn=connection;
    }
    public void showReportsMenu() throws SQLException
    {
        while(true)
        {
            System.out.println("\n === Admin Reports Menu ===");
            System.out.println("1. Frequent Visiting Patient");
            System.out.println("2. Busiest Doctor");
            System.out.println("3. Patients per Month");
            System.out.println("4. Patients per Year");
            System.out.println("5. Back to Admin Menu");
            int reportChoice = InputValidator.getInt(sc, "Enter your choice: ", 1, 5);

            switch(reportChoice)
            {
                case 1:
                    viewFrequentPatient();
                    break;
                case 2:
                    viewBusiestDoctor();
                    break;  
                case 3: 
                    viewPatientsForMonthYear();
                    break;
                case 4:
                    viewPatientsForYear();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        }
    }
    public void viewFrequentPatient()
    {
        try
        {
            String query = "SELECT p.name, Count(*) as Visits From "+
                            "Patients p JOIN appointments a ON a.patient_id=p.id "+
                            "GROUP BY p.name ORDER BY Visits DESC FETCH FIRST 5 ROWS ONLY";
            try(PreparedStatement ps = conn.prepareStatement(query))
            {
                try(ResultSet rs = ps.executeQuery())
                {   
                    boolean hasResults=false;
                    System.out.println("Frequent Visiting patients: ");
                    while(rs.next())
                    {
                        hasResults=true;
                        String name=rs.getString("name");
                        int visits=rs.getInt("visits");
                        System.out.println("Patient Name: " + name + "\tVisits: " + visits);
                        System.out.println("--------------------------------");

                    }
                    if(!hasResults)
                    {
                        System.out.println("No data available.");
                    }
                }
            }//end of sql try-catch
        }
        catch(SQLException e)
        {
            System.out.println("Error fetching frequent visiting patients: " + e.getMessage());
        }
    } //end of function

    public void viewBusiestDoctor()
    {
        try
        {
            String query="SELECT d.name, COUNT(*) AS appointments "+
                        "FROM appointments a JOIN Doctors d ON a.Doctor_id=d.id "+
                        "GROUP BY d.name ORDER BY appointments "+
                        "DESC FETCH FIRST 5 ROWS ONLY";
            try(PreparedStatement ps = conn.prepareStatement(query))
            {
                try(ResultSet rs = ps.executeQuery())
                {   
                    boolean hasResults = false;
                    System.out.println("Busiest Doctors:");
                    while(rs.next())
                    {
                        hasResults=true;
                        String name = rs.getString("name");
                        int visits = rs.getInt("appointments");
                        System.out.println("Doctor Name: " + name + "\tAppointments: " + visits);
                        System.out.println("--------------------------------");
                    
                    }
                    if(!hasResults)
                        System.out.println("No data available.");
                }
            }//end of sql try-catch
        }
        catch(SQLException e)
        {
            System.out.println("Error fetching busiest Doctor: " + e.getMessage());
        }
        
    }// end of function
    public void viewPatientsForMonthYear()
    {
        try
        {
            int year = InputValidator.getInt(sc, "Enter year(YYYY): ", 1900, 2100);
            int month = InputValidator.getInt(sc, "Enter month (1-12): ", 1, 12);

            String query = "SELECT COUNT(DISTINCT patient_id) AS Total_Patients " +
                            "FROM appointments " +
                            "WHERE EXTRACT(YEAR FROM appointment_date) = ? " +
                            "AND EXTRACT(MONTH FROM appointment_date) = ?";
            try (PreparedStatement ps=conn.prepareStatement(query))
            {
                ps.setInt(1,year);
                ps.setInt(2, month);

                try(ResultSet rs=ps.executeQuery())
                {
                    if(rs.next())
                    {
                        System.out.println("Total Patients in "+month+"/"+year+": "+rs.getInt("Total_Patients"));
                    }
                    else
                    {
                        System.out.println("No data available for " + month + "/" + year);
                    }
                }
            } 
        }
        catch(SQLException e)
        {
            System.out.println("Error fetching patients by month/year: " + e.getMessage());
        }
    }// end of function

    public void viewPatientsForYear()
    {
        try
        {
            int year = InputValidator.getInt(sc, "Enter year(YYYY): ", 1900, 2025);

            String query = "SELECT COUNT(DISTINCT patient_id) AS Total_Patients " +
                            "FROM appointments " +
                            "WHERE EXTRACT(YEAR FROM appointment_date) = ? ";
            try (PreparedStatement ps=conn.prepareStatement(query))
            {
                ps.setInt(1,year);;

                try(ResultSet rs=ps.executeQuery())
                {
                    if(rs.next())
                    {
                        System.out.println("Total Patients in "+year+": "+rs.getInt("Total_Patients"));
                    }
                    else
                    {
                        System.out.println("No data available for "+ year);
                    }
                }
            } 
        }
        catch(SQLException e)
        {
            System.out.println("Error fetching patients by month/year: " + e.getMessage());
        }
    }// end of function

}
