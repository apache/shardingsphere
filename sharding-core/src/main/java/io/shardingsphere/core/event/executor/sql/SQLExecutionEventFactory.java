/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.event.executor.sql;

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.executor.sql.SQLExecuteUnit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SQL execution event.
 * 
 * @author gaohongtao
 * @author maxiaoguang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLExecutionEventFactory {
    
    /**
     * Create SQL execution event.
     * 
     * @param sqlType SQL type
     * @param sqlExecuteUnit SQL execute unit
     * @param parameters parameters
     * @return SQL execution event
     */
    public static SQLExecutionEvent createEvent(final SQLType sqlType, final SQLExecuteUnit sqlExecuteUnit, final List<Object> parameters) {
        if (SQLType.DQL == sqlType) {
            return new DQLExecutionEvent(sqlExecuteUnit.getRouteUnit(), parameters);
        }
        if (SQLType.DML == sqlType) {
            return new DMLExecutionEvent(sqlExecuteUnit.getRouteUnit(), parameters);
        }
        return new SQLExecutionEvent(sqlExecuteUnit.getRouteUnit(), parameters);
    }
}
