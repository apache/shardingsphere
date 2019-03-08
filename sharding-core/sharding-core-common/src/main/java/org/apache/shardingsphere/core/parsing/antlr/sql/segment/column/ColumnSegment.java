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

package org.apache.shardingsphere.core.parsing.antlr.sql.segment.column;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.shardingsphere.core.parsing.antlr.sql.OwnerAvailable;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.SQLRightValueExpressionSegment;
import org.apache.shardingsphere.core.parsing.lexer.token.Symbol;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Condition;
import org.apache.shardingsphere.core.util.SQLUtil;

import java.util.List;

/**
 * Column segment.
 *
 * @author duhongjun
 * @author zhangliang
 */
@Getter
public class ColumnSegment implements SQLRightValueExpressionSegment, OwnerAvailable {
    
    private final String name;
    
    private final String owner;
    
    private final int startIndex;
    
    private final int stopIndex;
    
    public ColumnSegment(final String columnText, final int startIndex, final int stopIndex) {
        List<String> texts = Splitter.on(Symbol.DOT.getLiterals()).splitToList(columnText);
        if (1 == texts.size()) {
            name = SQLUtil.getExactlyValue(columnText);
            owner = null;
        } else {
            name = SQLUtil.getExactlyValue(texts.get(texts.size() - 1));
            owner = SQLUtil.getExactlyValue(texts.get(0));
        }
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
    }
    
    /**
     * Get qualified name.
     *
     * @return qualified name
     */
    public final String getQualifiedName() {
        return null == owner ? name : owner + Symbol.DOT.getLiterals() + name;
    }
    
    @Override
    public final Optional<String> getOwner() {
        return Optional.fromNullable(owner);
    }

    @Override
    public Condition buildCondition(final Column column, final String sql) {
        throw new RuntimeException("Unsupported right column segment to condition ");
    }
}
