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

package io.shardingsphere.proxy.transport.mysql.packet.command.query.binary.execute;

import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import lombok.Getter;
import lombok.Setter;

/**
 * Prepared statement parameter.
 *
 * @author zhangyonglun
 */
@Getter
@Setter
public final class PreparedStatementParameter {
    
    private final PreparedStatementParameterHeader preparedStatementParameterHeader;
    
    private Object value;
    
    public PreparedStatementParameter(final ColumnType columnType, final int unsignedFlag) {
        preparedStatementParameterHeader = new PreparedStatementParameterHeader(columnType, unsignedFlag);
    }
    
    public PreparedStatementParameter(final ColumnType columnType, final int unsignedFlag, final String value) {
        preparedStatementParameterHeader = new PreparedStatementParameterHeader(columnType, unsignedFlag);
        this.value = value;
    }
    
    /**
     * Get column type.
     *
     * @return column type
     */
    public ColumnType getColumnType() {
        return preparedStatementParameterHeader.getColumnType();
    }
    
    /**
     * Get unsigned flag.
     *
     * @return unsigned flag
     */
    public int getUnsignedFlag() {
        return preparedStatementParameterHeader.getUnsignedFlag();
    }
}
