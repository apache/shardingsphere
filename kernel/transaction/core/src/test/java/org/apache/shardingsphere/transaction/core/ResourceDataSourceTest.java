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

package org.apache.shardingsphere.transaction.core;

import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.transaction.exception.ResourceNameLengthExceededException;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceDataSourceTest {
    
    @Test
    void assertNewInstance() {
        String originalName = "foo_db.foo_ds";
        ResourceDataSource actual = new ResourceDataSource(originalName, new MockedDataSource());
        assertThat(actual.getOriginalName(), is(originalName));
        assertThat(actual.getDataSource(), isA(MockedDataSource.class));
        assertThat(actual.getUniqueResourceName(), matchesPattern(Pattern.compile("\\d+-foo_ds")));
    }
    
    @Test
    void assertNewInstanceFailureWithInvalidFormat() {
        assertThrows(IllegalStateException.class, () -> new ResourceDataSource("invalid", new MockedDataSource()));
    }
    
    @Test
    void assertNewInstanceFailureWithTooLongUniqueResourceName() {
        assertThrows(ResourceNameLengthExceededException.class, () -> new ResourceDataSource("foo_db.foo_ds_00000000000000000000000000000000000000000000000000", new MockedDataSource()));
    }
}
