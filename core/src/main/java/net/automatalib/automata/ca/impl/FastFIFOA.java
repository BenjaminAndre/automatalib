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
package net.automatalib.automata.ca.impl;

import net.automatalib.automata.base.fast.AbstractFastMutable;
import net.automatalib.automata.base.fast.AbstractFastMutableDet;
import net.automatalib.automata.base.fast.AbstractFastMutableNondet;
import net.automatalib.automata.base.fast.AbstractFastTransition;
import net.automatalib.automata.ca.MutableFIFOA;
import net.automatalib.automata.concepts.TransitionAction;
import net.automatalib.automata.fsa.MutableNFA;
import net.automatalib.commons.util.WrapperUtil;
import net.automatalib.words.Alphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

// T is the type of transition
public class FastFIFOA<C, I> extends AbstractFastMutableDet<FastFIFOAState<C, I>, I, FastFIFOATransition<C, I>, Void, FastFIFOATransitionAction<C,I>> {

    public FastFIFOA(Alphabet<I> inputAlphabet) {
        super(inputAlphabet);
    }

    @Override
    protected FastFIFOAState createState(@Nullable Void property) {
        return new FastFIFOAState(inputAlphabet.size());
    }

    @Override
    public void setStateProperty(FastFIFOAState state, @Nullable Void property) { return; }

    @Override
    public Void getStateProperty(FastFIFOAState state) {
        return null;
    }

    @Override
    public void setTransitionProperty(FastFIFOATransition<C,I> transition, FastFIFOATransitionAction<C,I> property) {
        transition.setProperty(property);
    }

    @Override
    public FastFIFOATransition<C,I> createTransition(FastFIFOAState<C,I> successor, FastFIFOATransitionAction<C,I> properties) {
        FastFIFOATransition<C,I> transition = new FastFIFOATransition<C,I>(successor, properties);
        return transition;
    }

    @Override
    public FastFIFOAState<C,I> getSuccessor(FastFIFOATransition<C,I> transition) {
        return transition.getSuccessor();
    }

    @Override
    public FastFIFOATransitionAction<C,I> getTransitionProperty(FastFIFOATransition<C,I> transition) {
        return transition.getProperty();
    }
}