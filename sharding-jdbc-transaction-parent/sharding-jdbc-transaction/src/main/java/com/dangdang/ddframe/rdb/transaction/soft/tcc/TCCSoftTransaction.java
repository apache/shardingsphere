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

package com.dangdang.ddframe.rdb.transaction.soft.tcc;

import com.dangdang.ddframe.rdb.transaction.soft.api.AbstractSoftTransaction;
import com.dangdang.ddframe.rdb.transaction.soft.constants.SoftTransactionType;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * TCC型柔性事务.
 * 
 * @author zhangliang
 */
public final class TCCSoftTransaction extends AbstractSoftTransaction {
    
    /**
     * 开启柔性事务.
     * 
     * @param connection 数据库连接对象
     */
    public void begin(final Connection connection) throws SQLException {
        beginInternal(connection, SoftTransactionType.TryConfirmCancel);
    }
}
