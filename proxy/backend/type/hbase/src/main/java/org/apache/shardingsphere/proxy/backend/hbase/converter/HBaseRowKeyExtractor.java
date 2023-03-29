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

package org.apache.shardingsphere.proxy.backend.hbase.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;

import java.util.List;
import java.util.stream.Collectors;

/**
 * HBase row key extractor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HBaseRowKeyExtractor {
    
    /**
     * Get row keys from binary operation expression.
     * 
     * @param expr binary operation expression
     * @return row keys
     */
    public static String getRowKey(final BinaryOperationExpression expr) {
        return String.valueOf(((LiteralExpressionSegment) expr.getRight()).getLiterals());
    }
    
    /**
     * Get row keys from in expression.
     * 
     * @param expr in expression
     * @return row keys
     */
    public static List<String> getRowKeys(final InExpression expr) {
        return ((ListExpression) expr.getRight()).getItems().stream().map(each -> String.valueOf(((LiteralExpressionSegment) each).getLiterals())).collect(Collectors.toList());
    }
}
