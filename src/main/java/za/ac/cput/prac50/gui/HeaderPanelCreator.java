package za.ac.cput.prac50.gui;

import javax.swing.*;
import java.awt.*;
import za.ac.za.cput.prac50.domain.UnifiedStudent;

/**
 * Utility class to create consistent headers showing logged-in student info
 * 
 * @author LearnHub Team
 */
public class HeaderPanelCreator {
    
    private static final Color LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color SELECTED_BLUE = new Color(70, 130, 180);
    
    /**
     * Create header panel with current student information
     * Uses SessionManager to get logged-in student data
     * 
     * @return JPanel with header
     */
    public static JPanel createHeader() {
        // Get current student from session
        UnifiedStudent currentStudent = SessionManager.getInstance().getCurrentStudent();
        
        // If no student logged in, use default
        if (currentStudent == null) {
            currentStudent = SessionManager.getInstance().getDefaultStudent();
            System.out.println("⚠ Warning: No student in session, using default");
        }
        
        return createHeaderWithStudent(currentStudent);
    }
    
    /**
     * Create header panel with specific student information
     * 
     * @param student Student to display in header
     * @return JPanel with header
     */
    public static JPanel createHeaderWithStudent(UnifiedStudent student) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        headerPanel.setPreferredSize(new Dimension(0, 70));
        
        // Left section: User icon and details
        JPanel userDetailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        userDetailsPanel.setBackground(Color.WHITE);
        
        // Dynamic User Icon with first letter of name
        JPanel iconPanel = createAvatarIcon(student);
        
        // User text info panel with dynamic data
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(student.getFullName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel studentNoLabel = new JLabel(student.getStudentId());
        studentNoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        studentNoLabel.setForeground(Color.GRAY);
        studentNoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel emailLabel = new JLabel(student.getEmail());
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        emailLabel.setForeground(Color.GRAY);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(studentNoLabel);
        textPanel.add(emailLabel);
        
        userDetailsPanel.add(iconPanel);
        userDetailsPanel.add(textPanel);
        
        // Right section: Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        logoPanel.setBackground(Color.WHITE);
        
        JLabel logoLabel = createLogoLabel();
        logoPanel.add(logoLabel);
        
        headerPanel.add(userDetailsPanel, BorderLayout.WEST);
        headerPanel.add(logoPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Create avatar icon with student's first initial
     * 
     * @param student Student object
     * @return JPanel with circular avatar
     */
    private static JPanel createAvatarIcon(final UnifiedStudent student) {
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw circle
                g2d.setColor(LIGHT_BLUE);
                g2d.fillOval(0, 0, getWidth(), getHeight());
                
                // Draw initial
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 18));
                
                FontMetrics fm = g2d.getFontMetrics();
                String text = student.getFirstInitial();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);
            }
        };
        iconPanel.setPreferredSize(new Dimension(40, 40));
        iconPanel.setOpaque(false);
        
        return iconPanel;
    }
    
    /**
     * Create logo label
     * Tries to load image, falls back to text
     * 
     * @return JLabel with logo or text
     */
    private static JLabel createLogoLabel() {
        JLabel logoLabel;
        try {
            java.net.URL logoUrl = HeaderPanelCreator.class.getResource("images/learnHub_Logo.png");
            if (logoUrl != null) {
                ImageIcon originalIcon = new ImageIcon(logoUrl);
                Image scaledImage = originalIcon.getImage().getScaledInstance(150, 50, Image.SCALE_SMOOTH);
                logoLabel = new JLabel(new ImageIcon(scaledImage));
            } else {
                logoLabel = new JLabel("LEARN HUB");
                logoLabel.setFont(new Font("Arial", Font.BOLD, 18));
                logoLabel.setForeground(SELECTED_BLUE);
            }
        } catch (Exception e) {
            logoLabel = new JLabel("LEARN HUB");
            logoLabel.setFont(new Font("Arial", Font.BOLD, 18));
            logoLabel.setForeground(SELECTED_BLUE);
        }
        
        return logoLabel;
    }
    
    /**
     * Create header with bounds (for absolute positioning)
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Panel width
     * @param height Panel height
     * @return JPanel with header and bounds set
     */
    public static JPanel createHeaderWithBounds(int x, int y, int width, int height) {
        JPanel header = createHeader();
        header.setBounds(x, y, width, height);
        return header;
    }
    
    /**
     * Update header with new student information
     * Use this when student data changes
     * 
     * @param headerPanel Existing header panel to update
     * @param student Updated student object
     */
    public static void updateHeader(JPanel headerPanel, UnifiedStudent student) {
        // Remove all components
        headerPanel.removeAll();
        
        // Recreate with new student data
        JPanel newHeader = createHeaderWithStudent(student);
        
        // Copy components to existing panel
        headerPanel.setLayout(newHeader.getLayout());
        Component[] components = newHeader.getComponents();
        for (Component comp : components) {
            headerPanel.add(comp);
        }
        
        // Refresh display
        headerPanel.revalidate();
        headerPanel.repaint();
    }
    
    /**
     * Create a logout button for the header
     * 
     * @param currentFrame Frame to close on logout
     * @return JButton configured for logout
     */
    public static JButton createLogoutButton(JFrame currentFrame) {
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        logoutBtn.setBackground(new Color(220, 53, 69));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        logoutBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                currentFrame,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                // Clear session
                SessionManager.getInstance().logout();
                
                // Close current frame
                currentFrame.dispose();
                
                // Open login page
                new LoginPage().setVisible(true);
            }
        });
        
        return logoutBtn;
    }
}