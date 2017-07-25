package xyz.upperlevel.spigot.quakecraft.shop.require;

import xyz.upperlevel.spigot.quakecraft.shop.Purchase;
import xyz.upperlevel.spigot.quakecraft.shop.require.impl.RequirePermission;
import xyz.upperlevel.spigot.quakecraft.shop.require.impl.RequirePurchase;
import xyz.upperlevel.uppercore.config.InvalidConfigurationException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RequireSystem {
    private static Map<String, RequireFactory> factories = new HashMap<>();

    static {
        registerDefaultFactory();
    }

    public static Require load(Purchase parent, Map.Entry<String, Object> require) {
        RequireFactory factory = factories.get(require.getKey());
        if (factory == null)
            throw new InvalidConfigurationException("Cannot find require: '" + require.getKey() + "'!");
        return factory.create(parent, require.getValue());
    }


    @SuppressWarnings("unchecked")
    public static List<Require> loadAll(Purchase parent, Object cnf) {
        if (cnf == null) return Collections.emptyList();
        if (!(cnf instanceof List))
            throw new InvalidConfigurationException("Cannot cast " + cnf.getClass().getSimpleName() + " to Require List!");
        return ((List<Object>) cnf).stream()
                .map(obj -> {
                    if (!(obj instanceof Map))
                        throw new InvalidConfigurationException("Cannot use " + obj.getClass().getSimpleName() + " as requirement!");
                    return load(parent, ((Map<String, Object>) obj).entrySet().iterator().next());
                })
                .collect(Collectors.toList());
    }

    public static void registerFactory(String id, RequireFactory factory) {
        if(factories.putIfAbsent(id, factory) != null)
            throw new IllegalArgumentException("Factory '" + id + "' already registered!");
    }

    private static void registerDefaultFactory() {
        registerFactory("purchase", RequirePurchase::new);
        registerFactory("permission", RequirePermission::new);
    }

    interface RequireFactory {
        Require create(Purchase parent, Object parameter);
    }
}
