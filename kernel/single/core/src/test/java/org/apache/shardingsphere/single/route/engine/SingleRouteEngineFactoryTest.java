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

package org.apache.shardingsphere.single.route.engine;

import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropSchemaStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SingleRouteEngineFactoryTest {
    
    @Test
    void assertNewInstanceWithNotEmptySingleTables() {
        assertTrue(SingleRouteEngineFactory.newInstance(Collections.singleton(new QualifiedTable("demo_ds", "t_order")), mock(SQLStatement.class)).isPresent());
    }
    
    @Test
    void assertNewInstanceWithEmptySingleTableNameAndCreateSchemaStatement() {
        assertTrue(SingleRouteEngineFactory.newInstance(Collections.emptyList(), mock(CreateSchemaStatement.class)).isPresent());
    }
    
    @Test
    void assertNewInstanceWithEmptySingleTableNameAndAlterSchemaStatement() {
        assertTrue(SingleRouteEngineFactory.newInstance(Collections.emptyList(), mock(AlterSchemaStatement.class)).isPresent());
    }
    
    @Test
    void assertNewInstanceWithEmptySingleTableNameAndDropSchemaStatement() {
        assertTrue(SingleRouteEngineFactory.newInstance(Collections.emptyList(), mock(DropSchemaStatement.class)).isPresent());
    }
    
    @Test
    void assertNewInstanceWithEmptySingleTableNameAndOtherStatement() {
        assertFalse(SingleRouteEngineFactory.newInstance(Collections.emptyList(), mock(SQLStatement.class)).isPresent());
    }
}
