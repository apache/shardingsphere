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

package org.apache.shardingsphere.proxy.convert;

import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.rdl.parser.binder.context.CreateDataSourcesStatementContext;
import org.apache.shardingsphere.rdl.parser.statement.rdl.CreateDataSourcesStatement;
import org.apache.shardingsphere.rdl.parser.statement.rdl.DataSourceConnectionSegment;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class CreateDataSourcesStatementContextConverterTest {
    
    private CreateDataSourcesStatementContext sqlStatement;
    
    @Before
    public void setUp() {
        sqlStatement = new CreateDataSourcesStatementContext(new CreateDataSourcesStatement(createDataSourceConnectionSegments()), new MySQLDatabaseType());
    }
    
    private Collection<DataSourceConnectionSegment> createDataSourceConnectionSegments() {
        Collection<DataSourceConnectionSegment> result = new LinkedList<>();
        for (int i = 0; i < 2; i++) {
            DataSourceConnectionSegment segment = new DataSourceConnectionSegment();
            segment.setName("ds" + i);
            segment.setHostName("127.0.0.1");
            segment.setPassword("3306");
            segment.setDb("demo_ds_" + i);
            segment.setUser("root" + i);
            segment.setPassword("root" + i);
            result.add(segment);
        }
        return result;
    }
    
    @Test
    public void assertGenerate() {
        Map<String, YamlDataSourceParameter> result = new CreateDataSourcesStatementContextConverter().convert(sqlStatement);
        assertThat(result.size(), is(2));
        assertTrue(result.keySet().containsAll(Arrays.asList("ds0", "ds1")));
        assertThat(result.values().iterator().next().getUsername(), is("root0"));
    }
}
