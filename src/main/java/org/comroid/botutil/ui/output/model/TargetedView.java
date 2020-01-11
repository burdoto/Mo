package org.comroid.botutil.ui.output.model;

public interface TargetedView<T, O> {
    O view(T target);
}
