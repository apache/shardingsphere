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

package org.apache.shardingsphere.core.parse.sql.statement.dml;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.core.parse.sql.context.insertvalue.InsertValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert statement.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@Getter
@ToString(callSuper = true)
public final class InsertStatement extends DMLStatement {
    
    private final Collection<String> columnNames = new LinkedList<>();
    
    private final List<InsertValue> values = new LinkedList<>();
    
    @Setter
    private boolean isNeededToAppendGeneratedKey;
    
    @Setter
    private boolean isNeededToAppendAssistedColumns;
    
    /**
     * Is needed to append columns.
     * 
     * @return append columns or not
     */
    public boolean isNeededToAppendColumns() {
        return isNeededToAppendGeneratedKey || isNeededToAppendAssistedColumns;
    }
}
