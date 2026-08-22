import os

# 1. Patch DALStatement.g4
dal_file = 'parser/sql/engine/dialect/doris/src/main/antlr4/imports/doris/DALStatement.g4'
with open(dal_file, 'r') as f:
    content = f.read()

# Fix adminCopyTablet
old_rule = '''adminCopyTablet\n    : ADMIN COPY TABLET NUMBER_ propertiesClause?\n    ;'''
new_rule = '''adminCopyTablet\n    : ADMIN COPY TABLET (NUMBER_ | identifier) propertiesClause?\n    ;'''
if old_rule in content:
    content = content.replace(old_rule, new_rule)
    print("✅ Fixed adminCopyTablet rule")

# Fix property (to allow a single string for PROPERTIES("xxx"))
old_prop = '''property\n    : STRING_ (EQ_ | colon=COLON_) (STRING_ | NUMBER_ | IDENTIFIER_)\n    ;'''
# (If we don't know the exact old property rule, we can try to replace it robustly, but for now we'll just rewrite it)
# We will inject a modified property rule if we can find it.
if "property\n    :" in content:
    import re
    # A generic regex replace for the property rule to allow a single STRING_ or a key-value
    content = re.sub(r'property\n\s+:.*?\n\s+;', 'property\n    : STRING_ ((EQ_ | COLON_) (STRING_ | NUMBER_ | IDENTIFIER_))?\n    ;', content, flags=re.DOTALL)
    print("✅ Fixed property rule for single string properties")

with open(dal_file, 'w') as f:
    f.write(content)


# 2. Add SQL Cases to select-special-function.xml
sql_file = 'test/it/parser/src/main/resources/sql/supported/dml/select-special-function.xml'
with open(sql_file, 'r') as f:
    sql_content = f.read()

new_sqls = """
    <sql-case id="select_bitor" value="SELECT BITOR(3,5)" db-types="Doris" />
    <sql-case id="select_bitand" value="SELECT BITAND(3,5)" db-types="Doris" />
    <sql-case id="select_bitnot" value="SELECT BITNOT(3)" db-types="Doris" />
    <sql-case id="select_substr" value="SELECT SUBSTR('Hello', 1, 3)" db-types="Doris" />"""

if 'id="select_bitor"' not in sql_content:
    sql_content = sql_content.replace('<sql-case id="select_bitxor" value="SELECT BITXOR(3,5)" db-types="Doris" />', 
                                      '<sql-case id="select_bitxor" value="SELECT BITXOR(3,5)" db-types="Doris" />' + new_sqls)
    with open(sql_file, 'w') as f:
        f.write(sql_content)
    print("✅ Added SQL cases for BITOR, BITAND, BITNOT, SUBSTR")


# 3. Add AST Assertions
case_file = 'test/it/parser/src/main/resources/case/dml/select-special-function.xml'
with open(case_file, 'r') as f:
    case_content = f.read()

new_cases = """
    <select sql-case-id="select_bitor" db-types="Doris">
        <projections start-index="7" stop-index="16">
            <expression-projection text="BITOR(3,5)" start-index="7" stop-index="16">
                <expr>
                    <function function-name="BITOR" start-index="7" stop-index="16" text="BITOR(3,5)">
                        <parameter>
                            <literal-expression value="3" start-index="13" stop-index="13" />
                        </parameter>
                        <parameter>
                            <literal-expression value="5" start-index="15" stop-index="15" />
                        </parameter>
                    </function>
                </expr>
            </expression-projection>
        </projections>
    </select>

    <select sql-case-id="select_bitand" db-types="Doris">
        <projections start-index="7" stop-index="17">
            <expression-projection text="BITAND(3,5)" start-index="7" stop-index="17">
                <expr>
                    <function function-name="BITAND" start-index="7" stop-index="17" text="BITAND(3,5)">
                        <parameter>
                            <literal-expression value="3" start-index="14" stop-index="14" />
                        </parameter>
                        <parameter>
                            <literal-expression value="5" start-index="16" stop-index="16" />
                        </parameter>
                    </function>
                </expr>
            </expression-projection>
        </projections>
    </select>

    <select sql-case-id="select_bitnot" db-types="Doris">
        <projections start-index="7" stop-index="15">
            <expression-projection text="BITNOT(3)" start-index="7" stop-index="15">
                <expr>
                    <function function-name="BITNOT" start-index="7" stop-index="15" text="BITNOT(3)">
                        <parameter>
                            <literal-expression value="3" start-index="14" stop-index="14" />
                        </parameter>
                    </function>
                </expr>
            </expression-projection>
        </projections>
    </select>

    <select sql-case-id="select_substr" db-types="Doris">
        <projections start-index="7" stop-index="27">
            <expression-projection text="SUBSTR('Hello', 1, 3)" start-index="7" stop-index="27">
                <expr>
                    <function function-name="SUBSTR" start-index="7" stop-index="27" text="SUBSTR('Hello', 1, 3)">
                        <parameter>
                            <literal-expression value="Hello" start-index="14" stop-index="20" />
                        </parameter>
                        <parameter>
                            <literal-expression value="1" start-index="23" stop-index="23" />
                        </parameter>
                        <parameter>
                            <literal-expression value="3" start-index="26" stop-index="26" />
                        </parameter>
                    </function>
                </expr>
            </expression-projection>
        </projections>
    </select>"""

if 'sql-case-id="select_bitor"' not in case_content:
    case_content = case_content.replace('</select>\n\n    <select sql-case-id="select_bitxor"', 
                                        '</select>\n' + new_cases + '\n\n    <select sql-case-id="select_bitxor"')
    with open(case_file, 'w') as f:
        f.write(case_content)
    print("✅ Added AST assertions for BITOR, BITAND, BITNOT, SUBSTR")
