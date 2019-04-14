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

package org.apache.shardingsphere.core.parse.antlr.sql.segment.common;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.antlr.sql.AliasAvailable;
import org.apache.shardingsphere.core.parse.antlr.sql.OwnerAvailable;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.token.TableToken;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

/**
 * Table segment.
 * 
 * @author duhongjun
 * @author panjuan
 */
@RequiredArgsConstructor
public class TableSegment implements SQLSegment, OwnerAvailable, AliasAvailable {
    
    @Getter
    private final TableToken token;
    
    private String owner;
    
    private String alias;
    
    /**
     * Get table name.
     *
     * @return table name
     */
    public String getName() {
        return token.getTableName();
    }
    
    @Override
    public final Optional<String> getOwner() {
        return Optional.fromNullable(owner);
    }
    
    @Override
    public final void setOwner(final String owner) {
        this.owner = SQLUtil.getExactlyValue(owner);
    }
    
    @Override
    public final Optional<String> getAlias() {
        return Optional.fromNullable(alias);
    }
    
    @Override
    public final void setAlias(final String alias) {
        this.alias = SQLUtil.getExactlyValue(alias);
    }
}
