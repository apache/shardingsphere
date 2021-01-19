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

package org.apache.shardingsphere.scaling.core.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Scaling constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScalingConstant {
    
    /**
     * Insert flag.
     */
    public static final String INSERT = "INSERT";
    
    /**
     * Update flag.
     */
    public static final String UPDATE = "UPDATE";
    
    /**
     * Delete flag.
     */
    public static final String DELETE = "DELETE";
    
    /**
     * Scaling root path.
     */
    public static final String SCALING_ROOT_PATH = "/scaling";
    
    /**
     * Scaling listener path.
     */
    public static final String SCALING_LISTENER_PATH = SCALING_ROOT_PATH + "/listener";
    
    /**
     * Scaling elastic job path.
     */
    public static final String SCALING_ELASTIC_JOB_PATH = SCALING_ROOT_PATH + "/elastic_job";
    
    /**
     * Config.
     */
    public static final String CONFIG = "config";
    
    /**
     * Status.
     */
    public static final String STATUS = "status";
    
    /**
     * Position.
     */
    public static final String POSITION = "position";
    
    /**
     * Workflow.
     */
    public static final String WORKFLOW = "workflow";
    
    /**
     * Incremental.
     */
    public static final String INCREMENTAL = "incremental";
    
    /**
     * Inventory.
     */
    public static final String INVENTORY = "inventory";
    
    /**
     * Delay.
     */
    public static final String DELAY = "delay";
}
