/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.globalclock.type.tso.provider.redis;

import com.google.common.base.Strings;
import org.apache.shardingsphere.globalclock.type.tso.provider.TSOProvider;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Redis TSO provider.
 */
public final class RedisTSOProvider implements TSOProvider {
    
    private static final String CSN_KEY = "csn";
    
    private static final long ERROR_CSN = 0L;
    
    private static final long INIT_CSN = Integer.MAX_VALUE;
    
    private static final String LOCK_KEY = "csn_lock";
    
    private static final String UNLOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    
    private static final long LOCK_EXPIRE_MILLIS = 30000L;
    
    private static final long LOCK_RETRY_INTERVAL_MILLIS = 10L;
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    
    private final String lockToken = UUID.randomUUID().toString();
    
    private final ReentrantLock localLock = new ReentrantLock();
    
    private JedisPool jedisPool;
    
    @Override
    public void init(final Properties props) {
        if (initialized.compareAndSet(false, true)) {
            createJedisPool(props);
            checkJedisPool();
            initCSN();
        }
    }
    
    private void createJedisPool(final Properties props) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(Integer.parseInt(getValue(props, RedisTSOPropertyKey.MAX_IDLE)));
        config.setMaxTotal(Integer.parseInt(getValue(props, RedisTSOPropertyKey.MAX_TOTAL)));
        jedisPool = new JedisPool(config, getValue(props, RedisTSOPropertyKey.HOST), Integer.parseInt(getValue(props, RedisTSOPropertyKey.PORT)),
                Integer.parseInt(getValue(props, RedisTSOPropertyKey.TIMEOUT_INTERVAL)), getValue(props, RedisTSOPropertyKey.PASSWORD));
    }
    
    private String getValue(final Properties props, final RedisTSOPropertyKey propertyKey) {
        return props.containsKey(propertyKey.getKey()) ? props.getProperty(propertyKey.getKey()) : propertyKey.getDefaultValue();
    }
    
    private void checkJedisPool() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.ping();
        }
    }
    
    private void initCSN() {
        try (Jedis jedis = jedisPool.getResource()) {
            String originalCSN = jedis.get(CSN_KEY);
            if (Strings.isNullOrEmpty(originalCSN) || String.valueOf(ERROR_CSN).equals(originalCSN)) {
                jedis.set(CSN_KEY, String.valueOf(INIT_CSN));
            }
        }
    }
    
    @Override
    public long getCurrentTimestamp() {
        try (Jedis jedis = jedisPool.getResource()) {
            return Long.parseLong(jedis.get(CSN_KEY));
        }
    }
    
    @Override
    public long getNextTimestamp() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incr(CSN_KEY);
        }
    }
    
    @Override
    public boolean tryLock(final long timeoutMillis) {
        long deadlineMillis = System.currentTimeMillis() + timeoutMillis;
        boolean result = false;
        try {
            result = localLock.tryLock(timeoutMillis, TimeUnit.MILLISECONDS) && tryLockRedis(deadlineMillis);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            if (!result) {
                unlockLocalLock();
            }
        }
        return result;
    }
    
    private boolean tryLockRedis(final long deadlineMillis) throws InterruptedException {
        try (Jedis jedis = jedisPool.getResource()) {
            while (null == jedis.set(LOCK_KEY, lockToken, SetParams.setParams().nx().px(LOCK_EXPIRE_MILLIS))) {
                if (System.currentTimeMillis() >= deadlineMillis) {
                    return false;
                }
                Thread.sleep(LOCK_RETRY_INTERVAL_MILLIS);
            }
            return true;
        }
    }
    
    private void unlockLocalLock() {
        if (localLock.isHeldByCurrentThread()) {
            localLock.unlock();
        }
    }
    
    @Override
    public void unlock() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.eval(UNLOCK_SCRIPT, Collections.singletonList(LOCK_KEY), Collections.singletonList(lockToken));
        } finally {
            localLock.unlock();
        }
    }
    
    @Override
    public String getType() {
        return "TSO.redis";
    }
}
