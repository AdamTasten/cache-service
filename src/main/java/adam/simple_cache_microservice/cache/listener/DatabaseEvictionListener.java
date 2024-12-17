package adam.simple_cache_microservice.cache.listener;

import adam.simple_cache_microservice.repository.CacheRepository;
import org.springframework.stereotype.Component;

@Component
public class DatabaseEvictionListener<K, V> implements EvictionListener<K, V> {
    private final CacheRepository cacheRepository;

    public DatabaseEvictionListener(CacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    @Override
    public void onEvict(K key, V value) {
        cacheRepository.deleteById((String) key);
    }
}
