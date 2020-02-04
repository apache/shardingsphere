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

package org.apache.shardingsphere.core.parse.sql.statement.generic;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * SQL statement abstract class.
 *
 * @author zhangliang
 * @author panjuan
 */
public abstract class AbstractSQLStatement implements SQLStatement {
    
    private final Collection<SQLSegment> sqlSegments = new LinkedList<>();
    
    @Getter
    @Setter
    private int parametersCount;
    
    @Override
    public final Collection<SQLSegment> getAllSQLSegments() {
        return sqlSegments;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public final <T extends SQLSegment> Optional<T> findSQLSegment(final Class<T> sqlSegmentType) {
        for (SQLSegment each : sqlSegments) {
            if (sqlSegmentType.isAssignableFrom(each.getClass())) {
                return Optional.of((T) each);
            }
        }
        return Optional.absent();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public final <T extends SQLSegment> Collection<T> findSQLSegments(final Class<T> sqlSegmentType) {
        Collection<T> result = new LinkedList<>();
        for (SQLSegment each : sqlSegments) {
            if (sqlSegmentType.isAssignableFrom(each.getClass())) {
                result.add((T) each);
            }
        }
        return result;
    }
}
