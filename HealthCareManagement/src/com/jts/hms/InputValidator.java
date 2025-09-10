package com.jts.hms;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class InputValidator {

    // Read a valid integer with prompt
    public static int getInt(Scanner scanner, String prompt, int min, int max) {
        int value;
        while (true) 
        {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try 
            {
                value = Integer.parseInt(input);
                if (value >= min && value <= max) 
                {
                    return value;
                } 
                else 
                {
                    System.out.println("Input must be between " + min + " and " + max + ".");
                }
            } 
            catch (NumberFormatException e) 
            {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }
    public static int getInt(Scanner scanner, String prompt) 
    {
        int value;
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                value = Integer.parseInt(input);
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }


    // Read a non-empty string
    public static String getString(Scanner scanner, String prompt) {
        String value;
        while (true) {
            System.out.print(prompt);
            value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("Input cannot be empty.");
        }
    }

    // Read a valid phone number (10 digits)
    public static String getPhoneNumber(Scanner scanner, String prompt) {
        String phone;
        while (true) {
            System.out.print(prompt);
            phone = scanner.nextLine().trim();
            if (phone.matches("\\d{10}")) return phone;
            System.out.println("Invalid phone number. Must be 10 digits.");
        }
    }

    // Read gender: male, female, or other
    public static String getGender(Scanner scanner, String prompt) 
    {
        String gender;
        while (true) {
            System.out.print(prompt);
            gender = scanner.nextLine().trim().toLowerCase();
            if (gender.equals("male") || gender.equals("female") || gender.equals("other")) return gender;
            System.out.println("Invalid gender. Enter male, female, or other.");
        }
    }
    public static String getDate(Scanner sc, String prompt) 
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        while (true) 
        {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try 
            {
                sdf.parse(input);
                return input; // valid date string
            } 
            catch (ParseException e) 
            {
                System.out.println("Invalid date format. Please enter in YYYY-MM-DD format.");
            }
        }
    }
    // Read a valid double value
    public static double getDouble(Scanner scanner, String prompt, double min, double max) {
        double value;
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                value = Double.parseDouble(input);
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.println("Input must be between " + min + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    // Read yes/no input
    public static boolean getYesNo(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("yes") || input.equals("y")) return true;
            if (input.equals("no") || input.equals("n")) return false;
            System.out.println("Invalid input. Please enter Yes or No.");
        }
    }
    public static Time getTime(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                // Append ":00" for seconds
                Time time = Time.valueOf(input + ":00");
                return time;
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid time format. Use HH:mm (e.g., 10:30).");
            }
        }
    }


}
