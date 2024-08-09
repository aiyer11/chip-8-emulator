package com.arjun.chip8;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class CPU {
    private byte[] memory;
    private int[] registers;
    private int[] stack;
    private int I;
    private int pc;
    private int sp;

    private int delayTimer;

    private int soundTimer;

    private boolean drawFlag;

    private final Logger LOG = LoggerFactory.getLogger(CPU.class);

    public CPU(byte[] romProgram){
        this.memory = new byte[4096];
        this.registers = new int[16];
        this.stack = new int[16];
        this.I = 0;
        this.sp = 0;
        this.pc = 0x200;

        for(int i =0; i < Font.FONT.length; i ++)
        {
            memory[i] = Font.FONT[i];
        }

        for(int i = 0; i < romProgram.length; i++)
        {
            memory[i + 0x200] = romProgram[i];
        }
    }

    private int fetchOpcode(){
        int opcode = ((this.memory[this.pc] << 8) & 0xFF) | (this.memory[pc+1] & 0xFF);
        this.pc += 2;
        return opcode;
    }

    private int decodeOpcode(int opcode) {
        int x = (opcode & 0x0F00) >> 8; // Second nibble (Used to get x in some instructions)
        int y = (opcode & 0x00F0) >> 4; // Third nibble (Used to get y in some instructions)
        int n = opcode & 0x000F; // Fourth nibble (Used to get n in some instructions)
        int nn = opcode & 0x00FF; // Third and fourth nibbled (Used to get nn in some instructions)
        int nnn = opcode & 0x0FFF; // Second, third and fourth nibble (Used to get nnn in some instructions)

        switch(opcode & 0xF000)
        {
            case 0x1000:
                this.pc = nnn; // Sets program counter to location nnn
                break;
            case 0x2000:
                this.stack[++sp] = this.pc; // Increments stack pointer and then put pc at top of stack
                this.pc = nnn; // Sets pc to location nnn to call subroutine at nnn
                break;
            case 0x3000:
                if(this.registers[x] == nn)
                {
                    this.pc+=2;
                }
                break;
            case 0x4000:
                if(this.registers[x] != nn)
                {
                    this.pc +=2;
                }
                break;
            case 0x5000:
                if(this.registers[x] == this.registers[y])
                {
                    this.pc+=2;
                }
                break;
            case 0x6000:
                this.registers[x] = nn;
                break;
            case 0x7000:
                this.registers[x] += nn;
                break;
            case 0x8000:
                switch(n)
                {
                    case 0x0000:
                        this.registers[x] = this.registers[y];
                        break;
                    case 0x0001:
                        this.registers[x] = (this.registers[x] | this.registers[y]);
                        break;
                    case 0x0002:
                        this.registers[x] = (this.registers[x] & this.registers[y]);
                        break;
                    case 0x0003:
                        this.registers[x] = (this.registers[x] ^ this.registers[y]);
                        break;
                    case 0x0004:
                        this.registers[x] += registers[y];
                        this.registers[0xF] = this.registers[x] > 0xFF ? 1:0;
                        break;
                    case 0x0005:
                        this.registers[0xF] = this.registers[x] > this.registers[y] ? 1:0;
                        this.registers[x] -= this.registers[y];
                        break;
                    case 0x0006:
                        this.registers[0xF] = this.registers[x] & 0x1;
                        this.registers[x] = this.registers[x] >> 1;
                        break;
                    case 0x0007:
                        this.registers[0xF] = this.registers[y] > this.registers[x] ? 1:0;
                        this.registers[x] = this.registers[y] - this.registers[x];
                        break;
                    case 0x000E:
                        this.registers[0xF] = (this.registers[x] >> 7) & 0x1;
                        this.registers[x] = this.registers[x] << 1;
                        break;
                    default:
                        LOG.debug("Unknown opcode: {}", opcode);
                        break;
                }
                break;
            case 0x9000:
                if(this.registers[x] != registers[y])
                {
                    this.pc+=2;
                }
                break;
            case 0xA000:
                this.I = nnn;
                break;
            case 0xB000:
                this.pc+= (nnn + registers[0]);
                break;
            case 0xC000:
                Random r = new Random();
                this.registers[x] = (r.nextInt(256) & nn);
                break;
            case 0xD000:
              this.drawFlag = true;
              this.registers[0xF] = 0;
              int px;

        }
        return -1;
    }

}
