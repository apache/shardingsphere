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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.impl;

import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShorthandProjectionConverterTest {
    
    @Test
    void assertConvertReturnsEmptyForNullSegment() {
        assertFalse(ShorthandProjectionConverter.convert(null).isPresent());
    }
    
    @Test
    void assertConvertReturnsStarWhenNoOwner() {
        Optional<SqlNode> actual = ShorthandProjectionConverter.convert(new ShorthandProjectionSegment(0, 0));
        assertTrue(actual.isPresent());
        assertTrue(((SqlIdentifier) actual.get()).isStar());
    }
    
    @Test
    void assertConvertReturnsQualifiedStarWhenOwnerPresent() {
        OwnerSegment table = new OwnerSegment(0, 0, new IdentifierValue("table"));
        table.setOwner(new OwnerSegment(0, 0, new IdentifierValue("schema")));
        ShorthandProjectionSegment projectionSegment = new ShorthandProjectionSegment(0, 0);
        projectionSegment.setOwner(table);
        Optional<SqlNode> actual = ShorthandProjectionConverter.convert(projectionSegment);
        assertTrue(actual.isPresent());
        SqlIdentifier identifier = (SqlIdentifier) actual.orElse(null);
        assertTrue(identifier.isStar());
        assertThat(identifier.names, is(Arrays.asList("schema", "table", "")));
    }
}
