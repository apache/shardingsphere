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

package org.apache.shardingsphere.distsql.parser.segment.converter;

import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataSourceSegmentsConverterTest {
    
    @Test
    void assertConvert() {
        Map<String, DataSourceProperties> actual = DataSourceSegmentsConverter.convert(new MySQLDatabaseType(), createDataSourceSegments());
        assertThat(actual.size(), is(2));
        assertTrue(actual.keySet().containsAll(Arrays.asList("ds0", "ds1")));
        assertThat(actual.values().iterator().next().getAllLocalProperties().get("username"), is("root0"));
        assertThat(actual.values().iterator().next().getAllStandardProperties().get("maxPoolSize"), is("30"));
    }
    
    private Collection<DataSourceSegment> createDataSourceSegments() {
        Collection<DataSourceSegment> result = new LinkedList<>();
        Properties customPoolProps = PropertiesBuilder.build(new Property("maxPoolSize", "30"));
        result.add(new HostnameAndPortBasedDataSourceSegment("ds0", "127.0.0.1", "3306", "demo_ds_0", "root0", "root0", customPoolProps));
        result.add(new URLBasedDataSourceSegment("ds1", "jdbc:mysql://127.0.0.1:3306/demo_ds_1?useSSL=false", "root1", "root1", customPoolProps));
        return result;
    }
}
