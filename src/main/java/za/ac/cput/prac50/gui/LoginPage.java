package za.ac.cput.prac50.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import za.ac.cput.prac50.dao.UnifiedStudentDAO;
import za.ac.za.cput.prac50.domain.UnifiedStudent;

/**
 * LoginPage - Modern styled version with SESSION MANAGEMENT
 * Sets the current student in SessionManager after successful login
 * @author PC
 */
public class LoginPage extends JFrame {
    
    private JTextField emailField;
    private JPasswordField passwordField;
    
    public LoginPage() {
        setTitle("LearnHub - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        
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
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        panel.add(createNorthPanel(), BorderLayout.NORTH);
        panel.add(createCenterPanel(), BorderLayout.CENTER);
        panel.add(createSouthPanel(), BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createNorthPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        JLabel titleLabel = new JLabel("Dive into the world of learning", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(new Color(50, 50, 50));
        
        JLabel titleLabel1 = new JLabel("Sign in", JLabel.CENTER);
        titleLabel1.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel1.setForeground(new Color(50, 50, 50));
        
        JLabel subtitleLabel = new JLabel("<html>Discover fun, interact with pupils who are like-minded,<br>and grow your knowledge.</html>", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        
        panel.add(titleLabel);
        panel.add(titleLabel1);
        panel.add(subtitleLabel);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JLabel emailLabel = new JLabel("Student Email");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        emailField = new JTextField();
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel passwordLabelPanel = new JPanel(new BorderLayout());
        passwordLabelPanel.setBackground(Color.WHITE);
        passwordLabelPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        passwordLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JLabel forgotPasswordLink = new JLabel("Forgot password?");
        forgotPasswordLink.setFont(new Font("Arial", Font.PLAIN, 12));
        forgotPasswordLink.setForeground(new Color(24, 144, 255));
        forgotPasswordLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        forgotPasswordLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                forgotPasswordLink.setText("<html><u>Forgot password?</u></html>");
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                forgotPasswordLink.setText("Forgot password?");
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                LoginPage.this.dispose();
                new SetPassword().setVisible(true);
            }
        });
        
        passwordLabelPanel.add(passwordLabel, BorderLayout.WEST);
        passwordLabelPanel.add(forgotPasswordLink, BorderLayout.EAST);
        
        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add Enter key listener for password field
        passwordField.addActionListener(e -> performLogin());
        
        panel.add(emailLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(emailField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(passwordLabelPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(passwordField);
        
        return panel;
    }
    
    private JPanel createSouthPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(24, 144, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setPreferredSize(new Dimension(120, 40));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        loginButton.addActionListener(e -> performLogin());
        
        JLabel signUpLink = new JLabel("Create a new account");
        signUpLink.setFont(new Font("Arial", Font.PLAIN, 13));
        signUpLink.setForeground(new Color(24, 144, 255));
        signUpLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        signUpLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                signUpLink.setText("<html><u>Create a new account</u></html>");
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                signUpLink.setText("Create a new account");
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                LoginPage.this.dispose();
                new RegisterPage().setVisible(true);
            }
        });
        
        panel.add(loginButton, BorderLayout.WEST);
        panel.add(signUpLink, BorderLayout.EAST);
        
        return panel;
    }
    
    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both email and password.", 
                "Login Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            UnifiedStudentDAO studentDAO = new UnifiedStudentDAO();
            UnifiedStudent student = studentDAO.authenticate(email, password);
            
            if (student != null) {
                // ✅ SET SESSION - This is the key addition!
                SessionManager.getInstance().setCurrentStudent(student);
                
                // Show success message with student info
                JOptionPane.showMessageDialog(this, 
                    "Welcome back, " + student.getFullName() + "!\n\n" +
                    "Student Number: " + student.getStudentId() + "\n" +
                    "Email: " + student.getEmail(), 
                    "Login Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Navigate to Dashboard
                new DashboardGUI().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Invalid email or password!\n\nPlease check your credentials and try again.", 
                    "Login Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Database error: " + ex.getMessage() + "\n\nPlease try again later.", 
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
            new LoginPage();
        });
    }
}