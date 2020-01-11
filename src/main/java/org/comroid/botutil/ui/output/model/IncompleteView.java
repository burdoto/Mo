package org.comroid.botutil.ui.output.model;

public interface IncompleteView<T, O> extends TargetedView<T, O> {
    @Override O view(T target);
}
