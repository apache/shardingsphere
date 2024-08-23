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

package org.apache.shardingsphere.data.pipeline.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Iterator;

/**
 * Pipeline string utils.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class PipelineStringUtils {
    
    /**
     * Equals ignore case.
     *
     * @param one one
     * @param another another
     * @return is equals ignore case
     */
    public static boolean equalsIgnoreCase(final Collection<String> one, final Collection<String> another) {
        if (null == one && null == another) {
            return true;
        }
        if (null == one || null == another) {
            return false;
        }
        if (one.size() != another.size()) {
            return false;
        }
        Iterator<String> oneIterator = one.iterator();
        Iterator<String> anotherIterator = another.iterator();
        while (oneIterator.hasNext() && anotherIterator.hasNext()) {
            if (!oneIterator.next().equalsIgnoreCase(anotherIterator.next())) {
                return false;
            }
        }
        return true;
    }
}
