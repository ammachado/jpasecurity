/*
 * Copyright 2008 Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.jpasecurity.jpql.parser;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jpasecurity.jpql.BaseContext;

import java.util.Objects;

/**
 * This visitor creates a JPQL-string of a query tree.
 *
 * @author Adriano Machado
 */
public class ToStringVisitor extends JpqlVisitorAdapter<StringBuilder> {

    private static final char LEFT_PAREN = '(';
    private static final char RIGHT_PAREN = ')';
    private static final char SPACE = ' ';
    private static final String COMMA = ", ";
    private static final String HINT_START = "/*";
    private static final String HINT_END = "*/";

    public ToStringVisitor() {
        this(new StringBuilder());
    }

    public ToStringVisitor(StringBuilder stringBuilder) {
        super(Objects.requireNonNull(stringBuilder,
                "A non null instance of StringBuilder is required in order to render the HQL query"));
    }

    public String toJqplString(BaseContext ctx) {
        return this.visit(ctx).toString();
    }

    @Override
    public StringBuilder visitAccessRule(JpqlParser.AccessRuleContext ctx) {
        visit(ctx.GRANT());
        prependSpaceAndVisit(ctx.CREATE());
        prependSpaceAndVisit(ctx.READ());
        prependSpaceAndVisit(ctx.UPDATE());
        prependSpaceAndVisit(ctx.DELETE());
        prependSpaceAndVisit(ctx.ACCESS());
        prependSpaceAndVisit(ctx.TO());
        prependSpaceAndVisit(ctx.pathRoot());
        prependSpaceAndVisit(ctx.whereClause());
        return defaultResult();
    }

    @Override
    public StringBuilder visitEntityName(JpqlParser.EntityNameContext ctx) {
        defaultResult().append(ctx.fullEntityName);
        return defaultResult();
    }

    @Override
    public StringBuilder visitIdentificationVariableDef(JpqlParser.IdentificationVariableDefContext ctx) {
        if (ctx.AS() != null) {
            visit(ctx.AS());
            prependSpaceAndVisit(ctx.identificationVariable());
            return defaultResult();
        }
        return visit(ctx.IDENTIFIER());
    }

    @Override
    public StringBuilder visitSelectStatement(JpqlParser.SelectStatementContext ctx) {
        visit(ctx.querySpec());
        prependSpaceAndVisit(ctx.orderByClause());
        return defaultResult();
    }

    @Override
    public StringBuilder visitQuerySpec(JpqlParser.QuerySpecContext ctx) {
        visit(ctx.selectClause());
        prependSpaceAndVisit(ctx.fromClause());
        prependSpaceAndVisit(ctx.whereClause());
        prependSpaceAndVisit(ctx.groupByClause());
        prependSpaceAndVisit(ctx.havingClause());
        return defaultResult();
    }

    @Override
    public StringBuilder visitSelectClause(JpqlParser.SelectClauseContext ctx) {
        visit(ctx.SELECT());
        prependSpaceAndVisit(ctx.hintStatement());
        prependSpaceAndVisit(ctx.DISTINCT());
        prependSpaceAndVisit(ctx.selectionList());
        return defaultResult();
    }

    @Override
    public StringBuilder visitSelectionList(JpqlParser.SelectionListContext ctx) {
        int i = 0;
        for (JpqlParser.SelectionContext selection : ctx.selection()) {
            if (i > 0) {
                defaultResult().append(COMMA);
            }
            visit(selection);
            i++;
        }

        return defaultResult();
    }

    @Override
    public StringBuilder visitHintStatement(JpqlParser.HintStatementContext ctx) {
        defaultResult().append(HINT_START);
        for (JpqlParser.HintValueContext hintValue : ctx.hintValue()) {
            prependSpaceAndVisit(hintValue);
        }
        defaultResult().append(SPACE).append(HINT_END);
        return super.visitHintStatement(ctx);
    }

    @Override
    public StringBuilder visitSelection(JpqlParser.SelectionContext ctx) {
        visit(ctx.selectExpression());
        prependSpaceAndVisit(ctx.identificationVariableDef());
        return defaultResult();
    }

    @Override
    public StringBuilder visitUpdateStatement(JpqlParser.UpdateStatementContext ctx) {
        visit(ctx.UPDATE());
        prependSpaceAndVisit(ctx.FROM());
        prependSpaceAndVisit(ctx.pathRoot());
        prependSpaceAndVisit(ctx.setClause());
        prependSpaceAndVisit(ctx.whereClause());
        return defaultResult();
    }

    @Override
    public StringBuilder visitSetClause(JpqlParser.SetClauseContext ctx) {
        visit(ctx.SET());
        prependSpaceAndVisit(ctx.assignment(0));
        for (int i = 1; i < ctx.assignment().size(); i++) {
            defaultResult().append(COMMA);
            visit(ctx.assignment(i));
        }
        return defaultResult();
    }

    @Override
    public StringBuilder visitAssignment(JpqlParser.AssignmentContext ctx) {
        return visitAssignmentLikeExpression(ctx.dotIdentifierSequence(), ctx.EQUAL(), ctx.expression());
    }

    @Override
    public StringBuilder visitDeleteStatement(JpqlParser.DeleteStatementContext ctx) {
        visit(ctx.DELETE());
        prependSpaceAndVisit(ctx.FROM());
        prependSpaceAndVisit(ctx.pathRoot());
        prependSpaceAndVisit(ctx.whereClause());
        return defaultResult();
    }

    @Override
    public StringBuilder visitOrderByClause(JpqlParser.OrderByClauseContext ctx) {
        visit(ctx.ORDER());
        prependSpaceAndVisit(ctx.BY());
        prependSpaceAndVisit(ctx.sortSpecification(0));
        for (int i = 1; i < ctx.sortSpecification().size(); i++) {
            defaultResult().append(COMMA);
            visit(ctx.sortSpecification(i));
        }
        return defaultResult();
    }

    @Override
    public StringBuilder visitSortSpecification(JpqlParser.SortSpecificationContext ctx) {
        visit(ctx.sortExpression());
        prependSpaceAndVisit(ctx.orderingSpecification());
        return defaultResult();
    }

    @Override
    public StringBuilder visitFromClause(JpqlParser.FromClauseContext ctx) {
        visit(ctx.FROM());
        prependSpaceAndVisit(ctx.fromElementSpace(0));
        for (int i = 1; i < ctx.fromElementSpace().size(); i++) {
            defaultResult().append(COMMA);
            visit(ctx.fromElementSpace(i));
        }
        return defaultResult();
    }

    @Override
    public StringBuilder visitWhereClause(JpqlParser.WhereClauseContext ctx) {
        visit(ctx.WHERE());
        prependSpaceAndVisit(ctx.predicate());
        return defaultResult();
    }

    @Override
    public StringBuilder visitIsNullPredicate(JpqlParser.IsNullPredicateContext ctx) {
        visit(ctx.expression());
        prependSpaceAndVisit(ctx.IS());
        prependSpaceAndVisit(ctx.NOT());
        prependSpaceAndVisit(ctx.NULL());
        return defaultResult();
    }

    @Override
    public StringBuilder visitIsEmptyPredicate(JpqlParser.IsEmptyPredicateContext ctx) {
        visit(ctx.expression());
        prependSpaceAndVisit(ctx.IS());
        prependSpaceAndVisit(ctx.NOT());
        prependSpaceAndVisit(ctx.EMPTY());
        return defaultResult();
    }

    @Override
    public StringBuilder visitEqualityPredicate(JpqlParser.EqualityPredicateContext ctx) {
        return visitAssignmentLikeExpression(ctx.expression(0), ctx.EQUAL(), ctx.expression(1));
    }

    @Override
    public StringBuilder visitLessThanPredicate(JpqlParser.LessThanPredicateContext ctx) {
        return visitAssignmentLikeExpression(ctx.expression(0), ctx.LESS(), ctx.expression(1));
    }

    @Override
    public StringBuilder visitLessThanOrEqualPredicate(JpqlParser.LessThanOrEqualPredicateContext ctx) {
        return visitAssignmentLikeExpression(ctx.expression(0), ctx.LESS_EQUAL(), ctx.expression(1));
    }

    @Override
    public StringBuilder visitGreaterThanPredicate(JpqlParser.GreaterThanPredicateContext ctx) {
        return visitAssignmentLikeExpression(ctx.expression(0), ctx.GREATER(), ctx.expression(1));
    }

    @Override
    public StringBuilder visitGreaterThanOrEqualPredicate(JpqlParser.GreaterThanOrEqualPredicateContext ctx) {
        return visitAssignmentLikeExpression(ctx.expression(0), ctx.GREATER_EQUAL(), ctx.expression(1));
    }

    @Override
    public StringBuilder visitInequalityPredicate(JpqlParser.InequalityPredicateContext ctx) {
        return visitAssignmentLikeExpression(ctx.expression(0), ctx.NOT_EQUAL(), ctx.expression(1));
    }

    @Override
    public StringBuilder visitAndPredicate(JpqlParser.AndPredicateContext ctx) {
        return visitAssignmentLikeExpression(ctx.predicate(0), ctx.AND(), ctx.predicate(1));
    }

    @Override
    public StringBuilder visitOrPredicate(JpqlParser.OrPredicateContext ctx) {
        return visitAssignmentLikeExpression(ctx.predicate(0), ctx.OR(), ctx.predicate(1));
    }

    @Override
    public StringBuilder visitNegatedPredicate(JpqlParser.NegatedPredicateContext ctx) {
        visit(ctx.NOT());
        prependSpaceAndVisit(ctx.predicate());
        return defaultResult();
    }

    @Override
    public StringBuilder visitAvgFunction(JpqlParser.AvgFunctionContext ctx) {
        return visitSingleDistinctArgumentFunction(ctx.AVG(), ctx.DISTINCT(), ctx.expression());
    }

    @Override
    public StringBuilder visitMaxFunction(JpqlParser.MaxFunctionContext ctx) {
        return visitSingleDistinctArgumentFunction(ctx.MAX(), ctx.DISTINCT(), ctx.expression());
    }

    @Override
    public StringBuilder visitMinFunction(JpqlParser.MinFunctionContext ctx) {
        return visitSingleDistinctArgumentFunction(ctx.MIN(), ctx.DISTINCT(), ctx.expression());
    }

    @Override
    public StringBuilder visitSumFunction(JpqlParser.SumFunctionContext ctx) {
        return visitSingleDistinctArgumentFunction(ctx.SUM(), ctx.DISTINCT(), ctx.expression());
    }

    @Override
    public StringBuilder visitGroupByClause(JpqlParser.GroupByClauseContext ctx) {
        visit(ctx.GROUP());
        prependSpaceAndVisit(ctx.BY());
        return visit(ctx.groupingSpecification());
    }

    @Override
    public StringBuilder visitGroupingSpecification(JpqlParser.GroupingSpecificationContext ctx) {
        prependSpaceAndVisit(ctx.groupingValue(0));
        for (int i = 1; i < ctx.groupingValue().size(); i++) {
            defaultResult().append(COMMA);
            visit((ctx.groupingValue(i)));
        }
        return defaultResult();
    }

    @Override
    public StringBuilder visitHavingClause(JpqlParser.HavingClauseContext ctx) {
        visit(ctx.HAVING());
        prependSpaceAndVisit(ctx.predicate());
        return defaultResult();
    }

    @Override
    public StringBuilder visitJpaCollectionJoin(JpqlParser.JpaCollectionJoinContext ctx) {
        defaultResult().append(COMMA);
        visit(ctx.IN());
        defaultResult().append(LEFT_PAREN);
        visit(ctx.path());
        defaultResult().append(RIGHT_PAREN);
        prependSpaceAndVisit(ctx.identificationVariableDef());
        return defaultResult();
    }

    @Override
    public StringBuilder visitQualifiedJoin(JpqlParser.QualifiedJoinContext ctx) {
        prependSpaceAndVisit(ctx.INNER());
        prependSpaceAndVisit(ctx.LEFT());
        prependSpaceAndVisit(ctx.RIGHT());
        prependSpaceAndVisit(ctx.OUTER());
        prependSpaceAndVisit(ctx.JOIN());
        prependSpaceAndVisit(ctx.FETCH());
        prependSpaceAndVisit(ctx.path());
        prependSpaceAndVisit(ctx.identificationVariableDef());
        prependSpaceAndVisit(ctx.qualifiedJoinPredicate());
        return defaultResult();
    }

    @Override
    public StringBuilder visitQualifiedJoinPredicate(JpqlParser.QualifiedJoinPredicateContext ctx) {
        visit(ctx.ON());
        prependSpaceAndVisit(ctx.predicate());
        return defaultResult();
    }

    @Override
    public StringBuilder visitTreatedNavigablePath(JpqlParser.TreatedNavigablePathContext ctx) {
        visit(ctx.TREAT()).append(LEFT_PAREN);
        visit(ctx.path());
        prependSpaceAndVisit(ctx.AS());
        prependSpaceAndVisit(ctx.dotIdentifierSequence());
        defaultResult().append(RIGHT_PAREN);
        visitOptionalToken(ctx.pathContinuation());
        return defaultResult();
    }

    @Override
    public StringBuilder visitCoalesce(JpqlParser.CoalesceContext ctx) {
        visit(ctx.COALESCE());
        defaultResult().append(LEFT_PAREN);
        visit(ctx.expression(0));
        for (int i = 1; i < ctx.expression().size(); i++) {
            defaultResult().append(COMMA);
            visit(ctx.expression(i));
        }
        return defaultResult().append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitBetweenPredicate(JpqlParser.BetweenPredicateContext ctx) {
        visit(ctx.expression(0));
        prependSpaceAndVisit(ctx.NOT());
        prependSpaceAndVisit(ctx.BETWEEN());
        prependSpaceAndVisit(ctx.expression(1));
        prependSpaceAndVisit(ctx.AND());
        prependSpaceAndVisit(ctx.expression(2));
        return defaultResult();
    }

    @Override
    public StringBuilder visitInPredicate(JpqlParser.InPredicateContext ctx) {
        visit(ctx.expression());
        prependSpaceAndVisit(ctx.NOT());
        prependSpaceAndVisit(ctx.IN());
        prependSpaceAndVisit(ctx.inList());
        return defaultResult();
    }

    @Override
    public StringBuilder visitExistsPredicate(JpqlParser.ExistsPredicateContext ctx) {
        visit(ctx.EXISTS());
        defaultResult().append(SPACE).append(LEFT_PAREN);
        visit(ctx.querySpec());
        return defaultResult().append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitAllOrAnyExpression(JpqlParser.AllOrAnyExpressionContext ctx) {
        defaultResult().append(ctx.op.getText()).append(SPACE).append(LEFT_PAREN);
        visit(ctx.querySpec());
        return defaultResult().append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitLikePredicate(JpqlParser.LikePredicateContext ctx) {
        visit(ctx.expression(0));
        prependSpaceAndVisit(ctx.NOT());
        prependSpaceAndVisit(ctx.LIKE());
        prependSpaceAndVisit(ctx.expression(1));
        prependSpaceAndVisit(ctx.likeEscape());
        return defaultResult();
    }

    @Override
    public StringBuilder visitConstructorExpression(JpqlParser.ConstructorExpressionContext ctx) {
        visit(ctx.NEW());
        defaultResult().append(SPACE);
        visit(ctx.dotIdentifierSequence());
        defaultResult().append(LEFT_PAREN);
        visit(ctx.constructorParameters());
        return defaultResult().append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitConstructorParameters(JpqlParser.ConstructorParametersContext ctx) {
        visit(ctx.constructionParameter(0));
        for (int i = 1; i < ctx.constructionParameter().size(); i++) {
            defaultResult().append(COMMA);
            visit(ctx.constructionParameter(i));
        }
        return defaultResult();
    }

    @Override
    public StringBuilder visitSimpleCaseStatement(JpqlParser.SimpleCaseStatementContext ctx) {
        visit(ctx.CASE());
        prependSpaceAndVisit(ctx.expression());
        for (int i = 0; i < ctx.simpleCaseWhen().size(); i++) {
            prependSpaceAndVisit(ctx.simpleCaseWhen(i));
        }
        prependSpaceAndVisit(ctx.caseOtherwise());
        prependSpaceAndVisit(ctx.END());
        return defaultResult();
    }

    @Override
    public StringBuilder visitSimpleCaseWhen(JpqlParser.SimpleCaseWhenContext ctx) {
        visit(ctx.WHEN());
        prependSpaceAndVisit(ctx.expression(0));
        prependSpaceAndVisit(ctx.THEN());
        prependSpaceAndVisit(ctx.expression(1));
        return defaultResult();
    }

    @Override
    public StringBuilder visitCaseOtherwise(JpqlParser.CaseOtherwiseContext ctx) {
        visit(ctx.ELSE());
        prependSpaceAndVisit(ctx.expression());
        return defaultResult();
    }

    @Override
    public StringBuilder visitSearchedCaseStatement(JpqlParser.SearchedCaseStatementContext ctx) {
        visit(ctx.CASE());
        for (int i = 0; i < ctx.searchedCaseWhen().size(); i++) {
            prependSpaceAndVisit(ctx.searchedCaseWhen(i));
        }
        prependSpaceAndVisit(ctx.caseOtherwise());
        prependSpaceAndVisit(ctx.END());
        return defaultResult();
    }

    @Override
    public StringBuilder visitSearchedCaseWhen(JpqlParser.SearchedCaseWhenContext ctx) {
        visit(ctx.WHEN());
        prependSpaceAndVisit(ctx.predicate());
        prependSpaceAndVisit(ctx.THEN());
        prependSpaceAndVisit(ctx.expression());
        return defaultResult();
    }

    @Override
    public StringBuilder visitMemberOfPredicate(JpqlParser.MemberOfPredicateContext ctx) {
        prependSpaceAndVisit(ctx.MEMBER());
        prependSpaceAndVisit(ctx.OF());
        prependSpaceAndVisit(ctx.path());
        return defaultResult();
    }

    @Override
    public StringBuilder visitTrimFunction(JpqlParser.TrimFunctionContext ctx) {
        visit(ctx.TRIM());
        defaultResult().append(LEFT_PAREN);
        if (ctx.trimSpecification() != null) {
            visitAndAppendSpace(ctx.trimSpecification());
        }
        if (ctx.trimCharacter() != null) {
            visitAndAppendSpace(ctx.trimCharacter());
        }
        if (ctx.FROM() != null) {
            visitAndAppendSpace(ctx.FROM());
        }
        visit(ctx.expression());
        return defaultResult().append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitCountFunction(JpqlParser.CountFunctionContext ctx) {
        final ParseTree expressionTree = ctx.ASTERISK() != null ? ctx.ASTERISK() : ctx.expression();
        return visitSingleDistinctArgumentFunction(ctx.COUNT(), ctx.DISTINCT(), expressionTree);
    }

    @Override
    public StringBuilder visitAdditionExpression(JpqlParser.AdditionExpressionContext ctx) {
        return visitAssignmentLikeExpression(ctx.expression(0), ctx.PLUS(), ctx.expression(1));
    }

    @Override
    public StringBuilder visitSubtractionExpression(JpqlParser.SubtractionExpressionContext ctx) {
        return visitAssignmentLikeExpression(ctx.expression(0), ctx.MINUS(), ctx.expression(1));
    }

    @Override
    public StringBuilder visitMultiplicationExpression(JpqlParser.MultiplicationExpressionContext ctx) {
        return visitAssignmentLikeExpression(ctx.expression(0), ctx.ASTERISK(), ctx.expression(1));
    }

    @Override
    public StringBuilder visitDivisionExpression(JpqlParser.DivisionExpressionContext ctx) {
        return visitAssignmentLikeExpression(ctx.expression(0), ctx.SLASH(), ctx.expression(1));
    }

    @Override
    public StringBuilder visitEntityTypeReference(JpqlParser.EntityTypeReferenceContext ctx) {
        visit(ctx.TYPE());
        defaultResult().append(LEFT_PAREN);
        visitOptionalToken(ctx.entityLiteralReference());
        return defaultResult().append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitTypeEqualityPredicate(JpqlParser.TypeEqualityPredicateContext ctx) {
        visit(ctx.entityTypeReference());
        defaultResult().append(SPACE).append(ctx.op.getText());
        prependSpaceAndVisit(ctx.entityParam());
        return defaultResult();
    }

    @Override
    public StringBuilder visitTypeInListPredicate(JpqlParser.TypeInListPredicateContext ctx) {
        visit(ctx.entityTypeReference());
        prependSpaceAndVisit(ctx.NOT());
        prependSpaceAndVisit(ctx.IN());
        defaultResult().append(LEFT_PAREN);
        visit(ctx.entityParam());
        return defaultResult().append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitLikeEscape(JpqlParser.LikeEscapeContext ctx) {
        visit(ctx.ESCAPE());
        prependSpaceAndVisit(ctx.expression());
        return defaultResult();
    }

    @Override
    public StringBuilder visitNamedParameter(JpqlParser.NamedParameterContext ctx) {
        visit(ctx.COLON());
        return visit(ctx.identifier());
    }

    @Override
    public StringBuilder visitPositionalParameter(JpqlParser.PositionalParameterContext ctx) {
        visit(ctx.QUESTION_MARK());
        return visit(ctx.INTEGER_LITERAL());
    }

    @Override
    public StringBuilder visitJpaNonStandardFunction(JpqlParser.JpaNonStandardFunctionContext ctx) {
        visit(ctx.FUNCTION());
        defaultResult().append(LEFT_PAREN);
        visit(ctx.jpaNonStandardFunctionName());
        defaultResult().append(COMMA);
        visit(ctx.nonStandardFunctionArguments());
        return defaultResult().append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitUnaryMinusExpression(JpqlParser.UnaryMinusExpressionContext ctx) {
        defaultResult().append("-");
        return visit(ctx.expression());
    }

    @Override
    public StringBuilder visitUnaryPlusExpression(JpqlParser.UnaryPlusExpressionContext ctx) {
        defaultResult().append("+");
        return visit(ctx.expression());
    }

    @Override
    public StringBuilder visitPathRoot(JpqlParser.PathRootContext ctx) {
        visit(ctx.entityName());
        prependSpaceAndVisit(ctx.identificationVariableDef());
        return defaultResult();
    }

    @Override
    public StringBuilder visitMapEntrySelection(JpqlParser.MapEntrySelectionContext ctx) {
        visit(ctx.ENTRY());
        defaultResult().append(LEFT_PAREN);
        visit(ctx.path());
        return defaultResult().append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitSelectExpression(JpqlParser.SelectExpressionContext ctx) {
        visitOptionalToken(ctx.constructorExpression());
        visitOptionalToken(ctx.expression());
        visitOptionalToken(ctx.jpaSelectObjectSyntax());
        visitOptionalToken(ctx.mapEntrySelection());
        return defaultResult();
    }

    @Override
    public StringBuilder visitJpaNonStandardFunctionPredicate(JpqlParser.JpaNonStandardFunctionPredicateContext ctx) {
        return super.visitJpaNonStandardFunctionPredicate(ctx);
    }

    @Override
    public StringBuilder visitGroupedPredicate(JpqlParser.GroupedPredicateContext ctx) {
        defaultResult().append(LEFT_PAREN);
        visit(ctx.predicate());
        return defaultResult().append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitJpaNonStandardFunctionName(JpqlParser.JpaNonStandardFunctionNameContext ctx) {
        return super.visitJpaNonStandardFunctionName(ctx);
    }

    @Override
    public StringBuilder visitNonStandardFunctionArguments(JpqlParser.NonStandardFunctionArgumentsContext ctx) {
        return super.visitNonStandardFunctionArguments(ctx);
    }

    @Override
    public StringBuilder visitStandardFunction(JpqlParser.StandardFunctionContext ctx) {
        return super.visitStandardFunction(ctx);
    }

    @Override
    public StringBuilder visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == JpqlParser.EOF) {
            return defaultResult();
        } else if (node.getSymbol().getType() == JpqlParser.COMMA) {
            return defaultResult().append(COMMA);
        }
        return defaultResult().append(node.getText());
    }

    private StringBuilder visitAssignmentLikeExpression(ParserRuleContext lhs,
                                                        TerminalNode terminalNode,
                                                        ParserRuleContext rhs) {
        visit(lhs);
        prependSpaceAndVisit(terminalNode);
        prependSpaceAndVisit(rhs);
        return defaultResult();
    }

    private void visitAndAppendSpace(ParseTree ctx) {
        if (ctx == null) {
            return;
        }
        visit(ctx);
        defaultResult().append(SPACE);
    }

    private void prependSpaceAndVisit(ParseTree ctx) {
        if (ctx == null) {
            return;
        }
        defaultResult().append(SPACE);
        visit(ctx);
    }

    private StringBuilder visitSingleDistinctArgumentFunction(TerminalNode functionName,
                                                              ParseTree distinctNode,
                                                              ParseTree argument) {
        defaultResult().append(functionName.getText()).append(LEFT_PAREN);
        visitOptionalToken(distinctNode);
        if (distinctNode != null) {
            prependSpaceAndVisit(argument);
        } else {
            visit(argument);
        }
        return defaultResult().append(RIGHT_PAREN);
    }
}
