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

package org.apache.shardingsphere.infra.util.close;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Data sources closer.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourcesCloser {
    
    /**
     * Close data sources.
     *
     * @param dataSources data sources
     */
    public static void close(final Collection<DataSource> dataSources) {
        Collection<Exception> causes = new LinkedList<>();
        for (DataSource each : dataSources) {
            if (each instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) each).close();
                    // CHECKSTYLE:OFF
                } catch (final Exception ex) {
                    // CHECKSTYLE:ON
                    causes.add(ex);
                }
            }
        }
        if (!causes.isEmpty()) {
            throwException(causes);
        }
    }
    
    private static void throwException(final Collection<Exception> causes) {
        SQLException sqlException = new SQLException("");
        for (Exception each : causes) {
            sqlException.setNextException(new SQLException(each));
        }
        throw new SQLWrapperException(sqlException);
    }
}
