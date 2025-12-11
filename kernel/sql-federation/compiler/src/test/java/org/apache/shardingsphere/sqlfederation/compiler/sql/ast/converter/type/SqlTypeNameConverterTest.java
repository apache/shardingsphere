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

class SqlTypeNameConverterTest {
    
    @Test
    void assertConvertRegisteredDataType() {
        assertThat(SqlTypeNameConverter.convert("int"), is(SqlTypeName.INTEGER));
        assertThat(SqlTypeNameConverter.convert("int2"), is(SqlTypeName.SMALLINT));
        assertThat(SqlTypeNameConverter.convert("INT4"), is(SqlTypeName.INTEGER));
        assertThat(SqlTypeNameConverter.convert("INT8"), is(SqlTypeName.BIGINT));
        assertThat(SqlTypeNameConverter.convert("money"), is(SqlTypeName.DECIMAL));
    }
    
    @Test
    void assertConvertUnregisteredDataType() {
        assertThat(SqlTypeNameConverter.convert("varchar"), is(SqlTypeName.VARCHAR));
        assertThat(SqlTypeNameConverter.convert("VARCHAR"), is(SqlTypeName.VARCHAR));
    }
}
