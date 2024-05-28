package de.werwolf2303.tldwr.swingextensions;

import de.werwolf2303.tldwr.Events;
import de.werwolf2303.tldwr.PublicValues;
import de.werwolf2303.tldwr.TLDWREvents;
import de.werwolf2303.tldwr.frames.WorkshopFrame;
import de.werwolf2303.tldwr.workshop.WorkshopAPI;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
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
    private WorkshopFrame mainFrame;
    public boolean isHighlighted = false;
    private boolean disableExtendedFrame;

    public ModDisplayModule(WorkshopFrame frame) {
        setLayout(null);
        setPreferredSize(new Dimension(299, 121));

        setBorder(new LineBorder(Color.black, 1));

        this.mainFrame = frame;
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
        modDescription.setBounds(119, 54, 170, 56);
        add(modDescription);
        modDescription.setFont(new Font(getFont().getName(), getFont().getStyle(), 8));
        modDescription.setEditable(false);
        modDescription.setContentType("text/html");
        modDescription.setBackground(getBackground());

        for(Component component : getComponents()) {
            component.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {

                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if(!disableExtendedFrame) {
                    if(e.getClickCount() == 2) {
                        PublicValues.extendedFrameVisible = true;
                        PublicValues.modExpandedFrame.showWith(mod);
                    }
                    }
                    if(e.getClickCount() == 1) {
                        if(isHighlighted) {
                            isHighlighted = false;
                            setBackground(frame.frame.getBackground());
                            Events.triggerEvent(TLDWREvents.MOD_UNSELECTED.getName());
                            return;
                        }
                        if(PublicValues.lastHighlightedMod != null) {
                            PublicValues.lastHighlightedMod.setBackground(frame.frame.getBackground());
                        }
                        PublicValues.lastHighlightedMod = thisOne;
                        thisOne.setBackground(Color.darkGray);
                        isHighlighted = true;
                        Events.triggerEvent(TLDWREvents.MOD_SELECTED.getName());
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if(isHighlighted) return;
                    setBackground(Color.gray);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if(isHighlighted) return;
                    setBackground(mainFrame.frame.getBackground());
                }
            });
        }
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);

        for(Component component : getComponents()) {
            component.setBackground(bg);
        }
    }

    public static ModDisplayModule init(WorkshopAPI.Mod mod, WorkshopFrame frame, boolean disableExtendedFrame) {
        ModDisplayModule module = new ModDisplayModule(frame);
        module.modDescription.setText("<body style='font-size:6px'>" + mod.Description + "<br>");
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
