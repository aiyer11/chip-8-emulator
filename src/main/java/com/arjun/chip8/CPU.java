package com.arjun.chip8;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class CPU {
    private final byte[] memory; // RAM of chip 8
    private final int[] registers; // Chip 8 registers
    private final int[] stack; // Chip 8 stack
    private int I; // Register I
    private int pc; // Program counter
    private int sp; // Stack pointer

    private final Display display; // Display

    private final Keyboard keyboard; // Keyboard

    private final Window window; // Window for graphics

    private final int[] keyboardRegisters;

    private int delayTimer;

    private int soundTimer;

    private int length;

    private final Logger LOG = LoggerFactory.getLogger(CPU.class);

    public CPU(int length){
        this.memory = new byte[4096]; // RAM size is 4096 bytes
        this.registers = new int[16]; // 16 registers
        this.stack = new int[16]; // stack
        this.display = new Display(); //Creates a new display object
        this.keyboard = new Keyboard(); //Creates a new keyboard object
        this.window = new Window(this.display,this.keyboard); // Creates a new window object
        this.length = length;
        this.keyboardRegisters = this.keyboard.getKeyboard(); // Gets current keyboard registers
        this.I = 0;
        this.sp = 0;
        this.pc = 0x200; // Program counter starts at 0x200

        // Loads fonts into memory
        for(int i =0; i < Font.FONT.length; i ++)
        {
            memory[i] = Font.FONT[i];
        }
    }

    //Loads the program into memory
    public void loadProgram(byte[] romProgram)
    {
        LOG.info("Loading program...");
        for(int i = 0; i < this.length; i++)
        {
            memory[i + 0x200] = romProgram[i];
        }
    }

    // Fetching the opcode
    public short fetchOpcode(){
        short opcode = (short) ((this.memory[this.pc] << 8) | (this.memory[this.pc+1]));
        this.pc += 2;
        return opcode;
    }

    // Decodes and executes the opcode
    public void decodeAndExecute(short opcode) {
        short x = (short) ((opcode & 0x0F00) >> 8); // Second nibble (Used to get x in some instructions)
        short y = (short) ((opcode & 0x00F0) >> 4); // Third nibble (Used to get y in some instructions)
        short n = (short) (opcode & 0x000F); // Fourth nibble (Used to get n in some instructions)
        short nn = (short) (opcode & 0x00FF); // Third and fourth nibbled (Used to get nn in some instructions)
        short nnn = (short) (opcode & 0x0FFF); // Second, third and fourth nibble (Used to get nnn in some instructions)

        String opcode_hex = String.format("%04X", opcode);
        LOG.info("Opcode: {}" , opcode_hex);

        switch(opcode & nn) {
                    case 0x00E0:
                        LOG.info("Instruction: 00E0");
                        this.display.clearDisplay(); // Clears the display
                        break;
                    case 0x00EE:
                        LOG.info("Instruction: 00EE");
                        this.pc = this.sp; // Sets the pc to the stack pointer
                        this.sp--; //Decrements the stack pointer
                        break;
        }

        switch(opcode & 0xF000) {
            case 0x1000:
                LOG.info("Instruction: 1nnn");
                this.pc = nnn; // Sets program counter to location nnn
                break;
            case 0x2000:
                LOG.info("Instruction: 2nnn");
                this.stack[++sp] = this.pc; // Increments stack pointer and then put pc at top of stack
                this.pc = nnn; // Sets pc to location nnn to call subroutine at nnn
                break;
            case 0x3000:
                LOG.info("Instruction: 3xnn");
                if (this.registers[x] == nn) // If register x is equal to nn increment the program counter by 2
                {
                    this.pc += 2;
                }
                break;
            case 0x4000:
                LOG.info("Instruction: 4xnn");
                if (this.registers[x] != nn) // If registers x is not equal to nn increment the program counter by 2
                {
                    this.pc += 2;
                }
                break;
            case 0x5000:
                LOG.info("Instruction: 5xy0");
                if (this.registers[x] == this.registers[y]) // If register x is equal to register y increment program counter by 2
                {
                    this.pc += 2;
                }
                break;
            case 0x6000:
                LOG.info("Instruction: 6xnn");
                this.registers[x] = nn; // Set register x to nn
                break;
            case 0x7000:
                LOG.info("Instruction: 7xnn");
                this.registers[x] += nn; // Set register x to x + nn
                break;
            case 0xA000:
                LOG.info("Instruction: Annn");
                this.I = nnn; // Set register I to address nnn
                break;
            case 0xB000:
                LOG.info("Instruction: Bnnn");
                this.pc+= (nnn + registers[0]); // Set program counter to nnn + register 0
                break;
            case 0xC000:
                LOG.info("Instruction: Cxnn");
                Random r = new Random();
                this.registers[x] = (r.nextInt(256) & nn); // Set register x to random number between 0 and 255 and nn
                break;
            case 0xD000:
                LOG.info("Instruction: Dxy0");
                this.registers[0xF] = 0; // Set register F to 0
                byte px; // Pixel in memory

                for(int row = 0; row < n; row++)
                {
                    px = this.memory[this.I + row]; // Setting pixel to current value at register I plus column j
                    for(int col = 0; col < 8; col++)
                    {
                        if((px & (0x80 >> col)) != 0) // Checking if the current pixel is equal to 1
                        {
                            byte pixel = this.display.getPixel(row + this.registers[y],col + this.registers[x]);
                            if(pixel == 1) // Checking if the pixel on display is equal to one
                            {
                                this.registers[0xF] = 1; // Sets register F to 1 to indicate a collision
                            }
                            byte pixelXOR = (byte) (pixel ^ 1); // Setting the value of the pixel using xor
                            this.display.setPixel(row + this.registers[y], col + this.registers[x],pixelXOR);
                        }
                    }
                }
                this.window.repaint();
                break;
        }
        switch(opcode & 0xF00F)
        {
            case 0x8000:
                LOG.info("Instruction: 8xy0");
                this.registers[x] = this.registers[y]; // Set register x to register y
                break;
            case 0x8001:
                LOG.info("Instruction: 8xy1");
                this.registers[x] = (this.registers[x] | this.registers[y]); // Set register x to x or y
                break;
            case 0x8002:
                LOG.info("Instruction: 8xy2");
                this.registers[x] = (this.registers[x] & this.registers[y]); // Set register x to x and y
                break;
            case 0x8003:
                LOG.info("Instruction: 8xy3");
                this.registers[x] = (this.registers[x] ^ this.registers[y]); // Set register x to x xor y
                break;
            case 0x8004:
                LOG.info("Instruction: 8xy4");
                this.registers[x] += registers[y]; // Set register x to x + y
                this.registers[0xF] = this.registers[x] > 0xFF ? 1:0; // If register x is greater than 255 set register F to 1 else 0
                break;
            case 0x8005:
                LOG.info("Instruction: 8xy5");
                this.registers[0xF] = this.registers[x] > this.registers[y] ? 1:0; // If register x is greater than register y then set register F to 1 else 0
                this.registers[x] -= this.registers[y]; // Set register x to x - y
                break;
            case 0x8006:
                LOG.info("Instruction: 8xy6");
                this.registers[0xF] = this.registers[x] & 0x1; // Set register F to lsb of x
                this.registers[x] = this.registers[x] >> 1; // Set register x to x divided by 2
                break;
            case 0x8007:
                LOG.info("Instruction: 8xy7");
                this.registers[0xF] = this.registers[y] > this.registers[x] ? 1:0; // Set register F to 1 if y is greater than x else 0
                this.registers[x] = this.registers[y] - this.registers[x];
                break;
            case 0x800E:
                LOG.info("Instruction: 8xy8");
                this.registers[0xF] = (this.registers[x] >> 7) & 0x1; // Set register F to msb of x
                this.registers[x] = this.registers[x] << 1; //Set register x to x * 2
                break;
            case 0x9000:
                LOG.info("Instruction: 9xy0");
                if(this.registers[x] != registers[y]) // If register x is not equal to y increment pc by 2
                {
                    this.pc+=2;
                }
                break;
        }
        switch(opcode & 0xF0FF){
            case 0xE09E:
                LOG.info("Instruction: Ex9E");
                if(this.keyboardRegisters[this.registers[x]] == 1) //Checks if a key has been pressed
                {
                    this.pc += 2; //Increments pc by 2
                }
                break;
            case 0xE0A1:
                LOG.info("Instruction: ExA1");
                if(this.keyboardRegisters[this.registers[x]] == 0) //Checks if a key hasn't been pressed
                {
                    this.pc+= 2; //Increments pc by 2
                }
                break;
            case 0xF007:
                LOG.info("Instruction: Fx07");
                registers[x] = this.delayTimer; // Set register x to delay timer
                break;
            case 0xF00A:
                LOG.info("Instruction: Fx0A");
                int keyPressed = 0;
                while(keyPressed == 0) // Waits for a key to be pressed
                {
                    for(int i = 0; i < 16; i++) //Iterates through all possible keys
                    {
                        if(this.keyboardRegisters[i] == 1) //Checks if a key is pressed
                        {
                            keyPressed = 1; // Set to one to end the while loop
                            this.registers[x] = i; // Set register x to the key value
                            break;
                        }
                    }

                }
                break;
            case 0xF015:
                LOG.info("Instruction: Fx15");
                this.delayTimer = registers[x]; // Sets the delay timer to register x
                break;
            case 0xF018:
                LOG.info("Instruction: Fx18");
                this.soundTimer = registers[x]; // Sets the sound timer to register x
                break;
            case 0xF01E:
                LOG.info("Instruction: Fx1E");
                this.I += this.registers[x]; // Sets I to I + register x
                break;
            case 0xF029:
                LOG.info("Instruction: Fx29");
                this.I = this.registers[x] * 5; // Sets I to sprite at register x
                break;
            case 0xF033:
                LOG.info("Instruction: Fx33");
                int value = this.registers[x]; // Gets the value of register x
                this.memory[this.I+2] = (byte) (value % 10); // Sets I + 2 to the least significant bit
                value /= 10;
                this.memory[this.I+1] = (byte) (value % 10); // Sets I + 1 to the middle bit
                value /= 10;
                this.memory[this.I] = (byte) (value % 10); // Sets I to the most significant bit
                break;
            case 0xF055:
                LOG.info("Instruction: Fx55");
                for(int i = 0; i <= x; i++)
                {
                    this.memory[this.I + i] = (byte) registers[i]; // Sets in memory the value of register 0 to x starting at I
                }
                break;
            case 0xF065:
                LOG.info("Instruction: Fx65");
                for(int i = 0; i <= x; i++)
                {
                    registers[i] = this.memory[this.I + i]; // Reads from memory the values starting at I in registers 0 to x
                }
                break;
        }
        if(this.delayTimer > 0)
        {
            this.delayTimer--; // Decrements delay timer
        }
        if(this.soundTimer > 0)
        {
            LOG.info("BEEP");
            this.soundTimer--; // Decrements sound timer
        }

    }

}
