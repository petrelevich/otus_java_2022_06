package ru.otus.cachehw;


import java.util.NoSuchElementException;
import java.util.WeakHashMap;

public class MyCache<K, V> implements HwCache<K, V> {

    private final WeakHashMap<K, V> cache = new WeakHashMap<>();
    private HwListener<K, V> listener;
//Надо реализовать эти методы

    @Override
    public void put(K key, V value) {
        callListener(key, value, "put");
        cache.put(key, value);
    }

    @Override
    public void remove(K key) {
        listener.notify(key, null, "remove");
        cache.remove(key);
    }

    @Override
    public V get(K key) {
        if (cache.containsKey(key)) {
            V value = cache.get(key);
            callListener(key, value, "get");
            return value;
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void addListener(HwListener<K, V> listener) {
        this.listener = listener;
    }

    @Override
    public void removeListener(HwListener<K, V> listener) {
        if (this.listener == listener) {
            this.listener = null;
        }
    }

    @Override
    public int size() {
        return cache.size();
    }

    private void callListener(K key, V value, String action) {
        if (listener != null) {
            listener.notify(key, value, action);
        }
    }
}
