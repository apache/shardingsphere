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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.type;

import org.apache.calcite.sql.type.SqlTypeName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DataTypeConverterTest {
    
    @Test
    void assertConvertRegisteredDataType() {
        assertThat(DataTypeConverter.convert("int"), is(SqlTypeName.INTEGER));
        assertThat(DataTypeConverter.convert("int2"), is(SqlTypeName.SMALLINT));
        assertThat(DataTypeConverter.convert("int4"), is(SqlTypeName.INTEGER));
        assertThat(DataTypeConverter.convert("int8"), is(SqlTypeName.BIGINT));
        assertThat(DataTypeConverter.convert("money"), is(SqlTypeName.DECIMAL));
    }
    
    @Test
    void assertConvertUnregisteredDataType() {
        assertThat(DataTypeConverter.convert("varchar"), is(SqlTypeName.VARCHAR));
        assertThat(DataTypeConverter.convert("VARCHAR"), is(SqlTypeName.VARCHAR));
    }
}
