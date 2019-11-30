parser grammar JpqlParser;

options {
	tokenVocab=JpqlLexer;
	contextSuperClass=org.jpasecurity.jpql.BaseContext;
}

@header {
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.invoke.MethodHandles;
}

@members {
	final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected void logUseOfReservedWordAsIdentifier(Token token) {
		LOGGER.error("Use of reserved word as identifier: {}", token.getText());
	}
}

statement
	: ( selectStatement | updateStatement | deleteStatement | accessRule ) EOF
	;

selectStatement
	: querySpec orderByClause?
	;

subQuery
    : querySpec
    ;

deleteStatement
	: DELETE FROM? pathRoot whereClause?
	;

updateStatement
	: UPDATE FROM? pathRoot setClause whereClause?
	;

setClause
	: SET assignment ( COMMA assignment )*
	;

assignment
	: dotIdentifierSequence EQUAL expression
	;

pathRoot
	: entityName identificationVariableDef?
	;

fromClause
	: FROM fromElementSpace ( COMMA fromElementSpace )*
	;

fromElementSpace
	: pathRoot ( jpaCollectionJoin | qualifiedJoin )*
	;

entityName returns [String fullEntityName] @init { $fullEntityName = ""; }
	: (i=identifier { $fullEntityName = _localctx.i.getText(); }) (DOT c=identifier { $fullEntityName += ("." + _localctx.c.getText() ); })*
	;

identificationVariableDef
	: ( AS identificationVariable )
	| IDENTIFIER
	;

jpaCollectionJoin
	: COMMA IN LEFT_PAREN path RIGHT_PAREN identificationVariableDef?
	;

qualifiedJoin
	: op = ( INNER | LEFT | RIGHT )? OUTER? JOIN FETCH? path identificationVariableDef? qualifiedJoinPredicate?
	;

qualifiedJoinPredicate
	: ON predicate
	;

selectClause
	: SELECT hintStatement? DISTINCT? selectionList
	;

hintStatement
    : HINT_START hintValue+ HINT_END
    ;

hintValue
    : IS_ACCESSIBLE_NOCACHE     #NoCacheIsAccessible
    | IS_ACCESSIBLE_NODB        #NoDbIsAccessible
    | QUERY_OPTIMIZE_NOCACHE    #NoCacheQueryOptimize
    ;

selectionList
	: selection ( COMMA selection )*
	;

selection
	: selectExpression identificationVariableDef?
	;

selectExpression
	: constructorExpression
	| jpaSelectObjectSyntax
	| mapEntrySelection
	| expression
	;

mapEntrySelection
	: ENTRY LEFT_PAREN path RIGHT_PAREN
	;

constructorExpression
	: NEW dotIdentifierSequence LEFT_PAREN constructorParameters RIGHT_PAREN
	;

constructorParameters
	: constructionParameter ( COMMA constructionParameter )*
	;

constructionParameter
	: constructionParameterExpression identificationVariableDef?
	;

constructionParameterExpression
	: expression
	| constructorExpression
	;

jpaSelectObjectSyntax
	: OBJECT LEFT_PAREN identifier RIGHT_PAREN
	;

dotIdentifierSequence
	: identifier dotIdentifierSequenceContinuation*
    ;

 dotIdentifierSequenceContinuation
    : DOT identifier
    ;

path
	: syntacticDomainPath pathContinuation?
	| generalPathFragment
	;

pathContinuation
	: DOT dotIdentifierSequence
	;

/**
 * Rule for cases where we syntactically know that the path is a
 * "domain path" because it is one of these special cases:
 *
 * 		* TREAT( path )
 * 		* ELEMENTS( path )
 *		* VALUE( path )
 * 		* KEY( path )
 * 		* path[ selector ]
 */
syntacticDomainPath
	: treatedNavigablePath
	| collectionElementNavigablePath
	| mapKeyNavigablePath
	| dotIdentifierSequence indexedPathAccessFragment
	;

generalPathFragment
	: dotIdentifierSequence indexedPathAccessFragment?
	;

indexedPathAccessFragment
	: LEFT_BRACKET expression RIGHT_BRACKET ( DOT generalPathFragment )?
	;

treatedNavigablePath
	: TREAT LEFT_PAREN path AS dotIdentifierSequence RIGHT_PAREN pathContinuation?
	;

collectionElementNavigablePath
	: op=( VALUE | ELEMENTS ) LEFT_PAREN path RIGHT_PAREN pathContinuation?
	;

mapKeyNavigablePath
	: KEY LEFT_PAREN path RIGHT_PAREN pathContinuation?
	;

groupByClause
	: GROUP BY groupingSpecification
	;

groupingSpecification
	: groupingValue ( COMMA groupingValue )*
	;

groupingValue
	: expression
	;

havingClause
	: HAVING predicate
	;

orderByClause
	: ORDER BY sortSpecification ( COMMA sortSpecification )*
	;

sortSpecification
	: sortExpression orderingSpecification?
	;

sortExpression
	: identifier
	| INTEGER_LITERAL
	| expression
	;

orderingSpecification
	:	ASC
	|	DESC
	;

whereClause
	: WHERE predicate
	;

predicate
	: LEFT_PAREN predicate RIGHT_PAREN								# GroupedPredicate
	| predicate OR predicate										# OrPredicate
	| predicate AND predicate										# AndPredicate
	| NOT predicate													# NegatedPredicate
	| expression NOT? IN inList										# InPredicate
	| expression IS NOT? NULL										# IsNullPredicate
	| expression IS NOT? EMPTY										# IsEmptyPredicate
	| expression EQUAL expression									# EqualityPredicate
	| expression NOT_EQUAL expression								# InequalityPredicate
	| expression GREATER expression									# GreaterThanPredicate
	| expression GREATER_EQUAL expression							# GreaterThanOrEqualPredicate
	| expression LESS expression									# LessThanPredicate
	| expression LESS_EQUAL expression								# LessThanOrEqualPredicate
	| expression NOT? IN inList										# InPredicate
	| expression NOT? BETWEEN expression AND expression				# BetweenPredicate
	| expression NOT? LIKE expression likeEscape?					# LikePredicate
	| MEMBER OF path												# MemberOfPredicate
	| EXISTS LEFT_PAREN querySpec RIGHT_PAREN						# ExistsPredicate
	| entityTypeReference op=( EQUAL | NOT_EQUAL ) entityParam		# TypeEqualityPredicate
	| entityTypeReference NOT? IN LEFT_PAREN entityParam RIGHT_PAREN	# TypeInListPredicate
	| jpaNonStandardFunction comparisonOperators expression			# JpaNonStandardFunctionPredicate
	;

inList
	: ELEMENTS? LEFT_PAREN dotIdentifierSequence RIGHT_PAREN		# PersistentCollectionReferenceInList
	| LEFT_PAREN expression ( COMMA expression )*	RIGHT_PAREN		# ExplicitTupleInList
	| expression													# SubQueryInList
	;

likeEscape
	: ESCAPE expression
	;

expression
	: LEFT_PAREN expression RIGHT_PAREN								# GroupedExpression
    | expression PLUS expression									# AdditionExpression
	| expression MINUS expression									# SubtractionExpression
	| expression ASTERISK expression								# MultiplicationExpression
	| expression SLASH expression									# DivisionExpression
	| expression PERCENT expression									# ModuloExpression
	| MINUS expression												# UnaryMinusExpression
	| PLUS expression												# UnaryPlusExpression
	| caseStatement													# CaseExpression
	| coalesce														# CoalesceExpression
	| nullIf														# NullIfExpression
	| literal														# LiteralExpression
	| parameter														# ParameterExpression
	| entityTypeReference											# EntityTypeExpression
	| path															# PathExpression
	| function														# FunctionExpression
	| LEFT_PAREN subQuery RIGHT_PAREN								# SubQueryExpression
	| op=( ALL | ANY | SOME ) LEFT_PAREN querySpec RIGHT_PAREN		# AllOrAnyExpression
	;

entityTypeReference
	: TYPE LEFT_PAREN entityLiteralReference RIGHT_PAREN
	;

caseStatement
	: simpleCaseStatement
	| searchedCaseStatement
	;

simpleCaseStatement
	: CASE expression simpleCaseWhen+ caseOtherwise? END
	;

simpleCaseWhen
	: WHEN expression THEN expression
	;

caseOtherwise
	: ELSE expression
	;

searchedCaseStatement
	: CASE searchedCaseWhen+ caseOtherwise? END
	;

searchedCaseWhen
	: WHEN predicate THEN expression
	;

coalesce
	: COALESCE LEFT_PAREN expression ( COMMA expression )+ RIGHT_PAREN
	;

nullIf
	: NULLIF LEFT_PAREN expression COMMA expression RIGHT_PAREN
	;

literal
	: STRING_LITERAL												# StringLiteral
	| CHARACTER_LITERAL												# CharacterLiteral
	| INTEGER_LITERAL												# IntegerLiteral
	| LONG_LITERAL													# LongLiteral
	| BIG_INTEGER_LITERAL											# BigIntegerLiteral
	| FLOAT_LITERAL													# FloatLiteral
	| DOUBLE_LITERAL												# DoubleLiteral
	| BIG_DECIMAL_LITERAL											# BigDecimalLiteral
	| HEX_LITERAL													# HexLiteral
	| OCTAL_LITERAL													# OctalLiteral
	| NULL															# NullLiteral
	| TRUE															# TrueLiteral
    | FALSE															# FalseLiteral
	| TIMESTAMP_LITERAL												# TimestampLiteral
	| DATE_LITERAL													# DateLiteral
	| TIME_LITERAL													# TimeLiteral
	;

parameter
	: COLON identifier												# NamedParameter
	| QUESTION_MARK INTEGER_LITERAL?								# PositionalParameter
	;

function
	: standardFunction
	| aggregateFunction
	| jpaCollectionFunction
	| jpaNonStandardFunction
	;

jpaNonStandardFunction
	: FUNCTION LEFT_PAREN jpaNonStandardFunctionName ( COMMA nonStandardFunctionArguments )? RIGHT_PAREN
	;

jpaNonStandardFunctionName
	: STRING_LITERAL
	;

nonStandardFunctionArguments
	: expression ( COMMA expression )*
	;

jpaCollectionFunction
	: SIZE LEFT_PAREN path RIGHT_PAREN					# CollectionSizeFunction
	| INDEX LEFT_PAREN identifier RIGHT_PAREN			# CollectionIndexFunction
	;

aggregateFunction
	: avgFunction
	| sumFunction
	| minFunction
	| maxFunction
	| countFunction
	;

avgFunction
	: AVG LEFT_PAREN DISTINCT? expression RIGHT_PAREN
	;

sumFunction
	: SUM LEFT_PAREN DISTINCT? expression RIGHT_PAREN
	;

minFunction
	: MIN LEFT_PAREN DISTINCT? expression RIGHT_PAREN
	;

maxFunction
	: MAX LEFT_PAREN DISTINCT? expression RIGHT_PAREN
	;

countFunction
	: COUNT LEFT_PAREN DISTINCT? ( expression | ASTERISK ) RIGHT_PAREN
	;

standardFunction
	: concatFunction
	| substringFunction
	| trimFunction
	| upperFunction
	| lowerFunction
	| lengthFunction
	| locateFunction
	| absFunction
	| sqrtFunction
	| modFunction
	| sizeFunction
	| indexFunction
	| currentDateFunction
	| currentTimeFunction
	| currentTimestampFunction
	| positionFunction
	| charLengthFunction
	| octetLengthFunction
	| bitLengthFunction
	;

concatFunction
	: CONCAT LEFT_PAREN expression ( COMMA expression )+ RIGHT_PAREN
	;

substringFunction
	: SUBSTRING LEFT_PAREN expression COMMA substringFunctionStartArgument ( COMMA substringFunctionLengthArgument )? RIGHT_PAREN
	;

substringFunctionStartArgument
	: expression
	;

substringFunctionLengthArgument
	: expression
	;

trimFunction
	: TRIM LEFT_PAREN trimSpecification? trimCharacter? FROM? expression RIGHT_PAREN
	;

trimSpecification
	: LEADING
	| TRAILING
	| BOTH
	;

trimCharacter
	: CHARACTER_LITERAL | STRING_LITERAL
	;

upperFunction
	: UPPER LEFT_PAREN expression RIGHT_PAREN
	;

lowerFunction
	: LOWER LEFT_PAREN expression RIGHT_PAREN
	;

lengthFunction
	: LENGTH LEFT_PAREN expression RIGHT_PAREN
	;

locateFunction
	: LOCATE LEFT_PAREN locateFunctionSubstrArgument COMMA locateFunctionStringArgument ( COMMA locateFunctionStartArgument )? RIGHT_PAREN
	;

locateFunctionSubstrArgument
	: expression
	;

locateFunctionStringArgument
	: expression
	;

locateFunctionStartArgument
	: expression
	;

absFunction
	: ABS LEFT_PAREN expression RIGHT_PAREN
	;

sqrtFunction
	: SQRT LEFT_PAREN expression RIGHT_PAREN
	;

modFunction
	: MOD LEFT_PAREN modDividendArgument COMMA modDivisorArgument RIGHT_PAREN
	;

modDividendArgument
	: expression
	;

modDivisorArgument
	: expression
	;

sizeFunction
	: SIZE LEFT_PAREN path RIGHT_PAREN
	;

indexFunction
	: INDEX LEFT_PAREN identifier RIGHT_PAREN
	;

currentDateFunction
	: CURRENT_DATE
	;

currentTimeFunction
	: CURRENT_TIME
	;

currentTimestampFunction
	: CURRENT_TIMESTAMP
	;

positionFunction
	: POSITION LEFT_PAREN positionSubstrArgument IN positionStringArgument RIGHT_PAREN
	;

positionSubstrArgument
	: expression
	;

positionStringArgument
	: expression
	;

charLengthFunction
	: CHARACTER_LENGTH LEFT_PAREN expression RIGHT_PAREN
	;

octetLengthFunction
	: OCTET_LENGTH LEFT_PAREN expression RIGHT_PAREN
	;

bitLengthFunction
	: BIT_LENGTH LEFT_PAREN expression RIGHT_PAREN
	;

identifier
	: IDENTIFIER
	| (ABS
	| ALL
	| AND
	| ANY
	| AS
	| ASC
	| AVG
	| BY
	| BETWEEN
	| BIT_LENGTH
	| BOTH
	| COALESCE
	| CONCAT
	| COUNT
	| DELETE
	| DESC
	| DISTINCT
	| ELEMENTS
	| ENTRY
	| FROM
	| FUNCTION
	| GROUP
	| IN
	| INDEX
	| INNER
	| JOIN
	| KEY
	| LEADING
	| LEFT
	| LENGTH
	| LIKE
	| LIST
	| LOWER
	| MAP
	| MAX
	| MIN
	| MEMBER
	| OBJECT
	| ON
	| OR
	| ORDER
	| OUTER
	| POSITION
	| RIGHT
	| SELECT
	| SET
	| SQRT
	| SUBSTRING
	| SUM
	| TRAILING
	| TREAT
	| UPDATE
	| UPPER
	| VALUE
	| WHERE) {
	    logUseOfReservedWordAsIdentifier(getCurrentToken());
	}
	;

querySpec
	: selectClause fromClause whereClause? ( groupByClause havingClause? )?
	;

identificationVariable returns [String value] @init { $value = ""; }
	: identifier { $value = _localctx.getText(); }
	;

entityLiteralReference
	: path
	| parameter
	;

entityParam
    : entityName
    | parameter
    ;

entityParamList
    : entityParam ( COMMA entityParam )*
    ;

comparisonOperators
    : EQUAL
    | NOT_EQUAL
    | GREATER
    | GREATER_EQUAL
    | LESS
    | LESS_EQUAL
    ;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// GRANT clause

accessRule
    : GRANT CREATE? READ? UPDATE? DELETE? ACCESS TO pathRoot whereClause?
    ;
