package za.ac.cput.prac50.gui;

import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import za.ac.cput.prac50.connection.UnifiedDBConnection;
import za.ac.za.cput.prac50.domain.StudyGroup;
import za.ac.za.cput.prac50.domain.UnifiedStudent;

/**
 * CreateNewGroupGUI - Dashboard with Session Management
 * Uses SessionManager for logged-in student info
 */
public class CreateNewGroupGUI extends JFrame implements ActionListener {

    private Color LIGHT_BLUE = new Color(173, 216, 230);
    private Color BUTTON_BLUE = new Color(24, 144, 255);
    private Color SELECTED_BLUE = new Color(70, 130, 180);
    private Color GREY_BACKGROUND = new Color(240, 240, 240);

    private JPanel pnlCenter;
    private JTextField txtGroupName;
    private JTextArea txtDescription;
    private JTextField txtMaxMembers;
    private JComboBox<String> cmbYear, cmbStream, cmbSubject;
    private JButton btnCreate;

    private JComboBox<String> cmbVenue;
    private Map<String, Integer> venueMap;

    private UnifiedStudent currentStudent;

    public CreateNewGroupGUI() {
        super("Create New Group - LearnHub");
        
        // Get current student from session
        currentStudent = SessionManager.getInstance().getCurrentStudent();
        
        // If no student in session, use default
        if (currentStudent == null) {
            System.out.println("⚠ Warning: No student in session, loading default");
            currentStudent = SessionManager.getInstance().getDefaultStudent();
        }
        
        System.out.println("✓ Dashboard loaded for: " + currentStudent.getFullName());

        // Setup UI
        this.setLayout(new BorderLayout());
        this.setSize(870, 700);
        this.getContentPane().setBackground(Color.WHITE);

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(Color.WHITE);

        JPanel headerSection = new JPanel(new BorderLayout());
        headerSection.setBackground(Color.WHITE);
        
        headerSection.add(HeaderPanelCreator.createHeader(), BorderLayout.NORTH);
        headerSection.add(NavigationPanelCreator.createNavigationPanel(this, "CREATE GROUP"), BorderLayout.SOUTH);
        
        mainContainer.add(headerSection, BorderLayout.NORTH);

        pnlCenter = new JPanel(new GridBagLayout());
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        pnlCenter.setBackground(Color.WHITE);

        createFormComponents();

        JScrollPane scrollCenter = new JScrollPane(pnlCenter);
        scrollCenter.setBorder(null);
        scrollCenter.getViewport().setBackground(Color.WHITE);
        
        mainContainer.add(scrollCenter, BorderLayout.CENTER);
        mainContainer.add(createTrademark(), BorderLayout.SOUTH);

        this.add(mainContainer);
        
        setupEventHandlers();
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void createFormComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblFormTitle = new JLabel("Create New Study Group", JLabel.CENTER);
        lblFormTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblFormTitle.setForeground(new Color(0, 0, 80));
        pnlCenter.add(lblFormTitle, gbc);

        // Group Name
        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel lblGroupName = new JLabel("Group Name:");
        lblGroupName.setFont(new Font("Arial", Font.BOLD, 14));
        pnlCenter.add(lblGroupName, gbc);

        gbc.gridx = 1;
        txtGroupName = new JTextField(20);
        txtGroupName.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlCenter.add(txtGroupName, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblDescription = new JLabel("Description:");
        lblDescription.setFont(new Font("Arial", Font.BOLD, 14));
        pnlCenter.add(lblDescription, gbc);

        gbc.gridx = 1;
        txtDescription = new JTextArea(3, 20);
        txtDescription.setFont(new Font("Arial", Font.PLAIN, 14));
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        scrollDesc.setPreferredSize(new Dimension(300, 80));
        pnlCenter.add(scrollDesc, gbc);

        // Year
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblYear = new JLabel("Select Year:");
        lblYear.setFont(new Font("Arial", Font.BOLD, 14));
        pnlCenter.add(lblYear, gbc);

        gbc.gridx = 1;
        cmbYear = new JComboBox<>(new String[]{"", "First Year", "Second Year", "Third Year"});
        cmbYear.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlCenter.add(cmbYear, gbc);

        // Stream
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblStream = new JLabel("Select Stream:");
        lblStream.setFont(new Font("Arial", Font.BOLD, 14));
        pnlCenter.add(lblStream, gbc);

        gbc.gridx = 1;
        cmbStream = new JComboBox<>(new String[]{
            "Select ICT Stream", 
            "Application Development", 
            "Multimedia", 
            "Communication and Networks"
        });
        cmbStream.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlCenter.add(cmbStream, gbc);

        // Subject
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblSubject = new JLabel("Select Subject:");
        lblSubject.setFont(new Font("Arial", Font.BOLD, 14));
        pnlCenter.add(lblSubject, gbc);

        gbc.gridx = 1;
        cmbSubject = new JComboBox<>();
        cmbSubject.addItem("Select Subject");
        cmbSubject.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlCenter.add(cmbSubject, gbc);

        // Venue Selection
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblVenue = new JLabel("Select Venue (Optional):");
        lblVenue.setFont(new Font("Arial", Font.BOLD, 14));
        pnlCenter.add(lblVenue, gbc);

        gbc.gridx = 1;
        cmbVenue = new JComboBox<>();
        cmbVenue.addItem("No Venue (Online/Flexible)");
        cmbVenue.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlCenter.add(cmbVenue, gbc);

        // Load venues from database
        loadVenues();
        
        // Max Members
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblMaxMembers = new JLabel("Maximum Members:");
        lblMaxMembers.setFont(new Font("Arial", Font.BOLD, 14));
        pnlCenter.add(lblMaxMembers, gbc);

        gbc.gridx = 1;
        txtMaxMembers = new JTextField("", 20);
        txtMaxMembers.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlCenter.add(txtMaxMembers, gbc);

        // Create Button - FIXED STYLING
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        btnCreate = new JButton("CREATE GROUP");
        btnCreate.setBackground(BUTTON_BLUE); // RGB(24, 144, 255)
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setFont(new Font("Arial", Font.BOLD, 16));
        btnCreate.setFocusPainted(false);
        btnCreate.setBorderPainted(false); // Remove border painting
        btnCreate.setOpaque(true); // Make button opaque
        btnCreate.setContentAreaFilled(true); // Fill content area
        btnCreate.setPreferredSize(new Dimension(200, 40));
        btnCreate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCreate.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        // Add hover effect
        btnCreate.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnCreate.setBackground(BUTTON_BLUE.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btnCreate.setBackground(BUTTON_BLUE);
            }
        });
        
        btnCreate.addActionListener(this);
        pnlCenter.add(btnCreate, gbc);

        // Info Label
        gbc.gridy++;
        JLabel lblInfo = new JLabel("<html><center>Fill in all fields to create a study group.<br>Select year and stream to see available subjects.</center></html>", JLabel.CENTER);
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 12));
        lblInfo.setForeground(Color.GRAY);
        pnlCenter.add(lblInfo, gbc);
    }

    /**
     * Load available venues from database
     */
    private void loadVenues() {
        venueMap = new HashMap<>();
        
        try {
            Connection conn = UnifiedDBConnection.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT venue_id, venue_name, capacity, current_usage " +
                "FROM venues ORDER BY venue_name"
            );
            
            while (rs.next()) {
                int venueId = rs.getInt("venue_id");
                String venueName = rs.getString("venue_name");
                int capacity = rs.getInt("capacity");
                int currentUsage = rs.getInt("current_usage");
                int available = capacity - currentUsage;
                
                String displayText = venueName + " (" + available + " seats available)";
                cmbVenue.addItem(displayText);
                venueMap.put(displayText, venueId);
            }
            
            rs.close();
            stmt.close();
            
            System.out.println("✅ Loaded " + venueMap.size() + " venues");
            
        } catch (Exception e) {
            System.err.println("Error loading venues: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupEventHandlers() {
        cmbYear.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateSubjectList();
            }
        });

        cmbStream.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateSubjectList();
            }
        });
    }

    private void updateSubjectList() {
        String selectedYear = (String) cmbYear.getSelectedItem();
        String selectedStream = (String) cmbStream.getSelectedItem();

        cmbSubject.removeAllItems();
        cmbSubject.addItem("Select Subject");

        if (selectedYear == null || selectedYear.isEmpty() || 
            selectedStream == null || selectedStream.equals("Select ICT Stream")) {
            return;
        }

        // Load subjects dynamically from database
        try {
            Connection conn = UnifiedDBConnection.getInstance().getConnection();
            String sql = "SELECT subject_code, module_name FROM subjects WHERE subject_year = ? AND stream = ? ORDER BY subject_code";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, selectedYear);
            stmt.setString(2, selectedStream);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String subjectCode = rs.getString("subject_code");
                String moduleName = rs.getString("module_name");
                cmbSubject.addItem(subjectCode + " - " + moduleName);
            }
            
            rs.close();
            stmt.close();
            
            // If no subjects found, show a message
            if (cmbSubject.getItemCount() == 1) { // Only "Select Subject" item
                cmbSubject.addItem("No subjects found for this stream/year");
            }
            
        } catch (Exception e) {
            System.err.println("Error loading subjects from database: " + e.getMessage());
            e.printStackTrace();
            // Fallback to hardcoded subjects if database fails
            loadFallbackSubjects(selectedYear, selectedStream);
        }
    }

    private void loadFallbackSubjects(String selectedYear, String selectedStream) {
        // Fallback hardcoded subjects
        if (selectedYear.equals("First Year")) {
            if (selectedStream.equals("Application Development") || 
                selectedStream.equals("Multimedia") || 
                selectedStream.equals("Communication and Networks")) {
                cmbSubject.addItem("ADF151S - Application Development Foundations 1");
                cmbSubject.addItem("BPR151S - Business Practice 1");
                cmbSubject.addItem("CNF151S - Communication Networks Foundations 1");
                cmbSubject.addItem("PRG151S - Programming 1");
                cmbSubject.addItem("PRJ151S - Project 1");
                cmbSubject.addItem("PRC151S - Professional Communication 1");
                cmbSubject.addItem("MMF151S - Multimedia Fundamentals 1");
                cmbSubject.addItem("ICT151S - ICT Fundamentals 1");
            }
        } else if (selectedYear.equals("Second Year")) {
            if (selectedStream.equals("Application Development")) {
                cmbSubject.addItem("ADF262S - Application Development Fundamentals 2");
                cmbSubject.addItem("ADP262S - Application Development Practice 2");
                cmbSubject.addItem("ISA262S - Information Systems Analysis");
                cmbSubject.addItem("INM262S - Information Management 2");
                cmbSubject.addItem("AI262S - ICT Electives - AI");
                cmbSubject.addItem("BAN262S - ICT Electives - Business Analysis");
                cmbSubject.addItem("ISV262S - ICT Electives - Internet Server");
                cmbSubject.addItem("PYT262S - ICT Electives - Python");
                cmbSubject.addItem("PHP262S - ICT Electives - PHP");
                cmbSubject.addItem("PRC262S - Professional Communication 2");
                cmbSubject.addItem("PRJ262S - Project 2");
            } else if (selectedStream.equals("Multimedia")) {
                cmbSubject.addItem("MAF260S - Multimedia Application Fundamentals 2");
                cmbSubject.addItem("MDS260S - Multimedia Design 2");
                cmbSubject.addItem("MTH260S - Multimedia Technology 2");
                cmbSubject.addItem("MPR260S - Multimedia Practice 2");
                cmbSubject.addItem("INM260S - Information Management 2");
                cmbSubject.addItem("AI260S - ICT Electives - AI");
                cmbSubject.addItem("BAN260S - ICT Electives - Business Analysis");
                cmbSubject.addItem("ISV260S - ICT Electives - Internet Server");
                cmbSubject.addItem("PYT260S - ICT Electives - Python");
                cmbSubject.addItem("PHP260S - ICT Electives - PHP");
                cmbSubject.addItem("PRC260S - Professional Communication 2");
                cmbSubject.addItem("PRJ260S - Project 2");
            } else if (selectedStream.equals("Communication and Networks")) {
                cmbSubject.addItem("CNP261S - Communication Networks Practice 2");
                cmbSubject.addItem("CND261S - Communication Networks Design 2");
                cmbSubject.addItem("CNT261S - Communication Networks Theory 2");
                cmbSubject.addItem("INM261S - Information Management 2");
                cmbSubject.addItem("AI261S - ICT Electives - AI");
                cmbSubject.addItem("BAN261S - ICT Electives - Business Analysis");
                cmbSubject.addItem("ISV261S - ICT Electives - Internet Server");
                cmbSubject.addItem("PYT261S - ICT Electives - Python");
                cmbSubject.addItem("PHP261S - ICT Electives - PHP");
                cmbSubject.addItem("PRC261S - Professional Communication 2");
                cmbSubject.addItem("PRJ261S - Project 2");
            }
        } else if (selectedYear.equals("Third Year")) {
            if (selectedStream.equals("Application Development")) {
                cmbSubject.addItem("ADT362S - Application Development Theory 3");
                cmbSubject.addItem("ADP362S - Application Development Practice 3");
                cmbSubject.addItem("INS362S - Information Systems");
                cmbSubject.addItem("PRM362S - Project Management 3");
                cmbSubject.addItem("ML362S - ICT Electives - Machine Learning");
                cmbSubject.addItem("EHK362S - ICT Electives - Ethical Hacking");
                cmbSubject.addItem("KTL362S - ICT Electives - Kotlin");
                cmbSubject.addItem("AND362S - ICT Electives - Android");
                cmbSubject.addItem("PRP362S - Professional Practice 3");
                cmbSubject.addItem("PPR362S - Professional Presentation 3");
            } else if (selectedStream.equals("Multimedia")) {
                cmbSubject.addItem("MAF360S - Multimedia Application Fundamentals 3");
                cmbSubject.addItem("MDS360S - Multimedia Design 3");
                cmbSubject.addItem("MTH360S - Multimedia Technology 3");
                cmbSubject.addItem("MPR360S - Multimedia Practice 3");
                cmbSubject.addItem("ML360S - ICT Electives - Machine Learning");
                cmbSubject.addItem("EHK360S - ICT Electives - Ethical Hacking");
                cmbSubject.addItem("KTL360S - ICT Electives - Kotlin");
                cmbSubject.addItem("AND360S - ICT Electives - Android");
                cmbSubject.addItem("PRP360S - Professional Practice 3");
                cmbSubject.addItem("PRM360S - Project Management 3");
                cmbSubject.addItem("PPR360S - Project Presentation 3");
            } else if (selectedStream.equals("Communication and Networks")) {
                cmbSubject.addItem("CNP361S - Communication Networks Practice 3");
                cmbSubject.addItem("CND361S - Communication Networks Design 3");
                cmbSubject.addItem("CNT361S - Communication Networks Theory 3");
                cmbSubject.addItem("INM361S - Information Management 3");
                cmbSubject.addItem("ML361S - ICT Electives - Machine Learning");
                cmbSubject.addItem("EHK361S - ICT Electives - Ethical Hacking");
                cmbSubject.addItem("KTL361S - ICT Electives - Kotlin");
                cmbSubject.addItem("AND361S - ICT Electives - Android");
                cmbSubject.addItem("PRP361S - Professional Practice 3");
                cmbSubject.addItem("PRM361S - Project Management 3");
                cmbSubject.addItem("PPR361S - Project Presentation 3");
            }
        }
    }

    private JPanel createTrademark() {
        JPanel trademarkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        trademarkPanel.setBackground(Color.WHITE);
        trademarkPanel.setPreferredSize(new Dimension(0, 30));
        trademarkPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 0, 5, 0)
        ));

        JLabel trademarkLabel = new JLabel("© 2025 LearnHub - All Rights Reserved");
        trademarkLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        trademarkLabel.setForeground(Color.GRAY);

        trademarkPanel.add(trademarkLabel);
        return trademarkPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCreate) {
            createGroup();
        }
    }

    private void createGroup() {
        String groupName = txtGroupName.getText().trim();
        String description = txtDescription.getText().trim();
        String year = (String) cmbYear.getSelectedItem();
        String stream = (String) cmbStream.getSelectedItem();
        String subjectSelection = (String) cmbSubject.getSelectedItem();
        String maxMembersStr = txtMaxMembers.getText().trim();

        // Validation
        if (groupName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a group name.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (year == null || year.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a year.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (stream == null || stream.equals("Select ICT Stream")) {
            JOptionPane.showMessageDialog(this, "Please select a stream.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (subjectSelection == null || subjectSelection.equals("Select Subject") || subjectSelection.contains("No subjects") || subjectSelection.contains("Error")) {
            JOptionPane.showMessageDialog(this, "Please select a valid subject.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (maxMembersStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter maximum members.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int maxMembers;
        try {
            maxMembers = Integer.parseInt(maxMembersStr);
            if (maxMembers < 1 || maxMembers > 100) {
                JOptionPane.showMessageDialog(this, "Maximum members must be between 1 and 100.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for maximum members.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Extract subject code from selection
        String subjectCode = "";
        if (subjectSelection.contains(" - ")) {
            subjectCode = subjectSelection.split(" - ")[0].trim();
        } else {
            subjectCode = subjectSelection; // fallback
        }

        // Get selected venue
        String venueSelection = (String) cmbVenue.getSelectedItem();
        Integer venueId = null;

        if (venueSelection != null && !venueSelection.startsWith("No Venue")) {
            venueId = venueMap.get(venueSelection);
        }
        
        // Create the study group directly in database
        try {
            Connection conn = UnifiedDBConnection.getInstance().getConnection();
            
            // First check if subject exists
            String checkSubjectSQL = "SELECT COUNT(*) FROM subjects WHERE subject_code = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSubjectSQL);
            checkStmt.setString(1, subjectCode);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int subjectCount = rs.getInt(1);
            rs.close();
            checkStmt.close();
            
            if (subjectCount == 0) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid subject code: " + subjectCode + "\n\nPlease select a valid subject from the list.", 
                    "Invalid Subject", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Insert the study group - adjust column names based on your actual table structure
            String sql = "INSERT INTO study_groups (group_name, description, group_year, stream, subject_code, max_members, venue_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, groupName);
            pstmt.setString(2, description.isEmpty() ? "Study group for " + subjectCode : description);
            pstmt.setString(3, year);
            pstmt.setString(4, stream);
            pstmt.setString(5, subjectCode);
            pstmt.setInt(6, maxMembers);
            pstmt.setObject(7, venueId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                String venueInfo = venueId != null ? "\nVenue: " + venueSelection : "\nVenue: Not assigned";
                
                JOptionPane.showMessageDialog(this, 
                    "Group Created Successfully!\n\n" +
                    "Group: " + groupName + "\n" +
                    "Subject: " + subjectCode + "\n" +
                    "Max Members: " + maxMembers + venueInfo + "\n\n" +
                    "Created by: " + currentStudent.getFullName(), 
                    "Success", JOptionPane.INFORMATION_MESSAGE);

                // Clear form
                txtGroupName.setText("");
                txtDescription.setText("");
                cmbYear.setSelectedIndex(0);
                cmbStream.setSelectedIndex(0);
                cmbSubject.removeAllItems();
                cmbSubject.addItem("Select Subject");
                txtMaxMembers.setText("");
                cmbVenue.setSelectedIndex(0);
                
                // Navigate to group finder
                NavigationManager.navigateToGroups(this);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to create group. Please try again.", 
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
            
            pstmt.close();
            
        } catch (SQLException ex) {
            // Handle specific database errors
            String errorMessage = ex.getMessage();
            if (errorMessage.contains("subject_code") || errorMessage.contains("SUBJECT") || errorMessage.contains("subject")) {
                JOptionPane.showMessageDialog(this, 
                    "Database error: Invalid subject code - " + subjectCode + "\n\nPlease select a valid subject from the list.", 
                    "Invalid Subject Code", JOptionPane.ERROR_MESSAGE);
            } else if (errorMessage.contains("foreign key") || errorMessage.contains("constraint")) {
                JOptionPane.showMessageDialog(this, 
                    "Database error: Referenced data not found.\n\nPlease make sure the subject exists in the database.", 
                    "Reference Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Database error: " + errorMessage, 
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}