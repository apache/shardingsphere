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
import org.apache.shardingsphere.infra.database.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class SchemaMetaDataReviseEngineTest {
    
    @Test
    void assertRevise() {
        Collection<ShardingSphereRule> rules = new ArrayList<>();
        Collection<TableMetaData> tableMetaData = Collections.singleton(mock(TableMetaData.class));
        ConfigurationProperties props = mock(ConfigurationProperties.class);
        DatabaseType databaseType = mock(DatabaseType.class);
        DataSource dataSource = mock(DataSource.class);
        SchemaMetaData schemaMetaData = new SchemaMetaData("expected", tableMetaData);
        SchemaMetaData actual = new SchemaMetaDataReviseEngine(rules, props, databaseType, dataSource).revise(schemaMetaData);
        assertThat(actual.getName(), is(schemaMetaData.getName()));
        assertThat(actual.getTables(), is(schemaMetaData.getTables()));
    }
}
