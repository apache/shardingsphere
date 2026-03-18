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

package org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param;

import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.PipelineE2EExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Pipeline E2E settings.
 */
@ExtendWith(PipelineE2EExtension.class)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface PipelineE2ESettings {
    
    /**
     * Whether fetch single.
     *
     * @return fetch single
     */
    boolean fetchSingle() default false;
    
    /**
     * Get database settings.
     *
     * @return database settings
     */
    PipelineE2EDatabaseSettings[] database();
    
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @interface PipelineE2EDatabaseSettings {
        
        /**
         * Get database type.
         * 
         * @return database type
         */
        String type();
        
        /**
         * Get table structures.
         *
         * @return table structures
         */
        String[] tableStructures() default {"default"};
        
        /**
         * Get storage container count.
         *
         * @return storage container count
         */
        int storageContainerCount() default 1;
    }
}
