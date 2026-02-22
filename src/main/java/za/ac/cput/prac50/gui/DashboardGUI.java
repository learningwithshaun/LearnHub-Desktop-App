package za.ac.cput.prac50.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;

import za.ac.cput.prac50.connection.UnifiedDBConnection;
import za.ac.cput.prac50.dao.*;
import za.ac.za.cput.prac50.domain.*;

/**
 * DashboardGUI - Main Overview Dashboard for LearnHub
 * Shows: Groups enrolled, recent messages, study points, quick stats
 * 
 * @author LearnHub Team
 */
public class DashboardGUI extends JFrame {
    
    // Color Theme
    private static final Color LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color BUTTON_BLUE = new Color(135, 206, 235);
    private static final Color SELECTED_BLUE = new Color(70, 130, 180);
    private static final Color GREY_BACKGROUND = new Color(240, 240, 240);
    private static final Color SUCCESS_GREEN = new Color(76, 175, 80);
    private static final Color WARNING_ORANGE = new Color(255, 152, 0);
    
    // Emoji-supporting font
    private static Font emojiFont;
    
    // Current user from session
    private UnifiedStudent currentStudent;
    
    // DAOs
    private UnifiedStudentDAO studentDAO;
    private UnifiedStudyGroupDAO groupDAO;
    private UnifiedMembershipDAO membershipDAO;
    
    // UI Components
    private JPanel mainContentPanel;
    private JLabel welcomeLabel;
    
    public DashboardGUI() {
        // Initialize emoji font
        initializeEmojiFont();
        
        // Get current student from session
        currentStudent = SessionManager.getInstance().getCurrentStudent();
        
        // Initialize DAOs
        studentDAO = new UnifiedStudentDAO();
        groupDAO = new UnifiedStudyGroupDAO();
        membershipDAO = new UnifiedMembershipDAO();
        
        // Setup UI
        initializeUI();
        loadDashboardData();
    }
    
    /**
     * Initialize a font that supports emoji rendering
     */
    private void initializeEmojiFont() {
        // Try to find a font that supports emojis
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
                    emojiFont = new Font(fontName, Font.PLAIN, 32);
                    return;
                }
            }
        }
        
        // Fallback to default
        emojiFont = new Font("Dialog", Font.PLAIN, 32);
    }
    
    /**
     * Create a JLabel that properly renders emojis
     */
    private JLabel createEmojiLabel(String emoji, int size) {
        JLabel label = new JLabel(emoji);
        Font scaledFont = emojiFont.deriveFont((float) size);
        label.setFont(scaledFont);
        return label;
    }
    
    private void initializeUI() {
        setTitle("LearnHub - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1000, 750);
        setResizable(true);
        
        // Main container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(Color.WHITE);
        
        // Header section
        JPanel headerSection = new JPanel(new BorderLayout());
        headerSection.setBackground(Color.WHITE);
        headerSection.add(HeaderPanelCreator.createHeader(), BorderLayout.NORTH);
        headerSection.add(NavigationPanelCreator.createNavigationPanel(this, "DASHBOARD"), BorderLayout.SOUTH);
        
        mainContainer.add(headerSection, BorderLayout.NORTH);
        
        // Main content area with scroll
        mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBackground(GREY_BACKGROUND);
        mainContentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JScrollPane scrollPane = new JScrollPane(mainContentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        mainContainer.add(scrollPane, BorderLayout.CENTER);
        
        // Footer
        mainContainer.add(createFooter(), BorderLayout.SOUTH);
        
        add(mainContainer);
        setLocationRelativeTo(null);
    }
    
    private void loadDashboardData() {
        mainContentPanel.removeAll();
        
        // Welcome section
        mainContentPanel.add(createWelcomeSection());
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Quick stats cards
        mainContentPanel.add(createQuickStatsSection());
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // My Groups section
        mainContentPanel.add(createMyGroupsSection());
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Recent Activity section
        mainContentPanel.add(createRecentActivitySection());
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Quick Actions section
        mainContentPanel.add(createQuickActionsSection());
        
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
    
    // ===== WELCOME SECTION =====
    
    private JPanel createWelcomeSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE, 2),
            new EmptyBorder(20, 25, 20, 25)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        // Welcome text
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        
        // Create welcome label with emoji
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel welcomeText = new JLabel("Welcome back, " + currentStudent.getFirstName() + "! ");
        welcomeText.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeText.setForeground(SELECTED_BLUE);
        
        JLabel waveEmoji = createEmojiLabel("👋", 24);
        
        welcomePanel.add(welcomeText);
        welcomePanel.add(waveEmoji);
        
        JLabel subLabel = new JLabel("Here's what's happening with your study groups today");
        subLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subLabel.setForeground(Color.GRAY);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textPanel.add(welcomePanel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(subLabel);
        
        // Time-based greeting icon
        JLabel iconLabel = createEmojiLabel(getGreetingIcon(), 48);
        
        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(iconLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private String getGreetingIcon() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) return "☀️";
        if (hour < 18) return "🌤️";
        return "🌙";
    }
    
    // ===== QUICK STATS SECTION =====
    
    private JPanel createQuickStatsSection() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(GREY_BACKGROUND);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        // Get stats
        int myGroupsCount = getMyGroupsCount();
        int totalStudents = studentDAO.getStudentCount();
        int totalGroups = groupDAO.getTotalGroupCount();
        int availableVenues = 4; // From sample data
        
        panel.add(createStatCard("My Groups", String.valueOf(myGroupsCount), "📚", SUCCESS_GREEN));
        panel.add(createStatCard("Total Students", String.valueOf(totalStudents), "👥", BUTTON_BLUE));
        panel.add(createStatCard("Total Groups", String.valueOf(totalGroups), "🎯", SELECTED_BLUE));
        panel.add(createStatCard("Study Venues", String.valueOf(availableVenues), "📍", WARNING_ORANGE));
        
        return panel;
    }
    
    private JPanel createStatCard(String label, String value, String icon, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Icon with emoji support
        JLabel iconLabel = createEmojiLabel(icon, 32);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Text panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(accentColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Arial", Font.PLAIN, 12));
        labelText.setForeground(Color.GRAY);
        labelText.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        textPanel.add(valueLabel);
        textPanel.add(labelText);
        
        card.add(iconLabel, BorderLayout.NORTH);
        card.add(textPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    // ===== MY GROUPS SECTION =====
    
    private JPanel createMyGroupsSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(GREY_BACKGROUND);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(GREY_BACKGROUND);
        
        JLabel titleLabel = new JLabel("My Study Groups");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(11, 44, 77));
        
        JButton viewAllBtn = createStyledButton("View All", BUTTON_BLUE, 12);
        viewAllBtn.addActionListener(e -> NavigationManager.navigateToGroups(this));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(viewAllBtn, BorderLayout.EAST);
        
        // Groups list
        JPanel groupsPanel = new JPanel();
        groupsPanel.setLayout(new BoxLayout(groupsPanel, BoxLayout.Y_AXIS));
        groupsPanel.setBackground(Color.WHITE);
        groupsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        List<UnifiedMembership> myMemberships = membershipDAO.getAllMembershipsForStudent(currentStudent.getStudentId());
        
        if (myMemberships.isEmpty()) {
            JLabel emptyLabel = new JLabel("You haven't joined any groups yet");
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            groupsPanel.add(emptyLabel);
            
            JButton joinBtn = createStyledButton("Find Groups", SUCCESS_GREEN, 14);
            joinBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            joinBtn.addActionListener(e -> NavigationManager.navigateToGroups(this));
            groupsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            groupsPanel.add(joinBtn);
        } else {
            for (UnifiedMembership membership : myMemberships) {
                StudyGroup group = groupDAO.getGroupById(membership.getGroupId());
                if (group != null) {
                    groupsPanel.add(createGroupCard(group, membership));
                    groupsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(groupsPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        scrollPane.setPreferredSize(new Dimension(0, 200));
        
        section.add(headerPanel, BorderLayout.NORTH);
        section.add(scrollPane, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createGroupCard(StudyGroup group, UnifiedMembership membership) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(GREY_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE, 1),
            new EmptyBorder(10, 15, 10, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(GREY_BACKGROUND);
        
        JLabel nameLabel = new JLabel(group.getGroupName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(SELECTED_BLUE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subjectLabel = new JLabel(group.getSubjectCode() + " • " + group.getCurrentMembers() + "/" + group.getMaxMembers() + " members");
        subjectLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subjectLabel.setForeground(Color.GRAY);
        subjectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(nameLabel);
        infoPanel.add(subjectLabel);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonsPanel.setBackground(GREY_BACKGROUND);
        
        JButton messageBtn = createStyledButton("Messages", BUTTON_BLUE, 11);
        messageBtn.addActionListener(e -> NavigationManager.navigateToMessages(this));
        
        JButton locationBtn = createStyledButton("Location", SUCCESS_GREEN, 11);
        locationBtn.addActionListener(e -> NavigationManager.navigateToLocation(this));
        
        buttonsPanel.add(messageBtn);
        buttonsPanel.add(locationBtn);
        
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buttonsPanel, BorderLayout.EAST);
        
        return card;
    }
    
    // ===== RECENT ACTIVITY SECTION =====
    
    private JPanel createRecentActivitySection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(GREY_BACKGROUND);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        JLabel titleLabel = new JLabel("Recent Activity");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(11, 44, 77));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JPanel activityPanel = new JPanel();
        activityPanel.setLayout(new BoxLayout(activityPanel, BoxLayout.Y_AXIS));
        activityPanel.setBackground(Color.WHITE);
        activityPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Sample activities
        List<UnifiedMembership> memberships = membershipDAO.getAllMembershipsForStudent(currentStudent.getStudentId());
        
        if (memberships.isEmpty()) {
            JLabel emptyLabel = new JLabel("No recent activity");
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            emptyLabel.setForeground(Color.GRAY);
            activityPanel.add(emptyLabel);
        } else {
            for (UnifiedMembership membership : memberships) {
                if (membership.isRecentMembership()) {
                    StudyGroup group = groupDAO.getGroupById(membership.getGroupId());
                    if (group != null) {
                        activityPanel.add(createActivityItem(
                            "Joined " + group.getGroupName(),
                            membership.getDaysSinceJoined() + " days ago",
                            "✅"
                        ));
                        activityPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                    }
                }
            }
            
            if (activityPanel.getComponentCount() == 0) {
                JLabel emptyLabel = new JLabel("No recent activity in the last 7 days");
                emptyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                emptyLabel.setForeground(Color.GRAY);
                activityPanel.add(emptyLabel);
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(activityPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        
        section.add(titleLabel, BorderLayout.NORTH);
        section.add(scrollPane, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createActivityItem(String text, String time, String icon) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(Color.WHITE);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        // Icon with emoji support
        JLabel iconLabel = createEmojiLabel(icon, 18);
        iconLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        textLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        timeLabel.setForeground(Color.GRAY);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textPanel.add(textLabel);
        textPanel.add(timeLabel);
        
        item.add(iconLabel, BorderLayout.WEST);
        item.add(textPanel, BorderLayout.CENTER);
        
        return item;
    }
    
    // ===== QUICK ACTIONS SECTION =====
    
    private JPanel createQuickActionsSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(GREY_BACKGROUND);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        
        JLabel titleLabel = new JLabel("Quick Actions");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(11, 44, 77));
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        JPanel actionsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        actionsPanel.setBackground(GREY_BACKGROUND);
        
        actionsPanel.add(createActionCard("Create Group", "📝", SUCCESS_GREEN, e -> {
            new CreateNewGroupGUI().setVisible(true);
            dispose();
        }));
        
        actionsPanel.add(createActionCard("Find Groups", "🔍", BUTTON_BLUE, e -> {
            NavigationManager.navigateToGroups(this);
        }));
        
        actionsPanel.add(createActionCard("View Map", "🗺️", SELECTED_BLUE, e -> {
            NavigationManager.navigateToLocation(this);
        }));
        
        actionsPanel.add(createActionCard("Study Points", "📍", WARNING_ORANGE, e -> {
            NavigationManager.navigateToStudyPoints(this);
        }));
        
        actionsPanel.add(createActionCard("Messages", "💬", new Color(156, 39, 176), e -> {
            NavigationManager.navigateToMessages(this);
        }));
        
        actionsPanel.add(createActionCard("Profile", "👤", new Color(96, 125, 139), e -> {
            NavigationManager.navigateToProfile(this);
        }));
        
        section.add(titleLabel, BorderLayout.NORTH);
        section.add(actionsPanel, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createActionCard(String title, String icon, Color color, ActionListener action) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Icon with emoji support
        JLabel iconLabel = createEmojiLabel(icon, 32);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(color);
        
        card.add(iconLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);
        
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.actionPerformed(new ActionEvent(card, ActionEvent.ACTION_PERFORMED, ""));
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(GREY_BACKGROUND);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });
        
        return card;
    }
    
    // ===== HELPER METHODS =====
    
    private int getMyGroupsCount() {
        try {
            return membershipDAO.getAllMembershipsForStudent(currentStudent.getStudentId()).size();
        } catch (Exception e) {
            return 0;
        }
    }
    
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
    
}