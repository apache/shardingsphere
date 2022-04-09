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
    public void assertNewInstance() {
        Optional<MySQLDataTypeHandler> result1 = MySQLDataTypeHandlerFactory.newInstance("TINYINT UNSIGNED");
        assertTrue(result1.isPresent());
        assertTrue(result1.get() instanceof MySQLUnsignedTinyintHandler);
        Optional<MySQLDataTypeHandler> result2 = MySQLDataTypeHandlerFactory.newInstance("SMALLINT UNSIGNED");
        assertTrue(result2.isPresent());
        assertTrue(result2.get() instanceof MySQLUnsignedSmallintHandler);
        Optional<MySQLDataTypeHandler> result3 = MySQLDataTypeHandlerFactory.newInstance("MEDIUMINT UNSIGNED");
        assertTrue(result3.isPresent());
        assertTrue(result3.get() instanceof MySQLUnsignedMediumintHandler);
        Optional<MySQLDataTypeHandler> result4 = MySQLDataTypeHandlerFactory.newInstance("INT UNSIGNED");
        assertTrue(result4.isPresent());
        assertTrue(result4.get() instanceof MySQLUnsignedIntHandler);
        Optional<MySQLDataTypeHandler> result5 = MySQLDataTypeHandlerFactory.newInstance("BIGINT UNSIGNED");
        assertTrue(result5.isPresent());
        assertTrue(result5.get() instanceof MySQLUnsignedBigintHandler);
    }
}
