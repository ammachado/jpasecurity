lexer grammar JpqlLexer;

WS : ( ' ' | '\t' | '\f' | EOL ) -> channel(HIDDEN);

fragment A : ('a' | 'A');
fragment B : ('b' | 'B');
fragment C : ('c' | 'C');
fragment D : ('d' | 'D');
fragment E : ('e' | 'E');
fragment F : ('f' | 'F');
fragment G : ('g' | 'G');
fragment H : ('h' | 'H');
fragment I : ('i' | 'I');
fragment J : ('j' | 'J');
fragment K : ('k' | 'K');
fragment L : ('l' | 'L');
fragment M : ('m' | 'M');
fragment N : ('n' | 'N');
fragment O : ('o' | 'O');
fragment P : ('p' | 'P');
fragment Q : ('q' | 'Q');
fragment R : ('r' | 'R');
fragment S : ('s' | 'S');
fragment T : ('t' | 'T');
fragment U : ('u' | 'U');
fragment V : ('v' | 'V');
fragment W : ('w' | 'W');
fragment X : ('x' | 'X');
fragment Y : ('y' | 'Y');
fragment Z : ('z' | 'Z');

fragment
EOL	: [\r\n]+;

INTEGER_LITERAL : INTEGER_NUMBER ;

fragment
INTEGER_NUMBER : ('0' | '1'..'9' '0'..'9'*) ;

LONG_LITERAL : INTEGER_NUMBER L;

BIG_INTEGER_LITERAL : INTEGER_NUMBER ('bi'|'BI');

HEX_LITERAL : '0' ('x'|'X') HEX_DIGIT+ L? ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

OCTAL_LITERAL : '0' ('0'..'7')+ L? ;

FLOAT_LITERAL : FLOATING_POINT_NUMBER F?;

fragment
FLOATING_POINT_NUMBER
	:	('0'..'9')+ '.' ('0'..'9')* EXPONENT?
	|	'.' ('0'..'9')+ EXPONENT?
	|	('0'..'9')+ EXPONENT
	|	('0'..'9')+
	;

DOUBLE_LITERAL : FLOATING_POINT_NUMBER D;

BIG_DECIMAL_LITERAL : FLOATING_POINT_NUMBER ('bd'|'BD');

fragment
EXPONENT
	:	E ('+'|'-')? ('0'..'9')+
	;

ESCAPE_CHARACTER_LITERAL
	:	'\'' ESCAPE_SEQUENCE '\''
	;

CHARACTER_LITERAL
	:	'\'' ( ESCAPE_SEQUENCE | ~('\''|'\\') ) '\''
	;

STRING_LITERAL
	:	'"' ( ESCAPE_SEQUENCE | ~('\\'|'"') )* '"'
	|	('\'' ( ESCAPE_SEQUENCE | ~('\\'|'\'') )* '\'')+
	;

//fragment
ESCAPE_SEQUENCE
	:	'\\' [btnfr"'\\]
	|	UNICODE_ESCAPE
	|	OCTAL_ESCAPE
	;

fragment
OCTAL_ESCAPE
	:	'\\' ('0'..'3') ('0'..'7') ('0'..'7')
	|	'\\' ('0'..'7') ('0'..'7')
	|	'\\' ('0'..'7')
	;

fragment
UNICODE_ESCAPE
	:	'\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
	;

// ESCAPE start tokens
TRUE				: 'true';
FALSE				: 'false';

EQUAL				: '=';
NOT_EQUAL			: '!=' | '^=' | '<>';
GREATER				: '>';
GREATER_EQUAL		: '>=';
LESS				: '<';
LESS_EQUAL			: '<=';

COMMA				: ',';
DOT					: '.';
LEFT_PAREN			: '(';
RIGHT_PAREN			: ')';
LEFT_BRACKET		: '[';
RIGHT_BRACKET		: ']';
LEFT_BRACE			: '{';
RIGHT_BRACE			: '}';
PLUS				: '+';
MINUS				: '-';
ASTERISK			: '*';
SLASH				: '/';
AMPERSAND			: '&';
SEMICOLON			: ';';
COLON				: ':';
PIPE				: '|';
QUESTION_MARK		: '?';
ARROW				: '->';

// Keywords
ABS					: A B S;
AS					: A S;
ALL					: A L L;
AND					: A N D;
ANY					: A N Y;
ASC					: A S C;
AVG					: A V G;
BY					: B Y;
BETWEEN				: B E T W E E N;
BIT_LENGTH			: B I T '_' L E N G T H;
BOTH				: B O T H;
CASE				: C A S E;
CHAR_LENGTH			: C H A R '_' L E N G T H;
CHARACTER_LENGTH	: C H A R A C T E R '_' L E N G T H;
CLASS				: C L A S S;
COALESCE			: C O A L E S C E;
COLLATE				: C O L L A T E;
CONCAT				: C O N C A T;
COUNT				: C O U N T;
CURRENT_DATE		: C U R R E N T '_' D A T E;
CURRENT_TIME		: C U R R E N T '_' T I M E;
CURRENT_TIMESTAMP	: C U R R E N T '_' T I M E S T A M P;
CROSS				: C R O S S;
DELETE				: D E L E T E;
DESC				: D E S C;
DISTINCT			: D I S T I N C T;
ELEMENTS			: E L E M E N T S;
ELSE				: E L S E;
EMPTY				: E M P T Y;
END					: E N D;
ENTRY				: E N T R Y;
ESCAPE				: E S C A P E;
EXISTS				: E X I S T S;
FETCH				: F E T C H;
FROM				: F R O M;
FUNCTION			: F U N C T I O N;
GROUP				: G R O U P;
HAVING				: H A V I N G;
IN					: I N;
INDEX				: I N D E X;
INNER				: I N N E R;
INTO 				: I N T O;
IS					: I S;
JOIN				: J O I N;
KEY					: K E Y;
LEADING				: L E A D I N G;
LEFT				: L E F T;
LENGTH				: L E N G T H;
LIKE				: L I K E;
LIMIT				: L I M I T;
LIST				: L I S T;
LOCATE				: L O C A T E;
LOWER				: L O W E R;
MAP					: M A P;
MAX					: M A X;
MEMBER				: M E M B E R;
MIN					: M I N;
MOD					: M O D;
NEW					: N E W;
NOT					: N O T;
NULL				: N U L L;
NULLIF				: N U L L I F;
OBJECT				: O B J E C T;
OCTET_LENGTH		: O C T E T '_' L E N G T H;
OF					: O F;
OFFSET				: O F F S E T;
ON					: O N;
OR					: O R;
ORDER				: O R D E R;
OUTER				: O U T E R;
POSITION			: P O S I T I O N;
SELECT				: S E L E C T;
SET					: S E T;
SIZE				: S I [zZ] E;
SOME				: S O M E;
SQRT				: S Q R T;
SUBSTRING			: S U B S T R I N G;
SUM					: S U M;
THEN				: T H E N;
TRAILING			: T R A I L I N G;
TREAT				: T R E A T;
TRIM				: T R I M;
TYPE				: T Y P E;
UPDATE				: U P D A T E;
UNKNOWN				: U N K N O W N;
UPPER				: U P P E R;
VALUE				: V A L U E;
WHEN				: W H E N;
WHERE				: W H E R E;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// JPA Security customizations

HINT_START
    : '/*'
    ;

HINT_END
    : '*/'
    ;

ACCESS                  : A C C E S S;
CREATE                  : C R E A T E;
//CURRENT_PRINCIPAL       : C U R R E N T '_' P R I N C I P A L;
//CURRENT_ROLES           : C U R R E N T '_' R O L E S;
//CURRENT_TENANT          : C U R R E N T '_' T E N A N T;
GRANT                   : G R A N T;
IS_ACCESSIBLE_NOCACHE   : I S '_' A C C E S S I B L E '_' N O C A C H E;
IS_ACCESSIBLE_NODB      : I S '_' A C C E S S I B L E '_' N O D B;
QUERY_OPTIMIZE_NOCACHE  : Q U E R Y '_' O P T I M I Z E '_' N O C A C H E;
READ                    : R E A D;
TO                      : T O;

DATE_LITERAL
    : LEFT_BRACKET ('d') (' ' | '\t')+ '\'' DATE_STRING '\'' (' ' | '\t')* RIGHT_BRACKET
    ;

TIME_LITERAL
    : LEFT_BRACKET ('t') (' ' | '\t')+ '\'' TIME_STRING '\'' (' ' | '\t')* RIGHT_BRACKET
    ;

TIMESTAMP_LITERAL
    : LEFT_BRACE ('ts') (' ' | '\t')+ '\'' DATE_STRING ' ' TIME_STRING '\'' (' ' | '\t')* RIGHT_BRACKET
    ;

DATE_STRING
    : '0'..'9' '0'..'9' '0'..'9' '0'..'9' '-' ('0' '1'..'9' | '1' '0'..'2') '-' '0'..'3' '0'..'9'
    ;

TIME_STRING
    : ('0'..'2')? '0'..'9'? COLON '0'..'5' '0'..'9' COLON '0'..'5' '0'..'9' DOT '0'..'9'*
    ;

// End JPA Security customizations
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// Identifiers
IDENTIFIER
	:	('a'..'z'|'A'..'Z'|'_'|'$'|'\u0080'..'\ufffe') ('a'..'z'|'A'..'Z'|'_'|'$'|'0'..'9'|'\u0080'..'\ufffe')*
	;

QUOTED_IDENTIFIER
	:	'`' ( ESCAPE_SEQUENCE | ~('\\'|'`') )* '`'
	;

