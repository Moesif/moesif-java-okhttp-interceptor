package com.moesif.helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionUtils {

    /**
     * Converts Map<K, List<V>> to Map<k,V> Seems only last V is kept
     *
     * @param multiMap Map<K, List<V>>
     * @param <K>
     * @param <V>
     * @return Map<k, V>
     */
    public static <K, V> Map<K, V> flattenMultiMap(Map<K, List<V>> multiMap) {
        Map<K, V> result = new HashMap<>();
        for (Map.Entry<K, List<V>> entry : multiMap.entrySet()) {
            for (V value : entry.getValue()) {
                result.put(entry.getKey(), value);
            }
        }
        return result;
    }
}
