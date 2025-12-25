package de.werwolf2303.tldwr.frames;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.squareup.okhttp.Request;
import de.werwolf2303.tldwr.Events;
import de.werwolf2303.tldwr.PublicValues;
import de.werwolf2303.tldwr.TLDWREvents;
import de.werwolf2303.tldwr.swingextensions.JImagePanel;
import de.werwolf2303.tldwr.workshop.WorkshopAPI;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Locale;

public class ModExpandedFrame {

    private JPanel contentPanel;
    private JTextPane modDescriptionAndChangelog;
    private JImagePanel modImage;
    private JLabel modDate;
    private JLabel modName;
    private JLabel modAuthor;
    private JButton downloadButton;
    private JButton backButton;
    private JButton homeButton;
    private JProgressBar progressBar;
    private JLabel status;
    private WorkshopAPI.Mod mod;
    private boolean isModPack;

    public ModExpandedFrame() {
        modDescriptionAndChangelog.setEditable(false);
        modDescriptionAndChangelog.setContentType("text/html");
        modDescriptionAndChangelog.setBackground(getContentPanel().getBackground());

        backButton.addActionListener(e -> hide());

        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setVisible(false);

        downloadButton.addActionListener(e -> {
            if (isModPack) {
                try {
                    progressBar.setVisible(true);

                    Request request = new Request.Builder()
                            .url(mod.Link)
                            .build();
                    String data = new String(PublicValues.client.newCall(request).execute().body().bytes());

                    FileOutputStream stream = new FileOutputStream(new File(PublicValues.tldUserPath + File.separator + "Modpacks", mod.FileName));
                    stream.write(data.getBytes());
                    stream.close();

                    final int[] total = new int[]{0};
                    for (String mod : data.split("\n")) {
                        if (mod.isEmpty()) continue;
                        total[0]++;
                    }

                    final int[] current = new int[]{0};

                    WorkshopAPI.runWithModPack(
                            new File(PublicValues.tldUserPath + File.separator + "Modpacks", mod.FileName).getAbsolutePath(),
                            percentage -> {
                                if (percentage == 100) {
                                    current[0]++;
                                    status.setText(current[0] + "/" + total[0]);
                                }
                                progressBar.setValue((int) percentage);
                            }
                    );
                } catch (IOException | NullPointerException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to download Modpack");
                    progressBar.setVisible(false);
                }
            } else {
                progressBar.setVisible(true);
                WorkshopAPI.download(mod, percentage -> {
                    progressBar.setValue((int) percentage);
                });
            }
        });

        Events.subscribe(TLDWREvents.DOWNLOAD_FINISHED.getName(), new Runnable() {
            @Override
            public void run() {
                if (!contentPanel.isVisible()) return;
                progressBar.setVisible(false);
            }
        });
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void showWith(WorkshopAPI.Mod mod, boolean isModPack) {
        this.mod = mod;
        this.isModPack = isModPack;
        modName.setText("Name: " + mod.Name);
        modAuthor.setText("Author: " + mod.Author);
        modDate.setText("Date: " + mod.Date);

        if (mod.Description == null) mod.Description = "";
        if (mod.Changelog == null) mod.Changelog = "";

        mod.Description = mod.Description.replaceAll("\n", "<br>");
        mod.Changelog = mod.Changelog.replaceAll("\n", "<br>");

        if (isModPack) {
            downloadButton.setText("Play Modpack!");
        } else downloadButton.setText("Download");

        modDescriptionAndChangelog.setText("<a>Description:</a><br><br>" + mod.Description + "<br><br><br><a>Changelog:</a><br><br>" + mod.Changelog);

        try {
            modImage.setImage(WorkshopAPI.getImageStream(mod.PictureLink));
        } catch (MalformedURLException me) {
            try {
                modImage.setImage(WorkshopAPI.getImageStream("https://gitlab.com/KolbenLP/WorkshopTLDMods/-/raw/WorkshopDatabase8.6/picture/Bild_2023-03-18_174359456.png"));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        PublicValues.mainFrame.switchView(MainFrame.Views.ModDetailedView);
    }

    public void hide() {
        PublicValues.mainFrame.navigateBack();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayoutManager(4, 2, new Insets(5, 5, 5, 5), -1, -1));
        contentPanel.setMinimumSize(new Dimension(299, 180));
        contentPanel.setPreferredSize(new Dimension(299, 180));
        final JScrollPane scrollPane1 = new JScrollPane();
        contentPanel.add(scrollPane1, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        modDescriptionAndChangelog = new JTextPane();
        Font modDescriptionAndChangelogFont = this.$$$getFont$$$(null, -1, 12, modDescriptionAndChangelog.getFont());
        if (modDescriptionAndChangelogFont != null) modDescriptionAndChangelog.setFont(modDescriptionAndChangelogFont);
        scrollPane1.setViewportView(modDescriptionAndChangelog);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(panel1, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        modImage = new JImagePanel();
        panel1.add(modImage, new GridConstraints(0, 0, 4, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(200, 200), new Dimension(200, 200), new Dimension(200, 200), 1, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        modName = new JLabel();
        modName.setText("Label");
        panel1.add(modName, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        modDate = new JLabel();
        modDate.setText("Label");
        panel1.add(modDate, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        modAuthor = new JLabel();
        modAuthor.setText("Label");
        panel1.add(modAuthor, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(panel2, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        backButton = new JButton();
        backButton.setText("Back");
        panel2.add(backButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        downloadButton = new JButton();
        downloadButton.setText("Download");
        panel2.add(downloadButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBar = new JProgressBar();
        panel2.add(progressBar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        status = new JLabel();
        status.setText("");
        panel2.add(status, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        homeButton = new JButton();
        homeButton.setText("Home");
        contentPanel.add(homeButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        contentPanel.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }
}
