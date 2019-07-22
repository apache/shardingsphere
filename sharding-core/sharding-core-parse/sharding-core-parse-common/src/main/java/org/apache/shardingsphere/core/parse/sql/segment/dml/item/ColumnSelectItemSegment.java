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

package org.apache.shardingsphere.core.parse.sql.segment.dml.item;

import com.google.common.base.Optional;
import lombok.Getter;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.AliasAvailable;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

/**
 * Column select item segment.
 * 
 * @author zhangliang
 */
public final class ColumnSelectItemSegment extends ColumnSegment implements SelectItemSegment, AliasAvailable {
    
    @Getter
    private final String text;
    
    private String alias;
    
    public ColumnSelectItemSegment(final String text, final ColumnSegment columnSegment) {
        super(columnSegment.getStartIndex(), columnSegment.getStopIndex(), columnSegment.getName());
        this.text = text;
        if (columnSegment.getOwner().isPresent()) {
            setOwner(columnSegment.getOwner().get());
        }
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
