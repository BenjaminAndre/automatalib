package net.automatalib.automata.visualization;

import net.automatalib.automata.ca.MutableFIFOA;
import net.automatalib.automata.concepts.TransitionAction;
import net.automatalib.automata.graphs.TransitionEdge;

import java.util.Map;

public class FIFOVisualizationHelper<S,C,I,T,A extends MutableFIFOA<S,C,I,T>> extends AutomatonVisualizationHelper<S, I, T, A> {

    public FIFOVisualizationHelper(A automaton) {
        super(automaton);
    }

    @Override
    public boolean getNodeProperties(S node, Map<String, String> properties) {
        return false;
    }

    @Override
    public boolean getEdgeProperties(S src, TransitionEdge<I, T> edge, S tgt, Map<String, String> properties) {
        if (!super.getEdgeProperties(src, edge, tgt, properties)) {
            return false;
        }
        TransitionAction ta = edge.property(automaton).getProperty();
        if (ta.getAction() == TransitionAction.Action.PASS) {
            String label = ta.getAction().getSymbol();
        } else {
            String label = String.valueOf(ta.getChannelName()) +
                    ta.getAction().getSymbol() +
                    String.valueOf(ta.getManipulatedSymbol());
            properties.put(EdgeAttrs.LABEL, label);
        }
        return true;
    }
}
