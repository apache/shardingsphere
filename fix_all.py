import re

# Fix 1: Add BITAND and BITNOT to DorisKeyword.g4
keyword_file = 'parser/sql/engine/dialect/doris/src/main/antlr4/imports/doris/DorisKeyword.g4'
with open(keyword_file, 'r') as f:
    content = f.read()

old = '''BITOR
    : B I T O R
    ;'''
new = '''BITOR
    : B I T O R
    ;
BITAND
    : B I T A N D
    ;
BITNOT
    : B I T N O T
    ;'''

if 'BITAND' not in content:
    content = content.replace(old, new)
    with open(keyword_file, 'w') as f:
        f.write(content)
    print("✅ Added BITAND, BITNOT to DorisKeyword.g4")

# Fix 2: Add BITAND to bitwiseBinaryFunctionName, add BITNOT as unary
baserule_file = 'parser/sql/engine/dialect/doris/src/main/antlr4/imports/doris/BaseRule.g4'
with open(baserule_file, 'r') as f:
    content = f.read()

# Add BITAND to binary list
old_bin = '''bitwiseBinaryFunctionName
    : BITXOR
    | BITOR
    ;'''
new_bin = '''bitwiseBinaryFunctionName
    : BITXOR
    | BITOR
    | BITAND
    ;'''

if 'BITAND' not in content:
    content = content.replace(old_bin, new_bin)
    
    # Add BITNOT unary function after bitwiseFunction block
    old_end = '''bitwiseFunction
    : bitwiseBinaryFunctionName LP_ expr COMMA_ expr RP_
    ;
// DORIS ADDED END'''
    new_end = '''bitwiseFunction
    : bitwiseBinaryFunctionName LP_ expr COMMA_ expr RP_
    ;

bitwiseNotFunction
    : BITNOT LP_ expr RP_
    ;
// DORIS ADDED END'''
    
    content = content.replace(old_end, new_end)
    
    # Wire bitwiseNotFunction into specialFunction
    old_special = '''    | bitwiseFunction
    // DORIS ADDED END'''
    new_special = '''    | bitwiseFunction
    | bitwiseNotFunction
    // DORIS ADDED END'''
    
    content = content.replace(old_special, new_special)
    
    with open(baserule_file, 'w') as f:
        f.write(content)
    print("✅ Added BITAND, BITNOT to BaseRule.g4")

# Fix 3: Rename duplicate select_substr in supported
supported_file = 'test/it/parser/src/main/resources/sql/supported/dml/select-special-function.xml'
with open(supported_file, 'r') as f:
    content = f.read()

# Only rename the Doris one we added (line 297)
old = '<sql-case id="select_substr" value="SELECT SUBSTR(\'Hello\', 1, 3)" db-types="Doris" />'
new = '<sql-case id="select_substr_doris_comma" value="SELECT SUBSTR(\'Hello\', 1, 3)" db-types="Doris" />'

if old in content:
    content = content.replace(old, new)
    with open(supported_file, 'w') as f:
        f.write(content)
    print("✅ Renamed duplicate select_substr in supported")

# Fix 4: Rename in case assertions
case_file = 'test/it/parser/src/main/resources/case/dml/select-special-function.xml'
with open(case_file, 'r') as f:
    content = f.read()

# Find the one near BITOR and rename it
if 'sql-case-id="select_substr" db-types="Doris"' in content:
    # Replace only the first occurrence of the Doris one (the one we added)
    content = content.replace('sql-case-id="select_substr" db-types="Doris">', 'sql-case-id="select_substr_doris_comma" db-types="Doris">', 1)
    with open(case_file, 'w') as f:
        f.write(content)
    print("✅ Renamed duplicate select_substr in case")

