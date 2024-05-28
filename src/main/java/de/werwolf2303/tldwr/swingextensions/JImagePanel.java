package de.werwolf2303.tldwr.swingextensions;

import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JImagePanel extends JPanel {
    private BufferedImage image = null;
    private byte[] imagebytes;
    private int width = 105;
    private int height = 105;

    void refresh() {
        try {
            image = ImageIO.read(new ByteArrayInputStream(imagebytes));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        this.repaint();
    }

    public void setImage(InputStream inputStream) {
        try {
            imagebytes = IOUtils.toByteArray(inputStream);
        }catch (IOException ex) {
            ex.printStackTrace();
        }
        refresh();
    }

    public void setImage(InputStream inputStream, int width, int height) {
        this.width = width;
        this.height = height;
        setImage(inputStream);
    }


    public InputStream getImageStream() {
        if(imagebytes == null || imagebytes.length == 0) {
            return null;
        }
        return new ByteArrayInputStream(imagebytes);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(image == null) {
            return;
        }
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.drawImage(new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH)).getImage(), 0, 0, null);
    }
}
