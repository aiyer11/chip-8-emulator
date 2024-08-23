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

    private boolean drawFlag; // Flag to indicate when to draw

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
            LOG.info("Instruction: {}", romProgram[i]);
            memory[i + 0x200] = romProgram[i];
        }
    }

    // Fetching the opcode
    public int fetchOpcode(){
        int opcode = ((this.memory[this.pc] << 8) & 0xFF) | (this.memory[pc+1] & 0xFF);
        this.pc += 2;
        return opcode;
    }

    public boolean getDrawFlag(){
        return this.drawFlag;
    }

    // Decodes and executes the opcode
    public void decodeAndExecute(int opcode) {
        int x = (opcode & 0x0F00) >> 8; // Second nibble (Used to get x in some instructions)
        int y = (opcode & 0x00F0) >> 4; // Third nibble (Used to get y in some instructions)
        int n = opcode & 0x000F; // Fourth nibble (Used to get n in some instructions)
        int nn = opcode & 0x00FF; // Third and fourth nibbled (Used to get nn in some instructions)
        int nnn = opcode & 0x0FFF; // Second, third and fourth nibble (Used to get nnn in some instructions)

        switch(opcode & 0xF000)
        {
            case 0x0000:
                switch(nn){
                    case 0x00E0:
                        LOG.info("00E0");
                        this.display.clearDisplay(); // Clears the display
                        break;
                    case 0x00EE:
                        LOG.info("00EE");
                        this.pc = this.sp; // Sets the pc to the stack pointer
                        this.sp--; //Decrements the stack pointer
                        break;
                    default:
                        LOG.debug("Unknown opcode {}", opcode);
                        break;
                }
                break;
            case 0x1000:
                LOG.info("1nnn");
                this.pc = nnn; // Sets program counter to location nnn
                break;
            case 0x2000:
                LOG.info("2nnn");
                this.stack[++sp] = this.pc; // Increments stack pointer and then put pc at top of stack
                this.pc = nnn; // Sets pc to location nnn to call subroutine at nnn
                break;
            case 0x3000:
                LOG.info("3xnn");
                if(this.registers[x] == nn) // If register x is equal to nn increment the program counter by 2
                {
                    this.pc+=2;
                }
                break;
            case 0x4000:
                LOG.info("4xnn");
                if(this.registers[x] != nn) // If registers x is not equal to nn increment the program counter by 2
                {
                    this.pc +=2;
                }
                break;
            case 0x5000:
                LOG.info("5xy0");
                if(this.registers[x] == this.registers[y]) // If register x is equal to register y increment program counter by 2
                {
                    this.pc+=2;
                }
                break;
            case 0x6000:
                LOG.info("6xnn");
                this.registers[x] = nn; // Set register x to nn
                break;
            case 0x7000:
                LOG.info("7xnn");
                this.registers[x] += nn; // Set register x to x + nn
                break;
            case 0x8000: // opcode 0x8xyn (check last byte since first 3 are the same)
                switch(n)
                {
                    case 0x0000:
                        LOG.info("8xy0");
                        this.registers[x] = this.registers[y]; // Set register x to register y
                        break;
                    case 0x0001:
                        LOG.info("8xy1");
                        this.registers[x] = (this.registers[x] | this.registers[y]); // Set register x to x or y
                        break;
                    case 0x0002:
                        LOG.info("8xy2");
                        this.registers[x] = (this.registers[x] & this.registers[y]); // Set register x to x and y
                        break;
                    case 0x0003:
                        LOG.info("8xy3: Set register x to x xor y");
                        this.registers[x] = (this.registers[x] ^ this.registers[y]); // Set register x to x xor y
                        break;
                    case 0x0004:
                        LOG.info("8xy4");
                        this.registers[x] += registers[y]; // Set register x to x + y
                        this.registers[0xF] = this.registers[x] > 0xFF ? 1:0; // If register x is greater than 255 set register F to 1 else 0
                        break;
                    case 0x0005:
                        LOG.info("8xy5");
                        this.registers[0xF] = this.registers[x] > this.registers[y] ? 1:0; // If register x is greater than register y then set register F to 1 else 0
                        this.registers[x] -= this.registers[y]; // Set register x to x - y
                        break;
                    case 0x0006:
                        LOG.info("8xy6");
                        this.registers[0xF] = this.registers[x] & 0x1; // Set register F to lsb of x
                        this.registers[x] = this.registers[x] >> 1; // Set register x to x divided by 2
                        break;
                    case 0x0007:
                        LOG.info("8xy7");
                        this.registers[0xF] = this.registers[y] > this.registers[x] ? 1:0; // Set register F to 1 if y is greater than x else 0
                        this.registers[x] = this.registers[y] - this.registers[x];
                        break;
                    case 0x000E:
                        LOG.info("8xy8");
                        this.registers[0xF] = (this.registers[x] >> 7) & 0x1; // Set register F to msb of x
                        this.registers[x] = this.registers[x] << 1; //Set register x to x * 2
                        break;
                    default:
                        LOG.debug("Unknown opcode: {}", opcode);
                        break;
                }
                break;
            case 0x9000:
                LOG.info("9xy0");
                if(this.registers[x] != registers[y]) // If register x is not equal to y increment pc by 2
                {
                    this.pc+=2;
                }
                break;
            case 0xA000:
                LOG.info("Annn");
                this.I = nnn; // Set register I to address nnn
                break;
            case 0xB000:
                LOG.info("Bnnn");
                this.pc+= (nnn + registers[0]); // Set program counter to nnn + register 0
                break;
            case 0xC000:
                LOG.info("Cxnn");
                Random r = new Random();
                this.registers[x] = (r.nextInt(256) & nn); // Set register x to random number between 0 and 255 and nn
                break;
            case 0xD000:
                LOG.info("Dxy0");
              this.drawFlag = true; // Set draw flag to true
              this.registers[0xF] = 0; // Set register F to 0
              byte px; // Pixel in memory

              for(int j = 0; j < n; j++)
              {
                  px = this.memory[this.I + j]; // Setting pixel to current value at register I plus column j
                  for(int i = 0; i < 8; i++)
                  {
                      if((px & (0x80 >> i)) != 0) // Checking if the current pixel is equal to 1
                      {
                          if(this.display.getPixel(i + registers[x], j + registers[y]) == 1) // Checking if the pixel on display is equal to one
                          {
                              registers[0xF] = 1; // Sets register F to 1 to indicate a collision
                          }
                          byte pixelXOR = (byte) (this.display.getPixel(i + registers[x], j + registers[y]) ^ 1); // Setting the value of the pixel using xor
                          this.display.setPixel(i + registers[x], j + registers[y],pixelXOR);
                      }
                  }
              }
              this.window.repaint();
              break;
            case 0xE000: // Check last 2 bytes of opcode
                switch(opcode & nn){
                    case 0x009E:
                        LOG.info("Ex9E");
                        if(this.keyboardRegisters[this.registers[x]] == 1) //Checks if a key has been pressed
                        {
                            this.pc += 2; //Increments pc by 2
                        }
                        break;
                    case 0x00A1:
                        LOG.info("ExA1");
                        if(this.keyboardRegisters[this.registers[x]] == 0) //Checks if a key hasn't been pressed
                        {
                            this.pc+= 2; //Increments pc by 2
                        }
                        break;
                    default:
                        LOG.debug("Unknown opcode {}", opcode);
                }
            case 0xF000: //Check last 2 bytes of opcode
                switch(opcode & nn){
                    case 0x0007:
                        LOG.info("Fx07");
                        registers[x] = this.delayTimer; // Set register x to delay timer
                        break;
                    case 0x000A:
                        LOG.info("Fx0A");
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
                    case 0x0015:
                        LOG.info("Fx15");
                        this.delayTimer = registers[x]; // Sets the delay timer to register x
                        break;
                    case 0x0018:
                        LOG.info("Fx18");
                        this.soundTimer = registers[x]; // Sets the sound timer to register x
                        break;
                    case 0x001E:
                        LOG.info("Fx1E");
                        this.I += this.registers[x]; // Sets I to I + register x
                        break;
                    case 0x0029:
                        LOG.info("Fx29");
                        this.I = this.registers[x] * 5; // Sets I to sprite at register x
                        break;
                    case 0x0033:
                        LOG.info("Fx33");
                        int value = this.registers[x]; // Gets the value of register x
                        this.memory[this.I+2] = (byte) (value % 10); // Sets I + 2 to the least significant bit
                        value /= 10;
                        this.memory[this.I+1] = (byte) (value % 10); // Sets I + 1 to the middle bit
                        value /= 10;
                        this.memory[this.I] = (byte) (value % 10); // Sets I to the most significant bit
                        break;
                    case 0x0055:
                        LOG.info("Fx55");
                        for(int i = 0; i <= x; i++)
                        {
                            this.memory[this.I + i] = (byte) registers[i]; // Sets in memory the value of register 0 to x starting at I
                        }
                        break;
                    case 0x0065:
                        LOG.info("Fx65");
                        for(int i = 0; i <= x; i++)
                        {
                            registers[i] = this.memory[this.I + i]; // Reads from memory the values starting at I in registers 0 to x
                        }
                        break;
                    default:
                        LOG.debug("Unknown opcode {}", opcode);
                }
            default:
                LOG.debug("Unknown opcode: {}", opcode);
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
