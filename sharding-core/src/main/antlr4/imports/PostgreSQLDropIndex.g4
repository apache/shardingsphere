grammar PostgreDropIndex;

import PostgreKeyword, Keyword, PostgreBase, BaseRule;

dropIndex
    : DROP INDEX (CONCURRENTLY)? (IF EXISTS)? indexNames
    ;