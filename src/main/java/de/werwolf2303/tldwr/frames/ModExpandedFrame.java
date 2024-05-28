package de.werwolf2303.tldwr.frames;

import de.werwolf2303.tldwr.PublicValues;
import de.werwolf2303.tldwr.swingextensions.JImagePanel;
import de.werwolf2303.tldwr.workshop.WorkshopAPI;

import javax.swing.*;
import java.io.IOException;
import java.net.MalformedURLException;

public class ModExpandedFrame {

    private JPanel contentPanel;
    private JTextPane modDescriptionAndChangelog;
    private JImagePanel modImage;
    private JLabel modDate;
    private JLabel modName;
    private JLabel modAuthor;
    private JButton downloadButton;
    private JButton backButton;
    private WorkshopFrame mainFrame;
    private WorkshopAPI.Mod mod;

    public ModExpandedFrame(WorkshopFrame frame) {
        this.mainFrame = frame;

        modDescriptionAndChangelog.setEditable(false);
        modDescriptionAndChangelog.setContentType("text/html");
        modDescriptionAndChangelog.setBackground(getContentPanel().getBackground());

        backButton.addActionListener(e -> hide());

        downloadButton.addActionListener(e -> WorkshopAPI.download(mod, percentage -> {
            if(!mainFrame.frame.getTitle().contains("-")) {
                mainFrame.frame.setTitle(mainFrame.frame.getTitle() + " - Downloading (" + Math.round(percentage) + "%)");
                return;
            }
            mainFrame.frame.setTitle(mainFrame.frame.getTitle().split(" - ")[0] + " - Downloading (" + Math.round(percentage) + "%)");
        }));
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void showWith(WorkshopAPI.Mod mod) {
        this.mod = mod;
        modName.setText(mod.Name);
        modAuthor.setText(mod.Author);
        modDate.setText(mod.Date);
        modDescriptionAndChangelog.setText("<a>Description:</a><br><br>" + mod.Description + "<br><br><br><a>Changelog:</a><br><br>" + mod.Changelog);

        try {
            modImage.setImage(WorkshopAPI.getImageStream(mod.PictureLink), 320, 320);
        } catch (MalformedURLException me) {
            try {
                modImage.setImage(WorkshopAPI.getImageStream("https://gitlab.com/KolbenLP/WorkshopTLDMods/-/raw/WorkshopDatabase8.5/picture/Bild_2023-03-18_174359456.png"), 320, 320);
            }catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        mainFrame.searchLabel.setVisible(false);
        mainFrame.searchField.setVisible(false);
        mainFrame.categories.setVisible(false);
        mainFrame.refreshButton.setVisible(false);
        mainFrame.bottomBar.setVisible(false);
        mainFrame.modDisplay.setVisible(false);
        contentPanel.setVisible(true);
    }

    public void hide() {
        PublicValues.extendedFrameVisible = false;
        mainFrame.searchLabel.setVisible(true);
        mainFrame.searchField.setVisible(true);
        mainFrame.categories.setVisible(true);
        mainFrame.refreshButton.setVisible(true);
        mainFrame.bottomBar.setVisible(true);
        mainFrame.modDisplay.setVisible(true);
        contentPanel.setVisible(false);
    }
}
