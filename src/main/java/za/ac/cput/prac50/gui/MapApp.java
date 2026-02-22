package za.ac.cput.prac50.gui;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import za.ac.cput.prac50.connection.UnifiedDBConnection;
import za.ac.za.cput.prac50.domain.UnifiedStudent;
import za.ac.za.cput.prac50.domain.StudyLocation;
import za.ac.za.cput.prac50.domain.StudyLocationWaypoint;

/**
 * MapApp - Study Groups Location Search WITH SESSION MANAGEMENT
 * Shows real study groups and venues on map
 */
public class MapApp extends JFrame implements ActionListener {
    private final Color LIGHT_BLUE = new Color(173, 216, 230);
    private final Color BUTTON_BLUE = new Color(24, 144, 255);
    private final Color SELECTED_BLUE = new Color(70, 130, 180);
    private final Color GREY_BACKGROUND = new Color(240, 240, 240);
    private final Color BUTTON_YELLOW = new Color(255, 215, 0); // Yellow color

    // Emoji-supporting font
    private static Font emojiFont;
    
    private boolean isLocationSelectionMode = false;
    private JButton setLocationButton;
    private JPanel locationModePanel;

    private JPanel mapPanel;
    private JScrollPane groupsScrollPane;
    private boolean mapExpanded = false;
    private final int normalMapHeight = 200;
    private final int expandedMapHeight = 350;
    private final int normalFrameHeight = 680;
    private final int expandedFrameHeight = 830;
    private boolean isGridView = false;
    private JButton listBtn, gridBtn;
    private JComboBox<String> sortCombo;
    private JButton expandButton;

    // JXMapViewer components
    private JXMapViewer mapViewer;
    private WaypointPainter<StudyLocationWaypoint> waypointPainter;
    private Set<StudyLocationWaypoint> waypoints;

    // Movement control variables
    private boolean isDragging = false;
    private Point lastMousePoint;

    // User's current location
    private double userLatitude = -33.93080102488844;
    private double userLongitude = 18.430230425585137;
    private GeoPosition userPosition;

    // List to store study locations from database
    private List<StudyLocation> studyLocations;

    // Database connection
    private UnifiedDBConnection dbConnection;

    // Current student from session
    private UnifiedStudent currentStudent;
    
    // Venue filter
    private JButton clearFilterButton;
    private String venueFilter = null;

    public MapApp() {
        // Initialize emoji font first
        initializeEmojiFont();
        
        setTitle("LearnHub - Study Groups Location Search");
        setSize(890, normalFrameHeight);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setResizable(true);

        // Get current student from session
        currentStudent = SessionManager.getInstance().getCurrentStudent();
        if (currentStudent == null) {
            System.out.println("⚠ Warning: No student in session, using default");
            currentStudent = SessionManager.getInstance().getDefaultStudent();
        }
        System.out.println("✓ MapApp loaded for: " + currentStudent.getFullName());

        // Initialize database
        dbConnection = UnifiedDBConnection.getInstance();

        // Load user location from DB
        loadUserLocationFromDatabase();
        userPosition = new GeoPosition(userLatitude, userLongitude);

        // Load study locations
        initializeDatabase();

        // Build UI
        setupUI();

        setLocationRelativeTo(null);
        
        // Don't show frame yet if there's a venue filter pending
        if (venueFilter == null) {
            setVisible(true);
        }
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
    
    /**
     * Filter map to show only locations at a specific venue
     */
    public void filterByVenue(String venueName) {
        this.venueFilter = venueName;
        
        if (clearFilterButton != null) {
            clearFilterButton.setVisible(true);
        }
        
        if (groupsScrollPane != null && sortCombo != null) {
            updateGroupsList();
            setupMapWaypoints();
            
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                    "Map filtered to show only locations at:\n" + venueName,
                    "Filter Applied",
                    JOptionPane.INFORMATION_MESSAGE);
            });
        }
    }

    private void setupUI() {
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(Color.WHITE);

        // Header section
        JPanel headerSection = new JPanel(new BorderLayout());
        headerSection.setBackground(Color.WHITE);
        headerSection.add(HeaderPanelCreator.createHeader(), BorderLayout.NORTH);
        headerSection.add(NavigationPanelCreator.createNavigationPanel(this, "LOCATION"), BorderLayout.SOUTH);
        
        mainContainer.add(headerSection, BorderLayout.NORTH);

        // Main content area
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Map section
        contentPanel.add(createMapSection(), BorderLayout.NORTH);

        // Groups section
        contentPanel.add(createGroupsSection(), BorderLayout.CENTER);

        mainContainer.add(contentPanel, BorderLayout.CENTER);

        // Footer
        mainContainer.add(createFooter(), BorderLayout.SOUTH);

        add(mainContainer);

        // Setup map interactions
        setupMouseControls();
    }

    // ===== MAP SECTION =====

    private JPanel createMapSection() {
        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.setBackground(Color.WHITE);
        mapContainer.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Map title panel
        JPanel mapTitlePanel = new JPanel(new BorderLayout());
        mapTitlePanel.setBackground(Color.LIGHT_GRAY);
        mapTitlePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        mapTitlePanel.setPreferredSize(new Dimension(0, 35));

        JLabel mapTitle = new JLabel("  STUDY GROUPS MAP");
        mapTitle.setFont(new Font("Arial", Font.BOLD, 12));

        expandButton = new JButton(mapExpanded ? "COLLAPSE" : "EXPAND");
        expandButton.setFont(new Font("Arial", Font.PLAIN, 10));
        expandButton.setBackground(BUTTON_BLUE);
        expandButton.setForeground(Color.BLACK);
        expandButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        expandButton.setFocusPainted(false);
        expandButton.addActionListener(e -> toggleMapSize());

        // Set Location button - YELLOW with emoji
        setLocationButton = new JButton("📍 SET MY LOCATION");
        setLocationButton.setFont(emojiFont.deriveFont(Font.BOLD, 10f));
        setLocationButton.setBackground(BUTTON_YELLOW);
        setLocationButton.setForeground(Color.BLACK);
        setLocationButton.setBorder(new EmptyBorder(5, 15, 5, 15));
        setLocationButton.setFocusPainted(false);
        setLocationButton.setOpaque(true);
        setLocationButton.setBorderPainted(false);
        setLocationButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        setLocationButton.addActionListener(e -> toggleLocationSelectionMode());
        
        // Add hover effect
        setLocationButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setLocationButton.setBackground(BUTTON_YELLOW.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                setLocationButton.setBackground(BUTTON_YELLOW);
            }
        });
        
        // Clear filter button with emoji
        clearFilterButton = new JButton("🔄 CLEAR FILTER");
        clearFilterButton.setFont(emojiFont.deriveFont(Font.BOLD, 10f));
        clearFilterButton.setBackground(new Color(76, 175, 80));
        clearFilterButton.setForeground(Color.WHITE);
        clearFilterButton.setBorder(new EmptyBorder(5, 15, 5, 15));
        clearFilterButton.setFocusPainted(false);
        clearFilterButton.setOpaque(true);
        clearFilterButton.setBorderPainted(false);
        clearFilterButton.setVisible(venueFilter != null);
        clearFilterButton.addActionListener(e -> {
            venueFilter = null;
            clearFilterButton.setVisible(false);
            updateGroupsList();
            setupMapWaypoints();
            JOptionPane.showMessageDialog(this,
                "Filter cleared. Showing all locations.",
                "Filter Cleared",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        // Add hover effect to clear filter button
        clearFilterButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                clearFilterButton.setBackground(new Color(76, 175, 80).darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                clearFilterButton.setBackground(new Color(76, 175, 80));
            }
        });

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        rightControls.setBackground(Color.LIGHT_GRAY);
        rightControls.add(clearFilterButton);
        rightControls.add(setLocationButton);
        rightControls.add(expandButton);

        mapTitlePanel.add(mapTitle, BorderLayout.WEST);
        mapTitlePanel.add(rightControls, BorderLayout.EAST);

        // Location mode panel
        createLocationModePanel();

        // Map viewer
        mapPanel = new JPanel(new BorderLayout());
        mapPanel.setBackground(Color.WHITE);
        mapPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        mapPanel.setPreferredSize(new Dimension(0, normalMapHeight));

        mapViewer = new JXMapViewer();
        TileFactoryInfo info = new OSMTileFactoryInfo(
                "LearnHub",
                "https://tile.openstreetmap.org"
        );
        
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        System.setProperty("http.agent", "LearnHub/1.0 Java");
        mapViewer.setTileFactory(tileFactory);
        mapViewer.setZoom(8);
        mapViewer.setAddressLocation(userPosition);

        waypointPainter = new StudyLocationWaypointPainter();
        waypoints = new HashSet<>();
        setupMapWaypoints();

        List<Painter<JXMapViewer>> painters = new ArrayList<>();
        painters.add(waypointPainter);
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(painter);

        mapPanel.add(mapViewer, BorderLayout.CENTER);

        mapContainer.add(mapTitlePanel, BorderLayout.NORTH);
        mapContainer.add(mapPanel, BorderLayout.CENTER);

        return mapContainer;
    }

    private void createLocationModePanel() {
        locationModePanel = new JPanel(new BorderLayout());
        locationModePanel.setBackground(new Color(255, 255, 200));
        locationModePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 165, 0), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        locationModePanel.setVisible(false);
        locationModePanel.setPreferredSize(new Dimension(0, 40));

        // Instruction label with emoji
        JLabel instructionLabel = new JLabel("📍 LOCATION SELECTION MODE: Click on the map to set your location");
        instructionLabel.setFont(emojiFont.deriveFont(Font.BOLD, 12f));
        instructionLabel.setForeground(new Color(139, 69, 19));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(new Color(255, 255, 200));

        // Confirm button with emoji
        JButton confirmBtn = new JButton("✓ CONFIRM");
        confirmBtn.setFont(emojiFont.deriveFont(Font.BOLD, 10f));
        confirmBtn.setBackground(new Color(34, 139, 34));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBorder(new EmptyBorder(5, 15, 5, 15));
        confirmBtn.setFocusPainted(false);
        confirmBtn.setOpaque(true);
        confirmBtn.setBorderPainted(false);
        confirmBtn.addActionListener(e -> confirmLocationSelection());

        // Cancel button with emoji
        JButton cancelBtn = new JButton("✗ CANCEL");
        cancelBtn.setFont(emojiFont.deriveFont(Font.BOLD, 10f));
        cancelBtn.setBackground(new Color(220, 20, 60));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBorder(new EmptyBorder(5, 15, 5, 15));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setOpaque(true);
        cancelBtn.setBorderPainted(false);
        cancelBtn.addActionListener(e -> cancelLocationSelection());

        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);

        locationModePanel.add(instructionLabel, BorderLayout.WEST);
        locationModePanel.add(buttonPanel, BorderLayout.EAST);
    }

    // ===== GROUPS SECTION =====

    private JPanel createGroupsSection() {
        JPanel groupsContainer = new JPanel(new BorderLayout());
        groupsContainer.setBackground(Color.WHITE);
        groupsContainer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setPreferredSize(new Dimension(0, 30));

        JLabel groupsLabel = new JLabel("GROUPS NEARBY");
        groupsLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setBackground(Color.WHITE);

        listBtn = new JButton("LIST");
        listBtn.setPreferredSize(new Dimension(60, 25));
        listBtn.setBackground(new Color(24, 144, 255));
        listBtn.setFont(new Font("Arial", Font.PLAIN, 10));
        listBtn.setBorder(BorderFactory.createEmptyBorder());
        listBtn.setFocusPainted(false);
        listBtn.addActionListener(e -> switchToListView());

        gridBtn = new JButton("GRID");
        gridBtn.setPreferredSize(new Dimension(60, 25));
        gridBtn.setFont(new Font("Arial", Font.PLAIN, 10));
        gridBtn.setFocusPainted(false);
        gridBtn.addActionListener(e -> switchToGridView());
        
        // Set initial button states
        listBtn.setBackground(BUTTON_BLUE);
        listBtn.setForeground(Color.WHITE);
        gridBtn.setBackground(Color.WHITE);
        gridBtn.setForeground(Color.BLACK);
        gridBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        sortCombo = new JComboBox<>();
        sortCombo.addItem("SORT BY DISTANCE");
        sortCombo.addItem("SORT BY NAME");
        sortCombo.addItem("SORT BY MEMBERS");
        sortCombo.addItem("WITHIN 100 M");
        sortCombo.addItem("WITHIN 500 M");
        sortCombo.addItem("WITHIN 1 KM");
        sortCombo.setPreferredSize(new Dimension(140, 25));
        sortCombo.setFont(new Font("Arial", Font.PLAIN, 10));
        sortCombo.addActionListener(e -> updateGroupsList());

        controlsPanel.add(listBtn);
        controlsPanel.add(gridBtn);
        controlsPanel.add(sortCombo);

        headerPanel.add(groupsLabel, BorderLayout.WEST);
        headerPanel.add(controlsPanel, BorderLayout.EAST);

        groupsContainer.add(headerPanel, BorderLayout.NORTH);

        // Groups list
        createGroupsList();
        groupsContainer.add(groupsScrollPane, BorderLayout.CENTER);

        return groupsContainer;
    }

    private void createGroupsList() {
        if (isGridView) {
            createGridView();
        } else {
            createListView();
        }
    }

    private void createListView() {
        JPanel scrollableContent = new JPanel();
        scrollableContent.setLayout(new BoxLayout(scrollableContent, BoxLayout.Y_AXIS));
        scrollableContent.setBackground(GREY_BACKGROUND);
        scrollableContent.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        List<StudyLocation> locations = getFilteredAndSortedLocations();

        if (locations.isEmpty()) {
            JLabel emptyLabel = new JLabel("No groups or venues found");
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            scrollableContent.add(Box.createVerticalGlue());
            scrollableContent.add(emptyLabel);
            scrollableContent.add(Box.createVerticalGlue());
        } else {
            for (StudyLocation location : locations) {
                scrollableContent.add(createGroupItem(location));
                scrollableContent.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        // Remove old scroll pane
        Container parent = getGroupsSectionParent();
        if (parent != null && groupsScrollPane != null) {
            parent.remove(groupsScrollPane);
        }

        groupsScrollPane = new JScrollPane(scrollableContent);
        groupsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        groupsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        groupsScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        if (parent != null) {
            parent.add(groupsScrollPane, BorderLayout.CENTER);
            parent.revalidate();
            parent.repaint();
        }
    }

    private void createGridView() {
        JPanel scrollableContent = new JPanel(new GridLayout(0, 3, 15, 15));
        scrollableContent.setBackground(GREY_BACKGROUND);
        scrollableContent.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        List<StudyLocation> locations = getFilteredAndSortedLocations();

        if (locations.isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(GREY_BACKGROUND);
            
            JLabel emptyLabel = new JLabel("No groups or venues found", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            emptyLabel.setForeground(Color.GRAY);
            
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            scrollableContent.add(emptyPanel);
        } else {
            for (StudyLocation location : locations) {
                scrollableContent.add(createGridItem(location));
            }
        }

        // Remove old scroll pane
        Container parent = getGroupsSectionParent();
        if (parent != null && groupsScrollPane != null) {
            parent.remove(groupsScrollPane);
        }

        groupsScrollPane = new JScrollPane(scrollableContent);
        groupsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        groupsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        groupsScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        if (parent != null) {
            parent.add(groupsScrollPane, BorderLayout.CENTER);
            parent.revalidate();
            parent.repaint();
        }
    }

    private Container getGroupsSectionParent() {
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JPanel) {
                JPanel mainContainer = (JPanel) comp;
                for (Component contentComp : mainContainer.getComponents()) {
                    if (contentComp instanceof JPanel) {
                        JPanel contentPanel = (JPanel) contentComp;
                        for (Component sectionComp : contentPanel.getComponents()) {
                            if (sectionComp instanceof JPanel) {
                                JPanel panel = (JPanel) sectionComp;
                                if (panel.getLayout() instanceof BorderLayout) {
                                    Component[] children = panel.getComponents();
                                    for (Component child : children) {
                                        if (child == groupsScrollPane || child instanceof JScrollPane) {
                                            return panel;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        System.err.println("⚠️ Warning: Could not find groups section parent");
        return null;
    }

    private JPanel createGroupItem(StudyLocation location) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(Color.LIGHT_GRAY);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        // Icon
        JPanel iconPanel = new JPanel();
        iconPanel.setPreferredSize(new Dimension(40, 40));
        iconPanel.setBackground(Color.GRAY);
        iconPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        
        if (location.getGroupName().startsWith("VENUE:")) {
            iconPanel.setBackground(new Color(255, 152, 0));
        }

        // Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.LIGHT_GRAY);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JLabel nameLabel = new JLabel(location.getGroupName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel locationLabel = new JLabel("• " + formatDistance(location.getDistance()) + " - " + location.getBuilding());
        locationLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        locationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel membersLabel;
        if (location.getGroupName().startsWith("VENUE:")) {
            membersLabel = new JLabel(location.getMemberCount() + " SEATS AVAILABLE");
        } else {
            membersLabel = new JLabel(location.getMemberCount() + " MEMBERS");
        }
        membersLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        membersLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(locationLabel);
        infoPanel.add(membersLabel);

        // Button
        JButton messageBtn = new JButton(location.getGroupName().startsWith("VENUE:") ? "VIEW VENUE" : "MESSAGE");
        messageBtn.setBackground(BUTTON_BLUE);
        messageBtn.setForeground(Color.BLACK);
        messageBtn.setFont(new Font("Arial", Font.PLAIN, 10));
        messageBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        messageBtn.setFocusPainted(false);
        messageBtn.addActionListener(e -> {
            if (location.getGroupName().startsWith("VENUE:")) {
                String venueName = location.getBuilding();
                NavigationManager.navigateToStudyPoints(this); 
                JOptionPane.showMessageDialog(this,
                        "Navigating to Study Points for: " + venueName,
                        "View Venue", JOptionPane.INFORMATION_MESSAGE);
            } else {
                NavigationManager.navigateToMessages(this);
            }
        });

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.LIGHT_GRAY);
        leftPanel.add(iconPanel, BorderLayout.WEST);
        leftPanel.add(infoPanel, BorderLayout.CENTER);

        itemPanel.add(leftPanel, BorderLayout.CENTER);
        itemPanel.add(messageBtn, BorderLayout.EAST);

        return itemPanel;
    }

    private JPanel createGridItem(StudyLocation location) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.LIGHT_GRAY);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setPreferredSize(new Dimension(250, 120));

        // Icon
        JPanel iconPanel = new JPanel();
        iconPanel.setPreferredSize(new Dimension(50, 50));
        iconPanel.setBackground(Color.GRAY);
        iconPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        
        if (location.getGroupName().startsWith("VENUE:")) {
            iconPanel.setBackground(new Color(255, 152, 0));
        }

        // Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.LIGHT_GRAY);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel nameLabel = new JLabel(location.getGroupName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 11));

        JLabel locationLabel = new JLabel("• " + formatDistance(location.getDistance()));
        locationLabel.setFont(new Font("Arial", Font.PLAIN, 9));

        JLabel membersLabel;
        if (location.getGroupName().startsWith("VENUE:")) {
            membersLabel = new JLabel(location.getMemberCount() + " seats available");
        } else {
            membersLabel = new JLabel(location.getMemberCount() + " members");
        }
        membersLabel.setFont(new Font("Arial", Font.PLAIN, 9));

        infoPanel.add(nameLabel);
        infoPanel.add(locationLabel);
        infoPanel.add(membersLabel);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.LIGHT_GRAY);
        topPanel.add(iconPanel, BorderLayout.WEST);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        // Button
        JButton messageBtn = new JButton(location.getGroupName().startsWith("VENUE:") ? "VIEW VENUE" : "MESSAGE");
        messageBtn.setBackground(BUTTON_BLUE);
        messageBtn.setForeground(Color.BLACK);
        messageBtn.setFont(new Font("Arial", Font.PLAIN, 8));
        messageBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        messageBtn.setFocusPainted(false);
        messageBtn.addActionListener(e -> {
            if (location.getGroupName().startsWith("VENUE:")) {
                String venueName = location.getBuilding();
                NavigationManager.navigateToStudyPoints(this);
                JOptionPane.showMessageDialog(this,
                        "Navigating to Study Points for: " + venueName,
                        "View Venue", JOptionPane.INFORMATION_MESSAGE);
            } else {
                NavigationManager.navigateToMessages(this);
            }
        });

        card.add(topPanel, BorderLayout.NORTH);
        card.add(messageBtn, BorderLayout.SOUTH);

        return card;
    }

    // ===== DATABASE METHODS =====

    private void initializeDatabase() {
        studyLocations = new ArrayList<>();

        try {
            if (dbConnection.testConnection()) {
                studyLocations = loadStudyGroupsAsLocations(userLatitude, userLongitude);
                studyLocations.addAll(loadVenuesAsStudyLocations(userLatitude, userLongitude));

                for (StudyLocation location : studyLocations) {
                    location.setDistance(calculateDistance(userLatitude, userLongitude,
                            location.getLatitude(), location.getLongitude()));
                }

                System.out.println("✅ Loaded " + studyLocations.size() + " locations (groups + venues)");
            } else {
                throw new Exception("Database connection failed");
            }

        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            studyLocations = new ArrayList<>();
        }
    }

    private List<StudyLocation> loadStudyGroupsAsLocations(double userLat, double userLon) {
        List<StudyLocation> groupLocations = new ArrayList<>();
        
        Map<Integer, double[]> venueCoordinates = new HashMap<>();
        venueCoordinates.put(1, new double[]{-33.93050, 18.43081});
        venueCoordinates.put(2, new double[]{-33.93087, 18.42936});
        venueCoordinates.put(3, new double[]{-33.92893, 18.42840});
        venueCoordinates.put(4, new double[]{-33.93020, 18.42946});
        
        String sql = "SELECT sg.group_id, sg.group_name, sg.subject_code, sg.venue_id, " +
                    "COALESCE((SELECT COUNT(*) FROM memberships m WHERE m.group_id = sg.group_id), 0) as member_count, " +
                    "v.venue_name, v.latitude, v.longitude " +
                    "FROM study_groups sg " +
                    "LEFT JOIN venues v ON sg.venue_id = v.venue_id " +
                    "ORDER BY sg.group_name";

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int groupId = rs.getInt("group_id");
                String groupName = rs.getString("group_name");
                String subjectCode = rs.getString("subject_code");
                int venueId = rs.getInt("venue_id");
                int memberCount = rs.getInt("member_count");
                String venueName = rs.getString("venue_name");
                
                double lat, lon;
                if (rs.getObject("latitude") != null && rs.getObject("longitude") != null) {
                    lat = rs.getDouble("latitude");
                    lon = rs.getDouble("longitude");
                } else if (venueId > 0 && venueCoordinates.containsKey(venueId)) {
                    double[] coords = venueCoordinates.get(venueId);
                    lat = coords[0];
                    lon = coords[1];
                } else {
                    lat = -33.93050;
                    lon = 18.43081;
                }
                
                String locationName = venueName != null ? venueName : "No Venue Assigned";
                String displayName = groupName + " (" + subjectCode + ")";
                
                StudyLocation groupLocation = new StudyLocation(
                    groupId,
                    displayName,
                    "Study Group",
                    locationName,
                    lat,
                    lon,
                    memberCount
                );
                
                double distance = calculateDistance(userLat, userLon, lat, lon);
                groupLocation.setDistance(distance);
                
                groupLocations.add(groupLocation);
            }

            System.out.println("✅ Loaded " + groupLocations.size() + " study groups");

        } catch (SQLException e) {
            System.err.println("Error loading study groups: " + e.getMessage());
            e.printStackTrace();
        }

        return groupLocations;
    }

    private List<StudyLocation> loadVenuesAsStudyLocations(double userLat, double userLon) {
        List<StudyLocation> venueLocations = new ArrayList<>();
        
        String sql = "SELECT v.venue_id, v.venue_name, v.capacity, v.current_usage, " +
                    "v.latitude, v.longitude " +
                    "FROM venues v " +
                    "ORDER BY v.venue_name";

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int venueId = rs.getInt("venue_id");
                String venueName = rs.getString("venue_name");
                int capacity = rs.getInt("capacity");
                int currentUsage = rs.getInt("current_usage");
                int availableSeats = capacity - currentUsage;
                
                double lat, lon;
                if (rs.getObject("latitude") != null && rs.getObject("longitude") != null) {
                    lat = rs.getDouble("latitude");
                    lon = rs.getDouble("longitude");
                } else {
                    Map<String, double[]> fallbackCoords = new HashMap<>();
                    fallbackCoords.put("D6 Library Seminar Room", new double[]{-33.93050, 18.43081});
                    fallbackCoords.put("Engineering Building Room 1.19", new double[]{-33.93087, 18.42936});
                    fallbackCoords.put("E-Learning Center", new double[]{-33.92893, 18.42840});
                    fallbackCoords.put("Commerce Building Study Room", new double[]{-33.93020, 18.42946});
                    
                    double[] coords = fallbackCoords.getOrDefault(venueName, 
                        new double[]{-33.93080, 18.43023});
                    lat = coords[0];
                    lon = coords[1];
                }
                
                StudyLocation venueLocation = new StudyLocation(
                    10000 + venueId,
                    "VENUE: " + venueName,
                    "Study Point",
                    venueName,
                    lat,
                    lon,
                    availableSeats
                );
                
                double distance = calculateDistance(userLat, userLon, lat, lon);
                venueLocation.setDistance(distance);
                
                venueLocations.add(venueLocation);
            }

            System.out.println("✅ Loaded " + venueLocations.size() + " study points (venues)");

        } catch (SQLException e) {
            System.err.println("Error loading venues: " + e.getMessage());
            e.printStackTrace();
        }

        return venueLocations;
    }

    private void loadUserLocationFromDatabase() {
        String sql = "SELECT latitude, longitude FROM user_location WHERE id = 1";

        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                userLatitude = rs.getDouble("latitude");
                userLongitude = rs.getDouble("longitude");
                userPosition = new GeoPosition(userLatitude, userLongitude);
                System.out.println("Loaded user location from DB: " + userLatitude + ", " + userLongitude);
            } else {
                userLatitude = -33.93080102488844;
                userLongitude = 18.430230425585137;
                userPosition = new GeoPosition(userLatitude, userLongitude);
                System.out.println("No saved location, using default");
            }

        } catch (SQLException e) {
            System.err.println("Error reading user location: " + e.getMessage());
            userLatitude = -33.93080102488844;
            userLongitude = 18.430230425585137;
            userPosition = new GeoPosition(userLatitude, userLongitude);
        }
    }

    private boolean saveUserLocationToDatabase(double latitude, double longitude) {
        String updateSQL = "UPDATE user_location SET latitude = ?, longitude = ?, updated_at = CURRENT_TIMESTAMP WHERE id = 1";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSQL)) {

            stmt.setDouble(1, latitude);
            stmt.setDouble(2, longitude);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                String insertSQL = "INSERT INTO user_location (id, latitude, longitude) VALUES (1, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                    insertStmt.setDouble(1, latitude);
                    insertStmt.setDouble(2, longitude);
                    insertStmt.executeUpdate();
                }
            }

            System.out.println("User location saved: " + latitude + ", " + longitude);
            return true;

        } catch (SQLException e) {
            System.err.println("Error saving location: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ===== LOCATION SELECTION =====

    private void toggleLocationSelectionMode() {
        isLocationSelectionMode = true;
        setLocationButton.setEnabled(false);
        mapViewer.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        JOptionPane.showMessageDialog(
                this,
                "Click anywhere on the map to set your new location.\n" +
                        "Use right-click to pan and scroll to zoom.\n" +
                        "Click CONFIRM when you're done, or CANCEL to abort.",
                "Location Selection Mode",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void confirmLocationSelection() {
        if (isLocationSelectionMode) {
            if (saveUserLocationToDatabase(userPosition.getLatitude(), userPosition.getLongitude())) {
                JOptionPane.showMessageDialog(
                        this,
                        String.format("Location saved successfully!\nLat: %.6f, Lon: %.6f",
                                userPosition.getLatitude(),
                                userPosition.getLongitude()),
                        "Location Confirmed",
                        JOptionPane.INFORMATION_MESSAGE
                );

                for (StudyLocation location : studyLocations) {
                    location.setDistance(calculateDistance(userLatitude, userLongitude,
                            location.getLatitude(), location.getLongitude()));
                }

                updateGroupsList();
                setupMapWaypoints();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to save location to database.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

            exitLocationSelectionMode();
        }
    }

    private void cancelLocationSelection() {
        loadUserLocationFromDatabase();
        exitLocationSelectionMode();

        JOptionPane.showMessageDialog(
                this,
                "Location selection cancelled. Your original location is preserved.",
                "Cancelled",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void exitLocationSelectionMode() {
        isLocationSelectionMode = false;
        setLocationButton.setEnabled(true);
        mapViewer.setCursor(Cursor.getDefaultCursor());
    }

    // ===== MAP CONTROLS =====

    private void setupMouseControls() {
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    isDragging = true;
                    lastMousePoint = e.getPoint();
                    mapViewer.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    centerMapOnClick(e.getPoint());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    isDragging = false;
                    mapViewer.setCursor(Cursor.getDefaultCursor());
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    zoomIn();
                    centerMapOnClick(e.getPoint());
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    checkWaypointClick(e);
                }
            }
        });

        mapViewer.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging && lastMousePoint != null) {
                    handleMousePanning(e.getPoint());
                }
            }
        });

        mapViewer.addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) {
                zoomIn();
            } else {
                zoomOut();
            }
        });
    }

    private void handleMousePanning(Point currentPoint) {
        if (lastMousePoint == null) return;

        int deltaX = currentPoint.x - lastMousePoint.x;
        int deltaY = currentPoint.y - lastMousePoint.y;

        GeoPosition currentCenter = mapViewer.getCenterPosition();
        Point2D centerPixel = mapViewer.getTileFactory().geoToPixel(currentCenter, mapViewer.getZoom());

        Point2D newCenterPixel = new Point2D.Double(
                centerPixel.getX() - deltaX,
                centerPixel.getY() - deltaY
        );

        GeoPosition newCenter = mapViewer.getTileFactory().pixelToGeo(newCenterPixel, mapViewer.getZoom());
        mapViewer.setCenterPosition(newCenter);
        lastMousePoint = currentPoint;
    }

    private void centerMapOnClick(Point clickPoint) {
        GeoPosition clickedPosition = mapViewer.convertPointToGeoPosition(clickPoint);
        mapViewer.setCenterPosition(clickedPosition);
    }

    private void checkWaypointClick(MouseEvent e) {
        if (isLocationSelectionMode) {
            GeoPosition clickedPosition = mapViewer.convertPointToGeoPosition(e.getPoint());
            userLatitude = clickedPosition.getLatitude();
            userLongitude = clickedPosition.getLongitude();
            userPosition = clickedPosition;
            setupMapWaypoints();
            mapViewer.repaint();
            return;
        }

        Point2D point = e.getPoint();
        for (StudyLocationWaypoint waypoint : waypoints) {
            if (waypoint.getStudyLocation() != null) {
                Point2D waypointPoint = mapViewer.convertGeoPositionToPoint(waypoint.getPosition());
                double distance = point.distance(waypointPoint);

                if (distance < 20) {
                    showWaypointInfo(waypoint.getStudyLocation());
                    break;
                }
            }
        }
    }

    private void zoomIn() {
        int currentZoom = mapViewer.getZoom();
        if (currentZoom > 1) {
            mapViewer.setZoom(currentZoom - 1);
        }
    }

    private void zoomOut() {
        int currentZoom = mapViewer.getZoom();
        if (currentZoom < 15) {
            mapViewer.setZoom(currentZoom + 1);
        }
    }

    private void showWaypointInfo(StudyLocation location) {
        String info;
        if (location.getGroupName().startsWith("VENUE:")) {
            info = String.format(
                "Venue: %s\nType: %s\nAvailable Seats: %d\nDistance: %s",
                location.getBuilding(),
                location.getLocationName(),
                location.getMemberCount(),
                formatDistance(location.getDistance())
            );
            JOptionPane.showMessageDialog(this, info, "Study Venue Information", JOptionPane.INFORMATION_MESSAGE);
        } else {
            info = String.format(
                "Group: %s\nLocation: %s\nBuilding: %s\nMembers: %d\nDistance: %s",
                location.getGroupName(),
                location.getLocationName(),
                location.getBuilding(),
                location.getMemberCount(),
                formatDistance(location.getDistance())
            );
            JOptionPane.showMessageDialog(this, info, "Study Group Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ===== WAYPOINTS =====

    private void setupMapWaypoints() {
        waypoints = new HashSet<>();

        StudyLocationWaypoint userWaypoint = new StudyLocationWaypoint(
                "Your Location", userPosition, Color.RED, true
        );
        waypoints.add(userWaypoint);

        List<StudyLocation> filteredLocations = getFilteredAndSortedLocations();

        for (StudyLocation location : filteredLocations) {
            GeoPosition position = new GeoPosition(location.getLatitude(), location.getLongitude());
            
            Color waypointColor = location.getGroupName().startsWith("VENUE:") ? 
                new Color(255, 152, 0) : Color.BLUE;
            
            StudyLocationWaypoint waypoint = new StudyLocationWaypoint(
                    location.getGroupName(), position, waypointColor, false
            );
            waypoint.setStudyLocation(location);
            waypoints.add(waypoint);
        }

        waypointPainter.setWaypoints(waypoints);
        mapViewer.repaint();
        
        System.out.println("🗺️ Map updated with " + waypoints.size() + " waypoints" +
            (venueFilter != null ? " (filtered by: " + venueFilter + ")" : ""));
    }

    // ===== VIEW CONTROLS =====

    private void switchToListView() {
        if (isGridView) {
            isGridView = false;
            
            listBtn.setBackground(BUTTON_BLUE);
            listBtn.setForeground(Color.WHITE);
            listBtn.setBorder(BorderFactory.createEmptyBorder());
            
            gridBtn.setBackground(Color.WHITE);
            gridBtn.setForeground(Color.BLACK);
            gridBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            
            createListView();
            
            System.out.println("✅ Switched to List View");
        }
    }

    private void switchToGridView() {
        if (!isGridView) {
            isGridView = true;
            
            gridBtn.setBackground(BUTTON_BLUE);
            gridBtn.setForeground(Color.WHITE);
            gridBtn.setBorder(BorderFactory.createEmptyBorder());
            
            listBtn.setBackground(Color.WHITE);
            listBtn.setForeground(Color.BLACK);
            listBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            
            createGridView();
            
            System.out.println("✅ Switched to Grid View");
        }
    }

    private void toggleMapSize() {
        mapExpanded = !mapExpanded;

        if (mapExpanded) {
            mapPanel.setPreferredSize(new Dimension(0, expandedMapHeight));
            setSize(890, expandedFrameHeight);
            expandButton.setText("COLLAPSE");
        } else {
            mapPanel.setPreferredSize(new Dimension(0, normalMapHeight));
            setSize(890, normalFrameHeight);
            expandButton.setText("EXPAND");
        }

        revalidate();
        repaint();
    }

    private void updateGroupsList() {
        createGroupsList();
        setupMapWaypoints();
        System.out.println("✅ Groups list updated (" + (isGridView ? "Grid" : "List") + " view)");
    }

    private List<StudyLocation> getFilteredAndSortedLocations() {
        List<StudyLocation> filtered = new ArrayList<>(studyLocations);
        
        String selectedSort = "SORT BY DISTANCE";
        if (sortCombo != null) {
            selectedSort = (String) sortCombo.getSelectedItem();
            if (selectedSort == null) {
                selectedSort = "SORT BY DISTANCE";
            }
        }

        if (venueFilter != null && !venueFilter.isEmpty()) {
            filtered.removeIf(location -> 
                !location.getBuilding().equalsIgnoreCase(venueFilter) &&
                !location.getLocationName().contains(venueFilter)
            );
            System.out.println("🔍 Filtering by venue: " + venueFilter + " (found " + filtered.size() + " locations)");
        }

        if (selectedSort.equals("WITHIN 100 M")) {
            filtered.removeIf(location -> location.getDistance() > 100);
        } else if (selectedSort.equals("WITHIN 500 M")) {
            filtered.removeIf(location -> location.getDistance() > 500);
        } else if (selectedSort.equals("WITHIN 1 KM")) {
            filtered.removeIf(location -> location.getDistance() > 1000);
        }

        if (selectedSort.equals("SORT BY DISTANCE") || selectedSort.startsWith("WITHIN")) {
            filtered.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));
        } else if (selectedSort.equals("SORT BY NAME")) {
            filtered.sort((a, b) -> a.getGroupName().compareToIgnoreCase(b.getGroupName()));
        } else if (selectedSort.equals("SORT BY MEMBERS")) {
            filtered.sort((a, b) -> Integer.compare(b.getMemberCount(), a.getMemberCount()));
        }

        return filtered;
    }

    // ===== UTILITY METHODS =====

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private String formatDistance(double distanceInMeters) {
        if (distanceInMeters < 1000) {
            return String.format("%.0f M", distanceInMeters);
        } else {
            return String.format("%.2f KM", distanceInMeters / 1000);
        }
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
        // Navigation handled by NavigationManager
    }
}