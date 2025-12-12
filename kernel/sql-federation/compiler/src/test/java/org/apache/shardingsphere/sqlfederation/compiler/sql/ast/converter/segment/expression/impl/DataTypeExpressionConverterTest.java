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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlBasicTypeNameSpec;
import org.apache.calcite.sql.SqlDataTypeSpec;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeLengthSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DataTypeExpressionConverterTest {
    
    @Test
    void assertConvertWithoutLength() {
        DataTypeSegment segment = new DataTypeSegment();
        segment.setStartIndex(0);
        segment.setStopIndex(0);
        segment.setDataTypeName("INTEGER");
        SqlDataTypeSpec actual = (SqlDataTypeSpec) DataTypeExpressionConverter.convert(segment);
        SqlBasicTypeNameSpec typeNameSpec = (SqlBasicTypeNameSpec) actual.getTypeNameSpec();
        assertThat(typeNameSpec.getTypeName().getSimple(), is("INTEGER"));
        assertThat(typeNameSpec.getPrecision(), is(-1));
    }
    
    @Test
    void assertConvertWithLength() {
        DataTypeSegment segment = new DataTypeSegment();
        segment.setStartIndex(0);
        segment.setStopIndex(0);
        segment.setDataTypeName("varchar");
        DataTypeLengthSegment dataLength = new DataTypeLengthSegment();
        dataLength.setPrecision(10);
        segment.setDataLength(dataLength);
        SqlDataTypeSpec actual = (SqlDataTypeSpec) DataTypeExpressionConverter.convert(segment);
        SqlBasicTypeNameSpec typeNameSpec = (SqlBasicTypeNameSpec) actual.getTypeNameSpec();
        assertThat(typeNameSpec.getPrecision(), is(10));
    }
}
