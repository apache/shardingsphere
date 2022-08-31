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

package org.apache.shardingsphere.integration.data.pipeline.cases.base;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.data.pipeline.cases.command.ExtraSQLCommand;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.DatabaseTypeUtil;

import javax.xml.bind.JAXB;
import java.sql.SQLException;
import java.util.Objects;

@Slf4j
public abstract class BaseExtraSQLITCase extends BaseITCase {
    
    @Getter
    private final ExtraSQLCommand extraSQLCommand;
    
    public BaseExtraSQLITCase(final ScalingParameterized parameterized) {
        super(parameterized);
        extraSQLCommand = JAXB.unmarshal(Objects.requireNonNull(BaseExtraSQLITCase.class.getClassLoader().getResource(parameterized.getScenario())), ExtraSQLCommand.class);
    }
    
    protected void createSourceOrderTable() throws SQLException {
        sourceExecuteWithLog(extraSQLCommand.getCreateTableOrder());
    }
    
    protected void createSourceTableIndexList(final String schema) throws SQLException {
        if (DatabaseTypeUtil.isPostgreSQL(getDatabaseType())) {
            sourceExecuteWithLog(String.format("CREATE INDEX IF NOT EXISTS idx_user_id ON %s.t_order_copy ( user_id )", schema));
        } else if (DatabaseTypeUtil.isOpenGauss(getDatabaseType())) {
            sourceExecuteWithLog(String.format("CREATE INDEX idx_user_id ON %s.t_order_copy ( user_id )", schema));
        }
    }
    
    protected void createSourceCommentOnList(final String schema) throws SQLException {
        sourceExecuteWithLog(String.format("COMMENT ON COLUMN %s.t_order_copy.user_id IS 'user id'", schema));
    }
    
    protected void createSourceOrderItemTable() throws SQLException {
        sourceExecuteWithLog(extraSQLCommand.getCreateTableOrderItem());
    }
}
