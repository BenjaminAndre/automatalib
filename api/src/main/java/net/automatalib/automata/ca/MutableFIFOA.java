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
package net.automatalib.automata.ca;

import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.concepts.TransitionAction;
import net.automatalib.automata.graphs.CAGraphView;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.visualization.FIFOVisualizationHelper;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.visualization.VisualizationHelper;

import java.util.Collection;

/**
 * @author fh
 */
public interface MutableFIFOA<S, C, I, T> extends FIFOA<S, C, I, T>, MutableDeterministic<S, I, T, Void, TransitionAction<C,I>>, MutableCA<S, C, I, T> {

    default UniversalGraph<S, TransitionEdge<I, T>, Void, TransitionEdge.Property<I, TransitionAction<C,I>>> transitionGraphView(
            Collection<? extends I> inputs, Collection<? extends C> channels) {
        return new MutableFIFOA.FIFOGraphView<>(this, inputs, channels);
    }

    class FIFOGraphView<S, C, I, T, A extends MutableFIFOA<S, C, I, T>>
            extends CAGraphView<S, C, I, T, A> {

        public FIFOGraphView(A automaton, Collection<? extends I> inputs, Collection<? extends C> channels) {
            super(automaton, inputs, channels);
        }

        @Override
        public VisualizationHelper<S, TransitionEdge<I, T>> getVisualizationHelper() {
            return new FIFOVisualizationHelper<>(automaton);
        }
    }

}
