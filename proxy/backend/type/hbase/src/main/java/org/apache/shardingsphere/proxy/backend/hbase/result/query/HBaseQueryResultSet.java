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

package org.apache.shardingsphere.proxy.backend.hbase.result.query;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;
import java.util.Collection;

/**
 * Query result set for HBase.
 */
public interface HBaseQueryResultSet extends TypedSPI {
    
    /**
     * Initialize data.
     *
     * @param sqlStatementContext SQL statement context
     */
    void init(SQLStatementContext sqlStatementContext);
    
    /**
     * Get result set column names.
     *
     * @return result set column names
     */
    Collection<String> getColumnNames();
    
    /**
     * Go to next data.
     *
     * @return true if next data exist
     */
    boolean next();
    
    /**
     * Get row data.
     *
     * @return row data
     */
    Collection<Object> getRowData();
}
