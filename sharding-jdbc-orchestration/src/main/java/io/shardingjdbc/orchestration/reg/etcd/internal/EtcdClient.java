package io.shardingjdbc.orchestration.reg.etcd.internal;

import com.google.common.base.Optional;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.List;

/**
 * Etcd client.
 *
 * @author junxiong
 */
public interface EtcdClient {

    /**
     * get value of a specific key.
     *
     * @param key key
     * @return value
     */
    Optional<String> get(String key);

    /**
     * list all child key/value for a directory.
     * directory should be end with "/"
     *
     * @param directory directory
     * @return value
     */
    Optional<List<String>> list(String directory);

    /**
     * put value to a specific key, if result is not absent, it is an update.
     *
     * @param key   key
     * @param value value
     * @return old value
     */
    Optional<String> put(String key, String value);

    /**
     * put value to a specific key, if result is not absent, it is an update.
     *
     * @param key   key
     * @param value value
     * @param ttl   time to live in milliseconds
     * @return old value
     */
    Optional<String> put(String key, String value, long ttl);

    /**
     * delete a key or a directory.
     * directory should be end with "/"
     *
     * @param keyOrDirectory key or directory
     * @return deleted keys
     */
    Optional<List<String>> delete(String keyOrDirectory);

    /**
     * create a lease with a specific ttl in milliseconds.
     *
     * @param ttl time to live
     * @return lease id
     */
    Optional<Long> lease(long ttl);

    /**
     * watch a keys.
     *
     * @param key String
     * @return list of watcher
     */
    Optional<Watcher> watch(String key);

    @Value
    @Wither
    @Builder
    class KeyedValue {
        
        String key;
        
        String value;
    }
}
