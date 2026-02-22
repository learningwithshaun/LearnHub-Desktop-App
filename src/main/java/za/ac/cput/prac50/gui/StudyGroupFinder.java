package za.ac.cput.prac50.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;

import za.ac.cput.prac50.dao.UnifiedMembershipDAO;
import za.ac.cput.prac50.dao.UnifiedStudyGroupDAO;
import za.ac.za.cput.prac50.domain.UnifiedMembership;
import za.ac.za.cput.prac50.domain.StudyGroup;
import za.ac.za.cput.prac50.domain.UnifiedStudent;

/**
 * StudyGroupFinder - UPDATED with SessionManager Integration
 * Shows all available groups and allows joining/switching
 * 
 * @author LearnHub Team
 */
public class StudyGroupFinder extends JFrame {
    
    // Color Theme
    private static final Color LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color BUTTON_BLUE = new Color(24, 144, 255);
    private static final Color SELECTED_BLUE = new Color(70, 130, 180);
    private static final Color GREY_BACKGROUND = new Color(240, 240, 240);
    private static final Color SUCCESS_GREEN = new Color(76, 175, 80);
    
    // Current user from session
    private UnifiedStudent currentStudent;
    
    // Subject data
    private String[] courses = {"ADF262S", "MAF261S", "CNF260S"};
    private Map<String, String[]> courseToSubjects = new HashMap<>();
    private String[] groups = new String[50];
    
    // UI Components
    private JComboBox<String> courseCombo;
    private JComboBox<String> subjectCombo;
    private JPanel groupsPanel;
    private JLabel currentLabel;
    private JTextField searchField;
    
    // DAOs
    private UnifiedMembershipDAO membershipDAO;
    private UnifiedStudyGroupDAO groupDAO;
    private String currentGroup = null;
    
    public StudyGroupFinder() {
        // ✅ GET CURRENT STUDENT FROM SESSION
        currentStudent = SessionManager.getInstance().getCurrentStudent();
        
        if (currentStudent == null) {
            System.out.println("⚠️ Warning: No student in session, loading default");
            currentStudent = SessionManager.getInstance().getDefaultStudent();
        }
        
        System.out.println("✅ StudyGroupFinder loaded for: " + currentStudent.getFullName());
        
        // Initialize data
        initializeData();
        initializeUI();
        setupEventHandlers();
        
        // Load current group membership
        updateCurrent();
    }
    
    private void initializeData() {
        // Initialize DAOs
        membershipDAO = new UnifiedMembershipDAO();
        groupDAO = new UnifiedStudyGroupDAO();
        
        // Initialize 50 groups
        for (int i = 0; i < 50; i++) {
            groups[i] = "Group " + (i + 1);
        }
        
        // Map courses to subjects
        courseToSubjects.put("ADF262S", new String[]{
            "ADF262S", "ADP262S", "CNF262S", "MAF262S", "PRC262S", 
            "PRT262S", "INM262S", "ISA262S", "ICE262S"
        });
        
        courseToSubjects.put("MAF261S", new String[]{
            "MAF261S", "ADF261S", "CNF261S", "PRC261S", "PRJ261S", 
            "ICE261S", "INM261S", "MUDesign261S", "MUTheory261S"
        });
        
        courseToSubjects.put("CNF260S", new String[]{
            "CNF260S", "CND260S", "ITS260S", "PRC260S", "PRJ260S", 
            "ICE260S", "ADF260S", "MAF260S"
        });
    }
    
    private void initializeUI() {
        setTitle("LearnHub - Find Study Groups");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        
        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Header section
        JPanel headerSection = new JPanel(new BorderLayout());
        headerSection.setBackground(Color.WHITE);
        
        // ✅ USE HeaderPanelCreator - automatically gets student from session
        headerSection.add(HeaderPanelCreator.createHeader(), BorderLayout.NORTH);
        
        // ✅ USE NavigationPanelCreator with "GROUPS" active
        headerSection.add(NavigationPanelCreator.createNavigationPanel(this, "GROUPS"), BorderLayout.SOUTH);
        
        mainPanel.add(headerSection, BorderLayout.NORTH);
        
        // Content area
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Create filter panel
        JPanel filterPanel = createFilterPanel();
        contentPanel.add(filterPanel, BorderLayout.NORTH);
        
        // Create groups display panel
        JPanel groupsDisplayPanel = new JPanel(new BorderLayout());
        groupsDisplayPanel.setBackground(Color.WHITE);
        
        JLabel groupsTitle = new JLabel("Available Study Groups");
        groupsTitle.setFont(new Font("Arial", Font.BOLD, 20));
        groupsTitle.setForeground(new Color(11, 44, 77));
        groupsTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 15, 0));
        groupsDisplayPanel.add(groupsTitle, BorderLayout.NORTH);
        
        groupsPanel = new JPanel();
        groupsPanel.setLayout(new BoxLayout(groupsPanel, BoxLayout.Y_AXIS));
        groupsPanel.setBackground(Color.WHITE);
        
        loadGroups();
        
        JScrollPane scrollPane = new JScrollPane(groupsPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        groupsDisplayPanel.add(scrollPane, BorderLayout.CENTER);
        
        contentPanel.add(groupsDisplayPanel, BorderLayout.CENTER);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Footer
        mainPanel.add(createFooter(), BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(LIGHT_BLUE, 2),
                "Filter Groups",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                SELECTED_BLUE
            ),
            BorderFactory.createEmptyBorder(10, 15, 15, 15)
        ));
        
        // Student info row (read-only, from session)
        JPanel studentRow = createFormRow("Student:", 
            new JLabel(currentStudent.getFullName() + " (" + currentStudent.getStudentId() + ")"));
        
        // Course selection
        JPanel courseRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        courseRow.setBackground(Color.WHITE);
        courseRow.add(new JLabel("Select Course:"));
        courseCombo = new JComboBox<>(courses);
        courseCombo.setPreferredSize(new Dimension(200, 30));
        courseRow.add(courseCombo);
        
        // Subject selection
        JPanel subjectRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        subjectRow.setBackground(Color.WHITE);
        subjectRow.add(new JLabel("Select Subject:"));
        subjectCombo = new JComboBox<>(courseToSubjects.get(courses[0]));
        subjectCombo.setPreferredSize(new Dimension(200, 30));
        subjectRow.add(subjectCombo);
        
        // Current group display
        JPanel currentRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        currentRow.setBackground(Color.WHITE);
        currentRow.add(new JLabel("Current Group:"));
        currentLabel = new JLabel("None");
        currentLabel.setFont(new Font("Arial", Font.BOLD, 14));
        currentLabel.setForeground(SELECTED_BLUE);
        currentRow.add(currentLabel);
        
        // Search field
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchRow.setBackground(Color.WHITE);
        searchRow.add(new JLabel("Search Groups:"));
        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 30));
        searchRow.add(searchField);
        
        JButton searchBtn = createStyledButton("Search", BUTTON_BLUE, 12);
        searchBtn.addActionListener(e -> performSearch());
        searchRow.add(searchBtn);
        
        JButton clearBtn = createStyledButton("Clear", Color.GRAY, 12);
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            loadGroups();
        });
        searchRow.add(clearBtn);
        
        filterPanel.add(studentRow);
        filterPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        filterPanel.add(courseRow);
        filterPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        filterPanel.add(subjectRow);
        filterPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        filterPanel.add(currentRow);
        filterPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        filterPanel.add(searchRow);
        
        return filterPanel;
    }
    
    private JPanel createFormRow(String labelText, Component component) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row.setBackground(Color.WHITE);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setPreferredSize(new Dimension(120, 25));
        
        row.add(label);
        row.add(component);
        
        return row;
    }
    
    private void loadGroups() {
        groupsPanel.removeAll();
        
        String subject = (String) subjectCombo.getSelectedItem();
        
        groupsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        for (String groupName : groups) {
            JPanel groupPanel = createGroupPanel(groupName, subject);
            groupsPanel.add(groupPanel);
            groupsPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        }
        
        groupsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        groupsPanel.revalidate();
        groupsPanel.repaint();
    }
    
    private JPanel createGroupPanel(String groupName, String subject) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(900, 75));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        
        // Get group size using unified methods
        int groupSize = 0;
        int maxSize = membershipDAO.getMaxGroupSize();
        
        // Get actual group from database to check size
        if (subject != null && !subject.isEmpty()) {
            StudyGroup group = groupDAO.getGroupByNameAndSubject(groupName, subject);
            if (group != null) {
                groupSize = group.getCurrentMembers();
                maxSize = group.getMaxMembers();
            }
        }
        
        // Group info
        JLabel groupLabel = new JLabel(groupName + "    " + groupSize + "/" + maxSize + " members");
        groupLabel.setFont(new Font("Arial", Font.BOLD, 16));
        groupLabel.setForeground(new Color(11, 44, 77));
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        if (currentGroup != null && currentGroup.equals(groupName)) {
            // User is in this group
            JLabel joinedLabel = new JLabel("✓ Joined");
            joinedLabel.setFont(new Font("Arial", Font.BOLD, 14));
            joinedLabel.setForeground(SUCCESS_GREEN);
            joinedLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
            
            JButton viewMembersButton = createStyledButton("View Members", BUTTON_BLUE, 12);
            viewMembersButton.addActionListener(e -> viewGroupMembers(groupName, subject));
            
            JButton leaveButton = createStyledButton("Leave Group", new Color(244, 67, 54), 12);
            leaveButton.addActionListener(e -> leaveGroup(groupName, subject));
            
            buttonPanel.add(joinedLabel);
            buttonPanel.add(viewMembersButton);
            buttonPanel.add(leaveButton);
            
        } else if (currentGroup == null) {
            // User not in any group
            JButton joinGroupButton = createStyledButton("Join Group", SUCCESS_GREEN, 12);
            joinGroupButton.addActionListener(e -> {
                if (joinGroupWithName(groupName)) {
                    // Navigate to Study Points after joining
                    NavigationManager.navigateToStudyPoints(this);
                }
            });
            
            buttonPanel.add(joinGroupButton);
            
        } else {
            // User in different group
            JButton moveGroupButton = createStyledButton("Move to this Group", BUTTON_BLUE, 12);
            moveGroupButton.addActionListener(e -> moveToSpecificGroup(groupName));
            
            buttonPanel.add(moveGroupButton);
        }
        
        panel.add(groupLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    // ===== GROUP OPERATIONS =====
    
    private boolean joinGroupWithName(String groupName) {
        String subject = (String) subjectCombo.getSelectedItem();
        
        if (currentGroup != null) {
            JOptionPane.showMessageDialog(this, 
                "You are already in a group. Use 'Move to this Group' to switch.", 
                "Already in Group", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        try {
            // Get the group from database
            StudyGroup group = groupDAO.getGroupByNameAndSubject(groupName, subject);
            
            if (group == null) {
                JOptionPane.showMessageDialog(this, 
                    "Group not found in database.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Check if group is full
            if (groupDAO.isGroupFull(group.getGroupId())) {
                JOptionPane.showMessageDialog(this, 
                    "Group is full. Please select another group.", 
                    "Group Full", 
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            // Create membership
            UnifiedMembership membership = new UnifiedMembership(
                currentStudent.getStudentId(), 
                group.getGroupId(), 
                subject
            );
            
            if (membershipDAO.addMembership(membership)) {
                JOptionPane.showMessageDialog(this, 
                    "Successfully joined " + groupName + " for " + subject + "!\n\n" +
                    "You can now:\n" +
                    "• View study locations\n" +
                    "• Send messages to group members\n" +
                    "• Access group resources", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                updateCurrent();
                return true;
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to join group. Please try again.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Database error: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void moveToSpecificGroup(String groupName) {
        if (groupName.equals(currentGroup)){
            JOptionPane.showMessageDialog(this, 
                "You are already in this group.", 
                "Already in Group", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Move from " + currentGroup + " to " + groupName + "?\n\n" +
            "This will remove you from your current group.",
            "Confirm Move",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        String subject = (String) subjectCombo.getSelectedItem();
        
        try {
            // Get the target group
            StudyGroup group = groupDAO.getGroupByNameAndSubject(groupName, subject);
            
            if (group == null) {
                JOptionPane.showMessageDialog(this, 
                    "Group not found.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if target group has space
            if (groupDAO.isGroupFull(group.getGroupId())) {
                JOptionPane.showMessageDialog(this, 
                    "Group is full. Please select another group.", 
                    "Group Full", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Update membership
            UnifiedMembership membership = new UnifiedMembership(
                currentStudent.getStudentId(), 
                group.getGroupId(), 
                subject
            );
            
            if (membershipDAO.updateMembership(membership)) {
                JOptionPane.showMessageDialog(this, 
                    "Successfully moved from " + currentGroup + " to " + groupName + " for " + subject, 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                updateCurrent();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to move to group.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Database error: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }   
    }
    
    private void leaveGroup(String groupName, String subject) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to leave " + groupName + "?\n\n" +
            "You will need to rejoin if you change your mind.",
            "Confirm Leave",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            if (membershipDAO.deleteMembership(currentStudent.getStudentId(), subject)) {
                JOptionPane.showMessageDialog(this,
                    "You have left " + groupName + ".",
                    "Left Group",
                    JOptionPane.INFORMATION_MESSAGE);
                updateCurrent();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to leave group.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error leaving group: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void viewGroupMembers(String groupName, String subject) {
        try {
            // Get the group
            StudyGroup group = groupDAO.getGroupByNameAndSubject(groupName, subject);
            
            if (group == null) {
                JOptionPane.showMessageDialog(this, 
                    "Group not found.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Get members
            java.util.List<String> members = membershipDAO.getGroupMembers(group.getGroupId());
            
            StringBuilder membersText = new StringBuilder();
            membersText.append("Group: ").append(groupName).append("\n");
            membersText.append("Subject: ").append(subject).append("\n");
            membersText.append("Members: ").append(members.size()).append("/").append(group.getMaxMembers()).append("\n\n");
            
            if (members.isEmpty()) {
                membersText.append("No members in this group yet.");
            } else {
                membersText.append("Current Members:\n");
                int counter = 1;
                for (String memberId : members) {
                    // You could load full student names here if needed
                    membersText.append(counter).append(". ").append(memberId);
                    if (memberId.equals(currentStudent.getStudentId())) {
                        membersText.append(" (You)");
                    }
                    membersText.append("\n");
                    counter++;
                }
            }
            
            JTextArea textArea = new JTextArea(membersText.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Arial", Font.PLAIN, 13));
            textArea.setBackground(new Color(245, 245, 245));
            textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            
            JOptionPane.showMessageDialog(this, scrollPane, 
                "Group Members - " + groupName, 
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading group members: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void performSearch() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        
        if (searchTerm.isEmpty()) {
            loadGroups();
            return;
        }
        
        groupsPanel.removeAll();
        groupsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        String subject = (String) subjectCombo.getSelectedItem();
        boolean foundAny = false;
        
        for (String groupName : groups) {
            if (groupName.toLowerCase().contains(searchTerm)) {
                JPanel groupPanel = createGroupPanel(groupName, subject);
                groupsPanel.add(groupPanel);
                groupsPanel.add(Box.createRigidArea(new Dimension(0, 12)));
                foundAny = true;
            }
        }
        
        if (!foundAny) {
            JLabel noResults = new JLabel("No groups found matching '" + searchTerm + "'");
            noResults.setFont(new Font("Arial", Font.ITALIC, 14));
            noResults.setForeground(Color.GRAY);
            noResults.setAlignmentX(Component.CENTER_ALIGNMENT);
            groupsPanel.add(noResults);
        }
        
        groupsPanel.revalidate();
        groupsPanel.repaint();
    }
    
    private void updateCurrent() {
        String subject = (String) subjectCombo.getSelectedItem();
        
        try {
            UnifiedMembership membership = membershipDAO.getMembership(
                currentStudent.getStudentId(), 
                subject
            );
            
            if (membership != null) {
                // Get the group to show its name
                StudyGroup group = groupDAO.getGroupById(membership.getGroupId());
                if (group != null) {
                    currentGroup = group.getGroupName();
                    currentLabel.setText(currentGroup);
                    currentLabel.setForeground(SUCCESS_GREEN);
                } else {
                    currentGroup = null;
                    currentLabel.setText("None");
                    currentLabel.setForeground(Color.GRAY);
                }
            } else {
                currentGroup = null;
                currentLabel.setText("None");
                currentLabel.setForeground(Color.GRAY);
            }
            
            loadGroups();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Database error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ===== EVENT HANDLERS =====
    
    private void setupEventHandlers() {
        courseCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String course = (String) courseCombo.getSelectedItem();
                String[] subs = courseToSubjects.get(course);
                subjectCombo.setModel(new DefaultComboBoxModel<>(subs));
                updateCurrent();
            }  
        });
        
        subjectCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateCurrent();
            }  
        });
        
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSearch();
                }
            }
        });
    }
    
    // ===== HELPER METHODS =====
    
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
            new EmptyBorder(10, 0, 10, 0)
        ));
        
        JLabel copyrightLabel = new JLabel("© 2025 LearnHub - All Rights Reserved");
        copyrightLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        copyrightLabel.setForeground(Color.GRAY);
        
        footer.add(copyrightLabel);
        return footer;
    }
    
    @Override
    public void dispose() {
        super.dispose();
    }
}