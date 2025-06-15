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
import org.apache.shardingsphere.infra.exception.dialect.exception.transaction.InTransactionException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.SetParams;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Redis TSO provider.
 */
public final class RedisTSOProvider implements TSOProvider {
    
    private static final String CSN_KEY = "csn";

    private static final String CSN_LOCK_KEY = "csn_lock";
    
    private static final long ERROR_CSN = 0L;

    private static final int LOCK_EXPIRE_TIME = 200;

    private static final long INIT_CSN = Integer.MAX_VALUE;
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    
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
        String lockValue = String.valueOf(System.nanoTime());

        try (Jedis jedis = jedisPool.getResource()) {
            String result = jedis.set(CSN_LOCK_KEY, lockValue, SetParams.setParams().nx().px(LOCK_EXPIRE_TIME));
            if (!"OK".equals(result)) {
                throw new InTransactionException();
            }
            return Long.parseLong(jedis.get(CSN_KEY));
        }
    }

    @Override
    public long getNextTimestamp() {
        try (Jedis jedis = jedisPool.getResource()) {
            long next = jedis.incr(CSN_KEY);

            jedis.del(CSN_LOCK_KEY);
            return next;
        }
    }
    
    @Override
    public String getType() {
        return "TSO.redis";
    }
}
