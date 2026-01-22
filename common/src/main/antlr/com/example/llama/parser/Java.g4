grammar Java;

options {
    language = Java;
}

// Parser Rules
compilationUnit
    : packageDeclaration? importDeclaration* typeDeclaration* EOF
    ;

packageDeclaration
    : 'package' qualifiedName ';'
    ;

importDeclaration
    : 'import' 'static'? qualifiedName ('.' '*')? ';'
    ;

typeDeclaration
    : classOrInterfaceDeclaration
    | ';'
    ;

classOrInterfaceDeclaration
    : modifiers? (CLASS | INTERFACE) IDENTIFIER typeParameters? (EXTENDS typeList)? (IMPLEMENTS typeList)? classOrInterfaceBody
    ;

classOrInterfaceBody
    : '{' classBodyDeclaration* '}'
    ;

classBodyDeclaration
    : SEMICOLON
    | block
    | memberDeclaration
    ;

memberDeclaration
    : methodDeclaration
    | fieldDeclaration
    | classOrInterfaceDeclaration
    ;

methodDeclaration
    : modifiers? type IDENTIFIER formalParameters (THROWS qualifiedNameList)? methodBody
    ;

methodBody
    : block
    | SEMICOLON
    ;

block
    : '{' blockStatement* '}'
    ;

blockStatement
    : variableDeclarationStatement
    | statement
    ;

variableDeclarationStatement
    : type variableDeclarator (',' variableDeclarator)* ';'
    ;

variableDeclarator
    : IDENTIFIER ('=' expression)?
    ;

statement
    : block
    | 'if' parExpression statement ('else' statement)?
    | 'for' '(' forControl ')' statement
    | 'while' parExpression statement
    | 'do' statement 'while' parExpression ';'
    | 'try' block catchClause* finallyBlock?
    | 'switch' parExpression '{' switchBlockStatementGroup* '}'
    | 'return' expression? ';'
    | 'break' IDENTIFIER? ';'
    | 'continue' IDENTIFIER? ';'
    | expression ';'
    | IDENTIFIER ':' statement
    | SEMICOLON
    ;

forControl
    : forInit? ';' expression? ';' forUpdate?
    ;

forInit
    : variableDeclarationStatement
    | expressionList
    ;

forUpdate
    : expressionList
    ;

parExpression
    : '(' expression ')'
    ;

expressionList
    : expression (',' expression)*
    ;

expression
    : primary                                                                                                                       #PrimaryExpression
    | expression '.' IDENTIFIER                                                                                                     #FieldAccess
    | expression '[' expression ']'                                                                                                 #ArrayAccess
    | expression '(' expressionList? ')'                                                                                            #MethodCall
    | 'new' type ('(' expressionList? ')')? ('{' classBodyDeclaration* '}')?                                                        #NewExpression
    | unaryOperator=('-' | '+' | '!' | '~') expression                                                                              #UnaryExpression
    | expression ('*' | '/' | '%') expression                                                                                       #MultiplicativeExpression
    | expression ('+' | '-') expression                                                                                             #AdditiveExpression
    | expression ('<<' | '>>' | '>>>') expression                                                                                   #ShiftExpression
    | expression ('<=' | '>=' | '<' | '>') expression                                                                               #RelationalExpression
    | expression 'instanceof' type                                                                                                  #InstanceofExpression
    | expression ('==' | '!=') expression                                                                                           #EqualityExpression
    | expression '&' expression                                                                                                     #BitwiseANDExpression
    | expression '^' expression                                                                                                     #BitwiseXORExpression
    | expression '|' expression                                                                                                     #BitwiseORExpression
    | expression '&&' expression                                                                                                    #LogicalANDExpression
    | expression '||' expression                                                                                                    #LogicalORExpression
    | expression '?' expression ':' expression                                                                                      #TernaryExpression
    | expression assignOperator=ASSIGN expression                                                                                   #AssignmentExpression
    ;

primary
    : IDENTIFIER
    | literal
    | parExpression
    ;

literal
    : INTEGER_LITERAL
    | FLOATING_POINT_LITERAL
    | BOOLEAN_LITERAL
    | CHARACTER_LITERAL
    | STRING_LITERAL
    | 'null'
    ;

type
    : primitiveType
    | classOrInterfaceType
    ;

primitiveType
    : 'boolean'
    | 'char'
    | 'byte'
    | 'short'
    | 'int'
    | 'long'
    | 'float'
    | 'double'
    ;

classOrInterfaceType
    : IDENTIFIER typeArguments? ('.' IDENTIFIER typeArguments?)*
    ;

typeArguments
    : '<' typeArgument (',' typeArgument)* '>'
    ;

typeArgument
    : type
    | '?' (EXTENDS type | SUPER type)?
    ;

formalParameters
    : '(' formalParameterList? ')'
    ;

formalParameterList
    : formalParameter (',' formalParameter)*
    ;

formalParameter
    : modifiers? type variableDeclarator
    ;

qualifiedName
    : IDENTIFIER ('.' IDENTIFIER)*
    ;

qualifiedNameList
    : qualifiedName (',' qualifiedName)*
    ;

modifiers
    : (PUBLIC | PROTECTED | PRIVATE | STATIC | ABSTRACT | FINAL | NATIVE | SYNCHRONIZED | TRANSIENT | VOLATILE | STRICTFP)*
    ;

typeParameters
    : '<' typeParameter (',' typeParameter)* '>'
    ;

typeParameter
    : IDENTIFIER (EXTENDS typeBound)?
    ;

typeBound
    : type (BITAND type)*
    ;

typeList
    : type (',' type)*
    ;

fieldDeclaration
    : modifiers? type variableDeclarator (',' variableDeclarator)* ';'
    ;

catchClause
    : 'catch' '(' qualifiedName IDENTIFIER ')' block
    ;

finallyBlock
    : 'finally' block
    ;

switchBlockStatementGroup
    : switchLabel (blockStatement | (switchLabel (blockStatement | switchLabel)*))
    ;

switchLabel
    : 'case' expression ':'
    | 'default' ':'
    ;

// Lexer Rules
IDENTIFIER : LETTER (LETTER | DIGIT)*;
LETTER     : [a-zA-Z$_];
DIGIT      : [0-9];

PUBLIC     : 'public';
PROTECTED  : 'protected';
PRIVATE    : 'private';
STATIC     : 'static';
ABSTRACT   : 'abstract';
FINAL      : 'final';
NATIVE     : 'native';
SYNCHRONIZED : 'synchronized';
TRANSIENT  : 'transient';
VOLATILE   : 'volatile';
STRICTFP   : 'strictfp';
CLASS      : 'class';
INTERFACE  : 'interface';
EXTENDS    : 'extends';
IMPLEMENTS : 'implements';
THROWS     : 'throws';
SUPER      : 'super';

INTEGER_LITERAL    : [0-9]+;
FLOATING_POINT_LITERAL : [0-9]+ '.' [0-9]* | '.' [0-9]+ | [0-9]+;
BOOLEAN_LITERAL    : 'true' | 'false';
CHARACTER_LITERAL  : '\'' (~'\'' | '\\\'') '\'';
STRING_LITERAL     : '"' (~'"' | '\\"')* '"';

WS         : [ \t\r\n]+ ->channel(HIDDEN); // Put whitespace on a hidden channel
LINE_COMMENT : '//' ~[\r\n]* ->channel(HIDDEN); // Put line comments on a hidden channel
BLOCK_COMMENT : '/*' .*? '*/' ->channel(HIDDEN); // Put block comments on a hidden channel
SEMICOLON  : ';';
COLON      : ':';
QUESTION   : '?';
LPAREN     : '('; RPAREN : ')';
LBRACK     : '['; RBRACK : ']';
LCURLY     : '{'; RCURLY : '}';
COMMA      : ',';
DOT        : '.';
ASSIGN     : '=';
LT         : '<'; GT         : '>';
LE         : '<='; GE         : '>=';
EQ         : '=='; NE         : '!=';
AND        : '&&'; OR         : '||';
NOT        : '!'; TILDE      : '~';
BITAND     : '&'; BITOR      : '|';
CARET      : '^';
PLUS       : '+'; MINUS      : '-';
STAR       : '*'; DIV        : '/';
MOD        : '%';


