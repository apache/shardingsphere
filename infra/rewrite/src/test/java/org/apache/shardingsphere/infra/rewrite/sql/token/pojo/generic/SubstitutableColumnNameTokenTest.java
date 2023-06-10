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

package org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic;

import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.SubqueryProjection;
import org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.enums.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class SubstitutableColumnNameTokenTest {
    
    @Test
    void assertToString() {
        Collection<Projection> projections = Collections.singletonList(new ColumnProjection(null, "id", null));
        assertThat(new SubstitutableColumnNameToken(0, 1, projections).toString(mock(RouteUnit.class)), is("id"));
    }
    
    @Test
    void assertToStringWithQuote() {
        Collection<Projection> projections = Collections.singletonList(new ColumnProjection(null,
                new IdentifierValue("id", QuoteCharacter.BACK_QUOTE), new IdentifierValue("id", QuoteCharacter.BACK_QUOTE)));
        assertThat(new SubstitutableColumnNameToken(0, 1, projections, QuoteCharacter.BACK_QUOTE).toString(mock(RouteUnit.class)), is("`id` AS `id`"));
    }
    
    @Test
    void assertToStringWithOwnerQuote() {
        Collection<Projection> projectionsWithOwnerQuote = Collections.singletonList(new ColumnProjection(new IdentifierValue("temp", QuoteCharacter.BACK_QUOTE),
                new IdentifierValue("id", QuoteCharacter.BACK_QUOTE), new IdentifierValue("id", QuoteCharacter.BACK_QUOTE)));
        assertThat(new SubstitutableColumnNameToken(0, 1, projectionsWithOwnerQuote, QuoteCharacter.BACK_QUOTE).toString(mock(RouteUnit.class)), is("`temp`.`id` AS `id`"));
        Collection<Projection> projectionsWithoutOwnerQuote = Collections.singletonList(new ColumnProjection(new IdentifierValue("temp", QuoteCharacter.NONE),
                new IdentifierValue("id", QuoteCharacter.BACK_QUOTE), new IdentifierValue("id", QuoteCharacter.BACK_QUOTE)));
        assertThat(new SubstitutableColumnNameToken(0, 1, projectionsWithoutOwnerQuote, QuoteCharacter.BACK_QUOTE).toString(mock(RouteUnit.class)), is("temp.`id` AS `id`"));
    }
    
    @Test
    void assertToStringWithSubqueryProjection() {
        Collection<Projection> projections = Arrays.asList(new ColumnProjection(new IdentifierValue("temp", QuoteCharacter.BACK_QUOTE),
                new IdentifierValue("id", QuoteCharacter.BACK_QUOTE), new IdentifierValue("id", QuoteCharacter.BACK_QUOTE)),
                new SubqueryProjection("(SELECT name FROM t_order)", new ColumnProjection(null, "name", null), "name", new OracleDatabaseType()));
        assertThat(new SubstitutableColumnNameToken(0, 1, projections, QuoteCharacter.BACK_QUOTE).toString(mock(RouteUnit.class)),
                is("`temp`.`id` AS `id`, `name`"));
    }
}
