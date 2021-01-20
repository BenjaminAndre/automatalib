package net.automatalib.automata.ca;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.DetSuffixOutputAutomaton;
import net.automatalib.automata.concepts.TransitionAction;
import net.automatalib.automata.fsa.NFA;
import net.automatalib.ts.UniversalTransitionSystem;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;

import java.util.Collection;

import java.util.Collection;
import java.util.List;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.DetSuffixOutputAutomaton;
import net.automatalib.ts.acceptors.DeterministicAcceptorTS;

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
}
