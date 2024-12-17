package adam.simple_cache_microservice.cache.listener;

public interface EvictionListener<K, V> {
    void onEvict(K key, V value);
}
