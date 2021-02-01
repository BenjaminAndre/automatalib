package net.automatalib.automata.graphs;

import net.automatalib.automata.Automaton;
import net.automatalib.automata.ca.MutableCA;
import net.automatalib.automata.concepts.ChannelNamesHolder;
import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.concepts.TransitionAction;
import net.automatalib.graphs.UniversalGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CAGraphView<S, C, I, T, A extends MutableCA<S, C, I, T>> implements UniversalGraph<S, TransitionEdge<I, T>, Void, TransitionEdge.Property<I, TransitionAction<C,I>>> {

    protected final A automaton;
    protected final Collection<? extends I> inputs;
    protected final Collection<? extends C> channels;

    public CAGraphView(A automaton, Collection<? extends I> inputs, Collection<? extends C> channels) {
        this.automaton = automaton;
        this.inputs = inputs;
        this.channels = channels;
    }

    public static <S, C, I, T, A extends MutableCA<S, C, I, T>> CAGraphView<S, C, I, T, A> create(A automaton,
                                                                                                Collection<? extends I> inputs, Collection<? extends C> channels) {
        return new CAGraphView<>(automaton, inputs, channels);
    }

    public static <S, C, I, T, A extends MutableCA<S, C, I, T> & InputAlphabetHolder<I> & ChannelNamesHolder<C>> CAGraphView<S, C, I, T, A> create(
            A automaton) {
        return new CAGraphView<>(automaton, automaton.getInputAlphabet(), automaton.getChannelNames());
    }

    @Override
    public Collection<TransitionEdge<I, T>> getOutgoingEdges(S node) {
        return createTransitionEdges(automaton, inputs, node);
    }

    public static <S, I, T> Collection<TransitionEdge<I, T>> createTransitionEdges(Automaton<S, I, T> automaton,
                                                                                   Collection<? extends I> inputs,
                                                                                   S state) {
        List<TransitionEdge<I, T>> result = new ArrayList<>();

        for (I input : inputs) {
            Collection<T> transitions = automaton.getTransitions(state, input);
            for (T t : transitions) {
                result.add(new TransitionEdge<>(input, t));
            }
        }

        return result;
    }


    @Override
    public S getTarget(TransitionEdge<I, T> edge) {
        return automaton.getSuccessor(edge.getTransition());
    }


    @Override
    public Collection<S> getNodes() {
        return automaton.getStates();
    }

    @Override
    public Void getNodeProperty(S node) {
        return null;
    }

    @Override
    public TransitionEdge.Property<I, TransitionAction<C, I>> getEdgeProperty(TransitionEdge<I, T> edge) {
        return new TransitionEdge.Property<>(edge.getInput(), automaton.getTransitionProperty(edge.getTransition()));
    }
}
