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

package org.apache.shardingsphere.traffic.api.traffic.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;

/**
 * Traffic statement type.
 */
@RequiredArgsConstructor
@Getter
public enum TrafficStatementType {
    
    INSERT(InsertStatement.class), DELETE(DeleteStatement.class), UPDATE(UpdateStatement.class), SELECT(SelectStatement.class), ALL(SQLStatement.class);
    
    private final Class<? extends SQLStatement> clazz;
    
    /**
     * Get statement type by clazz.
     * 
     * @param clazz class
     * @return statement type
     */
    public static TrafficStatementType getStatementTypeByClazz(final Class<? extends SQLStatement> clazz) {
        for (TrafficStatementType each : values()) {
            if (each.getClazz().isAssignableFrom(clazz)) {
                return each;
            }
        }
        return TrafficStatementType.ALL;
    }
}
    
