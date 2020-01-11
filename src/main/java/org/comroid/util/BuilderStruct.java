package org.comroid.util;

import java.util.function.Supplier;

public interface BuilderStruct<T> {
    default Supplier<T> supplier() {
        return this::build;
    }

    T build();
}
