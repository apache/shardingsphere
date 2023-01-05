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

/**
 * JedisPool config params for global logical time.
 */
public class GlobalLogicalTimeJedisPoolConfigParams {
    
    // the key of csn saved in redis server
    public static final String CSN_KEY = "csn";
    
    // the key of csn lock
    public static final String CSN_LOCK_NAME = "csnLock";
    
    public static final String TEST_KEY = "test_key";
    
    public static final String TEST_VALUE = "test_value";
    
    // init csn when redis server doesn't store csnKey
    public static final long INIT_CSN = 40000;
    
    // error csn
    public static final long ERROR_CSN = 0;
    
    // default host of redis server
    public static final String DEFAULT_HOST = "127.0.0.1";
    
    // default port of redis server
    public static final int DEFAULT_PORT = 6379;
    
    // default password of redis server
    public static final String DEFAULT_PASSWORD = "";
    
    // default timeout interval when get connection
    public static final int DEFAULT_TIME_INTERVAL = 40000;
    
    // default max idle redis connection number
    public static final int DEFAULT_MAX_IDLE = 8;
    
    // default max redis connection number
    public static final int DEFAULT_MAX_TOTAL = 18;
    
    // the default expiration time of csn lock, redis will keep the csn lock in lockExpirationTime
    public static final int DEFAULT_LOCK_EXPIRATION_TIME = 20;
    
    // the min expiration time of csn lock
    public static final int MIN_LOCK_EXPIRATION_TIME = 1;
    
    // the max expiration time of csn lock
    public static final int MAX_LOCK_EXPIRATION_TIME = 1000;
}
