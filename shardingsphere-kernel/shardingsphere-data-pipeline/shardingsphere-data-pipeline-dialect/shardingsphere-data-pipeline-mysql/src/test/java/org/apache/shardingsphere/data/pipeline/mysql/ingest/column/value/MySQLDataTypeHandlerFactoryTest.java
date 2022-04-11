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

import static org.junit.Assert.assertTrue;

public final class MySQLDataTypeHandlerFactoryTest {

    @Test
    public void assertNewInstanceWithMySQLUnsignedTinyintHandler() {
        Optional<MySQLDataTypeHandler> mySQLUnsignedTinyintHandler = MySQLDataTypeHandlerFactory.newInstance("TINYINT UNSIGNED");
        assertTrue(mySQLUnsignedTinyintHandler.isPresent());
        assertTrue(mySQLUnsignedTinyintHandler.get() instanceof MySQLUnsignedTinyintHandler);
    }

    @Test
    public void assertNewInstanceWithMySQLUnsignedSmallintHandler() {
        Optional<MySQLDataTypeHandler> mySQLUnsignedSmallintHandler = MySQLDataTypeHandlerFactory.newInstance("SMALLINT UNSIGNED");
        assertTrue(mySQLUnsignedSmallintHandler.isPresent());
        assertTrue(mySQLUnsignedSmallintHandler.get() instanceof MySQLUnsignedSmallintHandler);
    }

    @Test
    public void assertNewInstanceWithMySQLUnsignedMediumintHandler() {
        Optional<MySQLDataTypeHandler> mySQLUnsignedMediumintHandler = MySQLDataTypeHandlerFactory.newInstance("MEDIUMINT UNSIGNED");
        assertTrue(mySQLUnsignedMediumintHandler.isPresent());
        assertTrue(mySQLUnsignedMediumintHandler.get() instanceof MySQLUnsignedMediumintHandler);
    }

    @Test
    public void assertNewInstanceWithMySQLUnsignedIntHandler() {
        Optional<MySQLDataTypeHandler> mySQLUnsignedIntHandler = MySQLDataTypeHandlerFactory.newInstance("INT UNSIGNED");
        assertTrue(mySQLUnsignedIntHandler.isPresent());
        assertTrue(mySQLUnsignedIntHandler.get() instanceof MySQLUnsignedIntHandler);
    }

    @Test
    public void assertNewInstanceWithMySQLUnsignedBigintHandler() {
        Optional<MySQLDataTypeHandler> mySQLUnsignedBigintHandler = MySQLDataTypeHandlerFactory.newInstance("BIGINT UNSIGNED");
        assertTrue(mySQLUnsignedBigintHandler.isPresent());
        assertTrue(mySQLUnsignedBigintHandler.get() instanceof MySQLUnsignedBigintHandler);
    }
}
