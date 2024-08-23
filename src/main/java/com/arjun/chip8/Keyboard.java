package com.arjun.chip8;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class Keyboard extends KeyAdapter {
    private int[] keyboard;
    private final char[] allowedKeys = {'1','2','3','4',
                                  'q','w','e','r',
                                   'a','s','d','f',
                                    'z','x','c','v'};

    private final HashMap<Character, Integer> keyMappings;

    public Keyboard(){
        this.keyboard = new int[16];
        this.keyMappings = new HashMap<>();
        this.keyMappings.put(this.allowedKeys[0],1);
        this.keyMappings.put(this.allowedKeys[1],2);
        this.keyMappings.put(this.allowedKeys[2],3);
        this.keyMappings.put(this.allowedKeys[3],0xC);
        this.keyMappings.put(this.allowedKeys[4],4);
        this.keyMappings.put(this.allowedKeys[5],5);
        this.keyMappings.put(this.allowedKeys[6],6);
        this.keyMappings.put(this.allowedKeys[7],0xD);
        this.keyMappings.put(this.allowedKeys[8],7);
        this.keyMappings.put(this.allowedKeys[9],8);
        this.keyMappings.put(this.allowedKeys[10],9);
        this.keyMappings.put(this.allowedKeys[11],0xE);
        this.keyMappings.put(this.allowedKeys[12],0xA);
        this.keyMappings.put(this.allowedKeys[13],0);
        this.keyMappings.put(this.allowedKeys[14],0xB);
        this.keyMappings.put(this.allowedKeys[15],0xF);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        super.keyPressed(e);
        char charKeyPressed = e.getKeyChar();
        boolean isValid = this.isValidKey(charKeyPressed);
        if(isValid)
        {
            int keyboardIndex = this.keyMappings.get(charKeyPressed);
            this.keyboard[keyboardIndex] = 1;
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        super.keyReleased(e);
        char charKeyPressed = e.getKeyChar();
        boolean isValid = this.isValidKey(charKeyPressed);
        if(isValid)
        {
            int keyboardIndex = this.keyMappings.get(charKeyPressed);
            this.keyboard[keyboardIndex] = 0;
        }
    }

    private boolean isValidKey(char key)
    {
        for(char c: this.allowedKeys)
        {
            if(c == key)
            {
                return true;
            }
        }
        return false;
    }

    public int[] getKeyboard()
    {
        return this.keyboard;
    }

}
