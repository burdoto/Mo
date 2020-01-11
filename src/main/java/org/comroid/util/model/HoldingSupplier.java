package org.comroid.util.model;

import java.util.function.Supplier;

public final class HoldingSupplier<T> implements Supplier<T> {
    private final T value;

    public HoldingSupplier(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }
}
