/*
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

package com.dangdang.ddframe.rdb.sharding.exception;

/**
 * 不支持的数据库抛出的异常.
 * 
 * @author zhangliang
 */
public final class DatabaseTypeUnsupportedException extends ShardingJdbcException {
    
    private static final long serialVersionUID = -7807395469148925091L;
    
    private static final String MESSAGE = "Can not support database type [%s].";
    
    public DatabaseTypeUnsupportedException(final String databaseType) {
        super(MESSAGE, databaseType);
    }
}
