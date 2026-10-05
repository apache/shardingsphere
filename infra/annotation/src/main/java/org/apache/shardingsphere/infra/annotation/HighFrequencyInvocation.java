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

package org.apache.shardingsphere.infra.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks declarations on paths whose work repeats as throughput grows under a supported normal workload.
 * SQL execution, metadata queries, and transaction operations can each be high-frequency without running for every SQL request.
 * Repeated Pipeline processing of records, events, packets, or batches remains high-frequency inside a method invoked only once.
 * Judge external APIs by supported normal per-request use and internal helpers by actual call paths.
 * Missing repository callers or a construction or closing role alone do not prove low frequency; theoretical repeatability alone does not prove high frequency.
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.SOURCE)
public @interface HighFrequencyInvocation {
    
    /**
     * Whether the annotated target has a cacheable resource intended for reuse.
     *
     * @return whether the annotated target has a reusable cacheable resource
     */
    boolean canBeCached() default false;
}
