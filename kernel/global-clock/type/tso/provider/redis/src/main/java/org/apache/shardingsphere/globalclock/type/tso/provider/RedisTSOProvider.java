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

package org.apache.shardingsphere.globalclock.type.tso.provider;

import com.google.common.base.Strings;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Redis timestamp oracle provider.
 */
public final class RedisTSOProvider implements TSOProvider {
    
    private static final String CSN_KEY = "csn";
    
    private static final long ERROR_CSN = 0;
    
    private static final long INIT_CSN = Integer.MAX_VALUE;
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    
    private JedisPool jedisPool;
    
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        if (initialized.compareAndSet(false, true)) {
            createJedisPool();
            checkJedisPool();
            initCSN();
        }
    }
    
    private void createJedisPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(Integer.parseInt(getValue(props, RedisTSOPropertyKey.MAX_IDLE)));
        config.setMaxTotal(Integer.parseInt(getValue(props, RedisTSOPropertyKey.MAX_TOTAL)));
        jedisPool = new JedisPool(config, getValue(props, RedisTSOPropertyKey.HOST),
                Integer.parseInt(getValue(props, RedisTSOPropertyKey.PORT)), Integer.parseInt(getValue(props, RedisTSOPropertyKey.TIMEOUT_INTERVAL)),
                getValue(props, RedisTSOPropertyKey.PASSWORD));
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
    
    private String getValue(final Properties props, final RedisTSOPropertyKey propertyKey) {
        return props.containsKey(propertyKey.getKey()) ? props.getProperty(propertyKey.getKey()) : propertyKey.getDefaultValue();
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
    public String getType() {
        return "TSO.redis";
    }
}
