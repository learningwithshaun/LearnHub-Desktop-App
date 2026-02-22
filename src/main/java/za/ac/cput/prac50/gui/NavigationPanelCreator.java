package za.ac.cput.prac50.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Utility class to create consistent navigation panels across all subsystems
 * 
 * @author LearnHub Team
 */
public class NavigationPanelCreator {
    
    // Color constants
    private static final Color SELECTED_BLUE = new Color(70, 130, 180);
    private static final Color GREY = Color.GRAY;
    
    /**
     * Create navigation panel with proper button styling and actions
     * 
     * @param currentFrame The frame containing this navigation panel
     * @param activeTab Which tab should be highlighted (e.g., "GROUPS", "LOCATION")
     * @return Configured JPanel with navigation buttons
     */
    public static JPanel createNavigationPanel(JFrame currentFrame, String activeTab) {
        JPanel navPanel = new JPanel(new GridLayout(1, 6, 5, 0));
        navPanel.setBackground(Color.WHITE);
        navPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        navPanel.setPreferredSize(new Dimension(0, 45));
        
        String[] buttonTexts = {
            "DASHBOARD", 
            "CREATE GROUPS", 
            "GROUPS", 
            "LOCATION", 
            "MESSAGES", 
            "STUDY POINTS"
        };
        
        for (String text : buttonTexts) {
            JButton button = createNavButton(text, currentFrame, text.equals(activeTab));
            navPanel.add(button);
        }
        
        return navPanel;
    }
    
    /**
     * Create a single navigation button with proper styling
     * 
     * @param text Button text
     * @param currentFrame Current frame (for navigation)
     * @param isActive Whether this is the active/selected tab
     * @return Configured JButton
     */
    private static JButton createNavButton(String text, JFrame currentFrame, boolean isActive) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", isActive ? Font.BOLD : Font.PLAIN, 11));
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        
        if (isActive) {
            // Active tab styling
            button.setForeground(SELECTED_BLUE);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 10, 3, 10),
                BorderFactory.createMatteBorder(0, 0, 2, 0, SELECTED_BLUE)
            ));
        } else {
            // Inactive tab styling
            button.setForeground(GREY);
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            
            // Hover effect for inactive tabs
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setForeground(Color.BLACK);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    button.setForeground(GREY);
                }
            });
        }
        
        // Add navigation action
        button.addActionListener(NavigationManager.createNavigationListener(text, currentFrame));
        
        return button;
    }
    
    /**
     * Create navigation panel with bounds (for absolute positioning)
     * 
     * @param currentFrame The frame containing this navigation panel
     * @param activeTab Which tab should be highlighted
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Panel width
     * @param height Panel height
     * @return Configured JPanel with navigation buttons and bounds set
     */
    public static JPanel createNavigationPanelWithBounds(
            JFrame currentFrame, String activeTab, int x, int y, int width, int height) {
        
        JPanel navPanel = createNavigationPanel(currentFrame, activeTab);
        navPanel.setBounds(x, y, width, height);
        return navPanel;
    }
}