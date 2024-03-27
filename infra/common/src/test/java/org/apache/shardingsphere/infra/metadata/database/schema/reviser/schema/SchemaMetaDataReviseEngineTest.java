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

package org.apache.shardingsphere.infra.metadata.database.schema.reviser.schema;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.ConstraintMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.rule.builder.fixture.FixtureGlobalRule;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class SchemaMetaDataReviseEngineTest {
    
    @Test
    void assertReviseWithoutMetaDataReviseEntry() {
        SchemaMetaData schemaMetaData = new SchemaMetaData("expected", Collections.singleton(mock(TableMetaData.class)));
        SchemaMetaData actual = new SchemaMetaDataReviseEngine(
                Collections.emptyList(), new ConfigurationProperties(new Properties()), mock(DatabaseType.class), mock(DataSource.class)).revise(schemaMetaData);
        assertThat(actual.getName(), is(schemaMetaData.getName()));
        assertThat(actual.getTables(), is(schemaMetaData.getTables()));
    }
    
    @Test
    void assertReviseWithMetaDataReviseEntry() {
        SchemaMetaData schemaMetaData = new SchemaMetaData("expected", Collections.singletonList(createTableMetaData()));
        SchemaMetaData actual = new SchemaMetaDataReviseEngine(
                Collections.singleton(new FixtureGlobalRule()), new ConfigurationProperties(new Properties()), mock(DatabaseType.class), mock(DataSource.class)).revise(schemaMetaData);
        assertThat(actual.getName(), is(schemaMetaData.getName()));
        assertThat(actual.getTables(), is(schemaMetaData.getTables()));
    }
    
    private TableMetaData createTableMetaData() {
        Collection<ColumnMetaData> columns = new LinkedHashSet<>(Arrays.asList(new ColumnMetaData("id", Types.INTEGER, true, true, true, true, false, false),
                new ColumnMetaData("pwd_cipher", Types.VARCHAR, false, false, true, true, false, false),
                new ColumnMetaData("pwd_like", Types.VARCHAR, false, false, true, true, false, false)));
        IndexMetaData indexMetaData = new IndexMetaData("index_name");
        ConstraintMetaData constraintMetaData = new ConstraintMetaData("constraint_name", "table_name_2");
        return new TableMetaData("table_name", columns, Collections.singletonList(indexMetaData), Collections.singleton(constraintMetaData));
    }
    
}
