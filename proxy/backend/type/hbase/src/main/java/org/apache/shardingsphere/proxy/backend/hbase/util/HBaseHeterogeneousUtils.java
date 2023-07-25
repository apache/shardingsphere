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

package org.apache.shardingsphere.proxy.backend.hbase.util;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HBase heterogeneous utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HBaseHeterogeneousUtils {
    
    /**
     * Whether crc projection segment.
     * 
     * @param projectionSegment projection segment
     * @return Is crc projection segment or not
     */
    public static boolean isCrcProjectionSegment(final ProjectionSegment projectionSegment) {
        
        if (projectionSegment instanceof ExpressionProjectionSegment) {
            return ((ExpressionProjectionSegment) projectionSegment).getText().contains("crc32");
        }
        
        return false;
    }
    
    /**
     * Convert prepared statement to literal statement.
     * 
     * @param source source
     * @param target target
     * @param replacements replacements
     * @return literal statement
     */
    public static String replaceSQLStatementWithParameters(final String source, final CharSequence target, final Object... replacements) {
        if (null == source || null == replacements) {
            return source;
        }
        Matcher matcher = Pattern.compile(target.toString(), Pattern.LITERAL).matcher(source);
        int found = 0;
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            found++;
            Preconditions.checkState(found <= replacements.length, "Missing replacement for '%s' at [%s].", target, found);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacements[found - 1].toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * Return true if use * in SELECT statement.
     * 
     * @param statementContext select statement context
     * @return is use shorthand projection
     */
    public static boolean isUseShorthandProjection(final SelectStatementContext statementContext) {
        return statementContext.getProjectionsContext().getProjections().stream().anyMatch(ShorthandProjection.class::isInstance);
    }
}
