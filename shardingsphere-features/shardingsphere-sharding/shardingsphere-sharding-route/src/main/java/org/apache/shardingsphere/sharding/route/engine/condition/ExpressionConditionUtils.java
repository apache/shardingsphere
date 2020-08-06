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

package org.apache.shardingsphere.sharding.route.engine.condition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.ComplexExpressionSegment;

/**
 * Expression judgment tool for route.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpressionConditionUtils {

    /**
     * Judge now() expression.
     * @param segment ExpressionSegment
     * @return true or false
     */
    public static boolean isNowExpression(final ExpressionSegment segment) {
        return segment instanceof ComplexExpressionSegment && "now()".equalsIgnoreCase(((ComplexExpressionSegment) segment).getText());
    }

    /**
     * Judge null expression.
     * @param segment ExpressionSegment
     * @return true or false
     */
    public static boolean isNullExpression(final ExpressionSegment segment) {
        return segment instanceof CommonExpressionSegment && "null".equalsIgnoreCase(((CommonExpressionSegment) segment).getText());
    }
}
