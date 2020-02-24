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

package org.apache.shardingsphere.sql.parser.sql.segment.dml.item;

import com.google.common.base.Optional;
import lombok.Getter;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.util.SQLUtil;

/**
 * Column projection segment.
 */
public final class ColumnProjectionSegment extends ColumnSegment implements ProjectionSegment {
    
    @Getter
    private final String text;
    
    private AliasSegment alias;
    
    public ColumnProjectionSegment(final String text, final ColumnSegment columnSegment) {
        super(columnSegment.getStartIndex(), columnSegment.getStopIndex(), columnSegment.getIdentifier());
        this.text = SQLUtil.getExpressionWithoutOutsideParentheses(text);
        if (columnSegment.getOwner().isPresent()) {
            setOwner(columnSegment.getOwner().get());
        }
    }
    
    /**
     * Get alias.
     * @return alias
     */
    public Optional<String> getAlias() {
        return null == alias ? Optional.<String>absent() : Optional.fromNullable(alias.getIdentifier().getValue());
    }
    
    /**
     * Set alias.
     * @param alias alias
     */
    public void setAlias(final AliasSegment alias) {
        this.alias = alias;
    }
}
