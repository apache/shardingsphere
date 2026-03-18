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

package org.apache.shardingsphere.mode.manager.standalone.exclusive;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandaloneExclusiveOperatorContextTest {
    
    @Test
    void assertStartReturnsTrueWhenKeyAbsent() {
        assertTrue(new StandaloneExclusiveOperatorContext().start("operation-key", 50L));
    }
    
    @Test
    void assertStartReturnsFalseWhenKeyExists() {
        StandaloneExclusiveOperatorContext context = new StandaloneExclusiveOperatorContext();
        Collection<String> existingKeys = getExclusiveOperationKeys(context);
        existingKeys.add("duplicated-key");
        assertFalse(context.start("duplicated-key", 0L));
    }
    
    @Test
    void assertStopRemovesOperationKey() {
        StandaloneExclusiveOperatorContext context = new StandaloneExclusiveOperatorContext();
        Collection<String> existingKeys = getExclusiveOperationKeys(context);
        existingKeys.add("stopped-key");
        context.stop("stopped-key");
        assertTrue(existingKeys.isEmpty());
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Collection<String> getExclusiveOperationKeys(final StandaloneExclusiveOperatorContext context) {
        return (Collection<String>) Plugins.getMemberAccessor().get(StandaloneExclusiveOperatorContext.class.getDeclaredField("exclusiveOperationKeys"), context);
    }
}
