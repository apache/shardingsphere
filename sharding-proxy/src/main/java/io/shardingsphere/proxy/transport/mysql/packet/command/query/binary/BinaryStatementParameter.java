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

package io.shardingsphere.proxy.transport.mysql.packet.command.query.binary;

import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import lombok.Getter;
import lombok.Setter;

/**
 * Binary prepared statement parameter.
 *
 * @author zhangyonglun
 */
@Getter
@Setter
public final class BinaryStatementParameter {
    
    private final BinaryStatementParameterType type;
    
    private Object value;
    
    public BinaryStatementParameter(final ColumnType columnType, final int unsignedFlag) {
        this(columnType, unsignedFlag, null);
    }
    
    public BinaryStatementParameter(final ColumnType columnType, final int unsignedFlag, final String value) {
        type = new BinaryStatementParameterType(columnType, unsignedFlag);
        this.value = value;
    }
}
