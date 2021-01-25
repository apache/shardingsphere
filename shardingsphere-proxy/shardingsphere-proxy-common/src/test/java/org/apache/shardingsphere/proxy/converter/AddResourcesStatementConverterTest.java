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

package org.apache.shardingsphere.proxy.converter;

import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.AddResourceStatement;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class AddResourcesStatementConverterTest {
    
    @Test
    public void assertConvert() {
        Map<String, YamlDataSourceParameter> actual = AddResourcesStatementConverter.convert(new MySQLDatabaseType(), new AddResourceStatement(createDataSourceSegments()));
        assertThat(actual.size(), is(2));
        assertTrue(actual.keySet().containsAll(Arrays.asList("ds0", "ds1")));
        assertThat(actual.values().iterator().next().getUsername(), is("root0"));
    }
    
    private Collection<DataSourceSegment> createDataSourceSegments() {
        Collection<DataSourceSegment> result = new LinkedList<>();
        for (int i = 0; i < 2; i++) {
            DataSourceSegment segment = new DataSourceSegment();
            segment.setName(String.format("ds%s", i));
            segment.setHostName("127.0.0.1");
            segment.setPassword("3306");
            segment.setDb(String.format("demo_ds_%s", i));
            segment.setUser(String.format("root%s", i));
            segment.setPassword(String.format("root%s", i));
            result.add(segment);
        }
        return result;
    }
}
