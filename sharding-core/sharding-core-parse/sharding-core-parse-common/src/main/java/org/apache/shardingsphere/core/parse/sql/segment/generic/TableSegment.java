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

package org.apache.shardingsphere.core.parse.sql.segment.generic;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.core.parse.core.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

/**
 * Table segment.
 * 
 * @author duhongjun
 * @author panjuan
 * @author zhangliang
 */
@Getter
@ToString
public final class TableSegment implements SQLSegment, TableAvailable, OwnerAvailable<SchemaSegment>, AliasAvailable {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    @Getter(AccessLevel.NONE)
    private final String name;
    
    private final QuoteCharacter quoteCharacter;
    
    @Setter
    private SchemaSegment owner;
    
    private String alias;
    
    public TableSegment(final int startIndex, final int stopIndex, final String name) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.name = SQLUtil.getExactlyValue(name);
        this.quoteCharacter = QuoteCharacter.getQuoteCharacter(name);
    }
    
    @Override
    public int getStartIndex() {
        return null == owner ? startIndex : owner.getStartIndex(); 
    }
    
    @Override
    public String getTableName() {
        return name;
    }
    
    @Override
    public QuoteCharacter getTableQuoteCharacter() {
        return quoteCharacter;
    }
    
    @Override
    public Optional<SchemaSegment> getOwner() {
        return Optional.fromNullable(owner);
    }
    
    @Override
    public Optional<String> getAlias() {
        return Optional.fromNullable(alias);
    }
    
    @Override
    public void setAlias(final String alias) {
        this.alias = SQLUtil.getExactlyValue(alias);
    }
}
