import os

# Find file dynamically
target_file = "DorisDMLStatementVisitor.java"
file_path = None

for root, dirs, files in os.walk("."):
    if target_file in files:
        file_path = os.path.join(root, target_file)
        break

if not file_path:
    print(f"❌ Error: Could not find {target_file} in current directory.")
    exit(1)

print(f"Found target file at: {file_path}")

with open(file_path, 'r') as f:
    content = f.read()

# 1. Add import for BitwiseNotFunctionContext if needed
if 'BitwiseNotFunctionContext' not in content:
    content = content.replace(
        'import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.BitwiseFunctionContext;',
        'import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.BitwiseFunctionContext;\nimport org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.BitwiseNotFunctionContext;'
    )

# 2. Wire bitwiseNotFunction into visitSpecialFunction
old_special = 'if (null != ctx.bitwiseFunction()) {\n            return visitBitwiseFunction(ctx.bitwiseFunction());\n        }'
new_special = 'if (null != ctx.bitwiseFunction()) {\n            return visitBitwiseFunction(ctx.bitwiseFunction());\n        }\n        if (null != ctx.bitwiseNotFunction()) {\n            return visitBitwiseNotFunction(ctx.bitwiseNotFunction());\n        }'

if 'bitwiseNotFunction' not in content:
    content = content.replace(old_special, new_special)

    # 3. Add visitor method implementation
    visitor_method = '''
    @Override
    public ASTNode visitBitwiseNotFunction(final BitwiseNotFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.BITNOT().getText(), ctx.getText());
        result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
        return result;
    }
'''
    last_brace_idx = content.rfind('}')
    content = content[:last_brace_idx] + visitor_method + content[last_brace_idx:]

    with open(file_path, 'w') as f:
        f.write(content)
    print("✅ Patched DorisDMLStatementVisitor.java successfully")
else:
    print("Already patched or bitwiseNotFunction exists in Visitor")
