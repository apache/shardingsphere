package org.apache.shardingsphere.infra.optimize;

import lombok.Getter;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;

import java.util.List;

@Getter
public class ExecStmt {
    
    private final boolean success;
    
    private final SqlNode sqlNode;
    
    private final RelNode physicalPlan;
    
    private final List<ColumnMetaData> resultColumns;
    
    public ExecStmt() {
        this.success = false;
        this.sqlNode = null;
        this.physicalPlan = null;
        this.resultColumns = null;
    }
    
    public ExecStmt(final SqlNode sqlNode, final RelNode physicalPlan, final List<ColumnMetaData> resultColumns) {
        this.success = true;
        this.sqlNode = sqlNode;
        this.physicalPlan = physicalPlan;
        this.resultColumns = resultColumns;
    }
}
