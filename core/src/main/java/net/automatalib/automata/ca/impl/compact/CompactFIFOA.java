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
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class CompactFIFOA<C,I>
        extends UniversalChannelCompact<C, I>
        implements MutableFIFOA<Integer, C, I, CompactTransition<TransitionAction<C,I>>> {


    public CompactFIFOA(Alphabet<C> channelNames, Alphabet<I> alphabet, int stateCapacity) {
        super(channelNames, alphabet, stateCapacity, DEFAULT_RESIZE_FACTOR);
    }

    public CompactFIFOA(Alphabet<C> channelNames, Alphabet<I> alphabet) {
        this(channelNames, alphabet, DEFAULT_INIT_CAPACITY);
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

}
