package adam.simple_cache_microservice.service;

import adam.simple_cache_microservice.cache.listener.EvictionListener;
import adam.simple_cache_microservice.cache.core.LRUCache;
import adam.simple_cache_microservice.model.CacheEntity;
import adam.simple_cache_microservice.repository.CacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

@Service
public class CacheService {

    private final CacheRepository cacheRepository;
    private final LRUCache<String, String> lruCache;
    private final EvictionListener<String, String> evictionListener;

    @Autowired
    public CacheService(CacheRepository cacheRepository,
                        LRUCache<String, String> lruCache,
                        EvictionListener<String, String> evictionListener) {
        this.cacheRepository = cacheRepository;
        this.lruCache = lruCache;
        this.evictionListener = evictionListener;
    }

    @PostConstruct
    public void initialize() {
        lruCache.setEvictionListener(evictionListener);
        initializeCache();
    }

    public void save(String key, String value) {
        lruCache.put(key, value);
        cacheRepository.save(new CacheEntity(key, value));
    }

    public Optional<String> findByKey(String key) {
        return lruCache.get(key);
    }

    private void initializeCache() {
        Queue<CacheEntity> cacheEntities = new LinkedList<>(cacheRepository.findAll());

        while (!cacheEntities.isEmpty()) {
            if (lruCache.size() >= lruCache.getMaxSize()) {
                break;
            }

            CacheEntity entity = cacheEntities.poll();

            if (entity != null) {
                lruCache.put(entity.getKey(), entity.getValue());
            }
        }

        if (!cacheEntities.isEmpty()) {
            cacheRepository.deleteAllById(cacheEntities.stream().map(CacheEntity::getKey).toList());
        }
    }
}
