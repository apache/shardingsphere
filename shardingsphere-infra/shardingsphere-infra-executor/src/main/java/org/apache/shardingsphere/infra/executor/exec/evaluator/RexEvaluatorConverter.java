package org.apache.shardingsphere.infra.executor.exec.evaluator;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexLocalRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexProgram;
import org.apache.calcite.rex.RexVisitorImpl;
import org.apache.calcite.sql.SqlOperator;
import org.apache.shardingsphere.infra.executor.exec.ExecContext;
import org.apache.shardingsphere.infra.executor.exec.func.BuiltinFunction;
import org.apache.shardingsphere.infra.executor.exec.func.BuiltinFunctionTable;
import org.apache.shardingsphere.infra.executor.exec.func.implementor.RexCallToFunctionImplementor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RexEvaluatorConverter extends RexVisitorImpl<Evaluator> {
    
    private final ExecContext execContext;
    
    private final RexProgram program;
    
    private RexEvaluatorConverter(RexProgram program, ExecContext execContext) {
        super(true);
        this.program = program;
        this.execContext = execContext;
    }
    
    @Override
    public Evaluator visitInputRef(final RexInputRef inputRef) {
        return new ColumnEvaluator(inputRef.getIndex(), inputRef.getType());
    }
    
    @Override
    public Evaluator visitLiteral(final RexLiteral literal) {
        return new ConstantEvaluator(literal.getValue4(), literal.getType());
    }
    
    @Override
    public Evaluator visitCall(final RexCall call) {
        List<Evaluator> evaluators = visitList(call.getOperands());
        SqlOperator sqlOperator = call.getOperator();
        RexCallToFunctionImplementor implementor = BuiltinFunctionTable.INSTANCE.get(sqlOperator);
        List<RelDataType> retTypes = evaluators.stream().map(Evaluator::getRetType).collect(Collectors.toList());
        RelDataType[] argTypes = retTypes.toArray(new RelDataType[retTypes.size()]);
        BuiltinFunction builtinFunction = implementor.implement(call, argTypes);
        return new ScalarEvaluator(evaluators.toArray(new Evaluator[evaluators.size()]), builtinFunction, call.getType());
    }
    
    @Override
    public Evaluator visitLocalRef(final RexLocalRef localRef) {
        return deref(localRef).accept(this);
    }
    
    /** Dereferences an expression if it is a
     * {@link org.apache.calcite.rex.RexLocalRef}. */
    public RexNode deref(RexNode expr) {
        if (expr instanceof RexLocalRef) {
            RexLocalRef ref = (RexLocalRef) expr;
            final RexNode e2 = program.getExprList().get(ref.getIndex());
            assert ref.getType().equals(e2.getType());
            return e2;
        } else {
            return expr;
        }
    }
    
    public static Evaluator translateCondition(RexNode rexNode, ExecContext execContext) {
        // TODO 
        return rexNode.accept(new RexEvaluatorConverter(null, execContext));
    }
    
    public static RexEvaluatorConverter create(RexProgram program, ExecContext execContext) {
        return new RexEvaluatorConverter(program, execContext);
    }
    
    public static Evaluator translateCondition(RexProgram program, ExecContext execContext) {
        if(program.getCondition() == null) {
            return null;
        }
        RexEvaluatorConverter converter = create(program, execContext);
        return program.getCondition().accept(converter);
    }
    
    public static List<Evaluator> translateProjects(RexProgram program, ExecContext execContext) {
        if(program.getProjectList() == null) {
            return Collections.emptyList();
        }
        RexEvaluatorConverter converter = create(program, execContext);
        return converter.visitList(program.getProjectList());
    }
}
