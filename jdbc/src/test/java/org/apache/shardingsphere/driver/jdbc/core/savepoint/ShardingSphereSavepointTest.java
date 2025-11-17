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

package org.apache.shardingsphere.driver.jdbc.core.savepoint;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShardingSphereSavepointTest {
    
    @Test
    void assertGetSavepointNameWithUUID() {
        assertFalse(new ShardingSphereSavepoint().getSavepointName().isEmpty());
    }
    
    @Test
    void assertGetSavepointNameWithName() throws SQLException {
        assertThat(new ShardingSphereSavepoint("foo_point").getSavepointName(), is("foo_point"));
    }
    
    @Test
    void assertNewSavepointNameWithInvalidName() {
        SQLFeatureNotSupportedException exception = assertThrows(SQLFeatureNotSupportedException.class, () -> new ShardingSphereSavepoint(""));
        assertThat(exception.getMessage(), is("Savepoint name can not be NULL or empty"));
    }
    
    @Test
    void assertGetSavepointId() {
        SQLFeatureNotSupportedException exception = assertThrows(SQLFeatureNotSupportedException.class, () -> new ShardingSphereSavepoint().getSavepointId());
        assertThat(exception.getMessage(), is("Only named savepoint are supported."));
    }
}
