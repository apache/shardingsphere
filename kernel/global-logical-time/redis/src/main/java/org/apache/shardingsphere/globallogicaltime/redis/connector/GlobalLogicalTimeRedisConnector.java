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

package org.apache.shardingsphere.globallogicaltime.redis.connector;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.globallogicaltime.config.GlobalLogicalTimeRuleConfiguration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.SetParams;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeoutException;

/**
 * Redis connector for global logical time.
 */
@Slf4j
public class GlobalLogicalTimeRedisConnector {
    
    private static final Long RELEASE_SUCCESS = 1L;
    
    private static final int TRY_LOCK_TIMEOUT_INTERVAL = 40;
    
    private static final Random RETRY_TIME_INTERVAL_RANDOM = new Random();
    
    private int lockExpirationTime;
    
    private JedisPool jedisPool;
    
    private final GlobalLogicalTimeRuleConfiguration configuration;
    
    public GlobalLogicalTimeRedisConnector(final GlobalLogicalTimeRuleConfiguration configuration) {
        this.configuration = configuration;
        initJedisPool();
    }
    
    private boolean initJedisPool() throws JedisConnectionException {
        String host;
        int port;
        String password;
        int timeoutInterval;
        int maxIdle;
        int maxTotal;
        
        // Load configuration of jedis pool.
        if (configuration == null || configuration.getRedisOption() == null) {
            host = GlobalLogicalTimeJedisPoolConfigParams.DEFAULT_HOST;
            port = GlobalLogicalTimeJedisPoolConfigParams.DEFAULT_PORT;
            password = GlobalLogicalTimeJedisPoolConfigParams.DEFAULT_PASSWORD;
            timeoutInterval = GlobalLogicalTimeJedisPoolConfigParams.DEFAULT_TIME_INTERVAL;
            maxIdle = GlobalLogicalTimeJedisPoolConfigParams.DEFAULT_MAX_IDLE;
            maxTotal = GlobalLogicalTimeJedisPoolConfigParams.DEFAULT_MAX_TOTAL;
            this.lockExpirationTime = GlobalLogicalTimeJedisPoolConfigParams.DEFAULT_LOCK_EXPIRATION_TIME;
        } else {
            host = configuration.getRedisOption().getHost() != null ? configuration.getRedisOption().getHost() : GlobalLogicalTimeJedisPoolConfigParams.DEFAULT_HOST;
            port = configuration.getRedisOption().getPort() != null ? Integer.parseInt(configuration.getRedisOption().getPort()) : GlobalLogicalTimeJedisPoolConfigParams.DEFAULT_PORT;
            password = configuration.getRedisOption().getPassword() != null ? configuration.getRedisOption().getPassword() : GlobalLogicalTimeJedisPoolConfigParams.DEFAULT_PASSWORD;
            timeoutInterval =
                    configuration.getRedisOption().getTimeoutInterval() != 0 ? configuration.getRedisOption().getTimeoutInterval() : GlobalLogicalTimeJedisPoolConfigParams.DEFAULT_TIME_INTERVAL;
            maxIdle = configuration.getRedisOption().getTimeoutInterval() != 0 ? configuration.getRedisOption().getTimeoutInterval() : GlobalLogicalTimeJedisPoolConfigParams.DEFAULT_MAX_IDLE;
            maxTotal = configuration.getRedisOption().getMaxTotal() != 0 ? configuration.getRedisOption().getMaxTotal() : GlobalLogicalTimeJedisPoolConfigParams.DEFAULT_MAX_TOTAL;
            this.lockExpirationTime =
                    configuration.getRedisOption().getLockExpirationTime() != 0 ? configuration.getRedisOption().getMaxTotal() : GlobalLogicalTimeJedisPoolConfigParams.DEFAULT_LOCK_EXPIRATION_TIME;
        }
        
        // Build the jedis pool.
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(maxIdle);
        config.setMaxTotal(maxTotal);
        if ("".equals(password)) {
            jedisPool = new JedisPool(config, host, port, timeoutInterval);
        } else {
            jedisPool = new JedisPool(config, host, port, timeoutInterval, password);
        }
        if (checkJedisPool()) {
            log.info("create jedis pool, host: {}, port: {}, maxIdle: {}, maxTotal: {}, timeoutInterval: {}", host, port, maxIdle, maxTotal, timeoutInterval);
            return true;
        } else {
            log.error("create jedis pool failed, please check the configuration in 'server.yaml'.");
            return false;
        }
    }
    
    private boolean checkJedisPool() throws JedisConnectionException {
        String setResult;
        long deleteResult;
        try (Jedis jedis = jedisPool.getResource()) {
            setResult = jedis.set(GlobalLogicalTimeJedisPoolConfigParams.TEST_KEY, GlobalLogicalTimeJedisPoolConfigParams.TEST_VALUE);
            deleteResult = jedis.del(GlobalLogicalTimeJedisPoolConfigParams.TEST_KEY);
        } catch (JedisConnectionException e) {
            log.error("create jedis pool failed, please check the configuration of redis.");
            e.printStackTrace();
            throw e;
        }
        return "OK".equals(setResult) && deleteResult == 1;
    }
    
    /**
     * Get current global csn from Redis after add 1 to the global csn in Redis.
     *
     * @return the global csn after add 1
     * @throws JedisConnectionException jedis connection exception
     */
    public synchronized long getNextCSN() throws JedisConnectionException {
        Jedis jedis = jedisPool.getResource();
        long csn;
        try {
            csn = jedis.incr(GlobalLogicalTimeJedisPoolConfigParams.CSN_KEY);
        } finally {
            jedis.close();
        }
        return csn;
        
    }
    
    /**
     * Init global csn in redis.
     *
     * @return initialized global csn
     * @throws JedisConnectionException request to redis failed
     */
    public synchronized long initCSN() throws JedisConnectionException {
        Jedis jedis = jedisPool.getResource();
        String result;
        try {
            result = jedis.set(GlobalLogicalTimeJedisPoolConfigParams.CSN_KEY, String.valueOf(GlobalLogicalTimeJedisPoolConfigParams.INIT_CSN));
        } finally {
            jedis.close();
        }
        if ("OK".equals(result)) {
            return GlobalLogicalTimeJedisPoolConfigParams.INIT_CSN;
        } else {
            return GlobalLogicalTimeJedisPoolConfigParams.ERROR_CSN;
        }
    }
    
    /**
     * Get current global csn from redis.
     *
     * @return current global csn
     * @throws JedisConnectionException request to redis failed
     */
    public long getCurrentCSN() throws JedisConnectionException {
        Jedis jedis = jedisPool.getResource();
        long csn;
        try {
            csn = Long.parseLong(jedis.get(GlobalLogicalTimeJedisPoolConfigParams.CSN_KEY));
        } finally {
            jedis.close();
        }
        if (csn == GlobalLogicalTimeJedisPoolConfigParams.ERROR_CSN) {
            csn = initCSN();
        }
        return csn;
    }
    
    private synchronized boolean tryCSNLock(final String id) {
        return tryCSNLock(id, lockExpirationTime);
    }
    
    private synchronized boolean tryCSNLock(final String id, final int lockExpirationTime) throws JedisConnectionException {
        Jedis jedis = jedisPool.getResource();
        SetParams params = new SetParams();
        params.ex(lockExpirationTime);
        params.nx();
        String back = jedis.set(GlobalLogicalTimeJedisPoolConfigParams.CSN_LOCK_NAME, id, params);
        jedis.close();
        return "OK".equals(back);
    }
    
    private boolean isTimeout(final long startTIme, final long endTime) {
        final double msToS = 1000.0;
        double interval = (endTime - startTIme) / msToS;
        return !(interval < TRY_LOCK_TIMEOUT_INTERVAL);
    }
    
    /**
     * Try csn lock from redis in a loop before timeout.
     *
     * @param id cskLock id
     * @throws TimeoutException trying csn lock timeout
     * @throws JedisConnectionException request to redis failed
     */
    public void lockCSN(final String id) throws TimeoutException, JedisConnectionException {
        int minRetryTimeInterval = 50;
        int maxRetryTimeInterval = 100;
        boolean lockResult = false;
        try {
            long startTime = System.currentTimeMillis();
            while (true) {
                long endTime = System.currentTimeMillis();
                if (isTimeout(startTime, endTime)) {
                    break;
                }
                lockResult = tryCSNLock(id);
                if (lockResult) {
                    log.debug("get csn lock successfully, id : {}", id);
                    break;
                } else {
                    Thread.sleep(RETRY_TIME_INTERVAL_RANDOM.nextInt(maxRetryTimeInterval - minRetryTimeInterval) + minRetryTimeInterval);
                }
            }
            
        } catch (JedisConnectionException | InterruptedException e) {
            e.printStackTrace();
        }
        
        if (!lockResult) {
            throw new TimeoutException("Get lock failed because of timeout.");
        }
    }
    
    /**
     * Unlock csnLock if the held id equal to the saved id.
     *
     * @param id csn lock id
     * @return if csn lock is unlocked successfully
     * @throws JedisConnectionException request to redis failed
     */
    public synchronized boolean unLockCSN(final String id) throws JedisConnectionException {
        Jedis jedis = jedisPool.getResource();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(GlobalLogicalTimeJedisPoolConfigParams.CSN_LOCK_NAME), Collections.singletonList(id));
        jedis.close();
        if (RELEASE_SUCCESS.equals(result)) {
            log.debug("release csn lock successfully, id : {}", id);
            return true;
        } else {
            log.error("fail to unLock CSN.");
            return false;
        }
    }
    
    /**
     * Generate a csn lock id by process id , timeMillis and random number.
     *
     * @return csn lock id
     */
    public static String tryCSNLockId() {
        // get process id
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        String processId = String.valueOf(Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue());
        // get timeMillis
        String timeMillis = String.valueOf(System.currentTimeMillis());
        // get random number
        String seed = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
        return processId + timeMillis + seed;
    }
}
