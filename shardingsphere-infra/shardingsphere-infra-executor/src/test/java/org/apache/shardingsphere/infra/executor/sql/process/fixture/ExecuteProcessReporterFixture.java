package org.apache.shardingsphere.infra.executor.sql.process.fixture;

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.spi.ExecuteProcessReporter;

import java.util.Collection;
import java.util.LinkedList;

public class ExecuteProcessReporterFixture implements ExecuteProcessReporter {

    public static final LinkedList<String> ACTIONS = new LinkedList<>();

    @Override
    public void report(LogicSQL logicSQL, ExecutionGroupContext<? extends SQLExecutionUnit> executionGroupContext, ExecuteProcessConstants constants) {
        ACTIONS.add("Report the summary of this task.");
    }

    @Override
    public void report(String executionID, SQLExecutionUnit executionUnit, ExecuteProcessConstants constants) {
        ACTIONS.add("Report a unit of this task.");
    }

    @Override
    public void report(String executionID, ExecuteProcessConstants constants) {
        ACTIONS.add("Report this task on completion.");
    }
}
