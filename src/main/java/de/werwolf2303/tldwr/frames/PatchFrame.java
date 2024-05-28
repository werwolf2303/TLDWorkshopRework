package de.werwolf2303.tldwr.frames;

import de.werwolf2303.tldwr.PublicValues;
import de.werwolf2303.tldwr.TLDPatcher.TLDPatcher;
import de.werwolf2303.tldwr.search.SearchEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PatchFrame implements Frame {
    private JPanel contentPanel;
    private JButton patchButton;
    private JTextPane infoPane;
    private JFrame frame;
    private SearchEngine searchEngine;

    public PatchFrame() {
        searchEngine = new SearchEngine(null);
        infoPane.setContentType("text/html");
        infoPane.setText("<h2>TLDPatcher</h2><br><br><a>Click 'Patch' to install the modloader</a>");

        patchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TLDPatcher().startPatching();
            }
        });

        contentPanel.setPreferredSize(new Dimension(400, 300));
        contentPanel.setMinimumSize(new Dimension(400, 300));
    }

    public void open() {
        frame = new JFrame("The Long Drive Mod Workshop");
        frame.setContentPane(contentPanel);
        frame.setVisible(true);
        frame.pack();

        PublicValues.currentFrame = this;

        while(frame.isVisible()) {
            try {
                Thread.sleep(99);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        frame.dispose();
    }
}

