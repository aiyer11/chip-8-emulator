package com.arjun.chip8;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class Window extends JPanel{

    private int width = 600;
    private int height = 600;

    private final Display display;

    public Window(Display display, Keyboard keyboard)
    {
        this.display = display;

        JFrame frame = new JFrame("Chip 8 Emulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        this.setBackground(Color.BLACK);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addKeyListener(keyboard);
        frame.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                width = getWidth();
                height = getHeight();
            }

            @Override
            public void componentMoved(ComponentEvent e) {

            }

            @Override
            public void componentShown(ComponentEvent e) {

            }

            @Override
            public void componentHidden(ComponentEvent e) {

            }
        });
    }

    // Paints the graphics onto the screen
    @Override
    public void paint(Graphics g) {
        super.paint(g);
         int colWidth = width/Display.COLS;
         int rowHeight = height/Display.ROWS;

         g.setColor(Color.WHITE);
         for(int row = 0; row < Display.ROWS; row++) {
             for(int col = 0; col < Display.COLS; col++) {
                 int pixel = this.display.getPixel(row, col);
                 if(pixel == 1)
                 {
                     int x = col * colWidth;
                     int y = row * rowHeight;
                     g.fillRect(x, y, colWidth, rowHeight);
                 }
             }
         }
    }

}
