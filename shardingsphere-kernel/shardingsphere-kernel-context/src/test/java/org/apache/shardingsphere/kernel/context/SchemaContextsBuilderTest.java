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

package org.apache.shardingsphere.kernel.context;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaContextsBuilderTest {
    
    @Mock
    private DatabaseType databaseType;
    
    @Test
    public void assertBuildWithEmptyConfiguration() throws SQLException {
        SchemaContexts actual = new SchemaContextsBuilder(databaseType, Collections.emptyMap(), Collections.emptyMap(), null).build();
        assertThat(actual.getDatabaseType(), is(databaseType));
        assertTrue(actual.getSchemaContexts().isEmpty());
        assertTrue(actual.getAuthentication().getUsers().isEmpty());
        assertTrue(actual.getProps().getProps().isEmpty());
        assertFalse(actual.isCircuitBreak());
    }
}
