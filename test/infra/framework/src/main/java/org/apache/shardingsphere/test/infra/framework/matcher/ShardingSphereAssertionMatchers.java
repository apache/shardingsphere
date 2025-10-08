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
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * ShardingSphere assertion matchers.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereAssertionMatchers {
    
    /**
     * Deep equal.
     *
     * @param operand operand
     * @param <T> type of operand
     * @return matcher
     */
    public static <T> Matcher<T> deepEqual(final T operand) {
        return new DeepEqualMatcher<>(operand);
    }
    
    @RequiredArgsConstructor
    private static final class DeepEqualMatcher<T> extends BaseMatcher<T> {
        
        private final T expectedValue;
        
        @Override
        public boolean matches(final Object arg) {
            return DeepEquals.deepEquals(arg, expectedValue);
        }
        
        @Override
        public void describeTo(final Description description) {
            description.appendValue(expectedValue);
        }
    }
}
