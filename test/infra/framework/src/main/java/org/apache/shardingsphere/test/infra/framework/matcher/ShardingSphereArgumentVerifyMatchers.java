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

package org.apache.shardingsphere.test.infra.framework.matcher;

import com.cedarsoftware.util.DeepEquals;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.mockito.ArgumentMatcher;
import org.mockito.internal.progress.ThreadSafeMockingProgress;

/**
 * ShardingSphere argument verify matchers.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereArgumentVerifyMatchers {
    
    /**
     * Deep equals.
     *
     * @param obj to be verified object
     * @param <T> type of to be verified object
     * @return null
     */
    public static <T> T deepEq(final T obj) {
        reportMatcher(new DeepEqualsMatcher(obj));
        return obj;
    }
    
    private static void reportMatcher(final ArgumentMatcher<?> matcher) {
        ThreadSafeMockingProgress.mockingProgress().getArgumentMatcherStorage().reportMatcher(matcher);
    }
    
    @RequiredArgsConstructor
    private static final class DeepEqualsMatcher implements ArgumentMatcher<Object> {
        
        private final Object wanted;
        
        @Override
        public boolean matches(final Object actual) {
            return DeepEquals.deepEquals(wanted, actual);
        }
        
        @Override
        public String toString() {
            return "deepEq(" + wanted + ")";
        }
    }
}
