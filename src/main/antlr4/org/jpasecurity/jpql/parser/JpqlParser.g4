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

updateStatement
	: UPDATE FROM? mainEntityPersisterReference setClause whereClause?
	;

setClause
	: SET assignment ( COMMA assignment )*
	;

assignment
	: dotIdentifierSequence EQUAL expression
	;

deleteStatement
	: DELETE FROM? mainEntityPersisterReference whereClause?
	;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// ORDER BY clause

orderByClause
	: ORDER BY sortSpecification ( COMMA sortSpecification )*
	;

sortSpecification
	: expression orderingSpecification?
	;

orderingSpecification
	:	ASC
	|	DESC
	;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// QUERY SPEC - general structure of root query or sub query

querySpec
	: selectClause? fromClause whereClause? groupByClause? havingClause?
	;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// SELECT clause

selectClause
	: SELECT hintStatement? DISTINCT? selectionList
	;

selectionList
	: selection ( COMMA selection )*
	;

selection
	// I have noticed that without this predicate, Antlr will sometimes
	// interpret `select a.b from Something ...` as `from` being the
	// select-expression alias
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

dotIdentifierSequence
	: identifier ( DOT identifier )*
	;

simplePathQualifier
    : dotIdentifierSequence
    ;

path
	// a SimplePath may be any number of things like:
	//		* Class FQN
	//		* Java constant (enum/static)
	//		* an identification variable
	//		* an unqualified attribute name
	: simplePathQualifier																	# SimplePath
	// a Map.Entry cannot be further dereferenced
	| ENTRY LEFT_PAREN mapReference RIGHT_PAREN												# MapEntryPath
	// only one index-access is allowed per path
	| path LEFT_BRACKET expression RIGHT_BRACKET pathTerminal?								# IndexedPath
	// most path expressions fall into this bucket
	| pathRoot pathTerminal?																# CompoundPath
	;

pathRoot
	: identifier																			# SimplePathRoot
	| TREAT LEFT_PAREN dotIdentifierSequence AS dotIdentifierSequence RIGHT_PAREN			# TreatedPathRoot
	| KEY LEFT_PAREN mapReference RIGHT_PAREN												# MapKeyPathRoot
	| VALUE LEFT_PAREN collectionReference RIGHT_PAREN										# CollectionValuePathRoot
	;

pathTerminal
	: ( DOT identifier )+
	;

// having as a separate rule allows us to validate that the path indeed resolves to a Collection attribute
collectionReference
	: path
	;

// having as a separate rule allows us to validate that the path indeed resolves to a Map attribute
mapReference
	: path
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

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// FROM clause

fromClause
	: FROM fromElementSpace ( COMMA fromElementSpace )*
	;

fromElementSpace
	: fromElementSpaceRoot ( jpaCollectionJoin | qualifiedJoin )*
	;

fromElementSpaceRoot
	: mainEntityPersisterReference
	;

mainEntityPersisterReference
	: simplePathQualifier identificationVariableDef?
	;

identificationVariableDef
	: (AS identificationVariable)
	| IDENTIFIER
	;

identificationVariable returns [String value] @init { $value = ""; }
	: identifier { $value = _localctx.getText(); }
	;

jpaCollectionJoin
	: COMMA IN LEFT_PAREN path RIGHT_PAREN identificationVariableDef?
	;

qualifiedJoin
	: ( INNER | ( LEFT OUTER? ) )? JOIN FETCH? path identificationVariableDef? qualifiedJoinPredicate?
	;

qualifiedJoinPredicate
	: ON predicate
	;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// GROUP BY clause

groupByClause
	: GROUP BY groupingSpecification
	;

groupingSpecification
	: groupingValue ( COMMA groupingValue )*
	;

groupingValue
	: expression
	;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//HAVING clause

havingClause
	: HAVING predicate
	;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// WHERE clause

whereClause
	: WHERE predicate
	;

predicate
	: LEFT_PAREN predicate RIGHT_PAREN														# GroupedPredicate
	| predicate OR predicate																# OrPredicate
	| predicate AND predicate																# AndPredicate
	| NOT predicate																			# NegatedPredicate
	| expression NOT? IN inList																# InPredicate
	| expression IS NOT? NULL																# IsNullPredicate
	| expression IS NOT? EMPTY																# IsEmptyPredicate
	| expression EQUAL expression															# EqualityPredicate
	| expression GREATER expression															# GreaterThanPredicate
	| expression GREATER_EQUAL expression													# GreaterThanOrEqualPredicate
	| expression LESS expression															# LessThanPredicate
	| expression LESS_EQUAL expression														# LessThanOrEqualPredicate
	| expression NOT_EQUAL expression														# InequalityPredicate
	| expression NOT? BETWEEN expression AND expression										# BetweenPredicate
	| expression NOT? LIKE expression likeEscape?											# LikePredicate
	| expression NOT? MEMBER OF path														# MemberOfPredicate
	| EXISTS LEFT_PAREN querySpec RIGHT_PAREN												# ExistsPredicate
	| entityTypeReference op=( EQUAL | NOT_EQUAL ) dotParam									# TypeEqualityPredicate
	| entityTypeReference NOT? IN LEFT_PAREN dotParamList RIGHT_PAREN						# TypeInListPredicate
	| jpaNonStandardFunction ( comparisonOperators expression )?							# JpaNonStandardFunctionPredicate
	;

expression
	: LEFT_PAREN expression RIGHT_PAREN														# GroupedExpression
	| subQuery																				# SubQueryExpression
	| PLUS expression																		# UnaryPlusExpression
	| MINUS expression																		# UnaryMinusExpression
	| expression PLUS expression															# AdditionExpression
	| expression MINUS expression															# SubtractionExpression
	| expression ASTERISK expression														# MultiplicationExpression
	| expression SLASH expression															# DivisionExpression
	| caseStatement																			# CaseExpression
	| coalesce																				# CoalesceExpression
	| nullIf																				# NullIfExpression
	| literal																				# LiteralExpression
	| parameter																				# ParameterExpression
	| entityTypeReference																	# EntityTypeExpression
	| path																					# PathExpression
	| function																				# FunctionExpression
	| op=( ALL | ANY | SOME ) LEFT_PAREN querySpec RIGHT_PAREN								# AllOrAnyExpression
	;

inList
	: LEFT_PAREN expression ( COMMA expression )* RIGHT_PAREN								# ExplicitTupleInList
	| subQuery																				# SubQueryInList
	;

likeEscape
	: ESCAPE expression
	;

subQuery
	: LEFT_PAREN querySpec RIGHT_PAREN
	;

entityTypeReference
	: TYPE LEFT_PAREN entityLiteralReference RIGHT_PAREN
	;

entityLiteralReference
	: path
	| parameter
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
	: STRING_LITERAL																		# StringLiteral
	| CHARACTER_LITERAL																		# CharacterLiteral
	| INTEGER_LITERAL																		# IntegerLiteral
	| LONG_LITERAL																			# LongLiteral
	| BIG_INTEGER_LITERAL																	# BigIntegerLiteral
	| FLOAT_LITERAL																			# FloatLiteral
	| DOUBLE_LITERAL																		# DoubleLiteral
	| BIG_DECIMAL_LITERAL																	# BigDecimalLiteral
	| HEX_LITERAL																			# HexLiteral
	| OCTAL_LITERAL																			# OctalLiteral
	| NULL																					# NullLiteral
	| booleanLiteralRule																	# BooleanLiteral
	| TIMESTAMP_LITERAL																		# TimestampLiteral
	| DATE_LITERAL																			# DateLiteral
	| TIME_LITERAL																			# TimeLiteral
	;

booleanLiteralRule
    : TRUE
    | FALSE
    ;

parameter
	: COLON identifier																		# NamedParameter
	| QUESTION_MARK INTEGER_LITERAL?														# PositionalParameter
	;

function
	: standardFunction
	| aggregateFunction
	;

jpaNonStandardFunction
	: FUNCTION LEFT_PAREN nonStandardFunctionName ( COMMA nonStandardFunctionArguments )? RIGHT_PAREN
	;

nonStandardFunctionName
	: dotIdentifierSequence
	;

nonStandardFunctionArguments
	: expression ( COMMA expression )*
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

/**
 * The `identifier` is used to provide "keyword as identifier" handling.
 *
 * The lexer hands us recognized keywords using their specific tokens.  This is important
 * for the recognition of query structure, especially in terms of performance!
 *
 * However we want to continue to allow users to use mopst keywords as identifiers (e.g., attribute names).
 * This parser rule helps with that.  Here we expect that the caller already understands their
 * context enough to know that keywords-as-identifiers are allowed.
 */
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
	| COLLATE
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

dotParam
    : dotIdentifierSequence
    | parameter
    ;

dotParamList
    : dotParam ( COMMA dotParam )+
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
    : GRANT CREATE? READ? UPDATE? DELETE? ACCESS TO mainEntityPersisterReference whereClause?
    ;

functionsReturningStrings
    : concatFunction
    | substringFunction
    | trimFunction
    | lowerFunction
    | upperFunction
    ;

functionsReturningNumerics
    : absFunction
    | sqrtFunction
    | modFunction
    | sizeFunction
    | indexFunction
    | lengthFunction
    | locateFunction
    ;

functionsReturningDateTime
    : CURRENT_DATE
    | CURRENT_TIME
    | CURRENT_TIMESTAMP
    ;

hintStatement
    : HINT_START hintValue+ HINT_END
    ;

hintValue
    : IS_ACCESSIBLE_NOCACHE     #NoCacheIsAccessible
    | IS_ACCESSIBLE_NODB        #NoDbIsAccessible
    | QUERY_OPTIMIZE_NOCACHE    #NoCacheQueryOptimize
    ;
