import os

# Fix 1: Add BITOR keyword to DorisKeyword.g4
keyword_file = 'parser/sql/engine/dialect/doris/src/main/antlr4/imports/doris/DorisKeyword.g4'
with open(keyword_file, 'r') as f:
    content = f.read()

old_bitxor = '''BITXOR
    : B I T X O R
    ;'''

new_bitxor = '''BITXOR
    : B I T X O R
    ;
BITOR
    : B I T O R
    ;'''

if 'BITOR' not in content:
    content = content.replace(old_bitxor, new_bitxor)
    with open(keyword_file, 'w') as f:
        f.write(content)
    print(f"✅ Added BITOR to {keyword_file}")
else:
    print(f"⚠️ BITOR already exists in {keyword_file}")

# Fix 2: Add BITOR to bitwiseBinaryFunctionName in BaseRule.g4
baserule_file = 'parser/sql/engine/dialect/doris/src/main/antlr4/imports/doris/BaseRule.g4'
with open(baserule_file, 'r') as f:
    content = f.read()

old_rule = '''bitwiseBinaryFunctionName
    : BITXOR
    ;'''

new_rule = '''bitwiseBinaryFunctionName
    : BITXOR
    | BITOR
    ;'''

if 'BITOR' not in content:
    content = content.replace(old_rule, new_rule)
    with open(baserule_file, 'w') as f:
        f.write(content)
    print(f"✅ Added BITOR to {baserule_file}")
else:
    print(f"⚠️ BITOR already exists in {baserule_file}")
