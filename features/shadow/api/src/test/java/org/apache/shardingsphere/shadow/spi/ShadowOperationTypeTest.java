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

package org.apache.shardingsphere.shadow.spi;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ShadowOperationTypeTest {
    
    @Test
    void assertValueFrom() {
        assertThat(ShadowOperationType.valueFrom("INSERT"), is(Optional.of(ShadowOperationType.INSERT)));
        assertThat(ShadowOperationType.valueFrom("DELETE"), is(Optional.of(ShadowOperationType.DELETE)));
        assertThat(ShadowOperationType.valueFrom("UPDATE"), is(Optional.of(ShadowOperationType.UPDATE)));
        assertThat(ShadowOperationType.valueFrom("SELECT"), is(Optional.of(ShadowOperationType.SELECT)));
        assertFalse(ShadowOperationType.valueFrom("HINT_MATCH").isPresent());
    }
}
