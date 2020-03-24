/* Copyright (C) 2013-2019 TU Dortmund
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
package net.automatalib.util.ts.modal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.fsa.NFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.commons.util.Pair;
import net.automatalib.ts.modal.CompactMTS;
import net.automatalib.ts.modal.ModalEdgeProperty;
import net.automatalib.ts.modal.ModalTransitionSystem;
import net.automatalib.ts.modal.MutableModalEdgeProperty;
import net.automatalib.ts.modal.MutableModalTransitionSystem;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.util.fixedpoint.Worksets;
import net.automatalib.words.Alphabet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author msc
 * @author frohme
 */
public final class MTSUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MTSUtil.class);

    private MTSUtil() {
        // prevent instantiation
    }

    public static <S0, S1, I, T0, T1, TP0 extends ModalEdgeProperty, TP1 extends ModalEdgeProperty> CompactMTS<I> conjunction(
            ModalTransitionSystem<S0, I, T0, TP0> mc0, ModalTransitionSystem<S1, I, T1, TP1> mc1) {
        return conjunction(mc0, mc1, CompactMTS::new);
    }

    public static <A extends MutableModalTransitionSystem<S, I, T, TP>, S, S0, S1, I, T, T0, T1, TP extends MutableModalEdgeProperty, TP0 extends ModalEdgeProperty, TP1 extends ModalEdgeProperty> A conjunction(
            ModalTransitionSystem<S0, I, T0, TP0> mc0,
            ModalTransitionSystem<S1, I, T1, TP1> mc1,
            AutomatonCreator<A, I> creator) {
        return Worksets.map(new ModalConjunction<>(mc0, mc1, creator)).getSecond();
    }

    public static <S0, S1, I, T0, T1, TP0 extends ModalEdgeProperty, TP1 extends ModalEdgeProperty> CompactMTS<I> compose(
            ModalTransitionSystem<S0, I, T0, TP0> mc0, ModalTransitionSystem<S1, I, T1, TP1> mc1) {
        return compose(mc0, mc1, CompactMTS::new);
    }

    public static <A extends MutableModalTransitionSystem<S, I, T, TP>, S, S0, S1, I, T, T0, T1, TP extends MutableModalEdgeProperty, TP0 extends ModalEdgeProperty, TP1 extends ModalEdgeProperty> A compose(
            ModalTransitionSystem<S0, I, T0, TP0> mc0,
            ModalTransitionSystem<S1, I, T1, TP1> mc1,
            AutomatonCreator<A, I> creator) {
        return Worksets.map(new ModalParallelComposition<>(mc0, mc1, creator)).getSecond();
    }

    public static <AS, I, AT, ATP extends ModalEdgeProperty, BS, BT, BTP extends ModalEdgeProperty> boolean isRefinementOf(
            ModalTransitionSystem<AS, I, AT, ATP> a,
            ModalTransitionSystem<BS, I, BT, BTP> b,
            Collection<I> input) {

        final Set<Pair<AS, BS>> refinement = ModalRefinement.refinementRelation(a, b, input);

        final Set<AS> statesA = new HashSet<>(a.getInitialStates());
        final Set<BS> statesB = new HashSet<>(b.getInitialStates());

        for (Pair<AS, BS> p : refinement) {
            statesA.remove(p.getFirst());
            statesB.remove(p.getSecond());
        }

        LOGGER.info("Counterexamples: {}, {}", statesA, statesB);

        return statesA.isEmpty() && statesB.isEmpty();
    }

    public static <S, I, T, TP extends ModalEdgeProperty> NFA<?, I> asNFA(ModalTransitionSystem<S, I, T, TP> mts,
                                                                          boolean maximal) {

        final Alphabet<I> alphabet = mts.getInputAlphabet();
        final CompactNFA<I> result = new CompactNFA<>(alphabet);
        AutomatonLowLevelCopy.rawCopy(AutomatonCopyMethod.STATE_BY_STATE,
                                      mts,
                                      alphabet,
                                      result,
                                      sp -> true,
                                      tp -> null,
                                      sf -> true,
                                      (s, i, t) -> maximal || mts.getTransitionProperty(t).isMust());

        return result;
    }
}
