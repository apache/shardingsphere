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

public final class RexEvaluatorConverter extends RexVisitorImpl<Evaluator> {
    
    private final ExecContext execContext;
    
    private final RexProgram program;
    
    private RexEvaluatorConverter(final RexProgram program, final ExecContext execContext) {
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
     * {@link org.apache.calcite.rex.RexLocalRef}.
     * @param expr <code>RexNode</code> instance
     * @return <code>RexNode</code> from expr
     */
    public RexNode deref(final RexNode expr) {
        if (expr instanceof RexLocalRef) {
            RexLocalRef ref = (RexLocalRef) expr;
            final RexNode e2 = program.getExprList().get(ref.getIndex());
            assert ref.getType().equals(e2.getType());
            return e2;
        } else {
            return expr;
        }
    }
    
    /**
     * translate condition RexNode.
     * @param rexNode condition
     * @param execContext execution context
     * @return <code>Evaluator</code> instance that been translated.
     */
    public static Evaluator translateCondition(final RexNode rexNode, final ExecContext execContext) {
        // TODO mock the program parameter
        return rexNode.accept(new RexEvaluatorConverter(null, execContext));
    }
    
    /**
     * translate condition from <code>RexProgram</code>.
     * @param program program
     * @param execContext execution context
     * @return <code>Evaluator</code> instance
     */
    public static Evaluator translateCondition(final RexProgram program, final ExecContext execContext) {
        if (program.getCondition() == null) {
            return null;
        }
        RexEvaluatorConverter converter = create(program, execContext);
        return program.getCondition().accept(converter);
    }
    
    /**
     * Untility function for creating {@link RexEvaluatorConverter}.
     * @param program program
     * @param execContext execution context
     * @return instance that convert <code>RexNode</code> to <code>Evaluator</code>.
     */
    public static RexEvaluatorConverter create(final RexProgram program, final ExecContext execContext) {
        return new RexEvaluatorConverter(program, execContext);
    }
    
    /**
     * translate projects from <code>RexProgram</code>.
     * @param program program
     * @param execContext execution context
     * @return <code>Evaluator</code> instance collection
     */
    public static List<Evaluator> translateProjects(final RexProgram program, final ExecContext execContext) {
        if (program.getProjectList() == null) {
            return Collections.emptyList();
        }
        RexEvaluatorConverter converter = create(program, execContext);
        return converter.visitList(program.getProjectList());
    }
}
