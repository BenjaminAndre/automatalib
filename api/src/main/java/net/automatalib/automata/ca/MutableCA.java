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

import net.automatalib.automata.MutableAutomaton;
import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.concepts.TransitionAction;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.ts.UniversalTransitionSystem;

import java.util.Collection;

/**
 * @author fh
 */
public interface MutableCA<S, C, I, T> extends UniversalTransitionSystem<S,I,T,Void,TransitionAction<C,I>>,
                                            UniversalAutomaton<S,I,T,Void,TransitionAction<C,I>>,
                                            MutableAutomaton<S,I,T,Void,TransitionAction<C,I>> {


    UniversalGraph<S, TransitionEdge<I, T>, Void, TransitionEdge.Property<I, TransitionAction<C,I>>> transitionGraphView(
            Collection<? extends I> inputs, Collection<? extends C> channels);

}
