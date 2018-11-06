grammar OracleDCLStatement;

import OracleKeyword, Keyword, OracleBase, BaseRule, DataType, Symbol;

grant
    : GRANT
    (
    	(grantSystemPrivileges | grantObjectPrivileges) (CONTAINER EQ_OR_ASSIGN (CURRENT | ALL))?
        | grantRolesToPrograms
    )
    ;
    
grantSystemPrivileges
    : systemObjects TO (granteeClause | granteeIdentifiedBy) (WITH (ADMIN | DELEGATE) OPTION)?
    ; 
    
systemObjects
    : systemObject(COMMA systemObject)*
    ;
          
systemObject
    : ALL PRIVILEGES
    | roleName
    | systemPrivilege
    ;
        
systemPrivilege
    : ID *?
    ;


granteeClause
    : grantee (COMMA grantee)
    ;
    
grantee
    : userName 
    | roleName 
    | PUBLIC 
    ;
    
granteeIdentifiedBy
    : userName (COMMA userName)* IDENTIFIED BY STRING (COMMA STRING)*
    ;
    
grantObjectPrivileges
    : objectPrivilege (COMMA objectPrivilege)*
    ;
    
grantObjectPrivilege
    : (objectPrivilege | ALL PRIVILEGES?)( LEFT_PAREN columnName (COMMA columnName)* RIGHT_PAREN)?
    ;

objectPrivilege
    : ID *?
    ;
    
grantRolesToPrograms
    : roleName (COMMA roleName)* TO programUnit ( COMMA programUnit )*
    ;
    
programUnit
    : (FUNCTION | PROCEDURE | PACKAGE) schemaName? ID
    ;