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

package io.shardingsphere.transaction.saga.revert;

import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default implement for RevertEngine.
 *
 * @author yangyi
 */
@RequiredArgsConstructor
public final class RevertEngineImpl implements RevertEngine {
    
    private final Map<String, Connection> connectionMap;
    
    @Override
    public RevertResult revert(final String datasource, final String sql, final List<List<Object>> params) throws SQLException {
        RevertResult result = new RevertResult();
        result.setRevertSQL("");
        for (List<Object> each : params) {
            // TODO use new SnapShotEngine to get revert result.
            result.getRevertSQLParams().add(new ArrayList<Object>());
        }
        return result;
    }
    
    @Override
    public RevertResult revert(final String datasource, final String sql, final Object[] params) throws SQLException {
        RevertResult result = new RevertResult();
        result.setRevertSQL(sql);
        return result;
    }
}
