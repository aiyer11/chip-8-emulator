package com.arjun.chip8;

import javax.swing.*;
import java.util.Arrays;

public class Display {

    private final byte [][] display;
    public static final int ROWS = 32;
    public static final int COLS = 64;

    public Display()
    {
        this.display = new byte[ROWS][COLS];
    }

    public byte getPixel(int row, int col)
    {
        return this.display[row % 32][col % 64];
    }

    public void setPixel(int row, int col, byte value)
    {
        this.display[row % 32][col % 64] = value;
    }

    public void clearDisplay()
    {
        for(int i =0; i < this.display.length; i++)
        {
            Arrays.fill(this.display[i], (byte) 0);
        }
    }
}
