package net.automatalib.automata.base.compact;

import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.ca.MutableFIFOA;
import net.automatalib.automata.concepts.ChannelNamesHolder;
import net.automatalib.automata.concepts.TransitionAction;
import net.automatalib.automata.simple.SimpleDeterministicAutomaton;
import net.automatalib.words.Alphabet;
import net.automatalib.words.PhiChar;
import net.automatalib.words.impl.Alphabets;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class UniversalChannelCompact<C,I> extends AbstractCompact<I, CompactTransition<TransitionAction<C,I>>, Void, TransitionAction<C,I>>
                                          implements MutableFIFOA<Integer, C, I, CompactTransition<TransitionAction<C,I>>>,
                                                     MutableDeterministic.StateIntAbstraction<I, CompactTransition<TransitionAction<C,I>>, Void, TransitionAction<C,I>>,
                                                     MutableDeterministic.FullIntAbstraction<CompactTransition<TransitionAction<C,I>>, Void, TransitionAction<C,I>>,
                                                     Serializable,
                                                     ChannelNamesHolder<C> {

    private int[] transitions;
    private @Nullable TransitionAction<C, I>[] transitionProperties;
    private int initial = AbstractCompact.INVALID_STATE;
    private final Alphabet<C> channelNames;


    public UniversalChannelCompact(Alphabet<C> channelNames, Alphabet<I> alphabet) {
        this(channelNames, alphabet, DEFAULT_INIT_CAPACITY, DEFAULT_RESIZE_FACTOR);
    }

    public UniversalChannelCompact(Alphabet<C> channelNames, Alphabet<I> alphabet, int stateCapacity, float resizeFactor) {
        super(alphabet, stateCapacity, resizeFactor);
        this.channelNames = channelNames;

        final int size = stateCapacity * numInputs();

        this.transitions = new int[size];
        Arrays.fill(transitions, AbstractCompact.INVALID_STATE);
        this.transitionProperties = new TransitionAction[size];
        Arrays.fill(transitionProperties, null);

    }

    public Alphabet<C> getChannelNames() {
        return channelNames;
    }

    @Override
    public int getIntSuccessor(CompactTransition<TransitionAction<C, I>> transition) {
        return transition.getSuccId();
    }

    @Override
    public @Nullable CompactTransition<TransitionAction<C, I>> getTransition(int state, I input) {
        return getTransition(state, getSymbolIndex(input));
    }

    @Override
    public @Nullable CompactTransition<TransitionAction<C, I>> getTransition(int state, int input) {
        final int idx = toMemoryIndex(state, input);
        final int succ = transitions[idx];

        if (succ == AbstractCompact.INVALID_STATE) {
            return null;
        }

        final TransitionAction<C, I> action = transitionProperties[idx];

        return new CompactTransition<>(idx, succ, action);
    }

    @Override
    public void setStateProperty(Integer state, Void property) {
    }

    @Override
    public void setTransitionProperty(CompactTransition<TransitionAction<C, I>> transition, TransitionAction<C, I> property) {
        transition.setProperty(property);
    }

    @Override
    public void setInitialState(int stateId) {
        initial = stateId;
    }

    @Override
    public CompactTransition<TransitionAction<C, I>> createTransition(int successor, TransitionAction<C, I> property) {
        return new CompactTransition<>(successor, property);
    }

    @Override
    public int addIntInitialState(@Nullable Void property) {
        this.initial = addIntState(property);
        return this.initial;
    }

    @Override
    public void removeAllTransitions(Integer state) {
        final int lower = state * numInputs();
        final int upper = lower + numInputs();
        Arrays.fill(transitions, lower, upper, AbstractCompact.INVALID_STATE);
        Arrays.fill(transitionProperties, lower, upper, null);
    }

    @Override
    public CompactTransition<TransitionAction<C, I>> createTransition(Integer successor, TransitionAction<C, I> properties) {
        return createTransition(successor.intValue(), properties);
    }

    // Returns only the used transitions
    public int[] getTransitions() {
        int limit = this.transitions.length;
        for(int i = 0; i < this.transitions.length; i++){
            if(this.transitions[i] == AbstractCompact.INVALID_STATE) {
                limit = i;
                break;
            }
        }
        //end is exclusive but break is late
        return Arrays.copyOfRange(this.transitions, 0, limit);
    }

    public int getTransitionOriginState(int i) {
        return getStateFromMemoryIndex(i);
    }

    public int getTransitionTargetState(int i) {
        return this.transitions[i];
    }

    public @Nullable TransitionAction<C, I>[] getTransitionProperties() {
        int limit = this.transitionProperties.length;
        for(int i = 0; i < this.transitionProperties.length; i++){
            if(this.transitionProperties[i] == null) {
                limit = i;
                break;
            }
        }
        //end is exclusive but break is late
        return Arrays.copyOfRange(this.transitionProperties, 0, limit);
    }

    public @Nullable TransitionAction<C, I> getTransitionProperty(int i) {
        if (this.transitionProperties == null) return null;
        return this.transitionProperties[i];
    }


    @Override
    public void setInitialState(@Nullable Integer state) {
        this.initial = toId(state);
    }

    @Override
    public void setTransition(Integer state, I input, @Nullable CompactTransition<TransitionAction<C, I>> transition) {
        setTransition(state, getSymbolIndex(input), transition);
    }

    @Override
    public void setTransition(int state, I input, @Nullable CompactTransition<TransitionAction<C, I>> transition) {
        setTransition(state, getSymbolIndex(input), transition);
    }

    @Override
    public void setTransition(int state, I input, int successor, TransitionAction<C, I> property) {
        setTransition(state, getSymbolIndex(input), successor, property);
    }

    @Override
    public void setTransition(int state, int input, @Nullable CompactTransition<TransitionAction<C, I>> transition) {
        if (transition == null) {
            setTransition(state, input, AbstractCompact.INVALID_STATE, null);
        } else {
            setTransition(state, input, transition.getSuccId(), transition.getProperty());
            transition.setMemoryIdx(toMemoryIndex(state, input));
        }
    }

    @Override
    public void setTransition(int state, int input, int successor, TransitionAction<C, I> property) {
        final int idx = toMemoryIndex(state, input);
        transitions[idx] = successor;
        transitionProperties[idx] = property;
    }

    @Override
    public Void getStateProperty(int state) {
        return null;
    }

    @Override
    public int getIntInitialState() {
        return this.initial;
    }

    @Override
    public @Nullable CompactTransition<TransitionAction<C, I>> getTransition(Integer state, I input) {
        return getTransition(state, getSymbolIndex(input));
    }

    @Override
    public Integer getSuccessor(CompactTransition<TransitionAction<C, I>> transition) {
        return getIntSuccessor(transition);
    }

    @Override
    public Void getStateProperty(Integer state) {
        return null;
    }

    @Override
    public TransitionAction<C, I> getTransitionProperty(CompactTransition<TransitionAction<C, I>> transition) {
        return transition.getProperty();
    }

    @Override
    public @Nullable Integer getInitialState() {
        return this.initial;
    }

    @Override
    public void setStateProperty(int state, @Nullable Void property) {
    }

    /**
     * @return all the possible PhiChar for this
     */
    public Alphabet<PhiChar> getAnnotationAlphabet() {
        List<PhiChar> possibilities = new ArrayList<>();

        //Both bar and unbar only for sender
        for (int i = 0; i < this.transitions.length; i++) {
            if (this.transitions[i] != SimpleDeterministicAutomaton.IntAbstraction.INVALID_STATE) {
                if (this.transitionProperties[i].getAction() == TransitionAction.Action.PUSH) {
                    possibilities.add(new PhiChar(i, false));
                    possibilities.add(new PhiChar(i, true));
                }
            }
        }

        // State symbols
        for (int i : this.getStates()) {
            possibilities.add(new PhiChar(i, false, true));
        }
        return Alphabets.fromList(possibilities);
    }
}