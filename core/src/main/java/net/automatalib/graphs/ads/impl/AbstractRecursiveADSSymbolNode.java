/* Copyright (C) 2013-2017 TU Dortmund
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
package net.automatalib.graphs.ads.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.automatalib.graphs.ads.RecursiveADSNode;

/**
 * An abstract implementation of a symbol node, that may be used by other ADS-extending classes.
 *
 * @param <S>
 *         (hypothesis) state type
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 * @param <N>
 *         the concrete node type
 *
 * @author frohme
 */
public abstract class AbstractRecursiveADSSymbolNode<S, I, O, N extends RecursiveADSNode<S, I, O, N>>
        implements RecursiveADSNode<S, I, O, N>, Serializable {

    private N parent;

    private I symbol;

    private final Map<O, N> successors;

    public AbstractRecursiveADSSymbolNode(N parent, I symbol) {
        this.successors = new HashMap<>();
        this.parent = parent;
        this.symbol = symbol;
    }

    @Override
    public I getSymbol() {
        return this.symbol;
    }

    @Override
    public void setSymbol(I symbol) {
        this.symbol = symbol;
    }

    @Override
    public N getParent() {
        return this.parent;
    }

    @Override
    public void setParent(N parent) {
        this.parent = parent;
    }

    @Override
    public Map<O, N> getChildren() {
        return this.successors;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public S getHypothesisState() {
        return null;
    }

    @Override
    public void setHypothesisState(S state) {
        throw new UnsupportedOperationException("Cannot set hypothesis state on a symbol node");
    }

    @Override
    public String toString() {
        return this.symbol.toString();
    }
}