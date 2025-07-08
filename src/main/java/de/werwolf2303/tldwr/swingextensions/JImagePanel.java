/*
 * Copyright [2023-2025] [Gianluca Beil]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    private SVGImageRecalculate recalculate = null;
    private String rad = "";

    @FunctionalInterface
    public interface SVGImageRecalculate {
        byte[] svgImageRecalculate();
    }

    void refresh() {
        try {
            if(recalculate == null) {
                image = ImageIO.read(new ByteArrayInputStream(imagebytes));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        this.repaint();
    }

    public void setImage(InputStream inputStream) {
        try {
            imagebytes = IOUtils.toByteArray(inputStream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        refresh();
    }

    private void drawImage(Graphics graphics2D, BufferedImage image) {
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();
        int desiredWidth = this.getWidth();
        int desiredHeight = this.getHeight();
        double originalAspectRatio = (double) originalWidth / originalHeight;
        double desiredAspectRatio = (double) desiredWidth / desiredHeight;
        int newWidth, newHeight;
        int xOffset, yOffset;
        if (originalAspectRatio > desiredAspectRatio) {
            newWidth = desiredWidth;
            newHeight = (int) (desiredWidth / originalAspectRatio);
            xOffset = 0;
            yOffset = (desiredHeight - newHeight) / 2;
        } else {
            newWidth = (int) (desiredHeight * originalAspectRatio);
            newHeight = desiredHeight;
            xOffset = (desiredWidth - newWidth) / 2;
            yOffset = 0;
        }
        graphics2D.drawImage(image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), xOffset, yOffset, null);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null && recalculate == null) {
            return;
        }else if(recalculate != null) {
            try {
                byte[] newBytes = recalculate.svgImageRecalculate();
                if(newBytes != null && newBytes.length > 0) {
                    image = ImageIO.read(new ByteArrayInputStream(newBytes));
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        if (!(rad.isEmpty())) {
            Graphics2D graphics2D = (Graphics2D) g;
            if(graphics2D != null) graphics2D.rotate(Double.parseDouble(rad), (float) this.getWidth() / 2, (float) this.getHeight() / 2);
        }
        drawImage(g, image);
    }
}
