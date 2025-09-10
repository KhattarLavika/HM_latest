package com.jts.hms;

import java.util.Scanner;

public class Menu {
    private static boolean returnToMainMenu = false;

    // Call this when user chooses "Back to Main Menu"
    public static void goBackToMainMenu() {
        returnToMainMenu = true;
    }

    // Check if user requested to go back
    public static boolean shouldReturnToMainMenu() 
    {
        return returnToMainMenu;
    }


    // Reset after returning to main menu
    public static void reset() 
    {
        returnToMainMenu = false;
    }
}
