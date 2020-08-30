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

package org.apache.shardingsphere.infra.context.schema;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Data source parameters.
 */
@Getter
@Setter
@EqualsAndHashCode
public final class DataSourceParameter {
    
    private String url;
    
    private String username;
    
    private String password;
    
    private long connectionTimeoutMilliseconds = 30 * 1000L;
    
    private long idleTimeoutMilliseconds = 60 * 1000L;
    
    private long maxLifetimeMilliseconds;
    
    private int maxPoolSize = 50;
    
    private int minPoolSize = 1;
    
    private long maintenanceIntervalMilliseconds = 30 * 1000L;
    
    private boolean readOnly;
}
