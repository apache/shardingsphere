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

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Properties;

/**
 * Redis timestamp oracle provider.
 */
@Slf4j
public final class RedisTSOProvider implements TSOProvider {
    
    private static final String CSN_KEY = "csn";
    
    private static final long ERROR_CSN = 0;
    
    private static final long INIT_CSN = Integer.MAX_VALUE;
    
    private Properties redisTSOProperties;
    
    private JedisPool jedisPool;
    
    @Override
    public void init(final Properties props) {
        if (jedisPool != null) {
            return;
        }
        if (props == null) {
            redisTSOProperties = new Properties();
            RedisTSOProperties.HOST.set(redisTSOProperties, RedisTSOProperties.HOST.getDefaultValue());
            RedisTSOProperties.PORT.set(redisTSOProperties, RedisTSOProperties.PORT.getDefaultValue());
            RedisTSOProperties.PASSWORD.set(redisTSOProperties, RedisTSOProperties.PASSWORD.getDefaultValue());
            RedisTSOProperties.TIMEOUT_INTERVAL.set(redisTSOProperties, RedisTSOProperties.TIMEOUT_INTERVAL.getDefaultValue());
            RedisTSOProperties.MAX_IDLE.set(redisTSOProperties, RedisTSOProperties.MAX_IDLE.getDefaultValue());
            RedisTSOProperties.MAX_TOTAL.set(redisTSOProperties, RedisTSOProperties.MAX_TOTAL.getDefaultValue());
        } else {
            redisTSOProperties = new Properties(props);
            RedisTSOProperties.HOST.set(redisTSOProperties, RedisTSOProperties.HOST.get(props));
            RedisTSOProperties.PORT.set(redisTSOProperties, RedisTSOProperties.PORT.get(props));
            RedisTSOProperties.PASSWORD.set(redisTSOProperties, RedisTSOProperties.PASSWORD.get(props));
            RedisTSOProperties.TIMEOUT_INTERVAL.set(redisTSOProperties, RedisTSOProperties.TIMEOUT_INTERVAL.get(props));
            RedisTSOProperties.MAX_IDLE.set(redisTSOProperties, RedisTSOProperties.MAX_IDLE.get(props));
            RedisTSOProperties.MAX_TOTAL.set(redisTSOProperties, RedisTSOProperties.MAX_TOTAL.get(props));
        }
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(Integer.parseInt(RedisTSOProperties.MAX_IDLE.get(redisTSOProperties)));
        config.setMaxTotal(Integer.parseInt(RedisTSOProperties.MAX_TOTAL.get(redisTSOProperties)));
        if ("".equals(RedisTSOProperties.PASSWORD.get(redisTSOProperties))) {
            jedisPool = new JedisPool(config, RedisTSOProperties.HOST.get(redisTSOProperties),
                    Integer.parseInt(RedisTSOProperties.PORT.get(redisTSOProperties)),
                    Integer.parseInt(RedisTSOProperties.TIMEOUT_INTERVAL.get(redisTSOProperties)));
        } else {
            jedisPool = new JedisPool(config, RedisTSOProperties.HOST.get(redisTSOProperties),
                    Integer.parseInt(RedisTSOProperties.PORT.get(redisTSOProperties)),
                    Integer.parseInt(RedisTSOProperties.TIMEOUT_INTERVAL.get(redisTSOProperties)),
                    RedisTSOProperties.PASSWORD.get(redisTSOProperties));
        }
        checkJedisPool();
        initCSN();
    }
    
    @Override
    public long getCurrentTimestamp() throws JedisConnectionException {
        long result;
        try (Jedis jedis = jedisPool.getResource()) {
            result = Long.parseLong(jedis.get(CSN_KEY));
        }
        return result;
    }
    
    @Override
    public long getNextTimestamp() throws JedisConnectionException {
        long result;
        try (Jedis jedis = jedisPool.getResource()) {
            result = jedis.incr(CSN_KEY);
        }
        return result;
    }
    
    /**
     * Set csn to INIT_CSN.
     *
     * @return csn
     */
    public synchronized long initCSN() {
        String result = "";
        String oldCsn;
        Jedis jedis = jedisPool.getResource();
        try {
            oldCsn = jedis.get(CSN_KEY);
            if (oldCsn == null || oldCsn.equals(String.valueOf(ERROR_CSN))) {
                result = jedis.set(CSN_KEY, String.valueOf(INIT_CSN));
            }
        } finally {
            jedis.close();
        }
        if ("OK".equals(result)) {
            return INIT_CSN;
        } else {
            return ERROR_CSN;
        }
    }
    
    private void checkJedisPool() throws JedisConnectionException {
        Jedis resource = jedisPool.getResource();
        resource.ping();
    }
    
    /**
     * Get properties of redisTSOProvider.
     *
     * @return properties
     */
    public Properties getRedisTSOProperties() {
        return redisTSOProperties;
    }
    
    @Override
    public String getType() {
        return "TSO.redis";
    }
}
