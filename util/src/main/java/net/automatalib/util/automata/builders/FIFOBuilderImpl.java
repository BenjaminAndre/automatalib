/* Copyright (C) 2013-2021 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.util.automata.builders;

import com.github.misberner.duzzt.annotations.DSLAction;
import com.github.misberner.duzzt.annotations.GenerateEmbeddedDSL;
import net.automatalib.automata.base.compact.CompactTransition;
import net.automatalib.automata.ca.MutableFIFOA;
import net.automatalib.automata.ca.impl.FastFIFOATransitionAction;
import net.automatalib.automata.concepts.TransitionAction;

import java.util.*;

@GenerateEmbeddedDSL(name = "FIFOBuilder",
        enableAllMethods = false,
        syntax = "(from( on (write|read) (loop|to) )+ )* withInitial create")


@SuppressWarnings("nullness") // nullness correctness guaranteed by states of regular expression
class FIFOBuilderImpl<S, C, I, A extends MutableFIFOA<S,C,I, CompactTransition<TransitionAction<C,I>>>> {


    protected final A automaton;
    private final Map<Object, S> stateMap = new HashMap<>();

    protected List<S> currentStates;
    protected List<I> currentInputs;
    protected List<C> currentChannelNames;
    protected TransitionAction<C,I> currentTransProp;

    FIFOBuilderImpl(A automaton) {
        this.automaton = automaton;
    }


    @DSLAction
    public void from(Object stateId) {
        this.currentStates = getStates(stateId);
        this.currentInputs = null;
        this.currentChannelNames = null;
    }

    @DSLAction
    public void from(Object firstStateId, Object... otherStateIds) {
        this.currentStates = getStates(firstStateId, otherStateIds);
    }

    protected List<S> getStates(Object firstStateId, Object... otherStateIds) {
        if (otherStateIds.length == 0) {
            return Collections.singletonList(getState(firstStateId));
        }
        List<S> result = new ArrayList<>(1 + otherStateIds.length);
        result.add(getState(firstStateId));
        for (Object otherId : otherStateIds) {
            result.add(getState(otherId));
        }
        return result;
    }

    protected S getState(Object stateId) {
        if (stateMap.containsKey(stateId)) {
            return stateMap.get(stateId);
        }
        S state = automaton.addState();
        stateMap.put(stateId, state);
        return state;
    }

    @DSLAction
    public void to(Object stateId) {
        S tgt = getState(stateId);
        for (S src : currentStates) {
            for (I input : currentInputs) {
                automaton.addTransition(src, input, tgt, currentTransProp);
            }
        }
    }

    @DSLAction
    public void loop() {
        for (S src : currentStates) {
            for (I input : currentInputs) {
                automaton.addTransition(src, input, src, currentTransProp);
            }
        }
    }

    @DSLAction(terminator = true)
    public A create() {
        return automaton;
    }

    @DSLAction
    public void withInitial(Object stateId) {
        S state = getState(stateId);
        automaton.setInitial(state, true);
    }

    @DSLAction
    public void on(C channelName) {
        this.currentChannelNames = Collections.singletonList(channelName);
        this.currentTransProp = new FastFIFOATransitionAction<>(channelName);
    }

    @DSLAction
    @SafeVarargs
    public final void on(C firstChannel, C... otherChannels) {
        this.currentChannelNames = new ArrayList<>(1 + otherChannels.length);
        this.currentChannelNames.add(firstChannel);
        Collections.addAll(this.currentChannelNames, otherChannels);
        this.currentTransProp = new FastFIFOATransitionAction<>(firstChannel);
    }

    @DSLAction
    public void read(I symbol) {
        this.currentInputs = Collections.singletonList(symbol);
        this.currentTransProp.setAction(TransitionAction.Action.PUSH);
        this.currentTransProp.setManipulatedSymbol(symbol);
    }

    @DSLAction
    @SafeVarargs
    public final void read(I firstSymbol, I... otherSymbols) {
        this.currentInputs = new ArrayList<>(1 + otherSymbols.length);
        this.currentInputs.add(firstSymbol);
        Collections.addAll(this.currentInputs, otherSymbols);
        this.currentTransProp.setAction(TransitionAction.Action.PUSH);
        this.currentTransProp.setManipulatedSymbol(firstSymbol);
    }

    @DSLAction
    public void write(I symbol) {
        this.currentInputs = Collections.singletonList(symbol);
        this.currentTransProp.setAction(TransitionAction.Action.PULL);
        this.currentTransProp.setManipulatedSymbol(symbol);
    }

    @DSLAction
    @SafeVarargs
    public final void write(I firstSymbol, I... otherSymbols) {
        this.currentInputs = new ArrayList<>(1 + otherSymbols.length);
        this.currentInputs.add(firstSymbol);
        Collections.addAll(this.currentInputs, otherSymbols);
        this.currentTransProp.setAction(TransitionAction.Action.PULL);
        this.currentTransProp.setManipulatedSymbol(firstSymbol);
    }

}
