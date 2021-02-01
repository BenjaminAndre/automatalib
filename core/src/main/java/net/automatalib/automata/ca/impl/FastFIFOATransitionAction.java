package net.automatalib.automata.ca.impl;

import net.automatalib.automata.concepts.TransitionAction;


public class FastFIFOATransitionAction<C,I> implements TransitionAction<C,I> {

    private C channelName;
    private Action action;
    private I manipulatedSymbol;


    public FastFIFOATransitionAction(C channelName) {
        setChannelName(channelName);
        setAction(Action.PASS);
    }

    public FastFIFOATransitionAction(C channelName, I inputSymbol) {
        this(channelName);
        setManipulatedSymbol(inputSymbol);
    }


    public FastFIFOATransitionAction(C channelName, Action action, I inputSymbol){
        this(channelName, inputSymbol);
        setAction(action);
    }

    @Override
    public C getChannelName() {
        return channelName;
    }

    @Override
    public void setChannelName(C channelName) {
        this.channelName = channelName;
    }

    @Override
    public I getManipulatedSymbol() {
        return manipulatedSymbol;
    }

    @Override
    public void setManipulatedSymbol(I symbol) {
        this.manipulatedSymbol = symbol;
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public void setAction(Action action) {
        this.action = action;
    }

    public String toString(){
        return getChannelName() + getAction().getSymbol() + getManipulatedSymbol();
    }
}
