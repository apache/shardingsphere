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

package org.apache.shardingsphere.shadow.route.engine.util;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ShadowExtractorTest {

    @Test
    public void extractValuesIsSimpleExpressionSegment() {
        LiteralExpressionSegment LiteralExpressionSegment = new LiteralExpressionSegment(1, 2, "");
        List<Object> list = new LinkedList<>();
        Collection<Comparable<?>> result = new LinkedList<>();
        result.add((Comparable<?>) LiteralExpressionSegment.getLiterals());
        assertThat(ShadowExtractor.extractValues(LiteralExpressionSegment, list), equalTo(Optional.of(result)));
    }
}
