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

import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public final class SubstitutableColumnNameTokenTest {
    
    @Test
    public void assertToString() {
        Collection<ColumnProjection> projections = Collections.singletonList(new ColumnProjection(null, "id", null));
        assertThat(new SubstitutableColumnNameToken(0, 1, projections).toString(mock(RouteUnit.class)), is("id"));
    }
    
    @Test
    public void assertToStringWithQuote() {
        Collection<ColumnProjection> projections = Collections.singletonList(new ColumnProjection(null, "id", "id"));
        assertThat(new SubstitutableColumnNameToken(0, 1, projections, QuoteCharacter.BACK_QUOTE).toString(mock(RouteUnit.class)), is("`id` AS `id`"));
    }
}
