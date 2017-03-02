/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
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
 */
package com.alibaba.druid.sql.dialect.postgresql.ast.stmt;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.dialect.postgresql.ast.PGWithClause;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PGSelectQueryBlock extends SQLSelectQueryBlock {
    
    private PGWithClause with;
    
    private PGLimit limit;
    
    private WindowClause window;
    
    private SQLOrderBy orderBy;
    
    private IntoOption intoOption;
    
    private final List<SQLExpr> distinctOn = new ArrayList<>(2);

    public enum IntoOption {
        TEMPORARY, TEMP, UNLOGGED
    }
    
    public SQLExpr getOffset() {
        return null == limit ? null : limit.offset;
    }
    
    public void setOffset(final SQLExpr offset) {
        if (null == limit) {
            limit = new PGLimit();
            limit.setParent(this);
        }
        limit.setOffset(offset);
    }
    
    @Getter
    @Setter
    public static class WindowClause extends SQLObjectImpl {
        
        private SQLExpr name;
        
        private List<SQLExpr> definition = new ArrayList<>(2);
    }
    
    @Getter
    public static class PGLimit extends SQLObjectImpl implements SQLExpr {
        
        private SQLExpr rowCount;
        
        private SQLExpr offset;
        
        public void setRowCount(final SQLExpr rowCount) {
            if (null != rowCount) {
                rowCount.setParent(this);
            }
            this.rowCount = rowCount;
        }
        
        public void setOffset(final SQLExpr offset) {
            if (null != offset) {
                offset.setParent(this);
            }
            this.offset = offset;
        }
        
    }
}
