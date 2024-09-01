package com.arjun.chip8;

public class Instruction {

        public enum Instructions {
            CLS, RET, SYS, JP, CALL, SE, SNE, LD, ADD, XOR, SUB, SHR, SUBN, SHL, DRW, SKP, SKNP, RND, RTS, OR
        }

        public enum Operand {
            V0,V1,V2,V3,V4,V5,V6,V7,V8,V9,VA,VB,VC,VD,VE,VF,F,B,I,ST,DT,I_ARRAY
        }

        private Instructions instructions;
        private Operand op1,op2;
        private int value;

        public Instruction(Instructions instructions)
        {
            this.instructions = instructions;
        }

        public Instruction(Instructions instructions, Operand op1, int value)
        {
            this.instructions = instructions;
            this.op1 = op1;
            this.value = value;
        }

        public Instruction(Instructions instructions, Operand op1, Operand op2, int value)
        {
            this.instructions = instructions;
            this.op1 = op1;
            this.op2 = op2;
            this.value = value;
        }

        public Instruction(Instructions instructions, Operand op1, Operand op2)
        {
            this.instructions = instructions;
            this.op1 = op1;
            this.op2 = op2;
        }

        public Instruction(Instructions instructions, int value)
        {
            this.instructions = instructions;
            this.value = value;
        }
}
