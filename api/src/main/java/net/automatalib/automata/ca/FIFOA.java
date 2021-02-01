package net.automatalib.automata.ca;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.TransitionAction;
import net.automatalib.ts.UniversalTransitionSystem;
import net.automatalib.words.PhiChar;
import net.automatalib.words.Word;

/**
 * Automaton with channels working in a FIFO way. The Transition Type List refers to a triplet (C,Action,I)
 * @param <S> Type of states
 * @param <I> Type of character being manipulated
 * @param <C> Type of channel names
 */
public interface FIFOA<S, C, I, T> extends UniversalDeterministicAutomaton<S, I, T, Void, TransitionAction<C,I>>,
                                        UniversalTransitionSystem<S,I,T,Void, TransitionAction<C,I>>,
                                        Automaton<S,I,T>
{

    //does it uses correct transition names and ends with a sate, the only one ?
    boolean isValidAnnotedTrace(Word<PhiChar> input);

    //does the input trace lead to a valid state ?
    boolean validateTrace(Word<PhiChar> input);

    // For each transition id, get the distance to each other transition id(one state = 1 distance)
    int[][] getTransitionOrder();

}
