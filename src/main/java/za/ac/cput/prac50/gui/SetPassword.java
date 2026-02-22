package za.ac.cput.prac50.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import za.ac.cput.prac50.dao.UnifiedStudentDAO;

/**
 * SetPassword - Modern styled version with password reset functionality
 * @author PC
 */
public class SetPassword extends JFrame {
    
    private JTextField emailField;
    private JComboBox<String> securityComboBox;
    private JTextField answerField;
    private JPasswordField setPasswordField;
    private JPasswordField repeatPasswordField;

    public SetPassword() {
        setTitle("New Password Reset - Learning Platform");
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
        String text = "Image here";
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
        contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

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
                logoLabel.setText("LOGO Image");
                logoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                logoLabel.setForeground(new Color(150, 150, 150));
            }
        } catch (Exception e) {
            logoLabel.setText("LOGO Image");
            logoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            logoLabel.setForeground(new Color(150, 150, 150));
        }

        // Title label
        JLabel titleLabel = new JLabel("Reset Password");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(50, 50, 50));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(logoLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(titleLabel);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

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
        JLabel securityLabel = new JLabel("Security question");
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
        securityComboBox.setBackground(new Color(80, 80, 80));
        securityComboBox.setForeground(Color.WHITE);
        securityComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Answer field
        JLabel answerLabel = new JLabel("Answer");
        answerLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        answerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        answerField = new JTextField();
        answerField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        answerField.setFont(new Font("Arial", Font.PLAIN, 13));
        answerField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        answerField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Set Password field
        JLabel setPasswordLabel = new JLabel("Set Password:");
        setPasswordLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        setPasswordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        setPasswordField = new JPasswordField();
        setPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        setPasswordField.setFont(new Font("Arial", Font.PLAIN, 13));
        setPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        setPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        panel.add(emailLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(emailField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(securityLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(securityComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(answerLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(answerField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(setPasswordLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(setPasswordField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(repeatPasswordLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(repeatPasswordField);

        return panel;
    }

    private JPanel createSouthPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Login button (left)
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 13));
        loginButton.setBackground(new Color(24, 144, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setPreferredSize(new Dimension(100, 35));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        loginButton.addActionListener(e -> {
            SetPassword.this.dispose();
            new LoginPage().setVisible(true);
        });

        // Save button (right)
        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Arial", Font.BOLD, 13));
        saveButton.setBackground(new Color(24, 144, 255));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setPreferredSize(new Dimension(100, 35));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        saveButton.addActionListener(e -> performPasswordReset());

        panel.add(loginButton, BorderLayout.WEST);
        panel.add(saveButton, BorderLayout.EAST);

        return panel;
    }

    private void performPasswordReset() {
        String mail = emailField.getText().trim();
        String box = (String) securityComboBox.getSelectedItem();
        String answer = answerField.getText().trim();
        String password = new String(setPasswordField.getPassword());
        String rpassword = new String(repeatPasswordField.getPassword());

        // Validation
        if (mail.isEmpty() || box.isEmpty() || answer.isEmpty() || password.isEmpty() || rpassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "All fields are required!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (box.equals("<Select question>")) {
            JOptionPane.showMessageDialog(this, 
                "Please select a security question!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(rpassword)) {
            JOptionPane.showMessageDialog(this, 
                "Passwords do not match!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            UnifiedStudentDAO dao = new UnifiedStudentDAO();
            boolean success = dao.resetPassword(mail, answer, password);

            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Password reset successful!",
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);

                // Clear fields
                emailField.setText("");
                securityComboBox.setSelectedIndex(0);
                answerField.setText("");
                setPasswordField.setText("");
                repeatPasswordField.setText("");

                // Go to login
                this.dispose();
                new LoginPage().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Password reset failed. Please check your email and security answer.",
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage(),
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
            new SetPassword();
        });
    }
}