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

package org.apache.shardingsphere.infra.config.datasource;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Properties;

/**
 * Data source parameters.
 */
@Getter
@Setter
@EqualsAndHashCode
public final class DataSourceParameter {
    
    public static final long DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS = 30 * 1000L;
    
    public static final long DEFAULT_IDLE_TIMEOUT_MILLISECONDS = 60 * 1000L;
    
    public static final long DEFAULT_MAX_LIFETIME_MILLISECONDS = 30 * 60 * 1000L;
    
    public static final int DEFAULT_MAX_POOL_SIZE = 50;
    
    public static final int DEFAULT_MIN_POOL_SIZE = 1;
    
    public static final boolean DEFAULT_READ_ONLY = false;
    
    private String url;
    
    private String username;
    
    private String password;
    
    private long connectionTimeoutMilliseconds = DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS;
    
    private long idleTimeoutMilliseconds = DEFAULT_IDLE_TIMEOUT_MILLISECONDS;
    
    private long maxLifetimeMilliseconds = DEFAULT_MAX_LIFETIME_MILLISECONDS;
    
    private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;
    
    private int minPoolSize = DEFAULT_MIN_POOL_SIZE;
    
    private boolean readOnly = DEFAULT_READ_ONLY;
    
    private Properties customPoolProps;
}
