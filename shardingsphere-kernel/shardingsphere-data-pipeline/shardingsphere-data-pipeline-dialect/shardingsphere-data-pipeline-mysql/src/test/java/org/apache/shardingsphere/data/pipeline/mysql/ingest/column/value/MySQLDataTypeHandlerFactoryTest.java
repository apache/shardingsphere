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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.column.value;

import org.apache.shardingsphere.data.pipeline.mysql.ingest.column.value.impl.MySQLUnsignedBigintHandler;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.column.value.impl.MySQLUnsignedIntHandler;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.column.value.impl.MySQLUnsignedMediumintHandler;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.column.value.impl.MySQLUnsignedSmallintHandler;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.column.value.impl.MySQLUnsignedTinyintHandler;

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class MySQLDataTypeHandlerFactoryTest {
    
    @Test
    public void assertFindInstanceWithMySQLUnsignedTinyintHandler() {
        Optional<MySQLDataTypeHandler> actual = MySQLDataTypeHandlerFactory.findInstance("TINYINT UNSIGNED");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MySQLUnsignedTinyintHandler.class));
    }
    
    @Test
    public void assertFindInstanceWithMySQLUnsignedSmallintHandler() {
        Optional<MySQLDataTypeHandler> actual = MySQLDataTypeHandlerFactory.findInstance("SMALLINT UNSIGNED");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MySQLUnsignedSmallintHandler.class));
    }
    
    @Test
    public void assertFindInstanceWithMySQLUnsignedMediumintHandler() {
        Optional<MySQLDataTypeHandler> actual = MySQLDataTypeHandlerFactory.findInstance("MEDIUMINT UNSIGNED");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MySQLUnsignedMediumintHandler.class));
    }
    
    @Test
    public void assertFindInstanceWithMySQLUnsignedIntHandler() {
        Optional<MySQLDataTypeHandler> actual = MySQLDataTypeHandlerFactory.findInstance("INT UNSIGNED");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MySQLUnsignedIntHandler.class));
    }
    
    @Test
    public void assertFindInstanceWithMySQLUnsignedBigintHandler() {
        Optional<MySQLDataTypeHandler> actual = MySQLDataTypeHandlerFactory.findInstance("BIGINT UNSIGNED");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MySQLUnsignedBigintHandler.class));
    }
}
