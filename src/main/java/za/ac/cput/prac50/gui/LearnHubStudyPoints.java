package za.ac.cput.prac50.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import za.ac.cput.prac50.connection.UnifiedDBConnection;
import za.ac.za.cput.prac50.domain.UnifiedStudent;
import za.ac.za.cput.prac50.domain.Venue;

/**
 * LearnHubStudyPoints - WITH SESSION MANAGEMENT
 * Uses SessionManager for logged-in student info
 * Uses HeaderPanelCreator and NavigationPanelCreator for consistency
 * @author noroy - Updated with SessionManager
 */
public class LearnHubStudyPoints extends JFrame implements ActionListener {
    
    // Color constants
    private static final Color LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color BUTTON_BLUE = new Color(135, 206, 235);
    private static final Color SELECTED_BLUE = new Color(70, 130, 180);
    private static final Color GREY_BACKGROUND = new Color(240, 240, 240);
    private static final Color SUCCESS_GREEN = new Color(76, 175, 80);
    
    // Emoji-supporting font
    private static Font emojiFont;
    
    // Current student from session
    private UnifiedStudent currentStudent;
    private UnifiedDBConnection dbConnection;
    private Map<String, Venue> venues;
    private JPanel mainContentPanel;
    private JPanel detailsPanel;
    
    public LearnHubStudyPoints() {
        // Initialize emoji font first
        initializeEmojiFont();
        
        // Get current student from session
        currentStudent = SessionManager.getInstance().getCurrentStudent();
        if (currentStudent == null) {
            System.out.println("⚠ Warning: No student in session, using default");
            currentStudent = SessionManager.getInstance().getDefaultStudent();
        }
        System.out.println("✓ LearnHubStudyPoints loaded for: " + currentStudent.getFullName());
        
        // Initialize database and venues
        dbConnection = UnifiedDBConnection.getInstance();
        initializeVenues();
        
        // Setup UI
        setupUI();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (dbConnection != null) {
                dbConnection.closeConnection();
            }
        }));
    }
    
    /**
     * Initialize a font that supports emoji rendering
     */
    private void initializeEmojiFont() {
        String[] emojiSupportingFonts = {
            "Segoe UI Emoji",        // Windows
            "Apple Color Emoji",     // macOS
            "Noto Color Emoji",      // Linux
            "Symbola",               // Fallback
            "Arial Unicode MS"       // Fallback
        };
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();
        
        for (String fontName : emojiSupportingFonts) {
            for (String availableFont : availableFonts) {
                if (availableFont.equals(fontName)) {
                    emojiFont = new Font(fontName, Font.PLAIN, 14);
                    return;
                }
            }
        }
        
        // Fallback to default
        emojiFont = new Font("Dialog", Font.PLAIN, 14);
    }
    
    private void initializeVenues() {
        venues = new HashMap<>();

        // Load venues from database
        try {
            Connection conn = dbConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT venue_name, capacity, current_usage, study_types, operating_hours, amenities " +
                "FROM venues ORDER BY venue_name"
            );

            while (rs.next()) {
                String name = rs.getString("venue_name");
                int capacity = rs.getInt("capacity");
                int currentUsage = rs.getInt("current_usage");
                String studyTypesStr = rs.getString("study_types");
                String operatingHours = rs.getString("operating_hours");
                String amenitiesStr = rs.getString("amenities");

                // Parse comma-separated strings
                String[] studyTypes = studyTypesStr != null ? studyTypesStr.split(",") : new String[]{};
                String[] amenities = amenitiesStr != null ? amenitiesStr.split(",") : new String[]{};

                // Trim whitespace
                for (int i = 0; i < studyTypes.length; i++) {
                    studyTypes[i] = studyTypes[i].trim();
                }
                for (int i = 0; i < amenities.length; i++) {
                    amenities[i] = amenities[i].trim();
                }

                Venue venue = new Venue(name, capacity, currentUsage, studyTypes, operatingHours, amenities);
                venues.put(name, venue);
            }

            rs.close();
            stmt.close();

            System.out.println("✅ Loaded " + venues.size() + " venues from database");

        } catch (SQLException e) {
            System.err.println("❌ Error loading venues: " + e.getMessage());
            e.printStackTrace();

            // Fallback to sample data if database fails
            loadSampleVenues();
        }
    }

    // Keep as fallback
    private void loadSampleVenues() {
        venues.put("D6 Library Seminar Room", new Venue(
            "D6 Library Seminar Room", 25, 25, 
            new String[]{"Silent Study"}, "07:00 - 22:00",
            new String[]{"Wi-Fi", "Power Outlets", "Air Conditioning"}
        ));

        venues.put("Engineering Building Room 1.19", new Venue(
            "Engineering Building Room 1.19", 50, 10, 
            new String[]{"Group Discussion", "Silent Study"}, "07:00 - 22:00",
            new String[]{"Wi-Fi", "Power Outlets", "Whiteboard"}
        ));

        System.out.println("⚠️ Using sample venue data (database unavailable)");
    }
    
    private void setupUI() {
        setTitle("LearnHub - Study Points");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(870, 700);
        setResizable(true);
        
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(Color.WHITE);
        
        // Use HeaderPanelCreator
        JPanel headerSection = new JPanel(new BorderLayout());
        headerSection.setBackground(Color.WHITE);
        headerSection.add(HeaderPanelCreator.createHeader(), BorderLayout.NORTH);
        
        // Use NavigationPanelCreator with "STUDY POINTS" active
        headerSection.add(NavigationPanelCreator.createNavigationPanel(this, "STUDY POINTS"), BorderLayout.SOUTH);
        
        mainContainer.add(headerSection, BorderLayout.NORTH);
        
        // Main content area
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // Title panel with Add button
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("SUGGESTED STUDY POINTS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(11, 44, 77));
        
        // Add Venue button
        JButton addVenueButton = createStyledButton("+ ADD VENUE", SUCCESS_GREEN, 12);
        addVenueButton.setPreferredSize(new Dimension(120, 35));
        addVenueButton.addActionListener(e -> showAddVenueDialog());
        
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(addVenueButton, BorderLayout.EAST);
        titlePanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        contentPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Main content and details panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.6);
        
        mainContentPanel = createVenuesList();
        detailsPanel = createDetailsPanel();
        
        splitPane.setLeftComponent(new JScrollPane(mainContentPanel));
        splitPane.setRightComponent(new JScrollPane(detailsPanel));
        
        contentPanel.add(splitPane, BorderLayout.CENTER);
        mainContainer.add(contentPanel, BorderLayout.CENTER);
        
        // Footer
        mainContainer.add(createFooter(), BorderLayout.SOUTH);
        
        add(mainContainer);
        setLocationRelativeTo(null);
    }
    
    private JPanel createVenuesList() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        
        for (String venueName : venues.keySet()) {
            content.add(createVenueCard(venueName));
            content.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        
        return content;
    }
    
    private JPanel createVenueCard(String venueName) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(GREY_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setPreferredSize(new Dimension(450, 80));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        Venue venue = venues.get(venueName);
        int availableSeats = venue.getCapacity() - venue.getCurrentUsage();
        
        // Venue name
        JLabel nameLabel = new JLabel(venueName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(new Color(11, 44, 77));
        
        // Availability info
        String availabilityText = availableSeats + " seats available";
        JLabel availabilityLabel = new JLabel(availabilityText);
        availabilityLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        availabilityLabel.setForeground(availableSeats > 0 ? Color.GREEN.darker() : Color.RED);
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.add(nameLabel, BorderLayout.NORTH);
        infoPanel.add(availabilityLabel, BorderLayout.SOUTH);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false);
        
        JButton viewDetailsBtn = createStyledButton("DETAILS", BUTTON_BLUE, 12);
        viewDetailsBtn.setPreferredSize(new Dimension(100, 30));
        viewDetailsBtn.addActionListener(e -> showVenueDetails(venueName));

        // View on Map button with emoji
        JButton viewMapBtn = new JButton("🗺️ VIEW MAP");
        viewMapBtn.setFont(emojiFont.deriveFont(Font.BOLD, 10f));
        viewMapBtn.setBackground(new Color(255, 152, 0));
        viewMapBtn.setForeground(Color.WHITE);
        viewMapBtn.setPreferredSize(new Dimension(120, 30));
        viewMapBtn.setFocusPainted(false);
        viewMapBtn.setBorderPainted(false);
        viewMapBtn.setOpaque(true);
        viewMapBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewMapBtn.setBorder(new EmptyBorder(5, 10, 5, 10));
        viewMapBtn.setToolTipText("Show this venue's location on the map");
        viewMapBtn.addActionListener(e -> {
            NavigationManager.navigateToLocationWithVenue(this, venueName);
        });
        
        // Add hover effect
        viewMapBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                viewMapBtn.setBackground(new Color(255, 152, 0).darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                viewMapBtn.setBackground(new Color(255, 152, 0));
            }
        });
        
        JButton bookBtn = createStyledButton("BOOK", SELECTED_BLUE, 12);
        bookBtn.setPreferredSize(new Dimension(80, 30));
        bookBtn.addActionListener(e -> bookVenue(venueName));
        
        // Disable book button if no seats available
        if (availableSeats == 0) {
            bookBtn.setEnabled(false);
            bookBtn.setBackground(Color.GRAY);
        }
        
        buttonPanel.add(viewDetailsBtn);
        buttonPanel.add(viewMapBtn);
        buttonPanel.add(bookBtn);
        
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.EAST);
        
        return card;
    }
    
    private JPanel createDetailsPanel() {
        JPanel details = new JPanel();
        details.setLayout(new BorderLayout());
        details.setBackground(Color.WHITE);
        details.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Default message when no venue is selected
        JLabel defaultLabel = new JLabel(
            "<html><center>Select a venue to view details<br/><br/>" +
            "• Capacity<br/>" +
            "• Current usage<br/>" +
            "• Study types<br/>" +
            "• Operating hours<br/>" +
            "• Amenities</center></html>", 
            SwingConstants.CENTER
        );
        defaultLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        defaultLabel.setForeground(Color.GRAY);
        details.add(defaultLabel, BorderLayout.CENTER);
        
        return details;
    }
    
    private void showVenueDetails(String venueName) {
        detailsPanel.removeAll();
        detailsPanel.setLayout(new BorderLayout());
        
        Venue venue = venues.get(venueName);
        int availableSeats = venue.getCapacity() - venue.getCurrentUsage();
        
        JPanel detailsContent = new JPanel();
        detailsContent.setLayout(new BoxLayout(detailsContent, BoxLayout.Y_AXIS));
        detailsContent.setBackground(Color.WHITE);
        detailsContent.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Venue name
        JLabel nameLabel = new JLabel(venueName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(new Color(11, 44, 77));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsContent.add(nameLabel);
        detailsContent.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Capacity info
        addDetailRow(detailsContent, "Capacity:", venue.getCapacity() + " seats");
        addDetailRow(detailsContent, "Current Usage:", venue.getCurrentUsage() + "/" + venue.getCapacity() + " occupied");
        addDetailRow(detailsContent, "Available Seats:", availableSeats + " seats");
        
        // Study types
        addDetailRow(detailsContent, "Study Types:", venue.getStudyTypesAsString());
        
        // Operating hours
        addDetailRow(detailsContent, "Operating Hours:", venue.getOperatingHours());
        
        // Amenities
        addDetailRow(detailsContent, "Amenities:", venue.getAmenitiesAsString());
        
        detailsContent.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Availability status with emoji
        JLabel statusLabel = new JLabel();
        statusLabel.setFont(emojiFont.deriveFont(Font.BOLD, 14f));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        if (availableSeats > 0) {
            statusLabel.setText("✓ Available - " + availableSeats + " seats free");
            statusLabel.setForeground(Color.GREEN.darker());
        } else {
            statusLabel.setText("✗ Fully Booked");
            statusLabel.setForeground(Color.RED);
        }
        
        detailsContent.add(statusLabel);
        detailsContent.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Book button
        JButton bookButton = createStyledButton("BOOK SPOT", SELECTED_BLUE, 14);
        bookButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        bookButton.setPreferredSize(new Dimension(120, 35));
        bookButton.addActionListener(e -> bookVenue(venueName));
        
        if (availableSeats == 0) {
            bookButton.setEnabled(false);
            bookButton.setBackground(Color.GRAY);
        }
        
        detailsContent.add(bookButton);
        
        detailsContent.add(Box.createRigidArea(new Dimension(0, 10)));

        // View on Map button in details with emoji
        JButton viewMapButton = new JButton("📍 VIEW ON MAP");
        viewMapButton.setFont(emojiFont.deriveFont(Font.BOLD, 14f));
        viewMapButton.setBackground(new Color(255, 152, 0));
        viewMapButton.setForeground(Color.WHITE);
        viewMapButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        viewMapButton.setPreferredSize(new Dimension(160, 35));
        viewMapButton.setFocusPainted(false);
        viewMapButton.setBorderPainted(false);
        viewMapButton.setOpaque(true);
        viewMapButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewMapButton.setBorder(new EmptyBorder(8, 15, 8, 15));
        viewMapButton.setToolTipText("Open map filtered to this venue");
        viewMapButton.addActionListener(e -> {
            NavigationManager.navigateToLocationWithVenue(this, venueName);
        });
        
        // Add hover effect
        viewMapButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                viewMapButton.setBackground(new Color(255, 152, 0).darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                viewMapButton.setBackground(new Color(255, 152, 0));
            }
        });

        detailsContent.add(viewMapButton);
        
        detailsPanel.add(new JScrollPane(detailsContent), BorderLayout.CENTER);
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }
    
    private void addDetailRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.BOLD, 12));
        labelComp.setPreferredSize(new Dimension(120, 20));
        
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Arial", Font.PLAIN, 12));
        
        row.add(labelComp);
        row.add(valueComp);
        panel.add(row);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
    }
    
    private void bookVenue(String venueName) {
        Venue venue = venues.get(venueName);
        int availableSeats = venue.getCapacity() - venue.getCurrentUsage();
        
        if (availableSeats > 0) {
            int result = JOptionPane.showConfirmDialog(
                this,
                "Book a spot at " + venueName + "?\n\n" +
                "Available seats: " + availableSeats + "\n" +
                "Operating Hours: " + venue.getOperatingHours() + "\n\n" +
                "Confirm booking?",
                "Confirm Booking",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                // Navigate to map to show location
                NavigationManager.navigateToLocation(this);
            }
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Sorry, " + venueName + " is fully booked.\n\n" +
                "Please try another venue or check back later.",
                "Booking Unavailable",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }
    
    // ===== ADD VENUE FUNCTIONALITY =====
    
    private void showAddVenueDialog() {
        JDialog addVenueDialog = new JDialog(this, "Add New Venue", true);
        addVenueDialog.setLayout(new BorderLayout());
        addVenueDialog.setSize(500, 600);
        addVenueDialog.setLocationRelativeTo(this);
        addVenueDialog.setResizable(true);  // ✅ FIXED: Made resizable
        addVenueDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);  // ✅ FIXED: Close on X button
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Add New Study Venue");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(11, 44, 77));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(titleLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Venue Name
        JTextField nameField = createFormField("Venue Name:", "Enter venue name");
        formPanel.add(createFormRow("Venue Name:", nameField));
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Capacity
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(20, 1, 500, 1));
        ((JSpinner.DefaultEditor) capacitySpinner.getEditor()).getTextField().setBackground(Color.WHITE);
        formPanel.add(createFormRow("Capacity:", capacitySpinner));
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Current Usage
        JSpinner usageSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 500, 1));
        ((JSpinner.DefaultEditor) usageSpinner.getEditor()).getTextField().setBackground(Color.WHITE);
        formPanel.add(createFormRow("Current Usage:", usageSpinner));
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Operating Hours
        JTextField hoursField = createFormField("Operating Hours:", "e.g., 07:00 - 22:00");
        formPanel.add(createFormRow("Operating Hours:", hoursField));
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Study Types
        JPanel studyTypesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        studyTypesPanel.setBackground(Color.WHITE);
        studyTypesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel studyTypesLabel = new JLabel("Study Types:");
        studyTypesLabel.setFont(new Font("Arial", Font.BOLD, 12));
        studyTypesLabel.setPreferredSize(new Dimension(120, 20));
        
        String[] studyTypeOptions = {"Silent Study", "Group Discussion", "Individual Study", "Presentation Practice"};
        JCheckBox[] studyTypeCheckboxes = new JCheckBox[studyTypeOptions.length];
        
        for (int i = 0; i < studyTypeOptions.length; i++) {
            studyTypeCheckboxes[i] = new JCheckBox(studyTypeOptions[i]);
            studyTypeCheckboxes[i].setBackground(Color.WHITE);
            studyTypesPanel.add(studyTypeCheckboxes[i]);
        }
        
        JPanel studyRow = new JPanel(new BorderLayout());
        studyRow.setBackground(Color.WHITE);
        studyRow.add(studyTypesLabel, BorderLayout.WEST);
        studyRow.add(studyTypesPanel, BorderLayout.CENTER);
        formPanel.add(studyRow);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Amenities
        JTextArea amenitiesArea = new JTextArea(3, 20);
        amenitiesArea.setLineWrap(true);
        amenitiesArea.setWrapStyleWord(true);
        amenitiesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        amenitiesArea.setFont(new Font("Arial", Font.PLAIN, 12));
        amenitiesArea.setBackground(Color.WHITE);
        
        JScrollPane amenitiesScroll = new JScrollPane(amenitiesArea);
        amenitiesScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel amenitiesLabel = new JLabel("Amenities (one per line):");
        amenitiesLabel.setFont(new Font("Arial", Font.BOLD, 12));
        amenitiesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        formPanel.add(amenitiesLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(amenitiesScroll);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton cancelButton = createStyledButton("CANCEL", Color.GRAY, 12);
        cancelButton.addActionListener(e -> addVenueDialog.dispose());
        
        JButton saveButton = createStyledButton("SAVE VENUE", new Color(24, 144, 255), 12);
        saveButton.addActionListener(e -> {
            if (saveNewVenue(nameField, capacitySpinner, usageSpinner, 
                            hoursField, studyTypeCheckboxes, amenitiesArea)) {
                addVenueDialog.dispose();
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        formPanel.add(buttonPanel);
        
        addVenueDialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        addVenueDialog.setVisible(true);
    }

    private JTextField createFormField(String label, String placeholder) {
        JTextField field = new JTextField(20);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setBackground(Color.WHITE);
        return field;
    }

    private JPanel createFormRow(String label, JComponent component) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.BOLD, 12));
        labelComp.setPreferredSize(new Dimension(120, 25));
        
        row.add(labelComp, BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        
        return row;
    }

    private boolean saveNewVenue(JTextField nameField, JSpinner capacitySpinner, JSpinner usageSpinner,
                               JTextField hoursField, JCheckBox[] studyTypeCheckboxes, JTextArea amenitiesArea) {
        // Validation
        String venueName = nameField.getText().trim();
        if (venueName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a venue name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (venues.containsKey(venueName)) {
            JOptionPane.showMessageDialog(this, "A venue with this name already exists.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        int capacity = (Integer) capacitySpinner.getValue();
        int currentUsage = (Integer) usageSpinner.getValue();
        
        if (currentUsage > capacity) {
            JOptionPane.showMessageDialog(this, "Current usage cannot exceed capacity.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        String operatingHours = hoursField.getText().trim();
        if (operatingHours.isEmpty()) {
            operatingHours = "07:00 - 22:00";
        }

        // Get selected study types
        List<String> selectedStudyTypes = new ArrayList<>();
        for (JCheckBox checkbox : studyTypeCheckboxes) {
            if (checkbox.isSelected()) {
                selectedStudyTypes.add(checkbox.getText());
            }
        }

        if (selectedStudyTypes.isEmpty()) {
            selectedStudyTypes.add("Individual Study");
        }

        // Get amenities
        String amenitiesText = amenitiesArea.getText().trim();
        String[] amenities;
        if (amenitiesText.isEmpty()) {
            amenities = new String[]{"Wi-Fi", "Power Outlets"};
        } else {
            amenities = amenitiesText.split("\\r?\\n");
            for (int i = 0; i < amenities.length; i++) {
                amenities[i] = amenities[i].trim();
            }
        }

        // Save to database
        try {
            Connection conn = dbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO venues (venue_name, capacity, current_usage, study_types, operating_hours, amenities) " +
                "VALUES (?, ?, ?, ?, ?, ?)"
            );

            stmt.setString(1, venueName);
            stmt.setInt(2, capacity);
            stmt.setInt(3, currentUsage);
            stmt.setString(4, String.join(", ", selectedStudyTypes));
            stmt.setString(5, operatingHours);
            stmt.setString(6, String.join(", ", amenities));

            stmt.executeUpdate();
            stmt.close();

            System.out.println("✅ Venue saved to database: " + venueName);

            // Add to memory map
            Venue newVenue = new Venue(
                venueName, capacity, currentUsage,
                selectedStudyTypes.toArray(new String[0]),
                operatingHours, amenities
            );
            venues.put(venueName, newVenue);

            refreshVenueCards();

            JOptionPane.showMessageDialog(this,
                "Venue '" + venueName + "' has been successfully added!",
                "Venue Added",
                JOptionPane.INFORMATION_MESSAGE);

            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Database error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }
    
    private void refreshVenueCards() {
        mainContentPanel.removeAll();
        
        for (String venueName : venues.keySet()) {
            mainContentPanel.add(createVenueCard(venueName));
            mainContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
    
    // ===== UTILITY METHODS =====
    
    private JButton createStyledButton(String text, Color bgColor, int fontSize) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, fontSize));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));

        JLabel copyrightLabel = new JLabel("© 2025 LearnHub - All Rights Reserved");
        copyrightLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        copyrightLabel.setForeground(Color.GRAY);

        footer.add(copyrightLabel);
        return footer;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // Navigation is handled by NavigationManager through NavigationPanelCreator
    }
}