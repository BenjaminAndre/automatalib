package net.automatalib.words;

/**
 * Represents a character belonging to Phi : the barred theta and the state names.
 */
public class PhiChar {

    //Number of the transition or the state
    private int i;
    private Type type;

    /**
     * Barrable theta character
     * @param i the indice of theta_i
     */
    public PhiChar(int i) {
        this(i, false);
    }

    /**
     * Barrable theta character
     * @param i indice of theta_i
     * @param barred is theta_i barred (means it corresponds to another transition that consumes a character)
     */
    public PhiChar(int i, boolean barred) {
        this(i, barred, false);
    }

    public PhiChar(int i, boolean barred, boolean state) {
        this.i = i;
        if(state) {
            this.type = Type.STATE;
        } else {
            this.type = barred ? Type.BARRED : Type.THETA;
        }
    }

    public int getI() { return i;}


    public String toString() {
        return this.type.prefix+Integer.toString(i);
    }

    public boolean representsState() {
        return this.type == Type.STATE;
    }

    public boolean isBarred() {
        return this.type == Type.BARRED;
    }

    @Override
    public boolean equals(Object other){
        if(!(other instanceof PhiChar)){
            return false;
        } else {
            PhiChar pother = (PhiChar) other;
            return this.i == pother.i && this.type == pother.type;
        }
    }

    //Makes no sense for state symbols
    public PhiChar unbarred() {
        if (this.type == Type.STATE) return null;
        return new PhiChar(this.i, false, false);
    }

    enum Type {
        THETA('t'),
        BARRED('b'),
        STATE('s');

        public char prefix;

        Type(char prefix){
            this.prefix = prefix;
        }
    }
}
