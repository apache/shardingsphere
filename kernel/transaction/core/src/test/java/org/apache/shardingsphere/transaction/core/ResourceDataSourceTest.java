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

import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceDataSourceTest {
    
    private static final String DATABASE_NAME = "sharding_db";
    
    private static final String DATA_SOURCE_NAME = "fooDataSource";
    
    @Test
    void assertNewInstance() {
        String originalName = DATABASE_NAME + "." + DATA_SOURCE_NAME;
        ResourceDataSource actual = new ResourceDataSource(originalName, new MockedDataSource());
        assertThat(actual.getOriginalName(), is(originalName));
        assertThat(actual.getDataSource(), instanceOf(MockedDataSource.class));
        assertTrue(isStartWithNumber(actual.getUniqueResourceName()));
        assertTrue(actual.getUniqueResourceName().endsWith(DATA_SOURCE_NAME));
    }
    
    private boolean isStartWithNumber(final String resourceId) {
        Pattern pattern = Pattern.compile("[0-9]+-.*");
        return pattern.matcher(resourceId).matches();
    }
    
    @Test
    void assertDataSourceNameOnlyFailure() {
        assertThrows(IllegalStateException.class, () -> new ResourceDataSource(DATA_SOURCE_NAME, new MockedDataSource()));
    }
}
