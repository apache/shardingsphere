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

package org.apache.shardingsphere.infra.datasource.pool.destroyer;

import org.apache.shardingsphere.spi.required.RequiredSPI;
import org.apache.shardingsphere.spi.typed.TypedSPI;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Data source pool destroyer.
 */
public interface DataSourcePoolDestroyer extends TypedSPI, RequiredSPI {
    
    /**
     * destroy data source pool gracefully.
     * 
     * @param dataSource data source pool to be destroyed
     * @throws SQLException SQL exception
     */
    void destroy(DataSource dataSource) throws SQLException;
}
