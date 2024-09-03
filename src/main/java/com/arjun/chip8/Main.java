package com.arjun.chip8;

import java.io.IOException;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        String romName = "tetris.ch8";

        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(romName);
        byte[] program = new byte[4096];
        int length = inputStream.read(program);
        CPU cpu = new CPU(length);
        cpu.loadProgram(program);
        short opcode = 0;
        while(true)
        {
            opcode = cpu.fetchOpcode();
            cpu.decodeAndExecute(opcode);
            Thread.sleep(1);
        }
    }
}
