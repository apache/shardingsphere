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

package io.shardingsphere.core.executor.event;

import io.shardingsphere.core.routing.SQLUnit;

import java.util.List;

/**
 * DML execution event.
 * 
 * @author zhangliang
 * @author maxiaoguang
 */
public final class DMLExecutionEvent extends AbstractSQLExecutionEvent {
    
    public DMLExecutionEvent(final String dataSource, final SQLUnit sqlUnit, final List<Object> parameters) {
        super(dataSource, sqlUnit, parameters);
    }
}
