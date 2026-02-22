package za.ac.cput.prac50.gui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import za.ac.cput.prac50.connection.UnifiedDBConnection;
import za.ac.cput.prac50.dao.UnifiedMembershipDAO;
import za.ac.cput.prac50.dao.UnifiedStudentDAO;
import za.ac.cput.prac50.dao.UnifiedStudyGroupDAO;
import za.ac.za.cput.prac50.domain.StudyGroup;
import za.ac.za.cput.prac50.domain.UnifiedStudent;

/**
 * LearnHubGroups - UPDATED with SessionManager Integration
 * Group messaging and discussion platform
 * @author LearnHub Team
 */
public class LearnHubGroups extends JFrame {

    // ===== COLOR THEME =====
    public static class ColorTheme {
        public static final Color PRIMARY = new Color(66, 133, 244);
        public static final Color PRIMARY_DARK = new Color(25, 118, 210);
        public static final Color ACCENT = new Color(66, 133, 244);
        public static final Color BACKGROUND = new Color(255, 255, 255);
        public static final Color SURFACE = new Color(248, 249, 250);
        public static final Color TEXT_PRIMARY = new Color(33, 33, 33);
        public static final Color TEXT_SECONDARY = new Color(95, 99, 104);
        public static final Color SUCCESS = new Color(52, 168, 83);
        public static final Color WARNING = new Color(255, 152, 0);
        public static final Color ERROR = new Color(234, 67, 53);
        public static final Color ONLINE = new Color(52, 168, 83);
        public static final Color OFFLINE = new Color(189, 189, 189);
        public static final Color MESSAGE_SENT = new Color(220, 248, 198);
        public static final Color MESSAGE_RECEIVED = Color.WHITE;
        public static final Color INFO = new Color(255, 192, 203);
        public static final Color BORDER = new Color(218, 220, 224);
        public static final Color HOVER = new Color(241, 243, 244);
    }

    // ===== DATA CLASSES =====
    public static class User {
        String username, displayName, email, status;
        Color avatarColor;
        boolean isAdmin;

        public User(String username, String displayName, String email) {
            this.username = username;
            this.displayName = displayName;
            this.email = email;
            this.status = "online";
            this.avatarColor = generateAvatarColor();
            this.isAdmin = false;
        }

        private Color generateAvatarColor() {
            Random rand = new Random(username.hashCode());
            int hue = rand.nextInt(360);
            return Color.getHSBColor(hue / 360f, 0.6f, 0.9f);
        }
        
        public String getInitials() {
            String[] parts = displayName.split(" ");
            if (parts.length >= 2) {
                return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
            }
            return displayName.substring(0, Math.min(2, displayName.length())).toUpperCase();
        }
    }

    public static class Group {
        String name, description;
        User admin;
        Set<User> members;
        List<Message> messages;
        boolean isPrivate;

        public Group(String name, String description, User admin) {
            this.name = name;
            this.description = description;
            this.admin = admin;
            this.members = new HashSet<>();
            this.messages = new ArrayList<>();
            this.isPrivate = false;
            members.add(admin);
        }
    }

    public static class Message {
        User sender;
        String content;
        LocalDateTime timestamp;
        MessageType type;

        public enum MessageType { TEXT, SYSTEM, JOIN, LEAVE }

        public Message(User sender, String content, MessageType type) {
            this.sender = sender;
            this.content = content;
            this.type = type;
            this.timestamp = LocalDateTime.now();
        }
    }

    // ===== INSTANCE VARIABLES =====
    private UnifiedStudent currentStudent;
    private User currentUser;
    private Map<String, Group> allGroups;
    private Set<String> joinedGroupNames;
    private String selectedGroupName;
    private Map<String, User> allUsers;

    private JList<String> availableGroupsList;
    private JList<String> joinedGroupsList;
    private JPanel messagesPanel;
    private JScrollPane messagesScrollPane;
    private JTextField messageInput;
    private JButton joinButton, sendButton, createGroupButton;
    private JLabel statusLabel, currentGroupLabel;
    private JList<String> membersList;
    private JPanel emptyStatePanel;
    
    // Emoji font
    private static Font emojiFont;

    public LearnHubGroups() {
        currentStudent = SessionManager.getInstance().getCurrentStudent();
        
        if (currentStudent == null) {
            System.out.println("⚠️ Warning: No student in session, loading default");
            currentStudent = SessionManager.getInstance().getDefaultStudent();
        }
        
        System.out.println("✅ LearnHubGroups loaded for: " + currentStudent.getFullName());
        
        initializeEmojiFont();
        initializeData();
        initializeGUI();
        setupEventHandlers();
        startPeriodicUpdates();
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
                    emojiFont = new Font(fontName, Font.PLAIN, 18);
                    return;
                }
            }
        }
        
        // Fallback to default
        emojiFont = new Font("Dialog", Font.PLAIN, 18);
    }

    private void initializeData() {
        currentUser = new User(
            currentStudent.getStudentId(),
            currentStudent.getFullName(),
            currentStudent.getEmail()
        );
        allGroups = new HashMap<>();
        joinedGroupNames = new HashSet<>();
        allUsers = new HashMap<>();
        selectedGroupName = null;
        allUsers.put(currentUser.username, currentUser);

        loadUsersFromDatabase();
        loadGroupsFromDatabase();
    }

    private void loadGroupsFromDatabase() {
        try {
            UnifiedStudyGroupDAO groupDAO = new UnifiedStudyGroupDAO();
            List<StudyGroup> dbGroups = groupDAO.getAllGroups();

            for (StudyGroup dbGroup : dbGroups) {
                User admin = allUsers.getOrDefault(currentUser.username, currentUser);

                Group group = new Group(
                    dbGroup.getGroupName(),
                    dbGroup.getDescription(),
                    admin
                );

                UnifiedMembershipDAO membershipDAO = new UnifiedMembershipDAO();
                List<String> memberIds = membershipDAO.getGroupMembers(dbGroup.getGroupId());

                for (String memberId : memberIds) {
                    User memberUser = allUsers.get(memberId);
                    if (memberUser == null) {
                        UnifiedStudentDAO studentDAO = new UnifiedStudentDAO();
                        UnifiedStudent student = studentDAO.getStudentById(memberId);
                        if (student != null) {
                            memberUser = new User(
                                student.getStudentId(),
                                student.getFullName(),
                                student.getEmail()
                            );
                            allUsers.put(memberId, memberUser);
                        }
                    }
                    if (memberUser != null) {
                        group.members.add(memberUser);
                    }

                    if (memberId.equals(currentStudent.getStudentId())) {
                        joinedGroupNames.add(group.name);
                    }
                }

                loadMessagesForGroup(group, dbGroup.getGroupId());
                allGroups.put(group.name, group);
            }

            System.out.println("✅ Loaded " + allGroups.size() + " groups with messages");

        } catch (Exception e) {
            System.err.println("❌ Error loading groups: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMessagesForGroup(Group group, int groupId) {
        try {
            Connection conn = UnifiedDBConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT m.*, s.first_name, s.last_name FROM messages m " +
                "JOIN students s ON m.student_id = s.student_id " +
                "WHERE m.group_id = ? ORDER BY m.sent_at ASC"
            );
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String studentId = rs.getString("student_id");
                String content = rs.getString("message_content");
                String messageType = rs.getString("message_type");

                User sender = allUsers.get(studentId);
                if (sender == null) {
                    String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                    sender = new User(studentId, fullName, studentId + "@mycput.ac.za");
                    allUsers.put(studentId, sender);
                }

                Message.MessageType type = Message.MessageType.valueOf(messageType);
                Message message = new Message(sender, content, type);
                group.messages.add(message);
            }

            rs.close();
            stmt.close();

        } catch (Exception e) {
            System.err.println("Error loading messages for group: " + e.getMessage());
        }
    }

    private void loadUsersFromDatabase() {
        try {
            UnifiedStudentDAO studentDAO = new UnifiedStudentDAO();
            List<UnifiedStudent> students = studentDAO.getAllStudents();

            for (UnifiedStudent student : students) {
                if (!allUsers.containsKey(student.getStudentId())) {
                    User user = new User(
                        student.getStudentId(),
                        student.getFullName(),
                        student.getEmail()
                    );
                    allUsers.put(student.getStudentId(), user);
                }
            }

            System.out.println("✅ Loaded " + allUsers.size() + " users");

        } catch (Exception e) {
            System.err.println("❌ Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeGUI() {
        setTitle("LearnHub - Messages - " + currentStudent.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header (keep unchanged)
        JPanel headerSection = new JPanel(new BorderLayout());
        headerSection.setBackground(Color.WHITE);
        headerSection.add(HeaderPanelCreator.createHeader(), BorderLayout.NORTH);
        headerSection.add(NavigationPanelCreator.createNavigationPanel(this, "MESSAGES"), BorderLayout.SOUTH);
        
        add(headerSection, BorderLayout.NORTH);

        // Main content
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(ColorTheme.BACKGROUND);
        
        mainContent.add(createLeftPanel(), BorderLayout.WEST);
        mainContent.add(createCenterPanel(), BorderLayout.CENTER);
        mainContent.add(createRightPanel(), BorderLayout.EAST);
        
        add(mainContent, BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(300, 0));
        leftPanel.setBackground(ColorTheme.SURFACE);
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColorTheme.BORDER));

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(ColorTheme.SURFACE);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Available Groups Tab
        JPanel availablePanel = new JPanel(new BorderLayout());
        availablePanel.setBackground(ColorTheme.SURFACE);

        availableGroupsList = new JList<>();
        availableGroupsList.setBackground(ColorTheme.SURFACE);
        availableGroupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        availableGroupsList.setCellRenderer(new ModernGroupCellRenderer());
        availableGroupsList.setFixedCellHeight(70);
        updateAvailableGroupsList();

        JScrollPane availableScroll = new JScrollPane(availableGroupsList);
        availableScroll.setBorder(BorderFactory.createEmptyBorder());
        availableScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(ColorTheme.SURFACE);
        buttonPanel.setBorder(new EmptyBorder(10, 15, 15, 15));

        joinButton = createModernButton("⬅ Join", ColorTheme.SURFACE, ColorTheme.TEXT_PRIMARY);
        joinButton.setFont(emojiFont.deriveFont(Font.BOLD, 13f));
        joinButton.setEnabled(false);
        joinButton.addActionListener(e -> joinSelectedGroup());

        createGroupButton = createModernButton("+ Create", ColorTheme.PRIMARY, Color.WHITE);
        createGroupButton.addActionListener(e -> showCreateGroupDialog());

        buttonPanel.add(joinButton);
        buttonPanel.add(createGroupButton);

        availablePanel.add(availableScroll, BorderLayout.CENTER);
        availablePanel.add(buttonPanel, BorderLayout.SOUTH);

        // My Groups Tab
        JPanel joinedPanel = new JPanel(new BorderLayout());
        joinedPanel.setBackground(ColorTheme.SURFACE);

        joinedGroupsList = new JList<>();
        joinedGroupsList.setBackground(ColorTheme.SURFACE);
        joinedGroupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        joinedGroupsList.setCellRenderer(new ModernGroupCellRenderer());
        joinedGroupsList.setFixedCellHeight(70);
        updateJoinedGroupsList();

        JScrollPane joinedScroll = new JScrollPane(joinedGroupsList);
        joinedScroll.setBorder(BorderFactory.createEmptyBorder());
        joinedScroll.getVerticalScrollBar().setUnitIncrement(16);

        joinedPanel.add(joinedScroll, BorderLayout.CENTER);

        tabbedPane.addTab("Available", availablePanel);
        tabbedPane.addTab("My Groups", joinedPanel);

        leftPanel.add(tabbedPane, BorderLayout.CENTER);

        return leftPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(ColorTheme.BACKGROUND);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ColorTheme.BORDER),
            new EmptyBorder(15, 20, 15, 20)
        ));

        currentGroupLabel = new JLabel("Select a group to start chatting");
        currentGroupLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        currentGroupLabel.setForeground(ColorTheme.TEXT_PRIMARY);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(Color.WHITE);

        JButton settingsButton = createIconButton("⚙", ColorTheme.TEXT_SECONDARY);
        settingsButton.addActionListener(e -> showGroupSettings());
        settingsButton.setToolTipText("Settings");

        JButton leaveButton = createIconButton("🚪", ColorTheme.ERROR);
        leaveButton.addActionListener(e -> leaveCurrentGroup());
        leaveButton.setToolTipText("Leave");

        actionPanel.add(settingsButton);
        actionPanel.add(leaveButton);

        headerPanel.add(currentGroupLabel, BorderLayout.CENTER);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        // Messages area
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(ColorTheme.BACKGROUND);

        messagesScrollPane = new JScrollPane(messagesPanel);
        messagesScrollPane.setBorder(BorderFactory.createEmptyBorder());
        messagesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        messagesScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Empty state
        emptyStatePanel = createEmptyStatePanel();
        
        // Input panel
        JPanel inputPanel = createModernInputPanel();

        centerPanel.add(headerPanel, BorderLayout.NORTH);
        centerPanel.add(messagesScrollPane, BorderLayout.CENTER);
        centerPanel.add(inputPanel, BorderLayout.SOUTH);

        // Show empty state initially
        messagesPanel.add(emptyStatePanel);

        return centerPanel;
    }

    private JPanel createEmptyStatePanel() {
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setBackground(ColorTheme.BACKGROUND);
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ColorTheme.BACKGROUND);
        
        // Icon
        JLabel iconLabel = new JLabel("💬");
        iconLabel.setFont(emojiFont.deriveFont(64f));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Title
        JLabel titleLabel = new JLabel("Welcome to ADF GROUP");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(ColorTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("<html><center>This is the beginning of your chat history. Start the<br>conversation by sending a message below.</center></html>");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(ColorTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        content.add(iconLabel);
        content.add(Box.createVerticalStrut(20));
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(subtitleLabel);
        
        emptyPanel.add(content);
        return emptyPanel;
    }

    private JPanel createModernInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ColorTheme.BORDER),
            new EmptyBorder(15, 20, 15, 20)
        ));

        messageInput = new JTextField();
        messageInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        messageInput.setBackground(ColorTheme.SURFACE);
        messageInput.setEnabled(false);
        messageInput.addActionListener(e -> sendMessage());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        // Emoji button with proper emoji font
        JButton emojiButton = new JButton("😊");
        emojiButton.setFont(emojiFont);
        emojiButton.setForeground(ColorTheme.TEXT_SECONDARY);
        emojiButton.setBackground(ColorTheme.SURFACE);
        emojiButton.setFocusPainted(false);
        emojiButton.setBorderPainted(false);
        emojiButton.setContentAreaFilled(true);
        emojiButton.setOpaque(true);
        emojiButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        emojiButton.setPreferredSize(new Dimension(40, 40));
        emojiButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        emojiButton.setToolTipText("Add emoji");
        emojiButton.addActionListener(e -> showEmojiPicker());
        
        emojiButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                emojiButton.setBackground(ColorTheme.HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                emojiButton.setBackground(ColorTheme.SURFACE);
            }
        });
        
        sendButton = createModernButton("Send ➤", ColorTheme.PRIMARY, Color.WHITE);
        sendButton.setEnabled(false);
        sendButton.addActionListener(e -> sendMessage());

        buttonPanel.add(emojiButton);
        buttonPanel.add(sendButton);

        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        return inputPanel;
    }
    
    private void showEmojiPicker() {
        String[] emojis = {
            "😊", "😂", "❤️", "👍", "👎", "🎉", "🔥", "⭐", 
            "✅", "❌", "💯", "🙌", "👏", "🤝", "💪", "🙏",
            "😎", "🤔", "😅", "😍", "🥳", "😢", "😭", "😡"
        };
        
        JDialog emojiDialog = new JDialog(this, "Select Emoji", false);
        emojiDialog.setLayout(new GridLayout(0, 8, 5, 5));
        emojiDialog.setSize(400, 200);
        emojiDialog.setLocationRelativeTo(messageInput);
        
        for (String emoji : emojis) {
            JButton emojiBtn = new JButton(emoji);
            emojiBtn.setFont(emojiFont);
            emojiBtn.setPreferredSize(new Dimension(40, 40));
            emojiBtn.setFocusPainted(false);
            emojiBtn.setBorderPainted(false);
            emojiBtn.setBackground(Color.WHITE);
            emojiBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            emojiBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    emojiBtn.setBackground(ColorTheme.HOVER);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    emojiBtn.setBackground(Color.WHITE);
                }
            });
            
            emojiBtn.addActionListener(e -> {
                messageInput.setText(messageInput.getText() + emoji);
                messageInput.requestFocus();
                emojiDialog.dispose();
            });
            
            emojiDialog.add(emojiBtn);
        }
        
        emojiDialog.setVisible(true);
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(250, 0));
        rightPanel.setBackground(ColorTheme.SURFACE);
        rightPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorTheme.BORDER));

        JLabel membersTitle = new JLabel("Members");
        membersTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        membersTitle.setForeground(ColorTheme.TEXT_PRIMARY);
        membersTitle.setBorder(new EmptyBorder(15, 15, 10, 15));

        membersList = new JList<>();
        membersList.setBackground(ColorTheme.SURFACE);
        membersList.setCellRenderer(new ModernMemberCellRenderer());
        membersList.setFixedCellHeight(50);

        JScrollPane membersScroll = new JScrollPane(membersList);
        membersScroll.setBorder(BorderFactory.createEmptyBorder());
        membersScroll.getVerticalScrollBar().setUnitIncrement(16);

        rightPanel.add(membersTitle, BorderLayout.NORTH);
        rightPanel.add(membersScroll, BorderLayout.CENTER);

        return rightPanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ColorTheme.BORDER),
            new EmptyBorder(8, 20, 8, 20)
        ));

        statusLabel = new JLabel("Ready - Join a group to start chatting");
        statusLabel.setForeground(ColorTheme.TEXT_SECONDARY);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel connectionLabel = new JLabel("● Connected");
        connectionLabel.setForeground(ColorTheme.SUCCESS);
        connectionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(connectionLabel, BorderLayout.EAST);

        return bottomPanel;
    }

    // ===== MESSAGE DISPLAY =====

    private void displayGroupMessages(String groupName) {
        Group group = allGroups.get(groupName);
        messagesPanel.removeAll();

        if (group.messages.isEmpty()) {
            // Show empty state with group name
            emptyStatePanel.removeAll();
            emptyStatePanel.setLayout(new GridBagLayout());
            
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(ColorTheme.BACKGROUND);
            
            JLabel iconLabel = new JLabel("💬");
            iconLabel.setFont(emojiFont.deriveFont(64f));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel titleLabel = new JLabel("Welcome to " + groupName);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            titleLabel.setForeground(ColorTheme.TEXT_PRIMARY);
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel subtitleLabel = new JLabel("<html><center>This is the beginning of your chat history. Start the<br>conversation by sending a message below.</center></html>");
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            subtitleLabel.setForeground(ColorTheme.TEXT_SECONDARY);
            subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            content.add(iconLabel);
            content.add(Box.createVerticalStrut(20));
            content.add(titleLabel);
            content.add(Box.createVerticalStrut(10));
            content.add(subtitleLabel);
            
            emptyStatePanel.add(content);
            messagesPanel.add(emptyStatePanel);
        } else {
            messagesPanel.add(Box.createVerticalStrut(20));
            for (Message message : group.messages) {
                JPanel messageContainer = createMessageBubble(message);
                messagesPanel.add(messageContainer);
                messagesPanel.add(Box.createVerticalStrut(8));
            }
        }

        messagesPanel.revalidate();
        messagesPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = messagesScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private JPanel createMessageBubble(Message message) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(2, 20, 2, 20));

        boolean isCurrentUser = message.sender.equals(currentUser);
        boolean isSystemMessage = message.type == Message.MessageType.SYSTEM || 
                                  message.type == Message.MessageType.JOIN || 
                                  message.type == Message.MessageType.LEAVE;

        if (isSystemMessage) {
            JLabel systemLabel = new JLabel(message.content, JLabel.CENTER);
            systemLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            systemLabel.setForeground(ColorTheme.TEXT_SECONDARY);
            systemLabel.setOpaque(true);
            systemLabel.setBackground(ColorTheme.SURFACE);
            systemLabel.setBorder(new EmptyBorder(5, 12, 5, 12));
            container.add(systemLabel, BorderLayout.CENTER);
        } else {
            JPanel bubble = new JPanel(new BorderLayout());
            bubble.setBorder(new EmptyBorder(10, 15, 10, 15));

            if (isCurrentUser) {
                bubble.setBackground(ColorTheme.MESSAGE_SENT);
                container.add(Box.createHorizontalStrut(100), BorderLayout.WEST);
                container.add(bubble, BorderLayout.EAST);
            } else {
                bubble.setBackground(ColorTheme.MESSAGE_RECEIVED);
                bubble.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ColorTheme.BORDER, 1, true),
                        new EmptyBorder(10, 15, 10, 15)
                ));
                container.add(bubble, BorderLayout.WEST);
                container.add(Box.createHorizontalStrut(100), BorderLayout.EAST);
            }

            // Content panel with vertical layout
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setOpaque(false);

            if (!isCurrentUser) {
                JLabel senderLabel = new JLabel(message.sender.displayName);
                senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                senderLabel.setForeground(ColorTheme.PRIMARY);
                senderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPanel.add(senderLabel);
                contentPanel.add(Box.createVerticalStrut(3));
            }

            // Message content - wrapping properly for horizontal display
            JLabel contentLabel = new JLabel("<html><div style='width: 300px;'>" + message.content.replace("\n", "<br>") + "</div></html>");
            contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            contentLabel.setForeground(ColorTheme.TEXT_PRIMARY);
            contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(contentLabel);

            JLabel timeLabel = new JLabel(message.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")));
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            timeLabel.setForeground(ColorTheme.TEXT_SECONDARY);
            timeLabel.setAlignmentX(isCurrentUser ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
            contentPanel.add(Box.createVerticalStrut(3));
            contentPanel.add(timeLabel);

            bubble.add(contentPanel, BorderLayout.CENTER);
        }

        return container;
    }

    // ===== BUTTON CREATION =====

    private JButton createModernButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor.equals(ColorTheme.SURFACE) ? ColorTheme.HOVER : bgColor.darker());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }
    
    private JButton createIconButton(String icon, Color color) {
        JButton button = new JButton(icon);
        button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        button.setForeground(color);
        button.setBackground(ColorTheme.SURFACE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(40, 40));
        button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ColorTheme.HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(ColorTheme.SURFACE);
            }
        });

        return button;
    }

    // ===== CELL RENDERERS =====

    private class ModernGroupCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JPanel panel = new JPanel(new BorderLayout(10, 0));
            panel.setOpaque(true);
            panel.setBorder(new EmptyBorder(10, 15, 10, 15));

            if (value instanceof String) {
                String groupName = (String) value;
                Group group = allGroups.get(groupName);
                if (group != null) {
                    // Group name
                    JLabel nameLabel = new JLabel(groupName);
                    nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    nameLabel.setForeground(ColorTheme.TEXT_PRIMARY);

                    // Member count
                    JLabel countLabel = new JLabel(group.members.size() + " members");
                    countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    countLabel.setForeground(ColorTheme.TEXT_SECONDARY);

                    // Badge
                    JLabel badge = new JLabel(String.valueOf(group.members.size()));
                    badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    badge.setForeground(Color.WHITE);
                    badge.setBackground(ColorTheme.PRIMARY);
                    badge.setOpaque(true);
                    badge.setHorizontalAlignment(SwingConstants.CENTER);
                    badge.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ColorTheme.PRIMARY, 1, true),
                        new EmptyBorder(2, 8, 2, 8)
                    ));

                    JPanel textPanel = new JPanel();
                    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                    textPanel.setOpaque(false);
                    textPanel.add(nameLabel);
                    textPanel.add(Box.createVerticalStrut(3));
                    textPanel.add(countLabel);

                    panel.add(textPanel, BorderLayout.CENTER);
                    panel.add(badge, BorderLayout.EAST);
                }
            }

            if (isSelected) {
                panel.setBackground(ColorTheme.HOVER);
            } else {
                panel.setBackground(ColorTheme.SURFACE);
            }

            return panel;
        }
    }

    private class ModernMemberCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JPanel panel = new JPanel(new BorderLayout(10, 0));
            panel.setOpaque(true);
            panel.setBorder(new EmptyBorder(8, 15, 8, 15));

            if (value instanceof String) {
                String username = (String) value;
                User user = allUsers.get(username);
                if (user != null) {
                    // Avatar
                    JPanel avatar = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            Graphics2D g2 = (Graphics2D) g;
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(user.avatarColor);
                            g2.fillOval(0, 0, getWidth(), getHeight());
                            g2.setColor(Color.WHITE);
                            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                            FontMetrics fm = g2.getFontMetrics();
                            String initials = user.getInitials();
                            int x = (getWidth() - fm.stringWidth(initials)) / 2;
                            int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                            g2.drawString(initials, x, y);
                        }
                    };
                    avatar.setPreferredSize(new Dimension(36, 36));
                    avatar.setOpaque(false);

                    // Name and status
                    JLabel nameLabel = new JLabel(user.displayName);
                    nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    nameLabel.setForeground(ColorTheme.TEXT_PRIMARY);

                    JLabel statusLabel = new JLabel(user.status.equals("online") ? "Online" : "Offline");
                    statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    statusLabel.setForeground(user.status.equals("online") ? ColorTheme.ONLINE : ColorTheme.OFFLINE);

                    JPanel textPanel = new JPanel();
                    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                    textPanel.setOpaque(false);
                    textPanel.add(nameLabel);
                    textPanel.add(statusLabel);

                    // Status indicator
                    JLabel statusDot = new JLabel("●");
                    statusDot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    statusDot.setForeground(user.status.equals("online") ? ColorTheme.ONLINE : ColorTheme.OFFLINE);

                    panel.add(avatar, BorderLayout.WEST);
                    panel.add(textPanel, BorderLayout.CENTER);
                    panel.add(statusDot, BorderLayout.EAST);
                }
            }

            if (isSelected) {
                panel.setBackground(ColorTheme.HOVER);
            } else {
                panel.setBackground(ColorTheme.SURFACE);
            }

            return panel;
        }
    }

    // ===== EVENT HANDLERS =====

    private void setupEventHandlers() {
        availableGroupsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                joinButton.setEnabled(availableGroupsList.getSelectedValue() != null);
            }
        });

        joinedGroupsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = joinedGroupsList.getSelectedValue();
                if (selected != null) {
                    selectGroup(selected);
                }
            }
        });

        availableGroupsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    joinSelectedGroup();
                }
            }
        });

        messageInput.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateSendButton(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateSendButton(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateSendButton(); }

            private void updateSendButton() {
                sendButton.setEnabled(!messageInput.getText().trim().isEmpty() && selectedGroupName != null);
            }
        });
    }

    private void joinSelectedGroup() {
        String selectedGroup = availableGroupsList.getSelectedValue();
        if (selectedGroup == null) {
            showStatus("Please select a group to join", ColorTheme.ERROR);
            return;
        }

        if (joinedGroupNames.contains(selectedGroup)) {
            showStatus("You are already a member of " + selectedGroup, ColorTheme.WARNING);
            return;
        }

        Group group = allGroups.get(selectedGroup);
        group.members.add(currentUser);
        joinedGroupNames.add(selectedGroup);

        Message joinMsg = new Message(currentUser, currentUser.displayName + " joined the group", Message.MessageType.JOIN);
        group.messages.add(joinMsg);
        
        updateAvailableGroupsList();
        updateJoinedGroupsList();

        showStatus("Successfully joined " + selectedGroup + "!", ColorTheme.SUCCESS);

        selectGroup(selectedGroup);
        joinedGroupsList.setSelectedValue(selectedGroup, true);
    }

    private void selectGroup(String groupName) {
        selectedGroupName = groupName;
        Group group = allGroups.get(groupName);

        currentGroupLabel.setText(groupName);

        messageInput.setEnabled(true);
        sendButton.setEnabled(!messageInput.getText().trim().isEmpty());
        messageInput.requestFocus();

        displayGroupMessages(groupName);
        updateMembersList();

        showStatus("Viewing " + groupName, ColorTheme.INFO);
    }

    private void sendMessage() {
        if (selectedGroupName == null || messageInput.getText().trim().isEmpty()) {
            return;
        }
        String messageText = messageInput.getText().trim();
        Group group = allGroups.get(selectedGroupName);
        Message message = new Message(currentUser, messageText, Message.MessageType.TEXT);
        group.messages.add(message);

        saveMessageToDatabase(selectedGroupName, messageText, "TEXT");
        
        displayGroupMessages(selectedGroupName);
        messageInput.setText("");
        sendButton.setEnabled(false);
        showStatus("Message sent", ColorTheme.SUCCESS);
    }

    private void saveMessageToDatabase(String groupName, String content, String messageType) {
        try {
            UnifiedStudyGroupDAO groupDAO = new UnifiedStudyGroupDAO();
            List<StudyGroup> groups = groupDAO.getAllGroups();

            int groupId = -1;
            for (StudyGroup g : groups) {
                if (g.getGroupName().equals(groupName)) {
                    groupId = g.getGroupId();
                    break;
                }
            }

            if (groupId == -1) {
                System.err.println("Group not found: " + groupName);
                return;
            }

            Connection conn = UnifiedDBConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO messages (group_id, student_id, message_content, message_type) VALUES (?, ?, ?, ?)"
            );

            stmt.setInt(1, groupId);
            stmt.setString(2, currentStudent.getStudentId());
            stmt.setString(3, content);
            stmt.setString(4, messageType);

            stmt.executeUpdate();
            stmt.close();

            System.out.println("✅ Message saved to database");

        } catch (Exception e) {
            System.err.println("❌ Error saving message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void leaveCurrentGroup() {
        if (selectedGroupName == null) return;

        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to leave " + selectedGroupName + "?",
                "Leave Group",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            Group group = allGroups.get(selectedGroupName);
            group.members.remove(currentUser);
            joinedGroupNames.remove(selectedGroupName);

            Message leaveMsg = new Message(currentUser, currentUser.displayName + " left the group", Message.MessageType.LEAVE);
            group.messages.add(leaveMsg);

            updateAvailableGroupsList();
            updateJoinedGroupsList();

            selectedGroupName = null;
            currentGroupLabel.setText("Select a group to start chatting");
            messagesPanel.removeAll();
            messagesPanel.add(emptyStatePanel);
            messagesPanel.revalidate();
            messagesPanel.repaint();
            messageInput.setEnabled(false);
            sendButton.setEnabled(false);

            showStatus("Left the group", ColorTheme.INFO);
        }
    }

    private void showGroupSettings() {
        if (selectedGroupName == null) return;

        Group group = allGroups.get(selectedGroupName);
        
        JOptionPane.showMessageDialog(this,
            "Group: " + group.name + "\n" +
            "Description: " + group.description + "\n" +
            "Admin: " + group.admin.displayName + "\n" +
            "Members: " + group.members.size(),
            "Group Settings",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showCreateGroupDialog() {
        String groupName = JOptionPane.showInputDialog(this, "Enter group name:", "Create Group", JOptionPane.PLAIN_MESSAGE);
        
        if (groupName == null || groupName.trim().isEmpty()) {
            return;
        }

        if (allGroups.containsKey(groupName)) {
            JOptionPane.showMessageDialog(this, "Group name already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String description = JOptionPane.showInputDialog(this, "Enter group description:", "Create Group", JOptionPane.PLAIN_MESSAGE);
        
        if (description == null) {
            description = "";
        }

        Group newGroup = new Group(groupName, description, currentUser);
        allGroups.put(groupName, newGroup);
        joinedGroupNames.add(groupName);

        updateAvailableGroupsList();
        updateJoinedGroupsList();

        showStatus("Group '" + groupName + "' created successfully!", ColorTheme.SUCCESS);
    }

    private void updateAvailableGroupsList() {
        java.util.List<String> available = new ArrayList<>();
        for (String groupName : allGroups.keySet()) {
            if (!joinedGroupNames.contains(groupName)) {
                available.add(groupName);
            }
        }
        Collections.sort(available);
        availableGroupsList.setListData(available.toArray(new String[0]));
    }

    private void updateJoinedGroupsList() {
        String[] joined = joinedGroupNames.toArray(new String[0]);
        Arrays.sort(joined);
        joinedGroupsList.setListData(joined);
    }

    private void updateMembersList() {
        if (selectedGroupName != null) {
            Group group = allGroups.get(selectedGroupName);
            java.util.List<String> memberUsernames = group.members.stream()
                    .map(user -> user.username)
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
            membersList.setListData(memberUsernames.toArray(new String[0]));
        }
    }

    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);

        Timer timer = new Timer(3000, e -> {
            statusLabel.setText("Ready - Join a group to start chatting");
            statusLabel.setForeground(ColorTheme.TEXT_SECONDARY);
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void startPeriodicUpdates() {
        Timer updateTimer = new Timer(30000, e -> {
            for (User user : allUsers.values()) {
                if (Math.random() < 0.05) {
                    String[] statuses = {"online", "away", "offline"};
                    user.status = statuses[(int)(Math.random() * statuses.length)];
                }
            }

            if (selectedGroupName != null) {
                updateMembersList();
            }
        });
        updateTimer.start();
    }
}