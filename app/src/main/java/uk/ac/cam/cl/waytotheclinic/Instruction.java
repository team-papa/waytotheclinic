package uk.ac.cam.cl.waytotheclinic;

public class Instruction {
    private int instructionIcon;
    private String instructionText;

    public Instruction(int instructionIcon, String instructionText) {
        this.instructionIcon = instructionIcon;
        this.instructionText = instructionText;
    }

    public int getInstructionIcon() {
        return this.instructionIcon;
    }

    public String getInstructionText() {
        return this.instructionText;
    }
}
