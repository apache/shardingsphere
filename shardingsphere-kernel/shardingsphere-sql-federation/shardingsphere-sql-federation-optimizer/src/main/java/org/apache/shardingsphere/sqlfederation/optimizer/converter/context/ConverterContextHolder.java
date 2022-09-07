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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Hold convert context for current thread.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConverterContextHolder {
    
    private static final ThreadLocal<ConverterContext> CONVERTER_CONTEXT = new ThreadLocal<>();
    
    /**
     * Set convert context.
     *
     * @param converterContext convert context
     */
    public static void set(final ConverterContext converterContext) {
        CONVERTER_CONTEXT.set(converterContext);
    }
    
    /**
     * Get convert context.
     *
     * @return convert context
     */
    public static ConverterContext get() {
        return CONVERTER_CONTEXT.get();
    }
    
    /**
     * Remove convert context.
     */
    public static void remove() {
        CONVERTER_CONTEXT.remove();
    }
}
