package de.werwolf2303.tldwr.frames;

import com.intellij.uiDesigner.core.GridConstraints;
import de.werwolf2303.tldwr.PublicValues;
import de.werwolf2303.tldwr.TLDPatcher.TLDPatcher;
import de.werwolf2303.tldwr.workshop.WorkshopAPI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainFrame implements Frame {
    private JPanel contentPanel;
    private JButton reinstallButton;
    private JButton gotoModsButton;
    private JButton settingsButton;
    private JPanel mainFramePanel;
    private boolean drawThings = true;
    private Views currentView;
    private WorkshopFrame workshopFrame;
    private SettingsFrame settingsFrame;
    private JFrame frame;

    public enum Views {
        Settings,
        Workshop,
        Home
    }

    public MainFrame() {
        PublicValues.mainFrame = this;

        workshopFrame = new WorkshopFrame();
        GridConstraints workshopConstraints = new GridConstraints();
        workshopConstraints.setFill(GridConstraints.FILL_BOTH);
        contentPanel.add(workshopFrame.contentPanel, workshopConstraints);
        workshopFrame.contentPanel.setVisible(false);

        settingsFrame = new SettingsFrame();
        GridConstraints settingsConstraints = new GridConstraints();
        settingsConstraints.setFill(GridConstraints.FILL_BOTH);
        contentPanel.add(settingsFrame.contentPanel, settingsConstraints);
        settingsFrame.contentPanel.setVisible(false);

        gotoModsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchView(Views.Workshop);
            }
        });

        reinstallButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TLDPatcher().startPatching();
            }
        });

        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchView(Views.Settings);
            }
        });
    }

    private ArrayList<String> upLeftText = new ArrayList<>(
            Arrays.asList("Credits:",
                    "_RainBowSheep_",
                    "RUNDEN",
                    "KolbenLP",
                    "Werwolf2303",
                    "Special thank to:",
                    "Splendid")
    );

    private void createUIComponents() {
        mainFramePanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                if(!drawThings) return;
                int yCache = g.getFontMetrics().getHeight() + 3;
                int xCache = 3;

                //Draw background image
                try {
                    g.drawImage(ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/backgroundImage.png")))
                            .getScaledInstance(mainFramePanel.getWidth(), mainFramePanel.getHeight(), Image.SCALE_SMOOTH), 0, 0, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                g.setColor(Color.white);

                for(String text : upLeftText) {
                    g.drawString(text, xCache, yCache);
                    yCache += g.getFontMetrics().getHeight() + 3;
                }

                try {
                    Image welcomeImage = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/welcome.png")))).getImage();
                    g.drawImage(welcomeImage, frame.getWidth() / 2 - 500 / 2, gotoModsButton.getY() / 2 - 93 / 2, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void switchView(Views view) {
        switch (view) {
            case Settings:
                drawThings = false;
                mainFramePanel.setVisible(false);
                workshopFrame.contentPanel.setVisible(false);
                settingsFrame.contentPanel.setVisible(true);
                currentView = Views.Settings;
                break;
            case Workshop:
                drawThings = false;
                mainFramePanel.setVisible(false);
                settingsFrame.contentPanel.setVisible(false);
                workshopFrame.contentPanel.setVisible(true);
                currentView = Views.Workshop;
                break;
            case Home:
                settingsFrame.contentPanel.setVisible(false);
                workshopFrame.contentPanel.setVisible(false);
                drawThings = true;
                mainFramePanel.setVisible(true);
                currentView = Views.Home;
        }
    }

    public void open() {
        frame = new JFrame("The Long Drive Mod Workshop");

        workshopFrame.setFrame(frame);

        frame.setLocationRelativeTo(null);
        frame.setContentPane(contentPanel);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - frame.getPreferredSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - frame.getPreferredSize().height / 2);
        frame.setVisible(true);
        frame.pack();

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if(frame.getSize().width < frame.getMinimumSize().width) frame.setSize(frame.getMinimumSize().width, frame.getSize().height);
                if(frame.getSize().height < frame.getMinimumSize().height) frame.setSize(frame.getSize().width, frame.getMinimumSize().height);
            }
        });

        PublicValues.currentFrame = this;

        try {
            ArrayList<WorkshopAPI.Mod> modsWithUpdates = WorkshopAPI.checkForModUpdates();
            if(!modsWithUpdates.isEmpty()) {
                new UpdateModsFrame().showIt(modsWithUpdates);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to check for updates");
        }
    }

    public void close() {
        frame.dispose();
    }
}
