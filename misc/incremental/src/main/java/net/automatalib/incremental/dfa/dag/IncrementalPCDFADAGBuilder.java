/* Copyright (C) 2013 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib.incremental.dfa.dag;

import java.util.ArrayDeque;
import java.util.Deque;

import net.automatalib.incremental.ConflictException;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class IncrementalPCDFADAGBuilder<I> extends AbstractIncrementalDFADAGBuilder<I> {
	
	public IncrementalPCDFADAGBuilder(Alphabet<I> inputAlphabet) {
		super(inputAlphabet);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.dfa.AbstractIncrementalDFABuilder#getState(net.automatalib.words.Word)
	 */
	@Override
	protected State getState(Word<? extends I> word) {
		State s = init;
		
		for(I sym : word) {
			int idx = inputAlphabet.getSymbolIndex(sym);
			s = s.getSuccessor(idx);
			if(s == null || s == sink) {
				return s;
			}
		}
		return s;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.dfa.AbstractIncrementalDFABuilder#lookup(net.automatalib.words.Word)
	 */
	@Override
	public Acceptance lookup(Word<? extends I> word) {
		State s = getState(word);
		if(s == null)
			return Acceptance.DONT_KNOW;
		return (s != sink) ? s.getAcceptance() : Acceptance.FALSE;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.automatalib.incremental.dfa.AbstractIncrementalDFABuilder#insert(net.automatalib.words.Word, boolean)
	 */
	@Override
	public void insert(Word<? extends I> word, boolean accepting) {
		int len = word.length();
		Acceptance acc = Acceptance.fromBoolean(accepting);
		
		State curr = init;
		State conf = null;
		
		Deque<PathElem> path = new ArrayDeque<>();
		
		for(I sym : word) {
			if(curr == sink || curr.getAcceptance() == Acceptance.FALSE) {
				if(accepting) {
					throw new IllegalArgumentException("Conflict");
				}
				if(curr != sink) {
					purge(curr);
				}
				return;
			}
			
			if(conf == null && curr.isConfluence()) {
				conf = curr;
			}
			
			int idx = inputAlphabet.getSymbolIndex(sym);
			
			State succ = curr.getSuccessor(idx);
			if(succ == null) {
				break;
			}
			path.push(new PathElem(curr, idx));
			curr = succ;
		}
		
		int prefixLen = path.size();
		

		State last = curr;
		
		if(prefixLen == len) {
			Acceptance currAcc = curr.getAcceptance();
			if(currAcc == acc) {
				return;
			}
			if(currAcc != Acceptance.DONT_KNOW) {
				throw new ConflictException("Incompatible acceptances: " + currAcc + " vs " + acc);
			}
			if(!accepting) {
				purge(last);
				last = sink;
			}
			else {
				if(conf != null || last.isConfluence()) {
					last = clone(last, Acceptance.TRUE);
				}
				else if(last == init) {
					updateInitSignature(Acceptance.TRUE);
					return;
				}
				else {
					last = updateSignature(last, acc);
				}
			}
		}
		else {
			if(conf != null) {
				if(conf == last) {
					conf = null;
				}
				last = hiddenClone(last);
			}
			else if(last != init) {
				hide(last);
			}
			
			Word<? extends I> suffix = word.subWord(prefixLen);
			I sym = suffix.firstSymbol();
			int suffTransIdx = inputAlphabet.getSymbolIndex(sym);
			State suffixState = createSuffix(suffix.subWord(1), accepting);
			
			if(last != init) {
				if(accepting) {
					last = unhide(last, Acceptance.TRUE, suffTransIdx, suffixState);
				}
				else {
					last = unhide(last, suffTransIdx, suffixState);
				}
			}
			else {
				if(accepting) {
					updateInitSignature(Acceptance.TRUE, suffTransIdx, suffixState);
				}
				else {
					updateInitSignature(suffTransIdx, suffixState);
				}
			}
		}
		
		if(path.isEmpty()) {
			return;
		}
			
		if(conf != null) {
			PathElem next;
			do {
				next = path.pop();
				State state = next.state;
				int idx = next.transIdx;
				if(accepting) {
					state = clone(state, Acceptance.TRUE, idx, last);
				}
				else {
					state = clone(state, idx, last);
				}
				last = state;
			} while(next.state != conf);
		}
		
		
		
		while(path.size() > 1) {
			PathElem next = path.pop();
			State state = next.state;
			int idx = next.transIdx;
			State updated;
			Acceptance oldAcc = state.getAcceptance();
			if(accepting) {
				updated = updateSignature(state, Acceptance.TRUE, idx, last);
			}
			else {
				updated = updateSignature(state, idx, last);
			}
			if(state == updated && oldAcc == updated.getAcceptance())
				return;
			last = updated;
		}
		
		int finalIdx = path.pop().transIdx;
		
		if(accepting) {
			updateInitSignature(Acceptance.TRUE, finalIdx, last);
		}
		else {
			updateInitSignature(finalIdx, last);
		}
	}
		
	/**
	 * Removes a state and all of its successors from the register.
	 * @param state the state to purge
	 */
	private void purge(State state) {
		StateSignature sig = state.getSignature();
		if(state.getAcceptance() == Acceptance.TRUE) {
			throw new IllegalStateException("Attempting to purge accepting state");
		}
		if(register.remove(sig) == null) {
			return;
		}
		sig.acceptance = Acceptance.FALSE;
		for(int i = 0; i < alphabetSize; i++) {
			State succ = sig.successors[i];
			if(succ != null) {
				purge(succ);
			}
		}
	}
	
	/**
	 * Creates a suffix state sequence, i.e., a linear sequence of states connected by transitions
	 * labeled by the letters of the given suffix word.
	 * @param suffix the suffix word
	 * @param acc whether or not the final state should be accepting
	 * @return the first state in the sequence
	 */
	private State createSuffix(Word<? extends I> suffix, boolean accepting) {
		State last;
		Acceptance intermediate;
		if(!accepting) {
			if(sink == null)
				sink = new State(null);
			last = sink;
			intermediate = Acceptance.DONT_KNOW;
		}
		else {
			StateSignature sig = new StateSignature(alphabetSize, Acceptance.TRUE);
			last = replaceOrRegister(sig);
			intermediate = Acceptance.TRUE;
		}
		
		int len = suffix.length();
		for(int i = len - 1; i >= 0; i--) {
			StateSignature sig = new StateSignature(alphabetSize, intermediate);
			I sym = suffix.getSymbol(i);
			int idx = inputAlphabet.getSymbolIndex(sym);
			sig.successors[idx] = last;
			last = replaceOrRegister(sig);
		}
		
		return last;
	}

}