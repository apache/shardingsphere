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

package org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl;

import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.statement.core.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ParameterMarkerProjectionTest {
    
    @Test
    void assertGetColumnName() {
        assertThat(new ParameterMarkerProjection(1, ParameterMarkerType.QUESTION, new IdentifierValue("foo")).getColumnName(), is("1"));
    }
    
    @Test
    void assertGetColumnLabelWithAlias() {
        assertThat(new ParameterMarkerProjection(1, ParameterMarkerType.QUESTION, new IdentifierValue("foo")).getColumnLabel(), is("foo"));
    }
    
    @Test
    void assertGetColumnLabelWithoutAlias() {
        assertThat(new ParameterMarkerProjection(1, ParameterMarkerType.QUESTION, null).getColumnLabel(), is("1"));
    }
    
    @Test
    void assertGetExpression() {
        assertThat(new ParameterMarkerProjection(1, ParameterMarkerType.QUESTION, null).getExpression(), is("1"));
    }
    
    @Test
    void assertGetAlias() {
        assertThat(new ParameterMarkerProjection(1, ParameterMarkerType.QUESTION, new IdentifierValue("foo")).getAlias(), is(Optional.of(new IdentifierValue("foo", QuoteCharacter.NONE))));
    }
}
