package xyz.upperlevel.spigot.quakecraft.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class CollectionUtil {

    public static <K, U, M extends Map<K, U>> Collector<Map.Entry<K, U>, ?, M> toMap(Supplier<M> mapSupplier) {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, throwingMerger(), mapSupplier);
    }

    public static <K, U> Collector<Map.Entry<K, U>, ?, Map<K, U>> toMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, throwingMerger(), HashMap::new);
    }

    public static <T> BinaryOperator<T> throwingMerger() {
        return (var0, var1) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", var0));
        };
    }


    private CollectionUtil() {}
}
