package net.automatalib.automata.concepts;

public interface TransitionAction<C,I> {

    C getChannelName();
    void setChannelName(C channelName);

    I getManipulatedSymbol();
    void setManipulatedSymbol(I symbol);


    Action getAction();
    void setAction(Action action);


    enum Action {
        PUSH("!"),
        PULL("?"),
        PASS("τ");

        private String symbol;

        Action(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol(){
            return this.symbol;
        }
    }

}
