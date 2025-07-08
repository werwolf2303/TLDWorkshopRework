package de.werwolf2303.tldwr.swingextensions;

import de.werwolf2303.tldwr.Events;
import de.werwolf2303.tldwr.PublicValues;
import de.werwolf2303.tldwr.TLDWREvents;
import de.werwolf2303.tldwr.frames.WorkshopFrame;
import de.werwolf2303.tldwr.workshop.WorkshopAPI;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

public class ModDisplayModule extends JPanel {
    public JImagePanel modImage;
    public JLabel modName;
    public JLabel modVersion;
    public JLabel modAuthor;
    public JLabel modDate;
    public JTextPane modDescription;
    public WorkshopAPI.Mod mod;
    private ModDisplayModule thisOne;
    public boolean isHighlighted = false;
    private boolean disableExtendedFrame;
    private JProgressBar progressBar;
    private Color backgroundColor;
    private boolean singleClick;
    private boolean isModPacks = false;

    public ModDisplayModule(Color backgroundColor, boolean singleClick, boolean isModPacks) {
        setLayout(null);
        setPreferredSize(new Dimension(299, 121));

        setBorder(new LineBorder(Color.black, 1));

        this.backgroundColor = backgroundColor;
        this.singleClick = singleClick;
        this.isModPacks = isModPacks;

        thisOne = this;

        modImage = new JImagePanel();
        modImage.setBounds(10, 11, 99, 99);
        add(modImage);

        modName = new JLabel("New label");
        modName.setBounds(117, 11, 172, 14);
        add(modName);
        Map attributes = getFont().getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        modName.setFont(getFont().deriveFont(attributes));

        modVersion = new JLabel("New label");
        modVersion.setBounds(117, 29, 48, 14);
        add(modVersion);
        modVersion.setFont(new Font(getFont().getName(), getFont().getStyle(), 8));

        modAuthor = new JLabel("New label");
        modAuthor.setBounds(175, 29, 56, 14);
        add(modAuthor);
        modAuthor.setFont(new Font(getFont().getName(), getFont().getStyle(), 8));

        modDate = new JLabel("New label");
        modDate.setBounds(241, 29, 48, 14);
        add(modDate);
        modDate.setFont(new Font(getFont().getName(), getFont().getStyle(), 8));

        modDescription = new JTextPane();
        modDescription.setBounds(119, 44, 170, 70);
        add(modDescription);
        modDescription.setEditable(false);
        modDescription.setContentType("text/html");
        modDescription.setBackground(getBackground());

        progressBar = new JProgressBar();
        progressBar.setStringPainted(false);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(20);
        progressBar.setBounds(10, 50, 279, 20);
        progressBar.setForeground(Color.green);
        progressBar.setVisible(false);
        add(progressBar);

        installListenersRecursively(this);

        Events.subscribe(TLDWREvents.DOWNLOAD_FINISHED.getName(), new Runnable() {
            @Override
            public void run() {
                if(!isVisible()) return;
                modImage.setVisible(true);
                modName.setVisible(true);
                modVersion.setVisible(true);
                modAuthor.setVisible(true);
                modDate.setVisible(true);
                modDescription.setVisible(true);
                progressBar.setVisible(false);
                progressBar.setValue(0);

                PublicValues.mainFrame.workshopFrame.refreshButton.doClick();
            }
        });
    }

    private void installListenersRecursively(Component component) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if ((!disableExtendedFrame && e.getClickCount() == 2) || (!disableExtendedFrame && e.getClickCount() == 1 && singleClick)) {
                    PublicValues.mainFrame.modExpandedFrame.showWith(mod, isModPacks);
                    return;
                }

                if (e.getClickCount() == 1) {
                    if (isHighlighted) {
                        isHighlighted = false;
                        setBackground(backgroundColor);
                        Events.triggerEvent(TLDWREvents.MOD_UNSELECTED.getName());
                        return;
                    }

                    if (PublicValues.lastHighlightedMod != null) {
                        PublicValues.lastHighlightedMod.setBackground(backgroundColor);
                        PublicValues.lastHighlightedMod.isHighlighted = false;
                    }

                    PublicValues.lastHighlightedMod = thisOne;
                    thisOne.setBackground(new Color(255, 165, 0));
                    isHighlighted = true;
                    Events.triggerEvent(TLDWREvents.MOD_SELECTED.getName());
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isHighlighted) {
                    setBackground(Color.decode("#ffe3b1"));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isHighlighted) {
                    setBackground(backgroundColor);
                }
            }
        });

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                installListenersRecursively(child);
            }
        }
    }

    public void download() {
        modImage.setVisible(false);
        modName.setVisible(false);
        modVersion.setVisible(false);
        modAuthor.setVisible(false);
        modDate.setVisible(false);
        modDescription.setVisible(false);
        progressBar.setVisible(true);

        WorkshopAPI.download(mod, new WorkshopAPI.DownloadProgressRunnable() {
            @Override
            public void run(double percentage) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setValue((int) percentage);
                    }
                });
            }
        });
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);

        for(Component component : getComponents()) {
            component.setBackground(bg);
        }
    }

    public static ModDisplayModule init(WorkshopAPI.Mod mod, Color backgroundColor, boolean disableExtendedFrame, boolean singleClick, boolean isModPacks) {
        ModDisplayModule module = new ModDisplayModule(backgroundColor, singleClick, isModPacks);
        module.modDescription.setText("<body style='font-size:9px'>" + mod.Description + "<br>");
        Thread imageLoadingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if(mod.PictureLink.endsWith("/")) {
                    try {
                        module.modImage.setImage(WorkshopAPI.getImageStream("https://gitlab.com/KolbenLP/WorkshopTLDMods/-/raw/WorkshopDatabase8.5/picture/Bild_2023-03-18_174359456.png"));
                    }catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                    return;
                }
                try {
                    module.modImage.setImage(WorkshopAPI.getImageStream(mod.PictureLink));
                } catch (MalformedURLException me) {
                    try {
                        module.modImage.setImage(WorkshopAPI.getImageStream("https://gitlab.com/KolbenLP/WorkshopTLDMods/-/raw/WorkshopDatabase8.5/picture/Bild_2023-03-18_174359456.png"));
                    }catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        imageLoadingThread.start();
        module.modName.setText(mod.Name);
        module.modVersion.setText(mod.Version);
        module.modAuthor.setText(mod.Author);
        module.modDate.setText(mod.Date);
        module.mod = mod;
        module.disableExtendedFrame = disableExtendedFrame;

        return module;
    }
}
