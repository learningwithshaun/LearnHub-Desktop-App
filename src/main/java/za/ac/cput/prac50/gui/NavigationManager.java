package za.ac.cput.prac50.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Centralized Navigation Manager for LearnHub
 * Handles navigation between all subsystems
 * * @author LearnHub Team
 */
public class NavigationManager {
    
    /**
     * Navigate to Dashboard (Overview)
     * @param currentFrame Current frame to dispose
     */
    public static void navigateToDashboard(JFrame currentFrame) {
        SwingUtilities.invokeLater(() -> {
            try {
                new DashboardGUI().setVisible(true);  // ✅ FIXED: Changed from CreateNewGroupGUI
                if (currentFrame != null) {
                    currentFrame.dispose();
                }
            } catch (Exception e) {
                showNavigationError("Dashboard", e);
            }
        });
    }
    
    /**
     * Navigate to Create Group page
     * @param currentFrame Current frame to dispose
     */
    public static void navigateToCreateGroup(JFrame currentFrame) {
        SwingUtilities.invokeLater(() -> {
            try {
                new CreateNewGroupGUI().setVisible(true);
                if (currentFrame != null) {
                    currentFrame.dispose();
                }
            } catch (Exception e) {
                showNavigationError("Create Group", e);
            }
        });
    }
    
    /**
     * Navigate to Groups (Study Group Finder)
     * @param currentFrame Current frame to dispose
     */
    public static void navigateToGroups(JFrame currentFrame) {
        SwingUtilities.invokeLater(() -> {
            try {
                new StudyGroupFinder().setVisible(true);
                if (currentFrame != null) {
                    currentFrame.dispose();
                }
            } catch (Exception e) {
                showNavigationError("Groups", e);
            }
        });
    }
    
    /**
     * Navigate to Location (Map)
     * @param currentFrame Current frame to dispose
     */
    public static void navigateToLocation(JFrame currentFrame) {
        SwingUtilities.invokeLater(() -> {
            try {
                new MapApp().setVisible(true);
                if (currentFrame != null) {
                    currentFrame.dispose();
                }
            } catch (Exception e) {
                showNavigationError("Location", e);
            }
        });
    }
    
    /**
     * Navigate to Location with venue filter
     * @param currentFrame Current frame to dispose
     * @param venueName Venue to filter by
     */
    public static void navigateToLocationWithVenue(JFrame currentFrame, String venueName) {
        SwingUtilities.invokeLater(() -> {
            try {
                MapApp mapApp = new MapApp();
                // ✅ FIX: Set filter and update UI after frame is visible (to avoid race condition with component creation)
                SwingUtilities.invokeLater(() -> {
                    mapApp.filterByVenue(venueName);
                    mapApp.setVisible(true);
                });
                
                if (currentFrame != null) {
                    currentFrame.dispose();
                }
            } catch (Exception e) {
                showNavigationError("Location (with venue)", e);
            }
        });
    }
    
    /**
     * Navigate to Study Points
     * @param currentFrame Current frame to dispose
     */
    public static void navigateToStudyPoints(JFrame currentFrame) {
        SwingUtilities.invokeLater(() -> {
            try {
                new LearnHubStudyPoints().setVisible(true);
                if (currentFrame != null) {
                    currentFrame.dispose();
                }
            } catch (Exception e) {
                showNavigationError("Study Points", e);
            }
        });
    }
    
    /**
     * Navigate to Messages (Groups Messaging)
     * @param currentFrame Current frame to dispose
     */
    public static void navigateToMessages(JFrame currentFrame) {
        SwingUtilities.invokeLater(() -> {
            try {
                new LearnHubGroups().setVisible(true);
                if (currentFrame != null) {
                    currentFrame.dispose();
                }
            } catch (Exception e) {
                showNavigationError("Messages", e);
            }
        });
    }
    
    /**
     * Navigate to Messages with specific group selected
     * @param currentFrame Current frame to dispose
     * @param groupId Group to select
     */
    public static void navigateToMessagesWithGroup(JFrame currentFrame, int groupId) {
        SwingUtilities.invokeLater(() -> {
            try {
                LearnHubGroups messagesFrame = new LearnHubGroups();
                messagesFrame.setVisible(true);
                if (currentFrame != null) {
                    currentFrame.dispose();
                }
            } catch (Exception e) {
                showNavigationError("Messages (with group)", e);
            }
        });
    }
    
    /**
     * Navigate to Profile
     * @param currentFrame Current frame to dispose
     */
    public static void navigateToProfile(JFrame currentFrame) {
        SwingUtilities.invokeLater(() -> {
            try {
                JOptionPane.showMessageDialog(
                    currentFrame,
                    "Profile feature is under development.\nComing soon!",
                    "Under Construction",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception e) {
                showNavigationError("Profile", e);
            }
        });
    }
    
    /**
     * Show navigation error dialog
     * @param destination Destination that failed
     * @param exception Exception that occurred
     */
    private static void showNavigationError(String destination, Exception exception) {
        System.err.println("Navigation error to " + destination + ": " + exception.getMessage());
        exception.printStackTrace();
        
        JOptionPane.showMessageDialog(
            null,
            "Failed to navigate to " + destination + ".\n\n" +
            "Error: " + exception.getMessage() + "\n\n" +
            "Please check the console for details.",
            "Navigation Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Create navigation action listener for a button
     * @param destination Navigation destination
     * @param currentFrame Current frame
     * @return ActionListener for the button
     */
    public static java.awt.event.ActionListener createNavigationListener(
        String destination, JFrame currentFrame) {
    
        return e -> {
            switch (destination.toUpperCase()) {
                case "DASHBOARD":
                case "NAV_DASHBOARD":
                    navigateToDashboard(currentFrame);
                    break;
                    
                case "CREATE GROUP":
                case "CREATE GROUPS":
                case "NAV_CREATE_GROUP":
                    navigateToCreateGroup(currentFrame);
                    break;
                    
                case "GROUPS":
                case "NAV_GROUPS":
                    navigateToGroups(currentFrame);
                    break;
                    
                case "LOCATION":
                case "NAV_LOCATION":
                    navigateToLocation(currentFrame);
                    break;
                    
                case "STUDY POINTS":
                case "NAV_STUDY_POINTS":
                    navigateToStudyPoints(currentFrame);
                    break;
                    
                case "MESSAGES":
                case "NAV_MESSAGES":
                    navigateToMessages(currentFrame);
                    break;
                    
                case "PROFILE":
                case "NAV_PROFILE":
                    navigateToProfile(currentFrame);
                    break;
                    
                default:
                    JOptionPane.showMessageDialog(
                        currentFrame,
                        destination + " feature is under construction.",
                        "Under Construction",
                        JOptionPane.INFORMATION_MESSAGE
                    );
            }
        };
    }
    
    /**
     * Confirm navigation if there are unsaved changes
     * @param currentFrame Current frame
     * @param hasUnsavedChanges Whether there are unsaved changes
     * @param destination Navigation destination
     * @return true if navigation should proceed
     */
    public static boolean confirmNavigation(JFrame currentFrame, boolean hasUnsavedChanges, String destination) {
        if (!hasUnsavedChanges) {
            return true;
        }
        
        int result = JOptionPane.showConfirmDialog(
            currentFrame,
            "You have unsaved changes.\n\n" +
            "Are you sure you want to navigate to " + destination + "?",
            "Unsaved Changes",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        return result == JOptionPane.YES_OPTION;
    }
}