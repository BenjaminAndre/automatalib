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
package net.automatalib.ts.modal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.graphs.UniversalAutomatonGraphView;
import net.automatalib.automata.visualization.MCVisualizationHelper;
import net.automatalib.automata.visualization.MMCVisualizationHelper;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.ts.modal.ModalContractEdgeProperty.EdgeColor;
import net.automatalib.ts.modal.ModalEdgeProperty.ModalType;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.impl.ArrayAlphabet;
import net.automatalib.words.impl.GrowingMapAlphabet;

public class MembershipMC<I> extends AbstractCompactMTS<I, ModalContractMembershipEdgePropertyImpl>
        implements MutableModalContract<Integer, I, MTSTransition<I, ModalContractMembershipEdgePropertyImpl>, ModalContractMembershipEdgePropertyImpl> {

    protected final GrowingAlphabet<I> communicationAlphabet;
    protected Set<MTSTransition<I, ModalContractMembershipEdgePropertyImpl>> redTransitions;

    public MembershipMC(Alphabet<I> alphabet, Alphabet<I> gamma) {
        super(alphabet);
        this.communicationAlphabet = new GrowingMapAlphabet<I>(gamma);
        this.redTransitions = new HashSet<>();

        assert new HashSet<>(alphabet).containsAll(gamma) : "Communication alphabet needs to be a subset of alphabet";
    }

    @Override
    protected ModalContractMembershipEdgePropertyImpl getDefaultTransitionProperty() {
        return buildModalProperty(ModalType.MUST);
    }

    @Override
    protected ModalContractMembershipEdgePropertyImpl buildModalProperty(ModalType type) {
        return buildContractProperty(type, false, EdgeColor.NONE, -1);
    }

    public ModalContractMembershipEdgePropertyImpl buildContractProperty(ModalType type, boolean tau, EdgeColor color, int id) {
        return new ModalContractMembershipEdgePropertyImpl(type, tau, color, id);
    }

    @Override
    public boolean isSymbolInCommunicationAlphabet(I symbol) {
        return communicationAlphabet.containsSymbol(symbol);
    }

    @Override
    public Set<MTSTransition<I, ModalContractMembershipEdgePropertyImpl>> getRedTransitions() {
        return Collections.unmodifiableSet(redTransitions);
    }

    //TODO export as static
    @Override
    public boolean checkRedTransitions() {
        throw new UnsupportedOperationException();
        //        ModalContract<I, SP> other = deepCopy();
        //        other.removeTransitionIf(TransitionPredicate.of((tp) -> tp.getColor() == RedGreenTransitions.RED));
        //        other = ModalRemoveUnreachable.removeUnreachable(ModalContract::deepCopy, other);
        //
        //        Tuple2<Map<Set<State>, Integer>, CompactDFA<I>> result = Workset.map(new ModalDeterminization<>(other,
        //                                                                                                        other.getInputAlphabet(),
        //                                                                                                        ModalDeterminization.AcceptingPredicate
        //                                                                                                                .defaultPredicate()));
        //
        //        Map<Set<State>, Integer> map = result.first;
        //        CompactDFA<I> dfa = result.second;
        //
        //        // "find" the red transition in the dfa
        //        for (Map.Entry<Set<State>, Integer> mapEntry : map.entrySet()) {
        //
        //            Set<State> set = mapEntry.getKey();
        //
        //            for (Transition<I, TransitionProp> red : redTransitions) {
        //
        //                // found it
        //                if (set.contains(red.getPredecessor())) {
        //
        //                    Integer mappedStated = mapEntry.getValue();
        //                    Integer successor = dfa.getTransition(mappedStated, red.getLabel());
        //
        //                    // since red != null, red has a (valid) successor
        //                    assert successor != null;
        //
        //                    // since all red transitions were deleted, no transitions are expected to have the same source and label as a red one
        //                    // if one does, it should either end in the sink state or be may-only
        //
        //                    // test if the transition mappedState -> successor points to the sink state
        //                    if (!successor.equals(map.get(Collections.emptySet()))) {
        //
        //                        // it does not, therefore it is a possible witness for a conflict
        //                        // now check if the transition is may-only
        //                        LOGGER.info("red transition {} possibly collides with green transition", red);
        //
        //                        // lookup every possibly state in other from which the possible witness can originate
        //                        for (State state : set) {
        //
        //                            Collection<Transition<I, TransitionProp>> transitions =
        //                                    other.getTransitions(state, red.getLabel());
        //
        //                            // now look at every transition with the same label as the possible witness
        //                            for (Transition<I, TransitionProp> transition : transitions) {
        //
        //                                // check if this transition is must or may-only
        //                                // if must, it is in fact a witness
        //                                if (transition.getProperty().isMust()) {
        //                                    LOGGER.warn("red transition {} collides with green transition {}", red, transition);
        //                                    return false;
        //                                }
        //                            }
        //                        }
        //                    }
        //                }
        //            }
        //        }
        //        return true;
    }

    @Override
    public GrowingAlphabet<I> getCommunicationAlphabet() {
        return communicationAlphabet;
    }

    @Override
    public MTSTransition<I, ModalContractMembershipEdgePropertyImpl> addContractTransition(Integer src,
                                                                                    I input,
                                                                                    Integer tgt,
                                                                                    ModalType modalType,
                                                                                    boolean tau,
                                                                                    EdgeColor color) {
        return super.addTransition(src, input, tgt, buildContractProperty(modalType, tau, color, -1));
    }

    public static final class Creator<I> implements AutomatonCreator<MembershipMC<I>, I> {

        @Override
        public MembershipMC<I> createAutomaton(Alphabet<I> alphabet, int sizeHint) {
            return createAutomaton(alphabet);
        }

        @Override
        public MembershipMC<I> createAutomaton(Alphabet<I> alphabet) {
            return new MembershipMC<>(alphabet, new ArrayAlphabet<>());
        }
    }

    @Override
    public UniversalGraph<Integer, TransitionEdge<I, MTSTransition<I, ModalContractMembershipEdgePropertyImpl>>, Void, TransitionEdge.Property<I, ModalContractMembershipEdgePropertyImpl>> transitionGraphView(Collection<? extends I> inputs) {
        return new MMCGraphView<>(this, inputs);
    }

    class MMCGraphView<S, I, T, TP extends ModalContractMembershipEdgePropertyImpl, M extends ModalContract<S, I, T, TP>>
            extends UniversalAutomatonGraphView<S, I, T, Void, TP, M> {

        public MMCGraphView(M mc, Collection<? extends I> inputs) {
            super(mc, inputs);
        }

        @Override
        public VisualizationHelper<S, TransitionEdge<I, T>> getVisualizationHelper() {
            return new MMCVisualizationHelper<>(automaton);
        }
    }

}
