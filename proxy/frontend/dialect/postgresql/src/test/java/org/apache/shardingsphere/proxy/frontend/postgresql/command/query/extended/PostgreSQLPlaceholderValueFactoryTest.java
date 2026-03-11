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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Types;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

class PostgreSQLPlaceholderValueFactoryTest {
    
    @Test
    void assertCreateWithColumn() {
        ShardingSphereColumn column = new ShardingSphereColumn("order_id", Types.BIGINT, true, false, false, true, false, false);
        assertThat(PostgreSQLPlaceholderValueFactory.create(column, PostgreSQLBinaryColumnType.FLOAT8), is(0));
    }
    
    @Test
    void assertCreateWithBinaryColumnType() {
        assertThat(PostgreSQLPlaceholderValueFactory.create(PostgreSQLBinaryColumnType.NUMERIC), is(BigDecimal.ZERO));
    }
    
    @Test
    void assertCreateWithJdbcType() {
        assertThat(PostgreSQLPlaceholderValueFactory.create(Types.TIMESTAMP), isA(java.sql.Timestamp.class));
    }
}
