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

package org.apache.shardingsphere.core.rewrite.rewriter.sql;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.builder.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.placeholder.InsertColumnsPlaceholder;
import org.apache.shardingsphere.core.rewrite.placeholder.TablePlaceholder;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertColumnsToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.SQLToken;
import org.apache.shardingsphere.core.rewrite.token.pojo.TableToken;


/**
 * Base SQL rewriter.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
public final class BaseSQLRewriter implements SQLRewriter {
    
    @Override
    public void rewrite(final SQLBuilder sqlBuilder, final ParameterBuilder parameterBuilder, final SQLToken sqlToken) {
        if (sqlToken instanceof InsertColumnsToken) {
            appendInsertColumnsPlaceholder(sqlBuilder, (InsertColumnsToken) sqlToken);
        } else if (sqlToken instanceof TableToken) {
            appendTablePlaceholder(sqlBuilder, (TableToken) sqlToken);
        }
    }
    
    private void appendInsertColumnsPlaceholder(final SQLBuilder sqlBuilder, final InsertColumnsToken insertColumnsToken) {
        InsertColumnsPlaceholder columnsPlaceholder = new InsertColumnsPlaceholder(insertColumnsToken.getColumns(), insertColumnsToken.isToAppendCloseParenthesis());
        sqlBuilder.appendPlaceholder(columnsPlaceholder);
    }
    
    private void appendTablePlaceholder(final SQLBuilder sqlBuilder, final TableToken tableToken) {
        sqlBuilder.appendPlaceholder(new TablePlaceholder(tableToken.getTableName().toLowerCase(), tableToken.getQuoteCharacter()));
    }
}
