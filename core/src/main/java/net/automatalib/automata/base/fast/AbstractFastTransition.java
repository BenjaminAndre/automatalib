package net.automatalib.automata.base.fast;

import net.automatalib.commons.util.nid.AbstractMutableNumericID;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractFastTransition<S, TP> extends AbstractMutableNumericID {

    public TP property;
    public S successor;

    public final @Nullable TP getProperty() { return property; }

    public final void setProperty(TP property) { this.property = property;}

    public final @Nullable S getSuccessor() { return successor; }

    public final void setSuccessor(S successor) { this.successor = successor;}

    @Override
    public String toString() {
        return "t" + getId();
    }

}