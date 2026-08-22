import re

# 1. Remove BITNOT from DorisKeyword.g4
keyword_file = 'parser/sql/engine/dialect/doris/src/main/antlr4/imports/doris/DorisKeyword.g4'
with open(keyword_file, 'r') as f:
    content = f.read()
old = '''BITNOT
    : B I T N O T
    ;
'''
if old in content:
    content = content.replace(old, '')
    with open(keyword_file, 'w') as f:
        f.write(content)
    print("✅ Removed BITNOT from DorisKeyword.g4")

# 2. Remove bitwiseNotFunction from BaseRule.g4
baserule_file = 'parser/sql/engine/dialect/doris/src/main/antlr4/imports/doris/BaseRule.g4'
with open(baserule_file, 'r') as f:
    content = f.read()

old_rule = '''

bitwiseNotFunction
    : BITNOT LP_ expr RP_
    ;'''
if old_rule in content:
    content = content.replace(old_rule, '')

old_ref = '''    | bitwiseFunction
    | bitwiseNotFunction
    // DORIS ADDED END'''
new_ref = '''    | bitwiseFunction
    // DORIS ADDED END'''
if old_ref in content:
    content = content.replace(old_ref, new_ref)

with open(baserule_file, 'w') as f:
    f.write(content)
print("✅ Removed bitwiseNotFunction from BaseRule.g4")

# 3. Remove BITNOT test from supported SQL cases
supported_file = 'test/it/parser/src/main/resources/sql/supported/dml/select-special-function.xml'
with open(supported_file, 'r') as f:
    content = f.read()
old_test = '    <sql-case id="select_bitnot" value="SELECT BITNOT(3)" db-types="Doris" />\n'
if old_test in content:
    content = content.replace(old_test, '')
    with open(supported_file, 'w') as f:
        f.write(content)
    print("✅ Removed select_bitnot from supported")

# 4. Remove BITNOT test from case assertions
case_file = 'test/it/parser/src/main/resources/case/dml/select-special-function.xml'
with open(case_file, 'r') as f:
    content = f.read()
pattern = r'    <select sql-case-id="select_bitnot" db-types="Doris">.*?</select>\n'
if re.search(pattern, content, re.DOTALL):
    content = re.sub(pattern, '', content, flags=re.DOTALL)
    with open(case_file, 'w') as f:
        f.write(content)
    print("✅ Removed select_bitnot from case assertions")

# 5. Revert visitor patch if it exists
import os
target_file = "DorisDMLStatementVisitor.java"
file_path = None
for root, dirs, files in os.walk("."):
    if target_file in files:
        file_path = os.path.join(root, target_file)
        break

if file_path:
    with open(file_path, 'r') as f:
        content = f.read()
    if 'BitwiseNotFunctionContext' in content:
        # Remove the import
        content = content.replace(
            'import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.BitwiseNotFunctionContext;\n',
            ''
        )
        # Remove the visit method and its wiring
        content = content.replace(
            'if (null != ctx.bitwiseNotFunction()) {\n            return visitBitwiseNotFunction(ctx.bitwiseNotFunction());\n        }\n        ',
            ''
        )
        # Remove the method implementation
        method_pattern = r'    @Override\n    public ASTNode visitBitwiseNotFunction\(final BitwiseNotFunctionContext ctx\) \{.*?\n    \}\n'
        content = re.sub(method_pattern, '', content, flags=re.DOTALL)
        with open(file_path, 'w') as f:
            f.write(content)
        print("✅ Reverted visitor patch")
