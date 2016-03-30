/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.executor.wrapper;

import com.dangdang.ddframe.rdb.sharding.executor.event.DMLExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.EventExecutionType;
import com.dangdang.ddframe.rdb.sharding.router.SQLExecutionUnit;
import com.google.common.base.Optional;
import lombok.Getter;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * 预编译语句对象的执行上下文.
 * 
 * @author zhangliang
 */
public final class PreparedStatementExecutorWrapper extends AbstractExecutorWrapper {
    
    @Getter
    private final PreparedStatement preparedStatement;
    
    private final Optional<DMLExecutionEvent> dmlExecutionEvent;
    
    public PreparedStatementExecutorWrapper(final PreparedStatement preparedStatement, final List<Object> parameters,
                                            final SQLExecutionUnit sqlExecutionUnit) {
        super(sqlExecutionUnit);
        this.preparedStatement = preparedStatement;
        if (isDML()) {
            dmlExecutionEvent = Optional.of(new DMLExecutionEvent(getSqlExecutionUnit().getDataSource(), getSqlExecutionUnit().getSql(), parameters, EventExecutionType.BEFORE_EXECUTE));
        } else {
            dmlExecutionEvent = Optional.absent();
        }
    }
    
    @Override
    public Optional<DMLExecutionEvent> getDMLExecutionEvent() {
        return dmlExecutionEvent;
    }
}
