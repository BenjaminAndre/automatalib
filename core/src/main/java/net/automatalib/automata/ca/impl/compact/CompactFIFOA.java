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
package net.automatalib.automata.ca.impl.compact;

import net.automatalib.automata.base.compact.AbstractCompact;
import net.automatalib.automata.base.compact.CompactTransition;
import net.automatalib.automata.base.compact.UniversalChannelCompact;
import net.automatalib.automata.ca.MutableFIFOA;
import net.automatalib.automata.concepts.TransitionAction;
import net.automatalib.words.Alphabet;
import net.automatalib.words.PhiChar;
import net.automatalib.words.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompactFIFOA<C,I>
        extends UniversalChannelCompact<C, I>
        implements MutableFIFOA<Integer, C, I, CompactTransition<TransitionAction<C,I>>> {

    private int[][] transitionOrder;

    public CompactFIFOA(Alphabet<C> channelNames, Alphabet<I> alphabet, int stateCapacity) {
        super(channelNames, alphabet, stateCapacity, DEFAULT_RESIZE_FACTOR);
    }

    public CompactFIFOA(Alphabet<C> channelNames, Alphabet<I> alphabet) {
        this(channelNames, alphabet, DEFAULT_INIT_CAPACITY);
    }

    private Integer[] getConsummingTransitions(C channel, I symbol){
        List<Integer> elements = new ArrayList<Integer>();
        for(int i = 0; i < this.getTransitionProperties().length; i++){
            //Null values are useless
            if(this.getTransitionProperties()[i] == null) continue;
            TransitionAction ta = this.getTransitionProperty(i);
            if(ta.getChannelName() == channel && ta.getManipulatedSymbol() == symbol && ta.getAction() == TransitionAction.Action.PULL) {
                elements.add(i);
            }
        }
        return elements.toArray(new Integer[0]);
    }


    @Override
    public void setInitial(Integer state, boolean initial) {
        Integer currInitial = getInitialState();
        boolean equal = Objects.equals(state, currInitial);

        if (initial) {
            if (currInitial == null || currInitial.intValue() == AbstractCompact.INVALID_STATE) {
                setInitialState(state);
            } else if (!equal) {
                throw new IllegalStateException(
                        "Cannot set state '" + state + "' as " + "additional initial state (current initial state: '" +
                                currInitial + "'.");
            }
            // else the previous initial state remains the same
        } else if (equal) {
            setInitialState(null);
        }
        // else 'state' remains a non-initial state
    }

    @Override
    public boolean isValidAnnotedTrace(Word<PhiChar> gamma) {
        List<PhiChar> pcs = gamma.asList();
        if(pcs.size() == 0) {
            return false;
        }
        for(int i = 0; i < pcs.size()-1; i++) {
            PhiChar pc = pcs.get(i);
            //Only the last symbol can represent a state
            if(pc.representsState()){
                return false;
            } else {
                //In a not state representing char.
                // To be valid, its int value should represent a pushing transition
                // (because all pulling transitions are either consumed or couldn't be respected in the first place)
                int transitionIndex = pc.getI();
                TransitionAction ta = this.getTransitionProperty(transitionIndex);
                if(ta.getAction() != TransitionAction.Action.PUSH) return false;
            }
        }
        //Also needs the last symbol to represent a state
        return pcs.get(pcs.size()-1).representsState();
    }

    /**
     * A complex algorithm, Annex A of the master thesis
     * @param gamma The trace to analyse
     * @param qa the active state (q0 by default)
     * @return if the trace leads to a valid state
     */
    public boolean validateTrace(Word<PhiChar> gamma, int qa) {
        if(!isValidAnnotedTrace(gamma)){
            return false;
        }
        // First symbol and the rest
        PhiChar a = gamma.getSymbol(0);
        Word<PhiChar> gammaPrime = gamma.subWord(1, gamma.size());

        //If there is only a state, it can only by q0 as it is the initial state
        if(a.representsState()) {
            if(a.getI() == qa) {
                return true;
            }
        } else if(a.isBarred()) {
            PhiChar aPrime = a.unbarred();//new object
            int ai = aPrime.getI();
            // get (p, c!m, q) from delta(a')
            int p = this.getTransitionOriginState(ai);
            TransitionAction<C,I> cm = this.getTransitionProperty(ai);
            int q = this.getTransitionTargetState(ai);

            //Transition needs to be starting from the current state to be valid
            if(p == qa) {
                // Every theta_r consuming m in channel c
                for(int r : getConsummingTransitions(cm.getChannelName(), cm.getManipulatedSymbol())){
                    PhiChar thetaR = new PhiChar(r);
                    //Every position but not after the last symbol (which is the end state)
                    for(int i = 0; i < gammaPrime.size()-1; i++) {
                        Word<PhiChar> gammaSecond = gammaPrime.insert(thetaR, i);
                        if(validateTrace(gammaSecond, q)){
                            return true;
                        }
                    }
                }
            }
        } else {
            int p = this.getTransitionOriginState(a.getI());
            int q = this.getTransitionTargetState(a.getI());
            // Action is not important since we don't write the content of the canals
            if(p == qa) {
                return validateTrace(gammaPrime, q);
            }
        }
        return false;
    }

    @Override
    public boolean validateTrace(Word<PhiChar> gamma) {
        return validateTrace(gamma, 0);//First active state is the origin : q0
    }

    @Override
    public int[][] getTransitionOrder() {
        if(this.transitionOrder != null) {
            return this.transitionOrder;
        } else {
            //Floyd-Warshall where distance is number of states between transitions
            int[] transitions = this.getTransitions();
            int n = transitions.length;
            int[][] W = new int[n][n];

            //For each pair of transitions i and j
            for(int i = 0; i < n; i++) {
                for(int j = 0; j < n; j++) {

                    //Does i lead to a state that j starts from ?
                    boolean match = this.getTransitionTargetState(i) == this.getTransitionOriginState(j);
                    W[i][j] = match ? 1 : Integer.MAX_VALUE;
                }
            }

            //Floyd-Warshall
            for(int k = 0; k < n; k++) {
                for(int i = 1; i < n; i++) {
                    for(int j = 1; j < n; j++) {
                        W[i][j] = Math.min(W[i][j], W[i][k] + W[k][j]);
                    }
                }
            }

            return W;
        }
    }


}
