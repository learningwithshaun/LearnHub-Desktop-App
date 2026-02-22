package za.ac.cput.prac50.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;


/**
 *
 * @author PC
 */
public class WelcomePage extends JFrame {
    private JPanel pnlNorth, pnlCenter, pnlSouth;
    private JLabel lblSalutation, lblLoading;
    private Timer loadingTimer;
    private int currentProgress = 0;
    
    public  WelcomePage() {
        super("LearnHub Launcher");
        initializeComponents();
        startLoadingAnimation();
    }
    
    private void initializeComponents() {
        this.setLayout(new BorderLayout());
        this.setSize(568, 400);
        this.setLocationRelativeTo(null);
        this.getContentPane().setBackground(new Color(24, 144, 255));
        
        // North Panel - Welcome Message
        pnlNorth = new JPanel();
        pnlNorth.setBackground(new Color(24, 144, 255));
        pnlNorth.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        
        lblSalutation = new JLabel("Welcome to LearnHub", JLabel.CENTER);
        lblSalutation.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblSalutation.setForeground(Color.WHITE);
        
        pnlNorth.add(lblSalutation);
        this.add(pnlNorth, BorderLayout.NORTH);
        
        // Center Panel - Image
        pnlCenter = new JPanel();
        pnlCenter.setBackground(new Color(24, 144, 255));
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JLabel lblImage = new JLabel();
        try {
            ImageIcon icon = new ImageIcon("images/20251007_1449_LearnHub Logo Placement_remix_01k6zb9sz7f0qv605qmry5hdk78.PNG");
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(250, 180, Image.SCALE_SMOOTH);
                lblImage.setIcon(new ImageIcon(scaledImg));
            } else {
                lblImage.setText("LearnHub");
                lblImage.setFont(new Font("Segoe UI", Font.BOLD, 48));
                lblImage.setForeground(Color.WHITE);
            }
        } catch (Exception e) {
            lblImage.setText("LearnHub");
            lblImage.setFont(new Font("Segoe UI", Font.BOLD, 48));
            lblImage.setForeground(Color.WHITE);
        }
        
        pnlCenter.add(lblImage);
        this.add(pnlCenter, BorderLayout.CENTER);
        
        // South Panel - Loading Progress
        pnlSouth = new JPanel();
        pnlSouth.setBackground(new Color(24, 144, 255));
        pnlSouth.setBorder(BorderFactory.createEmptyBorder(20, 0, 40, 0));
        
        lblLoading = new JLabel("Loading 0%", JLabel.CENTER);
        lblLoading.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblLoading.setForeground(Color.WHITE);
        
        pnlSouth.add(lblLoading);
        this.add(pnlSouth, BorderLayout.SOUTH);
    }
    
    private void startLoadingAnimation() {
        loadingTimer = new Timer(50, e -> {
            currentProgress += 2;
            lblLoading.setText("Loading " + currentProgress + "%");
            
            if (currentProgress >= 100) {
                loadingTimer.stop();
                SwingUtilities.invokeLater(() -> {
                    try {
                        Thread.sleep(500);
                        dispose();
                        // Launch login screen
                        new LoginPage();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                });
            }
        });
        
        loadingTimer.start();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
             WelcomePage gui = new  WelcomePage();
            gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gui.setResizable(true);
            gui.setVisible(true);
        });
    }
}