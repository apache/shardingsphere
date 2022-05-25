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

package org.apache.shardingsphere.singletable.route.engine;

import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropSchemaStatement;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class SingleTableRouteEngineFactoryTest {
    
    @Test
    public void assertNewInstanceWithNotEmptySingleTableNames() {
        assertTrue(SingleTableRouteEngineFactory.newInstance(Collections.singleton(new QualifiedTable("demo_ds", "t_order")), mock(SQLStatement.class)).isPresent());
    }
    
    @Test
    public void assertNewInstanceWithEmptySingleTableNameAndCreateSchemaStatement() {
        assertTrue(SingleTableRouteEngineFactory.newInstance(Collections.emptyList(), mock(CreateSchemaStatement.class)).isPresent());
    }
    
    @Test
    public void assertNewInstanceWithEmptySingleTableNameAndAlterSchemaStatement() {
        assertTrue(SingleTableRouteEngineFactory.newInstance(Collections.emptyList(), mock(AlterSchemaStatement.class)).isPresent());
    }
    
    @Test
    public void assertNewInstanceWithEmptySingleTableNameAndDropSchemaStatement() {
        assertTrue(SingleTableRouteEngineFactory.newInstance(Collections.emptyList(), mock(DropSchemaStatement.class)).isPresent());
    }
    
    @Test
    public void assertNewInstanceWithEmptySingleTableNameAndOtherStatement() {
        assertFalse(SingleTableRouteEngineFactory.newInstance(Collections.emptyList(), mock(SQLStatement.class)).isPresent());
    }
}
