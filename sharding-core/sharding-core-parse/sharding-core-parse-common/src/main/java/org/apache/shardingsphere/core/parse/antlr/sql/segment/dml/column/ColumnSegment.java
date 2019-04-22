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

package org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.column;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.parse.antlr.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.antlr.sql.OwnerAvailable;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.SQLRightValueExpressionSegment;
import org.apache.shardingsphere.core.parse.old.lexer.token.Symbol;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

/**
 * Column segment.
 *
 * @author duhongjun
 * @author zhangliang
 */
@Getter
public class ColumnSegment implements SQLRightValueExpressionSegment, OwnerAvailable {
    
    private final int startIndex;
    
    private final String name;
    
    private String owner;
    
    @Setter(AccessLevel.PROTECTED)
    private QuoteCharacter ownerQuoteCharacter = QuoteCharacter.NONE;
    
    public ColumnSegment(final int startIndex, final String name) {
        this.startIndex = startIndex;
        this.name = SQLUtil.getExactlyValue(name);
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
    public final void setOwner(final String owner) {
        this.owner = SQLUtil.getExactlyValue(owner);
        ownerQuoteCharacter = QuoteCharacter.getQuoteCharacter(owner);
    }
    
    @Override
    public final Condition buildCondition(final Column column, final String sql) {
        throw new RuntimeException("Unsupported right column segment to condition ");
    }
}
