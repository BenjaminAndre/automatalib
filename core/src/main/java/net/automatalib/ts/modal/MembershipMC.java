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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import net.automatalib.automata.AutomatonCreator;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.graphs.UniversalAutomatonGraphView;
import net.automatalib.automata.visualization.MMCVisualizationHelper;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.ts.modal.transitions.ModalContractEdgeProperty.EdgeColor;
import net.automatalib.ts.modal.transitions.ModalContractMembershipEdgePropertyImpl;
import net.automatalib.ts.modal.transitions.ModalEdgeProperty.ModalType;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.ArrayAlphabet;
import net.automatalib.words.impl.GrowingMapAlphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MembershipMC<I> extends AbstractCompactMTS<I, ModalContractMembershipEdgePropertyImpl>
        implements MutableModalContract<Integer, I, MTSTransition<I, ModalContractMembershipEdgePropertyImpl>, ModalContractMembershipEdgePropertyImpl> {

    protected LinkedHashSet<I> communicationAlphabet;

    public MembershipMC(Alphabet<I> alphabet, Alphabet<I> gamma) {
        super(alphabet);
        this.communicationAlphabet = Sets.newLinkedHashSet(gamma);

        assert new HashSet<>(alphabet).containsAll(gamma) : "Communication alphabet needs to be a subset of alphabet";
    }

    public MembershipMC(Alphabet<I> alphabet) {
        super(alphabet);
        this.communicationAlphabet = Sets.newLinkedHashSetWithExpectedSize(alphabet.size() / 2);
    }

    public boolean addCommunicationSymbol(I symbol) {
        boolean wasNotPresent = communicationAlphabet.add(symbol);

        assert getInputAlphabet().contains(symbol) : "Communication alphabet needs to be a subset of alphabet";

        return wasNotPresent;
    }

    public void removeCommunicationSymbol(I symbol) {
        communicationAlphabet.remove(symbol);
    }

    public void setCommunicationAlphabet(Collection<I> alphabet) {
        communicationAlphabet = Sets.newLinkedHashSet(alphabet);

        assert new HashSet<>(alphabet).containsAll(communicationAlphabet) : "Communication alphabet needs to be a subset of alphabet";
    }

    public void clearCommunicationAlphabet() {
        communicationAlphabet.clear();
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
    public Alphabet<I> getCommunicationAlphabet() {
        return Alphabets.fromCollection(communicationAlphabet);
    }

    @Override
    public MTSTransition<I, ModalContractMembershipEdgePropertyImpl> addContractTransition(Integer src,
                                                                                    I input,
                                                                                    Integer tgt,
                                                                                    ModalType modalType,
                                                                                    boolean tau,
                                                                                    EdgeColor color) {
        return addContractTransition(src, input, tgt, modalType, tau, color, -1);
    }

    public MTSTransition<I, ModalContractMembershipEdgePropertyImpl> addContractTransition(Integer src,
                                                                                           I input,
                                                                                           Integer tgt,
                                                                                           ModalType modalType,
                                                                                           boolean tau,
                                                                                           EdgeColor color,
                                                                                           int memberId) {
        return super.addTransition(src, input, tgt, buildContractProperty(modalType, tau, color, memberId));
    }

    public static final class Creator<I> implements AutomatonCreator<MembershipMC<I>, I> {

        private final Alphabet<I> defaultInputAlphabet;
        private final Alphabet<I> defaultCommunicationAlphabet;

        public Creator() {
            this(null, null);
        }

        public Creator(Alphabet<I> defaultCommunicationAlphabet) {
            this(null, defaultCommunicationAlphabet);
        }

        public Creator(Alphabet<I> defaultInputAlphabet, Alphabet<I> defaultCommunicationAlphabet) {
            this.defaultInputAlphabet = defaultInputAlphabet;
            this.defaultCommunicationAlphabet = defaultCommunicationAlphabet;
        }

        @Override
        public MembershipMC<I> createAutomaton(Alphabet<I> alphabet, int sizeHint) {
            return createAutomaton(alphabet);
        }

        @Override
        public MembershipMC<I> createAutomaton(Alphabet<I> alphabet) {
            return new MembershipMC<>(
                    defaultInputAlphabet != null ? defaultInputAlphabet : alphabet,
                    defaultCommunicationAlphabet != null ? defaultCommunicationAlphabet : new ArrayAlphabet<>());
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
