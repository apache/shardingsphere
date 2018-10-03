grammar PostgreDropIndex;

import PostgreKeyword, PostgreBase, BaseRule;

dropIndex
    : DROP INDEX (CONCURRENTLY)? (IF EXISTS)? indexNames
    ;