package adam.simple_cache_microservice.cache.core;

import adam.simple_cache_microservice.cache.listener.EvictionListener;
import adam.simple_cache_microservice.cache.structure.DoublyLinkedList;
import adam.simple_cache_microservice.cache.structure.LinkedListNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class LRUCache<K, V> implements Cache<K, V> {
    private final int size;
    private final Map<K, LinkedListNode<CacheElement<K, V>>> linkedListNodeMap;
    private final DoublyLinkedList<CacheElement<K, V>> doublyLinkedList;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private EvictionListener<K, V> evictionListener = (k, v) -> {};

    public LRUCache(@Value("${app.memory}") int appMemory, @Value("${cache.fraction}") double cacheFraction) {
        this.size = (int) (appMemory * 1024 * cacheFraction);

        this.linkedListNodeMap = new ConcurrentHashMap<>(size);
        this.doublyLinkedList = new DoublyLinkedList<>();
    }

    public int getMaxSize() {
        return this.size;
    }

    public void setEvictionListener(EvictionListener<K, V> listener) {
        this.evictionListener = listener;
    }

    @Override
    public boolean put(K key, V value) {
        lock.writeLock().lock();
        try {
            CacheElement<K, V> item = new CacheElement<>(key, value);
            LinkedListNode<CacheElement<K, V>> newNode;

            if (linkedListNodeMap.containsKey(key)) {
                LinkedListNode<CacheElement<K, V>> node = linkedListNodeMap.get(key);
                newNode = doublyLinkedList.updateAndMoveToFront(node, item);
            } else {
                if (this.size() >= this.size) {
                    evictElement();
                }
                newNode = doublyLinkedList.add(item);
            }

            if (newNode.isEmpty()) {
                return false;
            }
            linkedListNodeMap.put(key, newNode);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<V> get(K key) {
        lock.readLock().lock();
        try {
            LinkedListNode<CacheElement<K, V>> node = linkedListNodeMap.get(key);
            if (node != null && !node.isEmpty()) {
                linkedListNodeMap.put(key, doublyLinkedList.moveToFront(node));
                return Optional.of(node.getElement().getValue());
            }
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return doublyLinkedList.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            linkedListNodeMap.clear();
            doublyLinkedList.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void evictElement() {
        lock.writeLock().lock();
        try {
            LinkedListNode<CacheElement<K, V>> node = doublyLinkedList.removeTail();
            if (!node.isEmpty()) {
                CacheElement<K, V> evictedElement = node.getElement();
                linkedListNodeMap.remove(evictedElement.getKey());
                evictionListener.onEvict(evictedElement.getKey(), evictedElement.getValue());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
