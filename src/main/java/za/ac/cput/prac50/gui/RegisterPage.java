package za.ac.cput.prac50.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import za.ac.cput.prac50.dao.UnifiedStudentDAO;
import za.ac.za.cput.prac50.domain.UnifiedStudent;

/**
 * RegisterPage - Modern styled version with registration functionality
 * @author PC
 */
public class RegisterPage extends JFrame {
    
    // Form fields
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField studentNumberField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField repeatPasswordField;
    private JComboBox<String> securityComboBox;
    private JTextField securityAnswerField;

    public RegisterPage() {
        setTitle("Create New Account - LearnHub");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Main container with JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setEnabled(false);

        splitPane.setLeftComponent(createLeftPanel());
        splitPane.setRightComponent(createRightPanel());

        add(splitPane);
        setVisible(true);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(24, 144, 255));

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);

        try {
            ImageIcon icon = new ImageIcon("images/Reading book-pana.png");
            if (icon.getIconWidth() <= 0) {
                imageLabel.setIcon(createPlaceholderIcon());
            } else {
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(350, 350, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImg));
            }
        } catch (Exception e) {
            imageLabel.setIcon(createPlaceholderIcon());
        }

        panel.add(imageLabel, BorderLayout.CENTER);
        return panel;
    }

    private ImageIcon createPlaceholderIcon() {
        int width = 300, height = 300;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.fillRoundRect(20, 20, width - 40, height - 40, 30, 30);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        String text = "Learning";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, (width - textWidth) / 2, height / 2);

        g2d.dispose();
        return new ImageIcon(img);
    }

    private JPanel createRightPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Content panel that will be scrollable
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        contentPanel.add(createNorthPanel(), BorderLayout.NORTH);
        contentPanel.add(createCenterPanel(), BorderLayout.CENTER);
        contentPanel.add(createSouthPanel(), BorderLayout.SOUTH);

        // Wrap content in JScrollPane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createNorthPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Logo with proper error handling
        JLabel logoLabel = new JLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        try {
            ImageIcon logoIcon = new ImageIcon("images/Screenshot 2025-07-11 165655.png");
            if (logoIcon.getIconWidth() > 0) {
                Image img = logoIcon.getImage();
                Image scaledImg = img.getScaledInstance(58, 56, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaledImg));
            } else {
                logoLabel.setText("LearnHub");
                logoLabel.setFont(new Font("Arial", Font.BOLD, 18));
                logoLabel.setForeground(new Color(24, 144, 255));
            }
        } catch (Exception e) {
            logoLabel.setText("LearnHub");
            logoLabel.setFont(new Font("Arial", Font.BOLD, 18));
            logoLabel.setForeground(new Color(24, 144, 255));
        }

        // Title label
        JLabel titleLabel = new JLabel("Create Your Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(new Color(50, 50, 50));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Join LearnHub and start your learning journey");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(logoLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(subtitleLabel);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // First name field
        JLabel firstNameLabel = new JLabel("First name:");
        firstNameLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        firstNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        firstNameField = new JTextField();
        firstNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        firstNameField.setFont(new Font("Arial", Font.PLAIN, 13));
        firstNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        firstNameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Last name field
        JLabel lastNameLabel = new JLabel("Last name:");
        lastNameLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        lastNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        lastNameField = new JTextField();
        lastNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        lastNameField.setFont(new Font("Arial", Font.PLAIN, 13));
        lastNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        lastNameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Student number field
        JLabel studentNumberLabel = new JLabel("Student number:");
        studentNumberLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        studentNumberLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        studentNumberField = new JTextField();
        studentNumberField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        studentNumberField.setFont(new Font("Arial", Font.PLAIN, 13));
        studentNumberField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        studentNumberField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Student email field
        JLabel emailLabel = new JLabel("Student email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        emailField = new JTextField();
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        emailField.setFont(new Font("Arial", Font.PLAIN, 13));
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Security question
        JLabel securityLabel = new JLabel("Security question:");
        securityLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        securityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] securityQuestions = {
            "<Select question>",
            "What is your pet name?",
            "Who is your role model?",
            "When is your birthday?",
            "What is your favourite movie?"
        };

        securityComboBox = new JComboBox<>(securityQuestions);
        securityComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        securityComboBox.setFont(new Font("Arial", Font.PLAIN, 13));
        securityComboBox.setBackground(Color.WHITE);
        securityComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Security answer field
        JLabel answerLabel = new JLabel("Answer:");
        answerLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        answerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        securityAnswerField = new JTextField();
        securityAnswerField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        securityAnswerField.setFont(new Font("Arial", Font.PLAIN, 13));
        securityAnswerField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        securityAnswerField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 13));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Repeat password field
        JLabel repeatPasswordLabel = new JLabel("Repeat Password:");
        repeatPasswordLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        repeatPasswordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        repeatPasswordField = new JPasswordField();
        repeatPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        repeatPasswordField.setFont(new Font("Arial", Font.PLAIN, 13));
        repeatPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        repeatPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add all components with spacing
        panel.add(firstNameLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(firstNameField);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        panel.add(lastNameLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(lastNameField);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        panel.add(studentNumberLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(studentNumberField);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        panel.add(emailLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(emailField);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        panel.add(securityLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(securityComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        panel.add(answerLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(securityAnswerField);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        panel.add(passwordLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(passwordField);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        panel.add(repeatPasswordLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(repeatPasswordField);

        return panel;
    }

    private JPanel createSouthPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Create Account button (left)
        JButton createButton = new JButton("Create Account");
        createButton.setFont(new Font("Arial", Font.BOLD, 13));
        createButton.setBackground(new Color(24, 144, 255));
        createButton.setForeground(Color.WHITE);
        createButton.setFocusPainted(false);
        createButton.setBorderPainted(false);
        createButton.setPreferredSize(new Dimension(130, 35));
        createButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        createButton.addActionListener(e -> createAccount());

        // Login link (right)
        JLabel loginLink = new JLabel("Already have an account? Login");
        loginLink.setFont(new Font("Arial", Font.PLAIN, 13));
        loginLink.setForeground(new Color(24, 144, 255));
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginLink.setText("<html><u>Already have an account? Login</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginLink.setText("Already have an account? Login");
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                RegisterPage.this.dispose();
                new LoginPage().setVisible(true);
            }
        });

        panel.add(createButton, BorderLayout.WEST);
        panel.add(loginLink, BorderLayout.EAST);

        return panel;
    }
    
    private void createAccount() {
        // Get all field values
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String studentNumber = studentNumberField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String repeatPassword = new String(repeatPasswordField.getPassword());
        String securityQuestion = (String) securityComboBox.getSelectedItem();
        String securityAnswer = securityAnswerField.getText().trim();
        
        // Validation
        if (firstName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your first name.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            firstNameField.requestFocus();
            return;
        }
        
        if (lastName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your last name.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            lastNameField.requestFocus();
            return;
        }
        
        if (studentNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your student number.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            studentNumberField.requestFocus();
            return;
        }
        
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email address.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            emailField.requestFocus();
            return;
        }
        
        // Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            emailField.requestFocus();
            return;
        }
        
        if (securityQuestion.equals("<Select question>")) {
            JOptionPane.showMessageDialog(this, "Please select a security question.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            securityComboBox.requestFocus();
            return;
        }
        
        if (securityAnswer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide an answer to the security question.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            securityAnswerField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a password.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            passwordField.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            passwordField.requestFocus();
            return;
        }
        
        if (!password.equals(repeatPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            repeatPasswordField.requestFocus();
            return;
        }
        
        try {
            // Use the student number entered by user
            String studentId = studentNumber;
            
            // Create unified student object
            UnifiedStudent student = new UnifiedStudent();
            student.setStudentId(studentId);
            student.setFirstName(firstName);
            student.setLastName(lastName);
            student.setEmail(email);
            student.setPassword(password);
            student.setYear("Second Year"); // Default or add field
            student.setStream("Application Development"); // Default or add field
            student.setCourse("Diploma in ICT"); // Default or add field
            
            // Use unified DAO to register
            UnifiedStudentDAO dao = new UnifiedStudentDAO();
            boolean success = dao.registerStudent(student, securityQuestion, securityAnswer);
            
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Welcome to LearnHub, " + firstName + "!\n\n" +
                    "Your account has been created successfully.\n" +
                    "Your Student ID: " + studentId,
                    "Account Created", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Clear fields
                firstNameField.setText("");
                lastNameField.setText("");
                studentNumberField.setText("");
                emailField.setText("");
                securityComboBox.setSelectedIndex(0);
                securityAnswerField.setText("");
                passwordField.setText("");
                repeatPasswordField.setText("");
                
                // Navigate to login
                this.dispose();
                new LoginPage().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Registration failed. Please try again.",
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error during registration: " + ex.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new RegisterPage();
        });
    }
}