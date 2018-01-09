package enshud.s4.compiler;

public enum CASLInst
{
	LD, ST, LAD,
	ADDA, ADDL, SUBA, SUBL, AND, OR, XOR, CPA, CPL, SLA, SRA, SLL, SRL,
	JPL, JMI, JNZ, JZE, JOV, JUMP,
	PUSH, POP, RPUSH, RPOP,
	CALL, RET,
	SVC, NOP,
	START, END, DS, DC, IN, OUT
}
