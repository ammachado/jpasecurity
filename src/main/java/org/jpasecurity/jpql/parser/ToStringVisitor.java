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

    private final StringBuilder sb;

    public ToStringVisitor() {
        this(new StringBuilder());
    }

    public ToStringVisitor(StringBuilder stringBuilder) {
        super(Objects.requireNonNull(stringBuilder,
                "A non null instance of StringBuilder is required in order to render the HQL query"));
        this.sb = stringBuilder;
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
        prependSpaceAndVisit(ctx.mainEntityPersisterReference());
        return prependSpaceAndVisit(ctx.whereClause());
    }

    @Override
    public StringBuilder visitMainEntityPersisterReference(JpqlParser.MainEntityPersisterReferenceContext ctx) {
        visit(ctx.simplePathQualifier());
        return prependSpaceAndVisit(ctx.identificationVariableDef());
    }

    @Override
    public StringBuilder visitIdentificationVariableDef(JpqlParser.IdentificationVariableDefContext ctx) {
        if (ctx.AS() != null) {
            visit(ctx.AS());
            return prependSpaceAndVisit(ctx.identificationVariable());
        }
        return visit(ctx.IDENTIFIER());
    }

    @Override
    public StringBuilder visitSelectStatement(JpqlParser.SelectStatementContext ctx) {
        visit(ctx.querySpec());
        return prependSpaceAndVisit(ctx.orderByClause());
    }

    @Override
    public StringBuilder visitQuerySpec(JpqlParser.QuerySpecContext ctx) {
        visit(ctx.selectClause());
        prependSpaceAndVisit(ctx.fromClause());
        prependSpaceAndVisit(ctx.whereClause());
        prependSpaceAndVisit(ctx.groupByClause());
        return prependSpaceAndVisit(ctx.havingClause());
    }

    @Override
    public StringBuilder visitSelectClause(JpqlParser.SelectClauseContext ctx) {
        visit(ctx.SELECT());
        prependSpaceAndVisit(ctx.hintStatement());
        prependSpaceAndVisit(ctx.DISTINCT());
        return prependSpaceAndVisit(ctx.selectionList());
    }

    @Override
    public StringBuilder visitSelectionList(JpqlParser.SelectionListContext ctx) {
        visit(ctx.selection(0));

        if (ctx.selection().size() > 1) {
            for (int i = 1; i < ctx.selection().size(); i++) {
                sb.append(COMMA);
                visit(ctx.selection(i));
            }
        }

        return defaultResult();
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
        prependSpaceAndVisit(ctx.mainEntityPersisterReference());
        prependSpaceAndVisit(ctx.setClause());
        return prependSpaceAndVisit(ctx.whereClause());
    }

    @Override
    public StringBuilder visitSetClause(JpqlParser.SetClauseContext ctx) {
        visit(ctx.SET());
        prependSpaceAndVisit(ctx.assignment(0));
        for (int i = 1; i < ctx.assignment().size(); i++) {
            sb.append(COMMA);
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
        prependSpaceAndVisit(ctx.mainEntityPersisterReference());
        return prependSpaceAndVisit(ctx.whereClause());
    }

    @Override
    public StringBuilder visitOrderByClause(JpqlParser.OrderByClauseContext ctx) {
        visit(ctx.ORDER());
        prependSpaceAndVisit(ctx.BY());
        prependSpaceAndVisit(ctx.sortSpecification(0));
        for (int i = 1; i < ctx.sortSpecification().size(); i++) {
            sb.append(COMMA);
            visit(ctx.sortSpecification(i));
        }
        return defaultResult();
    }

    @Override
    public StringBuilder visitSortSpecification(JpqlParser.SortSpecificationContext ctx) {
        visit(ctx.expression());
        return prependSpaceAndVisit(ctx.orderingSpecification());
    }

    @Override
    public StringBuilder visitFromClause(JpqlParser.FromClauseContext ctx) {
        visit(ctx.FROM());
        prependSpaceAndVisit(ctx.fromElementSpace(0));
        for (int i = 1; i < ctx.fromElementSpace().size(); i++) {
            sb.append(COMMA);
            visit(ctx.fromElementSpace(i));
        }
        return sb;
    }

    @Override
    public StringBuilder visitWhereClause(JpqlParser.WhereClauseContext ctx) {
        visit(ctx.WHERE());
        return prependSpaceAndVisit(ctx.predicate());
    }

    @Override
    public StringBuilder visitIsNullPredicate(JpqlParser.IsNullPredicateContext ctx) {
        visit(ctx.expression());
        prependSpaceAndVisit(ctx.IS());
        prependSpaceAndVisit(ctx.NOT());
        return prependSpaceAndVisit(ctx.NULL());
    }

    @Override
    public StringBuilder visitIsEmptyPredicate(JpqlParser.IsEmptyPredicateContext ctx) {
        visit(ctx.expression());
        prependSpaceAndVisit(ctx.IS());
        prependSpaceAndVisit(ctx.NOT());
        return prependSpaceAndVisit(ctx.EMPTY());
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
        return prependSpaceAndVisit(ctx.predicate());
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
            sb.append(COMMA);
            visit((ctx.groupingValue(i)));
        }
        return defaultResult();
    }

    @Override
    public StringBuilder visitHavingClause(JpqlParser.HavingClauseContext ctx) {
        visit(ctx.HAVING());
        return prependSpaceAndVisit(ctx.predicate());
    }

    @Override
    public StringBuilder visitJpaCollectionJoin(JpqlParser.JpaCollectionJoinContext ctx) {
        sb.append(COMMA);
        visit(ctx.IN());
        sb.append(LEFT_PAREN);
        visit(ctx.path());
        sb.append(RIGHT_PAREN);
        return prependSpaceAndVisit(ctx.identificationVariableDef());
    }

    @Override
    public StringBuilder visitQualifiedJoin(JpqlParser.QualifiedJoinContext ctx) {
        prependSpaceAndVisit(ctx.INNER());
        prependSpaceAndVisit(ctx.LEFT());
        prependSpaceAndVisit(ctx.OUTER());
        prependSpaceAndVisit(ctx.JOIN());
        prependSpaceAndVisit(ctx.FETCH());
        prependSpaceAndVisit(ctx.path());
        prependSpaceAndVisit(ctx.identificationVariableDef());
        return prependSpaceAndVisit(ctx.qualifiedJoinPredicate());
    }

    @Override
    public StringBuilder visitQualifiedJoinPredicate(JpqlParser.QualifiedJoinPredicateContext ctx) {
        visit(ctx.ON());
        return prependSpaceAndVisit(ctx.predicate());
    }

    @Override
    public StringBuilder visitTreatedPathRoot(JpqlParser.TreatedPathRootContext ctx) {
        visit(ctx.TREAT()).append(LEFT_PAREN);
        visit(ctx.dotIdentifierSequence(0));
        prependSpaceAndVisit(ctx.AS());
        prependSpaceAndVisit(ctx.dotIdentifierSequence(1));
        return sb.append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitCoalesce(JpqlParser.CoalesceContext ctx) {
        visit(ctx.COALESCE());
        sb.append(LEFT_PAREN);
        visit(ctx.expression(0));
        for (int i = 1; i < ctx.expression().size(); i++) {
            sb.append(COMMA);
            visit(ctx.expression(i));
        }
        return sb.append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitBetweenPredicate(JpqlParser.BetweenPredicateContext ctx) {
        visit(ctx.expression(0));
        prependSpaceAndVisit(ctx.NOT());
        prependSpaceAndVisit(ctx.BETWEEN());
        prependSpaceAndVisit(ctx.expression(1));
        prependSpaceAndVisit(ctx.AND());
        return prependSpaceAndVisit(ctx.expression(2));
    }

    @Override
    public StringBuilder visitInPredicate(JpqlParser.InPredicateContext ctx) {
        visit(ctx.expression());
        prependSpaceAndVisit(ctx.NOT());
        prependSpaceAndVisit(ctx.IN());
        return prependSpaceAndVisit(ctx.inList());
    }

    @Override
    public StringBuilder visitExistsPredicate(JpqlParser.ExistsPredicateContext ctx) {
        visit(ctx.EXISTS());
        sb.append(SPACE).append(LEFT_PAREN);
        visit(ctx.querySpec());
        return sb.append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitAllOrAnyExpression(JpqlParser.AllOrAnyExpressionContext ctx) {
        sb.append(ctx.op.getText()).append(SPACE).append(LEFT_PAREN);
        visit(ctx.querySpec());
        return sb.append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitLikePredicate(JpqlParser.LikePredicateContext ctx) {
        visit(ctx.expression(0));
        prependSpaceAndVisit(ctx.NOT());
        prependSpaceAndVisit(ctx.LIKE());
        prependSpaceAndVisit(ctx.expression(1));
        return prependSpaceAndVisit(ctx.likeEscape());
    }

    @Override
    public StringBuilder visitConstructorExpression(JpqlParser.ConstructorExpressionContext ctx) {
        visit(ctx.NEW());
        sb.append(SPACE);
        visit(ctx.dotIdentifierSequence());
        sb.append(LEFT_PAREN);
        visit(ctx.constructorParameters());
        return sb.append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitConstructorParameters(JpqlParser.ConstructorParametersContext ctx) {
        visit(ctx.constructionParameter(0));
        for (int i = 1; i < ctx.constructionParameter().size(); i++) {
            sb.append(COMMA);
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
        return prependSpaceAndVisit(ctx.END());
    }

    @Override
    public StringBuilder visitSimpleCaseWhen(JpqlParser.SimpleCaseWhenContext ctx) {
        visit(ctx.WHEN());
        prependSpaceAndVisit(ctx.expression(0));
        prependSpaceAndVisit(ctx.THEN());
        return prependSpaceAndVisit(ctx.expression(1));
    }

    @Override
    public StringBuilder visitCaseOtherwise(JpqlParser.CaseOtherwiseContext ctx) {
        visit(ctx.ELSE());
        return prependSpaceAndVisit(ctx.expression());
    }

    @Override
    public StringBuilder visitSearchedCaseStatement(JpqlParser.SearchedCaseStatementContext ctx) {
        visit(ctx.CASE());
        for (int i = 0; i < ctx.searchedCaseWhen().size(); i++) {
            prependSpaceAndVisit(ctx.searchedCaseWhen(i));
        }
        prependSpaceAndVisit(ctx.caseOtherwise());
        return prependSpaceAndVisit(ctx.END());
    }

    @Override
    public StringBuilder visitSearchedCaseWhen(JpqlParser.SearchedCaseWhenContext ctx) {
        visit(ctx.WHEN());
        prependSpaceAndVisit(ctx.predicate());
        prependSpaceAndVisit(ctx.THEN());
        return prependSpaceAndVisit(ctx.expression());
    }

    @Override
    public StringBuilder visitMemberOfPredicate(JpqlParser.MemberOfPredicateContext ctx) {
        visit(ctx.expression());
        prependSpaceAndVisit(ctx.NOT());
        prependSpaceAndVisit(ctx.MEMBER());
        prependSpaceAndVisit(ctx.OF());
        return prependSpaceAndVisit(ctx.path());
    }

    @Override
    public StringBuilder visitTrimFunction(JpqlParser.TrimFunctionContext ctx) {
        visit(ctx.TRIM());
        sb.append(LEFT_PAREN);
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
        return sb.append(RIGHT_PAREN);
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
        sb.append(LEFT_PAREN);
        visitOptionalToken(ctx.entityLiteralReference());
        return sb.append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitTypeEqualityPredicate(JpqlParser.TypeEqualityPredicateContext ctx) {
        visit(ctx.entityTypeReference());
        prependSpaceAndVisit(ctx.EQUAL());
        prependSpaceAndVisit(ctx.NOT_EQUAL());
        return prependSpaceAndVisit(ctx.dotParam());
    }

    @Override
    public StringBuilder visitTypeInListPredicate(JpqlParser.TypeInListPredicateContext ctx) {
        visit(ctx.entityTypeReference());
        prependSpaceAndVisit(ctx.NOT());
        prependSpaceAndVisit(ctx.IN());
        sb.append(LEFT_PAREN);
        visit(ctx.dotParamList());
        return sb.append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitLikeEscape(JpqlParser.LikeEscapeContext ctx) {
        visit(ctx.ESCAPE());
        return prependSpaceAndVisit(ctx.expression());
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
        sb.append(LEFT_PAREN);
        visit(ctx.nonStandardFunctionName());
        sb.append(COMMA);
        visit(ctx.nonStandardFunctionArguments());
        return sb.append(RIGHT_PAREN);
    }

    @Override
    public StringBuilder visitUnaryMinusExpression(JpqlParser.UnaryMinusExpressionContext ctx) {
        sb.append("-");
        return visit(ctx.expression());
    }

    @Override
    public StringBuilder visitUnaryPlusExpression(JpqlParser.UnaryPlusExpressionContext ctx) {
        sb.append("+");
        return visit(ctx.expression());
    }

    @Override
    public StringBuilder visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == JpqlParser.EOF) {
            return defaultResult();
        } else if (node.getSymbol().getType() == JpqlParser.COMMA) {
            return sb.append(COMMA);
        }
        return sb.append(node.getText());
    }

    private StringBuilder visitAssignmentLikeExpression(ParserRuleContext lhs,
                                                        TerminalNode terminalNode,
                                                        ParserRuleContext rhs) {
        visit(lhs);
        prependSpaceAndVisit(terminalNode);
        return prependSpaceAndVisit(rhs);
    }

    private StringBuilder visitAndAppendSpace(ParseTree ctx) {
        if (ctx == null) {
            return sb;
        }
        visit(ctx);
        return sb.append(SPACE);
    }

    private StringBuilder prependSpaceAndVisit(ParseTree ctx) {
        if (ctx == null) {
            return sb;
        }
        sb.append(SPACE);
        return visit(ctx);
    }

    private StringBuilder visitSingleDistinctArgumentFunction(TerminalNode functionName,
                                                              ParseTree distinctNode,
                                                              ParseTree argument) {
        sb.append(functionName.getText()).append(LEFT_PAREN);
        visitOptionalToken(distinctNode);
        if (distinctNode != null) {
            prependSpaceAndVisit(argument);
        } else {
            visit(argument);
        }
        return sb.append(RIGHT_PAREN);
    }
}
