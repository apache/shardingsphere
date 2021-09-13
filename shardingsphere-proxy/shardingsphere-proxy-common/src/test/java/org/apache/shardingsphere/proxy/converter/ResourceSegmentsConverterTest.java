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
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ResourceSegmentsConverterTest {
    
    @Test
    public void assertConvert() {
        Map<String, YamlDataSourceParameter> actual = ResourceSegmentsConverter.convert(new MySQLDatabaseType(), createDataSourceSegments());
        assertThat(actual.size(), is(2));
        assertTrue(actual.keySet().containsAll(Arrays.asList("ds0", "ds1")));
        assertThat(actual.values().iterator().next().getUsername(), is("root0"));
        assertThat(actual.values().iterator().next().getCustomPoolProps().getProperty("maxPoolSize"), is("30"));
    }
    
    private Collection<DataSourceSegment> createDataSourceSegments() {
        Collection<DataSourceSegment> result = new LinkedList<>();
        Properties customPoolProps = new Properties();
        customPoolProps.setProperty("maxPoolSize", "30");
        result.add(new DataSourceSegment("ds0", null, "127.0.0.1", "3306", "demo_ds_0", "root0", "root0", customPoolProps));
        result.add(new DataSourceSegment("ds1", "jdbc:mysql://127.0.0.1:3306/demo_ds_1?useSSL=false", null, null, null, "root1", "root1", customPoolProps));
        return result;
    }
}
