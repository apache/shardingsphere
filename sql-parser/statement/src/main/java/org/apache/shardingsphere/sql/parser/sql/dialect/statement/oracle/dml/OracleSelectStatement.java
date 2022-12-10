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

package org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml;

import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.ModelSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.OracleStatement;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Oracle select statement.
 */
@Setter
public final class OracleSelectStatement extends SelectStatement implements OracleStatement {
    
    private LockSegment lock;
    
    private ModelSegment modelSegment;
    
    private WithSegment withSegment;
    
    /**
     * the select statement associated with rownum alias. the map should has only 1 entry.<br>
     * if use oracle's rownun alias in where, it means use the "order by" and "group by" exists in  the rownum's from sub query.<br>
     * ex: select * from (select t.*, rownum r from (select * from a order by a.name)t ) where r<10<br>
    -><br>
    select * from (select t.*, rownum r from (select * from a order by a.name)t ) where r<10 orde by name<br>
     */
    private Map<String, OracleSelectStatement> rowNumSelect = new ConcurrentHashMap<String, OracleSelectStatement>();
    
    /**
     * Get lock segment.
     *
     * @return lock segment
     */
    public Optional<LockSegment> getLock() {
        return Optional.ofNullable(lock);
    }
    
    /**
     * Get model segment.
     *
     * @return model segment
     */
    public Optional<ModelSegment> getModelSegment() {
        return Optional.ofNullable(modelSegment);
    }
    
    /**
     * Get with segment.
     *
     * @return with segment.
     */
    public Optional<WithSegment> getWithSegment() {
        return Optional.ofNullable(withSegment);
    }
    
    /**
     * get the select statement associated with rownum alias.
     * @return select statement associated with rownum alias.  
     */
    public Map<String, OracleSelectStatement> getRowNumSelect() {
        return rowNumSelect;
    }
}
