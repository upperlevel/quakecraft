package xyz.upperlevel.quakecraft.shop.require;

import xyz.upperlevel.quakecraft.shop.purchase.Purchase;
import xyz.upperlevel.quakecraft.shop.require.impl.RequirePermission;
import xyz.upperlevel.quakecraft.shop.require.impl.RequirePurchase;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigException;
import xyz.upperlevel.uppercore.util.Pair;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static xyz.upperlevel.uppercore.util.TypeUtil.typeOf;

public class RequireSystem {
    public static Type requireConfType = typeOf(List.class, typeOf(Pair.class, String.class, String.class));// List<Map<String, String>>
    private static Map<String, RequireFactory> factories = new HashMap<>();

    static {
        registerDefaultFactory();
    }

    public static Require load(Purchase<?> parent, Pair<String, String> require) {
        RequireFactory factory = factories.get(require.getFirst());
        if (factory == null)
            throw new InvalidConfigException("Cannot find require: '" + require.getFirst() + "'!");
        return factory.create(parent, require.getSecond());
    }


    @SuppressWarnings("unchecked")
    public static List<Require> loadAll(Purchase<?> parent, List<Pair<String, String>> cnf) {
        if (cnf == null) return Collections.emptyList();

        return cnf.stream()
                .map(obj -> load(parent, obj))
                .collect(Collectors.toList());
    }

    public static void registerFactory(String id, RequireFactory factory) {
        if(factories.putIfAbsent(id, factory) != null) {
            throw new IllegalArgumentException("Factory '" + id + "' already registered!");
        }
    }

    private static void registerDefaultFactory() {
        registerFactory("purchase", RequirePurchase::new);
        registerFactory("permission", RequirePermission::new);
    }

    interface RequireFactory {
        Require create(Purchase<?> parent, String parameter);
    }
}
