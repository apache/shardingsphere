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

package io.shardingsphere.shardingproxy.backend.netty.client.response;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.shardingproxy.backend.netty.client.response.mysql.MySQLResponseHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Response handler factory for using netty connect backend.
 *
 * @author wangkai
 * @author linjiaqi
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseHandlerFactory {
    /**
     * Create new instance of response handler factory for using netty connect backend.
     *
     * @param databaseType database type
     * @param dataSourceName data source name
     * @param schema schema
     * @return new instance of response handler factory for using netty connect backend
     */
    public static ResponseHandler newInstance(final DatabaseType databaseType, final String dataSourceName, final String schema) {
        switch (databaseType) {
            case MySQL:
                return new MySQLResponseHandler(dataSourceName, schema);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database type '%s'", databaseType));
        }
    }
}
