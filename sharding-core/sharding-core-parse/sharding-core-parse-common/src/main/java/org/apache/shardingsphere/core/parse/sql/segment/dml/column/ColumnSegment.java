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

package org.apache.shardingsphere.core.parse.sql.segment.dml.column;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.core.parse.core.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.generic.OwnerAvailable;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

/**
 * Column segment.
 *
 * @author duhongjun
 * @author zhangliang
 * @author panjuan
 */
@Getter
@Setter
@ToString
public class ColumnSegment implements SQLSegment, PredicateRightValue, OwnerAvailable<TableSegment> {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final String name;
    
    private final QuoteCharacter quoteCharacter;
    
    private TableSegment owner;
    
    public ColumnSegment(final int startIndex, final int stopIndex, final String name) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.name = SQLUtil.getExactlyValue(name);
        this.quoteCharacter = QuoteCharacter.getQuoteCharacter(name);
    }
    
    /**
     * Get qualified name.
     *
     * @return qualified name
     */
    public final String getQualifiedName() {
        return null == owner ? name : owner.getTableName() + "." + name;
    }
    
    @Override
    public final Optional<TableSegment> getOwner() {
        return Optional.fromNullable(owner);
    }
}
