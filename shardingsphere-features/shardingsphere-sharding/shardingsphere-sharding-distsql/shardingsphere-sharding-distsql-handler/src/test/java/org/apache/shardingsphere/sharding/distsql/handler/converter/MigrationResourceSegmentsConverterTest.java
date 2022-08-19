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

package org.apache.shardingsphere.sharding.distsql.handler.converter;

import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

public class MigrationResourceSegmentsConverterTest {
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertNotSuppHostnameAndPort() {
        DataSourceSegment segment = new HostnameAndPortBasedDataSourceSegment("test", "localhost", "3306", "ds_0", "root", "root", new Properties());
        MigrationResourceSegmentsConverter.convert(Collections.singletonList(segment));
    }
    
    @Test
    public void assertNotSuppHostnameAndPort2() {
        DataSourceSegment segment = new URLBasedDataSourceSegment("test", "jdbc:mysql://localhost:3306/ds_0", "root", "root", new Properties());
        Map<String, DataSourceProperties> actual = MigrationResourceSegmentsConverter.convert(Collections.singletonList(segment));
        assertNotNull(actual.get("test"));
    }
}
