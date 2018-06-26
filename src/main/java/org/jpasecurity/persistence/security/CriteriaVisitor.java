/*
 * Copyright 2011 - 2016 Arne Limburg
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
package org.jpasecurity.persistence.security;

import static org.jpasecurity.util.Validate.notNull;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import org.jpasecurity.persistence.mapping.ManagedTypeFilter;

/**
 * This visitor creates a {@link javax.persistence.criteria.CriteriaQuery} of a query tree.
 * @author Arne Limburg
 */
public class CriteriaVisitor extends JpqlVisitorAdapter<CriteriaHolder> {

    private Metamodel metamodel;
    private CriteriaBuilder builder;

    public CriteriaVisitor(Metamodel metamodel, CriteriaBuilder criteriaBuilder, CriteriaHolder criteriaHolder) {
        super(criteriaHolder);
        notNull(Metamodel.class, metamodel);
        notNull(CriteriaBuilder.class, criteriaBuilder);
        this.metamodel = metamodel;
        this.builder = criteriaBuilder;
    }

    @Override
    public CriteriaHolder visitUpdateStatement(JpqlParser.UpdateStatementContext ctx) {
        String entityName = ctx.mainEntityPersisterReference().getText();
        Class<?> entityType = ManagedTypeFilter.forModel(metamodel).filter(entityName.trim()).getJavaType();
        defaultResult().setValue(builder.createCriteriaUpdate(entityType));
        return defaultResult();
    }

    @Override
    public CriteriaHolder visitSetClause(JpqlParser.SetClauseContext ctx) {
        return super.visitSetClause(ctx);
    }

    @Override
    public CriteriaHolder visitAssignment(JpqlParser.AssignmentContext ctx) {
        return super.visitAssignment(ctx);
    }

    @Override
    public CriteriaHolder visitDeleteStatement(JpqlParser.DeleteStatementContext ctx) {
        String entityName = ctx.mainEntityPersisterReference().getText();
        Class<?> entityType = ManagedTypeFilter.forModel(metamodel).filter(entityName.trim()).getJavaType();
        defaultResult().setValue(builder.createCriteriaDelete(entityType));
        return defaultResult();
    }

    @Override
    public CriteriaHolder visitStatement(JpqlParser.StatementContext ctx) {
        return super.visitStatement(ctx);
    }

    @Override
    public CriteriaHolder visitSelectStatement(JpqlParser.SelectStatementContext ctx) {
        return super.visitSelectStatement(ctx);
    }

    @Override
    public CriteriaHolder visitOrderByClause(JpqlParser.OrderByClauseContext ctx) {
        return super.visitOrderByClause(ctx);
    }

    @Override
    public CriteriaHolder visitSortSpecification(JpqlParser.SortSpecificationContext ctx) {
        return super.visitSortSpecification(ctx);
    }

    @Override
    public CriteriaHolder visitOrderingSpecification(JpqlParser.OrderingSpecificationContext ctx) {
        return super.visitOrderingSpecification(ctx);
    }

    @Override
    public CriteriaHolder visitQuerySpec(JpqlParser.QuerySpecContext ctx) {
        return super.visitQuerySpec(ctx);
    }

    @Override
    public CriteriaHolder visitSelectClause(JpqlParser.SelectClauseContext ctx) {
        return super.visitSelectClause(ctx);
    }

    @Override
    public CriteriaHolder visitSelectionList(JpqlParser.SelectionListContext ctx) {
        return super.visitSelectionList(ctx);
    }

    @Override
    public CriteriaHolder visitSelection(JpqlParser.SelectionContext ctx) {
        return super.visitSelection(ctx);
    }

    @Override
    public CriteriaHolder visitSelectExpression(JpqlParser.SelectExpressionContext ctx) {
        return super.visitSelectExpression(ctx);
    }

    @Override
    public CriteriaHolder visitMapEntrySelection(JpqlParser.MapEntrySelectionContext ctx) {
        return super.visitMapEntrySelection(ctx);
    }

    @Override
    public CriteriaHolder visitConstructorExpression(JpqlParser.ConstructorExpressionContext ctx) {
        return super.visitConstructorExpression(ctx);
    }

    @Override
    public CriteriaHolder visitDotIdentifierSequence(JpqlParser.DotIdentifierSequenceContext ctx) {
        return super.visitDotIdentifierSequence(ctx);
    }

    @Override
    public CriteriaHolder visitCompoundPath(JpqlParser.CompoundPathContext ctx) {
        return super.visitCompoundPath(ctx);
    }

    @Override
    public CriteriaHolder visitIndexedPath(JpqlParser.IndexedPathContext ctx) {
        return super.visitIndexedPath(ctx);
    }

    @Override
    public CriteriaHolder visitSimplePath(JpqlParser.SimplePathContext ctx) {
        return super.visitSimplePath(ctx);
    }

    @Override
    public CriteriaHolder visitMapEntryPath(JpqlParser.MapEntryPathContext ctx) {
        return super.visitMapEntryPath(ctx);
    }

    @Override
    public CriteriaHolder visitSimplePathRoot(JpqlParser.SimplePathRootContext ctx) {
        return super.visitSimplePathRoot(ctx);
    }

    @Override
    public CriteriaHolder visitTreatedPathRoot(JpqlParser.TreatedPathRootContext ctx) {
        return super.visitTreatedPathRoot(ctx);
    }

    @Override
    public CriteriaHolder visitMapKeyPathRoot(JpqlParser.MapKeyPathRootContext ctx) {
        return super.visitMapKeyPathRoot(ctx);
    }

    @Override
    public CriteriaHolder visitCollectionValuePathRoot(JpqlParser.CollectionValuePathRootContext ctx) {
        return super.visitCollectionValuePathRoot(ctx);
    }

    @Override
    public CriteriaHolder visitPathTerminal(JpqlParser.PathTerminalContext ctx) {
        return super.visitPathTerminal(ctx);
    }

    @Override
    public CriteriaHolder visitCollectionReference(JpqlParser.CollectionReferenceContext ctx) {
        return super.visitCollectionReference(ctx);
    }

    @Override
    public CriteriaHolder visitMapReference(JpqlParser.MapReferenceContext ctx) {
        return super.visitMapReference(ctx);
    }

    @Override
    public CriteriaHolder visitConstructorParameters(JpqlParser.ConstructorParametersContext ctx) {
        return super.visitConstructorParameters(ctx);
    }

    @Override
    public CriteriaHolder visitConstructionParameter(JpqlParser.ConstructionParameterContext ctx) {
        return super.visitConstructionParameter(ctx);
    }

    @Override
    public CriteriaHolder visitConstructionParameterExpression(JpqlParser.ConstructionParameterExpressionContext ctx) {
        return super.visitConstructionParameterExpression(ctx);
    }

    @Override
    public CriteriaHolder visitJpaSelectObjectSyntax(JpqlParser.JpaSelectObjectSyntaxContext ctx) {
        return super.visitJpaSelectObjectSyntax(ctx);
    }

    @Override
    public CriteriaHolder visitFromElementSpace(JpqlParser.FromElementSpaceContext ctx) {
        return super.visitFromElementSpace(ctx);
    }

    @Override
    public CriteriaHolder visitFromElementSpaceRoot(JpqlParser.FromElementSpaceRootContext ctx) {
        return super.visitFromElementSpaceRoot(ctx);
    }

    @Override
    public CriteriaHolder visitMainEntityPersisterReference(JpqlParser.MainEntityPersisterReferenceContext ctx) {
        return super.visitMainEntityPersisterReference(ctx);
    }

    @Override
    public CriteriaHolder visitIdentificationVariableDef(JpqlParser.IdentificationVariableDefContext ctx) {
        return super.visitIdentificationVariableDef(ctx);
    }

    @Override
    public CriteriaHolder visitIdentificationVariable(JpqlParser.IdentificationVariableContext ctx) {
        return super.visitIdentificationVariable(ctx);
    }

    @Override
    public CriteriaHolder visitJpaCollectionJoin(JpqlParser.JpaCollectionJoinContext ctx) {
        return super.visitJpaCollectionJoin(ctx);
    }

    @Override
    public CriteriaHolder visitQualifiedJoin(JpqlParser.QualifiedJoinContext ctx) {
        return super.visitQualifiedJoin(ctx);
    }

    @Override
    public CriteriaHolder visitQualifiedJoinPredicate(JpqlParser.QualifiedJoinPredicateContext ctx) {
        return super.visitQualifiedJoinPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitGroupByClause(JpqlParser.GroupByClauseContext ctx) {
        return super.visitGroupByClause(ctx);
    }

    @Override
    public CriteriaHolder visitGroupingSpecification(JpqlParser.GroupingSpecificationContext ctx) {
        return super.visitGroupingSpecification(ctx);
    }

    @Override
    public CriteriaHolder visitGroupingValue(JpqlParser.GroupingValueContext ctx) {
        return super.visitGroupingValue(ctx);
    }

    @Override
    public CriteriaHolder visitHavingClause(JpqlParser.HavingClauseContext ctx) {
        return super.visitHavingClause(ctx);
    }

    @Override
    public CriteriaHolder visitWhereClause(JpqlParser.WhereClauseContext ctx) {
        return super.visitWhereClause(ctx);
    }

    @Override
    public CriteriaHolder visitBetweenPredicate(JpqlParser.BetweenPredicateContext ctx) {
        return super.visitBetweenPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitJpaNonStandardFunctionPredicate(JpqlParser.JpaNonStandardFunctionPredicateContext ctx) {
        return super.visitJpaNonStandardFunctionPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitExistsPredicate(JpqlParser.ExistsPredicateContext ctx) {
        return super.visitExistsPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitAndPredicate(JpqlParser.AndPredicateContext ctx) {
        return super.visitAndPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitLessThanOrEqualPredicate(JpqlParser.LessThanOrEqualPredicateContext ctx) {
        return super.visitLessThanOrEqualPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitInequalityPredicate(JpqlParser.InequalityPredicateContext ctx) {
        return super.visitInequalityPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitGroupedPredicate(JpqlParser.GroupedPredicateContext ctx) {
        return super.visitGroupedPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitLikePredicate(JpqlParser.LikePredicateContext ctx) {
        return super.visitLikePredicate(ctx);
    }

    @Override
    public CriteriaHolder visitInPredicate(JpqlParser.InPredicateContext ctx) {
        return super.visitInPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitTypeInListPredicate(JpqlParser.TypeInListPredicateContext ctx) {
        return super.visitTypeInListPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitEqualityPredicate(JpqlParser.EqualityPredicateContext ctx) {
        return super.visitEqualityPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitGreaterThanPredicate(JpqlParser.GreaterThanPredicateContext ctx) {
        return super.visitGreaterThanPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitNegatedPredicate(JpqlParser.NegatedPredicateContext ctx) {
        return super.visitNegatedPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitOrPredicate(JpqlParser.OrPredicateContext ctx) {
        return super.visitOrPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitMemberOfPredicate(JpqlParser.MemberOfPredicateContext ctx) {
        return super.visitMemberOfPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitIsEmptyPredicate(JpqlParser.IsEmptyPredicateContext ctx) {
        return super.visitIsEmptyPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitLessThanPredicate(JpqlParser.LessThanPredicateContext ctx) {
        return super.visitLessThanPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitIsNullPredicate(JpqlParser.IsNullPredicateContext ctx) {
        return super.visitIsNullPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitGreaterThanOrEqualPredicate(JpqlParser.GreaterThanOrEqualPredicateContext ctx) {
        return super.visitGreaterThanOrEqualPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitTypeEqualityPredicate(JpqlParser.TypeEqualityPredicateContext ctx) {
        return super.visitTypeEqualityPredicate(ctx);
    }

    @Override
    public CriteriaHolder visitAdditionExpression(JpqlParser.AdditionExpressionContext ctx) {
        return super.visitAdditionExpression(ctx);
    }

    @Override
    public CriteriaHolder visitSubQueryExpression(JpqlParser.SubQueryExpressionContext ctx) {
        return super.visitSubQueryExpression(ctx);
    }

    @Override
    public CriteriaHolder visitLiteralExpression(JpqlParser.LiteralExpressionContext ctx) {
        return super.visitLiteralExpression(ctx);
    }

    @Override
    public CriteriaHolder visitEntityTypeExpression(JpqlParser.EntityTypeExpressionContext ctx) {
        return super.visitEntityTypeExpression(ctx);
    }

    @Override
    public CriteriaHolder visitGroupedExpression(JpqlParser.GroupedExpressionContext ctx) {
        return super.visitGroupedExpression(ctx);
    }

    @Override
    public CriteriaHolder visitCaseExpression(JpqlParser.CaseExpressionContext ctx) {
        return super.visitCaseExpression(ctx);
    }

    @Override
    public CriteriaHolder visitSubtractionExpression(JpqlParser.SubtractionExpressionContext ctx) {
        return super.visitSubtractionExpression(ctx);
    }

    @Override
    public CriteriaHolder visitFunctionExpression(JpqlParser.FunctionExpressionContext ctx) {
        return super.visitFunctionExpression(ctx);
    }

    @Override
    public CriteriaHolder visitUnaryMinusExpression(JpqlParser.UnaryMinusExpressionContext ctx) {
        return super.visitUnaryMinusExpression(ctx);
    }

    @Override
    public CriteriaHolder visitPathExpression(JpqlParser.PathExpressionContext ctx) {
        return super.visitPathExpression(ctx);
    }

    @Override
    public CriteriaHolder visitAllOrAnyExpression(JpqlParser.AllOrAnyExpressionContext ctx) {
        return super.visitAllOrAnyExpression(ctx);
    }

    @Override
    public CriteriaHolder visitParameterExpression(JpqlParser.ParameterExpressionContext ctx) {
        return super.visitParameterExpression(ctx);
    }

    @Override
    public CriteriaHolder visitUnaryPlusExpression(JpqlParser.UnaryPlusExpressionContext ctx) {
        return super.visitUnaryPlusExpression(ctx);
    }

    @Override
    public CriteriaHolder visitNullIfExpression(JpqlParser.NullIfExpressionContext ctx) {
        return super.visitNullIfExpression(ctx);
    }

    @Override
    public CriteriaHolder visitDivisionExpression(JpqlParser.DivisionExpressionContext ctx) {
        return super.visitDivisionExpression(ctx);
    }

    @Override
    public CriteriaHolder visitMultiplicationExpression(JpqlParser.MultiplicationExpressionContext ctx) {
        return super.visitMultiplicationExpression(ctx);
    }

    @Override
    public CriteriaHolder visitCoalesceExpression(JpqlParser.CoalesceExpressionContext ctx) {
        return super.visitCoalesceExpression(ctx);
    }

    @Override
    public CriteriaHolder visitExplicitTupleInList(JpqlParser.ExplicitTupleInListContext ctx) {
        return super.visitExplicitTupleInList(ctx);
    }

    @Override
    public CriteriaHolder visitSubQueryInList(JpqlParser.SubQueryInListContext ctx) {
        return super.visitSubQueryInList(ctx);
    }

    @Override
    public CriteriaHolder visitLikeEscape(JpqlParser.LikeEscapeContext ctx) {
        return super.visitLikeEscape(ctx);
    }

    @Override
    public CriteriaHolder visitSubQuery(JpqlParser.SubQueryContext ctx) {
        return super.visitSubQuery(ctx);
    }

    @Override
    public CriteriaHolder visitEntityTypeReference(JpqlParser.EntityTypeReferenceContext ctx) {
        return super.visitEntityTypeReference(ctx);
    }

    @Override
    public CriteriaHolder visitEntityLiteralReference(JpqlParser.EntityLiteralReferenceContext ctx) {
        return super.visitEntityLiteralReference(ctx);
    }

    @Override
    public CriteriaHolder visitCaseStatement(JpqlParser.CaseStatementContext ctx) {
        return super.visitCaseStatement(ctx);
    }

    @Override
    public CriteriaHolder visitSimpleCaseStatement(JpqlParser.SimpleCaseStatementContext ctx) {
        return super.visitSimpleCaseStatement(ctx);
    }

    @Override
    public CriteriaHolder visitSimpleCaseWhen(JpqlParser.SimpleCaseWhenContext ctx) {
        return super.visitSimpleCaseWhen(ctx);
    }

    @Override
    public CriteriaHolder visitCaseOtherwise(JpqlParser.CaseOtherwiseContext ctx) {
        return super.visitCaseOtherwise(ctx);
    }

    @Override
    public CriteriaHolder visitSearchedCaseStatement(JpqlParser.SearchedCaseStatementContext ctx) {
        return super.visitSearchedCaseStatement(ctx);
    }

    @Override
    public CriteriaHolder visitSearchedCaseWhen(JpqlParser.SearchedCaseWhenContext ctx) {
        return super.visitSearchedCaseWhen(ctx);
    }

    @Override
    public CriteriaHolder visitCoalesce(JpqlParser.CoalesceContext ctx) {
        return super.visitCoalesce(ctx);
    }

    @Override
    public CriteriaHolder visitNullIf(JpqlParser.NullIfContext ctx) {
        return super.visitNullIf(ctx);
    }

    @Override
    public CriteriaHolder visitStringLiteral(JpqlParser.StringLiteralContext ctx) {
        return super.visitStringLiteral(ctx);
    }

    @Override
    public CriteriaHolder visitCharacterLiteral(JpqlParser.CharacterLiteralContext ctx) {
        return super.visitCharacterLiteral(ctx);
    }

    @Override
    public CriteriaHolder visitIntegerLiteral(JpqlParser.IntegerLiteralContext ctx) {
        return super.visitIntegerLiteral(ctx);
    }

    @Override
    public CriteriaHolder visitLongLiteral(JpqlParser.LongLiteralContext ctx) {
        return super.visitLongLiteral(ctx);
    }

    @Override
    public CriteriaHolder visitBigIntegerLiteral(JpqlParser.BigIntegerLiteralContext ctx) {
        return super.visitBigIntegerLiteral(ctx);
    }

    @Override
    public CriteriaHolder visitFloatLiteral(JpqlParser.FloatLiteralContext ctx) {
        return super.visitFloatLiteral(ctx);
    }

    @Override
    public CriteriaHolder visitDoubleLiteral(JpqlParser.DoubleLiteralContext ctx) {
        return super.visitDoubleLiteral(ctx);
    }

    @Override
    public CriteriaHolder visitBigDecimalLiteral(JpqlParser.BigDecimalLiteralContext ctx) {
        return super.visitBigDecimalLiteral(ctx);
    }

    @Override
    public CriteriaHolder visitHexLiteral(JpqlParser.HexLiteralContext ctx) {
        return super.visitHexLiteral(ctx);
    }

    @Override
    public CriteriaHolder visitOctalLiteral(JpqlParser.OctalLiteralContext ctx) {
        return super.visitOctalLiteral(ctx);
    }

    @Override
    public CriteriaHolder visitNullLiteral(JpqlParser.NullLiteralContext ctx) {
        return super.visitNullLiteral(ctx);
    }

    @Override
    public CriteriaHolder visitBooleanLiteral(JpqlParser.BooleanLiteralContext ctx) {
        return super.visitBooleanLiteral(ctx);
    }

    @Override
    public CriteriaHolder visitTimestampLiteral(JpqlParser.TimestampLiteralContext ctx) {
        return super.visitTimestampLiteral(ctx);
    }

    @Override
    public CriteriaHolder visitDateLiteral(JpqlParser.DateLiteralContext ctx) {
        return super.visitDateLiteral(ctx);
    }

    @Override
    public CriteriaHolder visitTimeLiteral(JpqlParser.TimeLiteralContext ctx) {
        return super.visitTimeLiteral(ctx);
    }

    @Override
    public CriteriaHolder visitBooleanLiteralRule(JpqlParser.BooleanLiteralRuleContext ctx) {
        return super.visitBooleanLiteralRule(ctx);
    }

    @Override
    public CriteriaHolder visitNamedParameter(JpqlParser.NamedParameterContext ctx) {
        return super.visitNamedParameter(ctx);
    }

    @Override
    public CriteriaHolder visitPositionalParameter(JpqlParser.PositionalParameterContext ctx) {
        return super.visitPositionalParameter(ctx);
    }

    @Override
    public CriteriaHolder visitFunction(JpqlParser.FunctionContext ctx) {
        return super.visitFunction(ctx);
    }

    @Override
    public CriteriaHolder visitJpaNonStandardFunction(JpqlParser.JpaNonStandardFunctionContext ctx) {
        return super.visitJpaNonStandardFunction(ctx);
    }

    @Override
    public CriteriaHolder visitNonStandardFunctionName(JpqlParser.NonStandardFunctionNameContext ctx) {
        return super.visitNonStandardFunctionName(ctx);
    }

    @Override
    public CriteriaHolder visitNonStandardFunctionArguments(JpqlParser.NonStandardFunctionArgumentsContext ctx) {
        return super.visitNonStandardFunctionArguments(ctx);
    }

    @Override
    public CriteriaHolder visitAggregateFunction(JpqlParser.AggregateFunctionContext ctx) {
        return super.visitAggregateFunction(ctx);
    }

    @Override
    public CriteriaHolder visitAvgFunction(JpqlParser.AvgFunctionContext ctx) {
        return super.visitAvgFunction(ctx);
    }

    @Override
    public CriteriaHolder visitSumFunction(JpqlParser.SumFunctionContext ctx) {
        return super.visitSumFunction(ctx);
    }

    @Override
    public CriteriaHolder visitMinFunction(JpqlParser.MinFunctionContext ctx) {
        return super.visitMinFunction(ctx);
    }

    @Override
    public CriteriaHolder visitMaxFunction(JpqlParser.MaxFunctionContext ctx) {
        return super.visitMaxFunction(ctx);
    }

    @Override
    public CriteriaHolder visitCountFunction(JpqlParser.CountFunctionContext ctx) {
        return super.visitCountFunction(ctx);
    }

    @Override
    public CriteriaHolder visitStandardFunction(JpqlParser.StandardFunctionContext ctx) {
        return super.visitStandardFunction(ctx);
    }

    @Override
    public CriteriaHolder visitConcatFunction(JpqlParser.ConcatFunctionContext ctx) {
        return super.visitConcatFunction(ctx);
    }

    @Override
    public CriteriaHolder visitSubstringFunction(JpqlParser.SubstringFunctionContext ctx) {
        return super.visitSubstringFunction(ctx);
    }

    @Override
    public CriteriaHolder visitSubstringFunctionStartArgument(JpqlParser.SubstringFunctionStartArgumentContext ctx) {
        return super.visitSubstringFunctionStartArgument(ctx);
    }

    @Override
    public CriteriaHolder visitSubstringFunctionLengthArgument(JpqlParser.SubstringFunctionLengthArgumentContext ctx) {
        return super.visitSubstringFunctionLengthArgument(ctx);
    }

    @Override
    public CriteriaHolder visitTrimFunction(JpqlParser.TrimFunctionContext ctx) {
        return super.visitTrimFunction(ctx);
    }

    @Override
    public CriteriaHolder visitTrimSpecification(JpqlParser.TrimSpecificationContext ctx) {
        return super.visitTrimSpecification(ctx);
    }

    @Override
    public CriteriaHolder visitTrimCharacter(JpqlParser.TrimCharacterContext ctx) {
        return super.visitTrimCharacter(ctx);
    }

    @Override
    public CriteriaHolder visitUpperFunction(JpqlParser.UpperFunctionContext ctx) {
        return super.visitUpperFunction(ctx);
    }

    @Override
    public CriteriaHolder visitLowerFunction(JpqlParser.LowerFunctionContext ctx) {
        return super.visitLowerFunction(ctx);
    }

    @Override
    public CriteriaHolder visitLengthFunction(JpqlParser.LengthFunctionContext ctx) {
        return super.visitLengthFunction(ctx);
    }

    @Override
    public CriteriaHolder visitLocateFunction(JpqlParser.LocateFunctionContext ctx) {
        return super.visitLocateFunction(ctx);
    }

    @Override
    public CriteriaHolder visitLocateFunctionSubstrArgument(JpqlParser.LocateFunctionSubstrArgumentContext ctx) {
        return super.visitLocateFunctionSubstrArgument(ctx);
    }

    @Override
    public CriteriaHolder visitLocateFunctionStringArgument(JpqlParser.LocateFunctionStringArgumentContext ctx) {
        return super.visitLocateFunctionStringArgument(ctx);
    }

    @Override
    public CriteriaHolder visitLocateFunctionStartArgument(JpqlParser.LocateFunctionStartArgumentContext ctx) {
        return super.visitLocateFunctionStartArgument(ctx);
    }

    @Override
    public CriteriaHolder visitAbsFunction(JpqlParser.AbsFunctionContext ctx) {
        return super.visitAbsFunction(ctx);
    }

    @Override
    public CriteriaHolder visitSqrtFunction(JpqlParser.SqrtFunctionContext ctx) {
        return super.visitSqrtFunction(ctx);
    }

    @Override
    public CriteriaHolder visitModFunction(JpqlParser.ModFunctionContext ctx) {
        return super.visitModFunction(ctx);
    }

    @Override
    public CriteriaHolder visitModDividendArgument(JpqlParser.ModDividendArgumentContext ctx) {
        return super.visitModDividendArgument(ctx);
    }

    @Override
    public CriteriaHolder visitModDivisorArgument(JpqlParser.ModDivisorArgumentContext ctx) {
        return super.visitModDivisorArgument(ctx);
    }

    @Override
    public CriteriaHolder visitSizeFunction(JpqlParser.SizeFunctionContext ctx) {
        return super.visitSizeFunction(ctx);
    }

    @Override
    public CriteriaHolder visitIndexFunction(JpqlParser.IndexFunctionContext ctx) {
        return super.visitIndexFunction(ctx);
    }

    @Override
    public CriteriaHolder visitCurrentDateFunction(JpqlParser.CurrentDateFunctionContext ctx) {
        return super.visitCurrentDateFunction(ctx);
    }

    @Override
    public CriteriaHolder visitCurrentTimeFunction(JpqlParser.CurrentTimeFunctionContext ctx) {
        return super.visitCurrentTimeFunction(ctx);
    }

    @Override
    public CriteriaHolder visitCurrentTimestampFunction(JpqlParser.CurrentTimestampFunctionContext ctx) {
        return super.visitCurrentTimestampFunction(ctx);
    }

    @Override
    public CriteriaHolder visitPositionFunction(JpqlParser.PositionFunctionContext ctx) {
        return super.visitPositionFunction(ctx);
    }

    @Override
    public CriteriaHolder visitPositionSubstrArgument(JpqlParser.PositionSubstrArgumentContext ctx) {
        return super.visitPositionSubstrArgument(ctx);
    }

    @Override
    public CriteriaHolder visitPositionStringArgument(JpqlParser.PositionStringArgumentContext ctx) {
        return super.visitPositionStringArgument(ctx);
    }

    @Override
    public CriteriaHolder visitCharLengthFunction(JpqlParser.CharLengthFunctionContext ctx) {
        return super.visitCharLengthFunction(ctx);
    }

    @Override
    public CriteriaHolder visitOctetLengthFunction(JpqlParser.OctetLengthFunctionContext ctx) {
        return super.visitOctetLengthFunction(ctx);
    }

    @Override
    public CriteriaHolder visitBitLengthFunction(JpqlParser.BitLengthFunctionContext ctx) {
        return super.visitBitLengthFunction(ctx);
    }

    @Override
    public CriteriaHolder visitIdentifier(JpqlParser.IdentifierContext ctx) {
        return super.visitIdentifier(ctx);
    }

    @Override
    public CriteriaHolder visitDotParam(JpqlParser.DotParamContext ctx) {
        return super.visitDotParam(ctx);
    }

    @Override
    public CriteriaHolder visitDotParamList(JpqlParser.DotParamListContext ctx) {
        return super.visitDotParamList(ctx);
    }

    @Override
    public CriteriaHolder visitComparisonOperators(JpqlParser.ComparisonOperatorsContext ctx) {
        return super.visitComparisonOperators(ctx);
    }

    @Override
    public CriteriaHolder visitAccessRule(JpqlParser.AccessRuleContext ctx) {
        return super.visitAccessRule(ctx);
    }

    @Override
    public CriteriaHolder visitFunctionsReturningStrings(JpqlParser.FunctionsReturningStringsContext ctx) {
        return super.visitFunctionsReturningStrings(ctx);
    }

    @Override
    public CriteriaHolder visitFunctionsReturningNumerics(JpqlParser.FunctionsReturningNumericsContext ctx) {
        return super.visitFunctionsReturningNumerics(ctx);
    }

    @Override
    public CriteriaHolder visitFunctionsReturningDateTime(JpqlParser.FunctionsReturningDateTimeContext ctx) {
        return super.visitFunctionsReturningDateTime(ctx);
    }

    @Override
    public CriteriaHolder visitHintStatement(JpqlParser.HintStatementContext ctx) {
        return super.visitHintStatement(ctx);
    }

    @Override
    public CriteriaHolder visitNoCacheIsAccessible(JpqlParser.NoCacheIsAccessibleContext ctx) {
        return super.visitNoCacheIsAccessible(ctx);
    }

    @Override
    public CriteriaHolder visitNoDbIsAccessible(JpqlParser.NoDbIsAccessibleContext ctx) {
        return super.visitNoDbIsAccessible(ctx);
    }

    @Override
    public CriteriaHolder visitNoCacheQueryOptimize(JpqlParser.NoCacheQueryOptimizeContext ctx) {
        return super.visitNoCacheQueryOptimize(ctx);
    }

    /*
    public boolean visit(JpqlSelect node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        List<Selection<?>> selections = query.<List<Selection<?>>>getCurrentValue();
        if (selections.size() == 1) {
            query.getCriteria().select((Selection<?>)selections.iterator().next());
        } else {
            query.getCriteria().multiselect(selections);
        }
        return visit(node);
    }

    public boolean visit(JpqlFromItem node, CriteriaHolder criteriaHolder) {
        AbstractQuery<?> query = criteriaHolder.getCurrentQuery();
        String entityName = node.jjtGetChild(0).toString();
        Alias alias = getAlias(node);
        Class<?> entityType = ManagedTypeFilter.forModel(metamodel).filter(entityName.trim()).getJavaType();
        query.from(entityType).alias(alias.getName());
        return false;
    }

    public boolean visit(JpqlWhere node, CriteriaHolder query) {
        AbstractQuery<?> criteriaQuery = query.getCurrentQuery();
        Predicate restriction = criteriaQuery.getRestriction();
        node.jjtGetChild(0).visit(this, query);
        if (query.isValueOfType(Predicate.class)) {
            if (restriction == null) {
                criteriaQuery.where(query.<Predicate>getCurrentValue());
            } else {
                criteriaQuery.where(restriction, query.<Predicate>getCurrentValue());
            }
        } else {
            if (restriction == null) {
                criteriaQuery.where(query.<Expression<Boolean>>getCurrentValue());
            } else {
                criteriaQuery.where(restriction, builder.isTrue(query.<Expression<Boolean>>getCurrentValue()));
            }
        }
        return false;
    }

    public boolean visit(JpqlInCollection node, CriteriaHolder query) {
        Expression<?> expression = query.<Expression<?>>getCurrentValue();
        node.jjtGetChild(0).visit(this, query);
        Collection<?> collection = query.<Collection<?>>getCurrentValue();
        query.setValue(expression.in(collection));
        return false;
    }

    public boolean visit(JpqlInnerJoin node, CriteriaHolder query) {
        Path path = new Path(node.jjtGetChild(0).toString());
        Alias alias = getAlias(node);
        From<?, ?> from = query.getFrom(path.getRootAlias());
        Join<Object, Object> join = from.join(path.getSubpath(), JoinType.INNER);
        if (alias != null) {
            join.alias(alias.getName());
        }
        return false;
    }

    public boolean visit(JpqlOuterJoin node, CriteriaHolder query) {
        Path path = new Path(node.jjtGetChild(0).toString());
        Alias alias = getAlias(node);
        From<?, ?> from = query.getFrom(path.getRootAlias());
        Join<Object, Object> join = from.join(path.getSubpath(), JoinType.LEFT);
        if (alias != null) {
            join.alias(alias.getName());
        }
        return false;
    }

    public boolean visit(JpqlOuterFetchJoin node, CriteriaHolder query) {
        Path path = new Path(node.jjtGetChild(0).toString());
        From<?, ?> from = query.getFrom(path.getRootAlias());
        from.fetch(path.toString(), JoinType.LEFT);
        return false;
    }

    public boolean visit(JpqlInnerFetchJoin node, CriteriaHolder query) {
        Path path = new Path(node.jjtGetChild(0).toString());
        From<?, ?> from = query.getFrom(path.getRootAlias());
        from.fetch(path.toString(), JoinType.INNER);
        return false;
    }

    public boolean visit(JpqlWith node, CriteriaHolder query) {
        throw new UnsupportedOperationException("WITH is not supported for criterias");
    }

    public boolean visit(JpqlPath node, CriteriaHolder query) {
        return visitPath(node, query);
    }

    public boolean visit(JpqlCollectionValuedPath node, CriteriaHolder query) {
        return visitPath(node, query);
    }

    public boolean visitPath(Node node, CriteriaHolder query) {
        Path path = new Path(node.toString());
        // Oliver Zhou: Call getFrom instead of getPath, getPath doesn't work with Join
        javax.persistence.criteria.Path<?> currentPath = query.getFrom(path.getRootAlias());

        if (path.getSubpath() != null) {
            for (String attribute: path.getSubpath().split("\\.")) {
                currentPath = currentPath.get(attribute);
            }
        }
        query.setValue(currentPath);
        return false;
    }

    public boolean visit(JpqlSetClause node, CriteriaHolder query) {
        throw new UnsupportedOperationException("SET not supported by criteria");
    }

    public boolean visit(JpqlSelectExpressions node, CriteriaHolder query) {
        List<Selection<?>> selections = new ArrayList<Selection<?>>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
            selections.add(query.<Selection<?>>getCurrentValue());
        }
        query.setValue(selections);
        return false;
    }

    public boolean visit(JpqlSelectExpression node, CriteriaHolder query) {
        validateChildCount(node, 1, 2);
        node.jjtGetChild(0).visit(this, query);
        Alias alias = getAlias(node);
        if (alias != null) {
            query.<Selection<?>>getCurrentValue().alias(alias.getName());
        }
        return false;
    }

    public boolean visit(JpqlConstructor node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        String entityName = query.<String>getCurrentValue();
        Class<?> entityType = ManagedTypeFilter.forModel(metamodel).filter(entityName.trim()).getJavaType();
        List<Selection<?>> constructorParameters = new ArrayList<Selection<?>>();
        query.setValue(constructorParameters);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        Selection<?>[] selection = constructorParameters.toArray(new Selection<?>[constructorParameters.size()]);
        query.setValue(builder.construct(entityType, selection));
        return false;
    }

    public boolean visit(JpqlConstructorParameter node, CriteriaHolder query) {
        List<Selection<?>> selections = query.<List<Selection<?>>>getCurrentValue();
        node.jjtGetChild(0).visit(this, query);
        selections.add(query.<Selection<?>>getCurrentValue());
        query.setValue(selections);
        return false;
    }

    public boolean visit(JpqlDistinct node, CriteriaHolder query) {
        query.getCriteria().distinct(true);
        return true;
    }

    public boolean visit(JpqlDistinctPath node, CriteriaHolder query) {
        query.getCriteria().distinct(true);
        return true;
    }

    public boolean visit(JpqlCount node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.count(query.<Expression<?>>getCurrentValue()));
        return false;
    }

    public boolean visit(JpqlAverage node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.avg(query.<Expression<Number>>getCurrentValue()));
        return false;
    }

    public boolean visit(JpqlMaximum node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.max(query.<Expression<Number>>getCurrentValue()));
        return false;
    }

    public boolean visit(JpqlMinimum node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.min(query.<Expression<Number>>getCurrentValue()));
        return false;
    }

    public boolean visit(JpqlSum node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.sum(query.<Expression<Number>>getCurrentValue()));
        return false;
    }

    public boolean visit(JpqlGroupBy node, CriteriaHolder query) {
        List<Expression<?>> groupByExpressions = new ArrayList<Expression<?>>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
            groupByExpressions.add(query.<Expression<?>>getCurrentValue());
        }
        query.getCriteria().groupBy(groupByExpressions);
        return false;
    }

    public boolean visit(JpqlHaving node, CriteriaHolder query) {
        List<Expression<Boolean>> havingExpressions = new ArrayList<Expression<Boolean>>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
            havingExpressions.add(query.<Expression<Boolean>>getCurrentValue());
        }
        if (havingExpressions.size() == 1) {
            query.getCriteria().having(havingExpressions.iterator().next());
        } else {
            query.getCriteria().having(havingExpressions.toArray(new Predicate[havingExpressions.size()]));
        }
        return false;
    }

    public boolean visit(JpqlSubselect node, CriteriaHolder query) {
        try {
            Subquery<Object> subquery = query.createSubquery();
            node.jjtGetChild(1).visit(this, query);
            node.jjtGetChild(0).visit(this, query);
            List<Selection<?>> selections = query.<List<Selection<?>>>getCurrentValue();
            subquery.select((Expression<Object>)selections.iterator().next());
            query.setValue(subquery);
            for (int i = 2; i < node.jjtGetNumChildren(); i++) {
                node.jjtGetChild(i).visit(this, query);
            }
            query.setValue(subquery);
            return false;
        } finally {
            query.removeSubquery();
        }
    }

    public boolean visit(JpqlOr node, CriteriaHolder query) {
        Predicate p1 = getPredicate(node.jjtGetChild(0), query);
        Predicate p2 = getPredicate(node.jjtGetChild(1), query);
        query.setValue(builder.or(p1, p2));
        return false;
    }

    public boolean visit(JpqlAnd node, CriteriaHolder query) {
        Predicate p1 = getPredicate(node.jjtGetChild(0), query);
        Predicate p2 = getPredicate(node.jjtGetChild(1), query);
        query.setValue(builder.and(p1, p2));
        return false;
    }

    public boolean visit(JpqlNot node, CriteriaHolder query) {
        query.setValue(getPredicate(node.jjtGetChild(0), query).not());
        return false;
    }

    public boolean visit(JpqlBetween node, CriteriaHolder query) {
        assert node.jjtGetNumChildren() == 3;
        node.jjtGetChild(0).visit(this, query);
        Expression<Comparable<Object>> e1 = query.<Expression<Comparable<Object>>>getCurrentValue();
        node.jjtGetChild(1).visit(this, query);
        if (query.isValueOfType(Expression.class)) {
            Expression<Comparable<Object>> e2 = query.<Expression<Comparable<Object>>>getCurrentValue();
            node.jjtGetChild(2).visit(this, query);
            Expression<Comparable<Object>> e3 = query.<Expression<Comparable<Object>>>getCurrentValue();
            query.setValue(builder.between(e1, e2, e3));
        } else {
            Comparable<Object> e2 = query.<Comparable<Object>>getCurrentValue();
            node.jjtGetChild(2).visit(this, query);
            Comparable<Object> e3 = query.<Comparable<Object>>getCurrentValue();
            query.setValue(builder.between(e1, e2, e3));
        }
        return false;
    }

    public boolean visit(JpqlIn node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        Expression<?> expression = query.<Expression<?>>getCurrentValue();
        node.jjtGetChild(1).visit(this, query);
        query.setValue(expression.in(query.<Subquery<?>>getCurrentValue()));
        return false;
    }

    public boolean visit(JpqlLike node, CriteriaHolder query) {
        assert node.jjtGetNumChildren() == 2;
        node.jjtGetChild(0).visit(this, query);
        node.jjtGetChild(1).visit(this, query);
        //the method visit(JpqlPatternValue node, CriteriaHolder query)
        //handles the actual creation of the like predicate
        return false;
    }

    public boolean visit(JpqlPatternValue node, CriteriaHolder query) {
        Expression<String> expression = query.<Expression<String>>getCurrentValue();
        node.jjtGetChild(0).visit(this, query);
        if (query.isValueOfType(Expression.class)) {
            Expression<String> patternExpression = query.<Expression<String>>getCurrentValue();
            if (node.jjtGetNumChildren() == 1) {
                query.setValue(builder.like(expression, patternExpression));
            } else {
                node.jjtGetChild(1).visit(this, query);
                if (query.isValueOfType(Expression.class)) {
                    Expression<Character> escapeExpression = query.<Expression<Character>>getCurrentValue();
                    query.setValue(builder.like(expression, patternExpression, escapeExpression));
                } else {
                    Character escapeCharacter = query.<Character>getCurrentValue();
                    query.setValue(builder.like(expression, patternExpression, escapeCharacter));
                }
            }
        } else {
            String patternValue = query.<String>getCurrentValue();
            if (node.jjtGetNumChildren() == 1) {
                query.setValue(builder.like(expression, patternValue));
            } else {
                node.jjtGetChild(1).visit(this, query);
                if (query.isValueOfType(Expression.class)) {
                    Expression<Character> escapeExpression = query.<Expression<Character>>getCurrentValue();
                    query.setValue(builder.like(expression, patternValue, escapeExpression));
                } else {
                    Character escapeCharacter = query.<Character>getCurrentValue();
                    query.setValue(builder.like(expression, patternValue, escapeCharacter));
                }
            }
        }
        return visit(node);
    }

    public boolean visit(JpqlIsNull node, CriteriaHolder query) {
        final JpqlPath pathElement = (JpqlPath)node.jjtGetChild(0);
        query.setValue(builder.isNull(getExpression(pathElement, query)));
        return false;
    }

    public boolean visit(JpqlIsEmpty node, CriteriaHolder query) {
        assert node.jjtGetNumChildren() == 1;
        query.setValue(builder.isEmpty(query.<Expression<Collection<?>>>getCurrentValue()));
        return false;
    }

    public boolean visit(JpqlMemberOf node, CriteriaHolder query) {
        assert node.jjtGetNumChildren() == 2;
        node.jjtGetChild(0).visit(this, query);
        buildMemberOf(node, query);
        return false;
    }

    public boolean visit(JpqlExists node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.exists(query.<Subquery<?>>getCurrentValue()));
        return false;
    }

    public boolean visit(JpqlAny node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.any(query.<Subquery<?>>getCurrentValue()));
        return false;
    }

    public boolean visit(JpqlAll node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.all(query.<Subquery<?>>getCurrentValue()));
        return false;
    }

    public boolean visit(JpqlEquals node, CriteriaHolder query) {
        Expression<?> e1 = getExpression(node.jjtGetChild(0), query);
        Expression<?> e2 = getExpression(node.jjtGetChild(1), query);
        if (e1 != null && e2 != null) {
            query.setValue(builder.equal(e1, e2));
        } else {
            query.setValue(builder.isTrue(builder.literal(false)));
        }
        return false;
    }

    public boolean visit(JpqlNotEquals node, CriteriaHolder query) {
        Expression<?> e1 = getExpression(node.jjtGetChild(0), query);
        Expression<?> e2 = getExpression(node.jjtGetChild(1), query);
        if (e1 != null && e2 != null) {
            query.setValue(builder.notEqual(e1, e2));
        } else {
            query.setValue(builder.isTrue(builder.literal(false)));
        }
        return false;
    }

    public boolean visit(JpqlGreaterThan node, CriteriaHolder query) {
        Expression<Comparable<Object>> e1 = getComparableExpression(node.jjtGetChild(0), query);
        Expression<Comparable<Object>> e2 = getComparableExpression(node.jjtGetChild(1), query);
        query.setValue(builder.greaterThan(e1, e2));
        return false;
    }

    public boolean visit(JpqlGreaterOrEquals node, CriteriaHolder query) {
        Expression<Comparable<Object>> e1 = getComparableExpression(node.jjtGetChild(0), query);
        Expression<Comparable<Object>> e2 = getComparableExpression(node.jjtGetChild(1), query);
        query.setValue(builder.greaterThanOrEqualTo(e1, e2));
        return false;
    }

    public boolean visit(JpqlLessThan node, CriteriaHolder query) {
        Expression<Comparable<Object>> e1 = getComparableExpression(node.jjtGetChild(0), query);
        Expression<Comparable<Object>> e2 = getComparableExpression(node.jjtGetChild(1), query);
        query.setValue(builder.lessThan(e1, e2));
        return false;
    }

    public boolean visit(JpqlLessOrEquals node, CriteriaHolder query) {
        Expression<Comparable<Object>> e1 = getComparableExpression(node.jjtGetChild(0), query);
        Expression<Comparable<Object>> e2 = getComparableExpression(node.jjtGetChild(1), query);
        query.setValue(builder.lessThanOrEqualTo(e1, e2));
        return false;
    }

    public boolean visit(JpqlAdd node, CriteriaHolder query) {
        Expression<Number> e1 = getNumberExpression(node.jjtGetChild(0), query);
        Expression<Number> e2 = getNumberExpression(node.jjtGetChild(1), query);
        query.setValue(builder.sum(e1, e2));
        return false;
    }

    public boolean visit(JpqlSubtract node, CriteriaHolder query) {
        Expression<Number> e1 = getNumberExpression(node.jjtGetChild(0), query);
        Expression<Number> e2 = getNumberExpression(node.jjtGetChild(1), query);
        query.setValue(builder.diff(e1, e2));
        return false;
    }

    public boolean visit(JpqlMultiply node, CriteriaHolder query) {
        Expression<Number> e1 = getNumberExpression(node.jjtGetChild(0), query);
        Expression<Number> e2 = getNumberExpression(node.jjtGetChild(1), query);
        query.setValue(builder.prod(e1, e2));
        return false;
    }

    public boolean visit(JpqlDivide node, CriteriaHolder query) {
        Expression<Number> e1 = getNumberExpression(node.jjtGetChild(0), query);
        Expression<Number> e2 = getNumberExpression(node.jjtGetChild(1), query);
        query.setValue(builder.quot(e1, e2));
        return false;
    }

    public boolean visit(JpqlNegative node, CriteriaHolder query) {
        query.setValue(builder.neg(getNumberExpression(node.jjtGetChild(0), query)));
        return true;
    }

    public boolean visit(JpqlConcat node, CriteriaHolder query) {
        Expression<String> e1 = getStringExpression(node.jjtGetChild(0), query);
        Expression<String> e2 = getStringExpression(node.jjtGetChild(1), query);
        query.setValue(builder.concat(e1, e2));
        return false;
    }

    public boolean visit(JpqlSubstring node, CriteriaHolder query) {
        Expression<String> e1 = getStringExpression(node.jjtGetChild(0), query);
        Expression<Integer> e2 = getIntegerExpression(node.jjtGetChild(1), query);
        if (node.jjtGetNumChildren() == 2) {
            query.setValue(builder.substring(e1, e2));
        } else {
            query.setValue(builder.substring(e1, e2, getIntegerExpression(node.jjtGetChild(2), query)));
        }
        return false;
    }

    public boolean visit(JpqlTrim node, CriteriaHolder query) {
        Trimspec trimspec = Trimspec.BOTH;
        Expression<Character> trimCharacter = builder.literal(' ');
        Expression<String> trimExpression;
        if (node.jjtGetNumChildren() > 1) {
            node.jjtGetChild(0).visit(this, query);
            if (query.isValueOfType(Trimspec.class)) {
                trimspec = query.<Trimspec>getCurrentValue();
                if (node.jjtGetNumChildren() == 3) {
                    trimCharacter = getCharacterExpression(node.jjtGetChild(1), query);
                }
            } else {
                trimCharacter = getCharacterExpression(query);
            }
        }
        trimExpression = getStringExpression(node.jjtGetChild(node.jjtGetNumChildren() - 1), query);
        query.setValue(builder.trim(trimspec, trimCharacter, trimExpression));
        return false;
    }

    public boolean visit(JpqlLower node, CriteriaHolder query) {
        query.setValue(builder.lower(getStringExpression(node.jjtGetChild(0), query)));
        return false;
    }

    public boolean visit(JpqlUpper node, CriteriaHolder query) {
        query.setValue(builder.upper(getStringExpression(node.jjtGetChild(0), query)));
        return false;
    }

    public boolean visit(JpqlTrimLeading node, CriteriaHolder query) {
        query.setValue(Trimspec.LEADING);
        return false;
    }

    public boolean visit(JpqlTrimTrailing node, CriteriaHolder query) {
        query.setValue(Trimspec.TRAILING);
        return false;
    }

    public boolean visit(JpqlTrimBoth node, CriteriaHolder query) {
        query.setValue(Trimspec.BOTH);
        return false;
    }

    public boolean visit(JpqlLength node, CriteriaHolder query) {
        query.setValue(builder.length(getStringExpression(node.jjtGetChild(0), query)));
        return false;
    }

    public boolean visit(JpqlLocate node, CriteriaHolder query) {
        Expression<String> e1 = getStringExpression(node.jjtGetChild(0), query);
        Expression<String> e2 = getStringExpression(node.jjtGetChild(1), query);
        if (node.jjtGetNumChildren() == 2) {
            query.setValue(builder.locate(e1, e2));
        } else {
            Expression<Integer> e3 = getIntegerExpression(node.jjtGetChild(2), query);
            query.setValue(builder.locate(e1, e2, e3));
        }
        return false;
    }

    public boolean visit(JpqlAbs node, CriteriaHolder query) {
        query.setValue(builder.abs(getNumberExpression(node.jjtGetChild(0), query)));
        return false;
    }

    public boolean visit(JpqlSqrt node, CriteriaHolder query) {
        query.setValue(builder.sqrt(getNumberExpression(node.jjtGetChild(0), query)));
        return false;
    }

    public boolean visit(JpqlMod node, CriteriaHolder query) {
        Expression<Integer> e1 = getIntegerExpression(node.jjtGetChild(0), query);
        Expression<Integer> e2 = getIntegerExpression(node.jjtGetChild(1), query);
        query.setValue(builder.mod(e1, e2));
        return false;
    }

    public boolean visit(JpqlSize node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.size(query.<Expression<Collection<?>>>getCurrentValue()));
        return false;
    }

    public boolean visit(JpqlCurrentDate node, CriteriaHolder query) {
        query.setValue(builder.currentDate());
        return true;
    }

    public boolean visit(JpqlCurrentTime node, CriteriaHolder query) {
        query.setValue(builder.currentTime());
        return true;
    }

    public boolean visit(JpqlCurrentTimestamp node, CriteriaHolder query) {
        query.setValue(builder.currentTimestamp());
        return true;
    }

    public boolean visit(JpqlOrderBy node, CriteriaHolder query) {
        List<Order> orders = new ArrayList<Order>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
            orders.add(query.<Order>getCurrentValue());
        }
        query.getCriteria().orderBy(orders);
        return false;
    }

    public boolean visit(JpqlOrderByItem node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        if (node.jjtGetNumChildren() == 1) {
            query.setValue(builder.asc(query.<Expression<?>>getCurrentValue()));
        } else {
            node.jjtGetChild(1).visit(this, query);
        }
        return false;
    }

    public boolean visit(JpqlAscending node, CriteriaHolder query) {
        query.setValue(builder.asc(query.<Expression<?>>getCurrentValue()));
        return true;
    }

    public boolean visit(JpqlDescending node, CriteriaHolder query) {
        query.setValue(builder.desc(query.<Expression<?>>getCurrentValue()));
        return true;
    }

    public boolean visit(JpqlIntegerLiteral node, CriteriaHolder query) {
        query.setValue(builder.literal(Integer.valueOf(node.getValue())));
        return true;
    }

    public boolean visit(JpqlDecimalLiteral node, CriteriaHolder query) {
        query.setValue(builder.literal(new BigDecimal(node.getValue())));
        return true;
    }

    public boolean visit(JpqlBooleanLiteral node, CriteriaHolder query) {
        query.setValue(builder.literal(Boolean.valueOf(node.getValue())));
        return true;
    }

    public boolean visit(JpqlStringLiteral node, CriteriaHolder query) {
        String value = node.getValue();
        query.setValue(builder.literal(value.substring(value.indexOf('\'') + 1, value.lastIndexOf('\''))));
        return true;
    }

    public boolean visit(JpqlNamedInputParameter node, CriteriaHolder query) {
        Parameter<?> parameter = builder.parameter(String.class, node.jjtGetChild(0).getValue());
        query.setValue(parameter);
        query.addParameter(parameter);
        return true;
    }

    public boolean visit(JpqlEscapeCharacter node, CriteriaHolder query) {
        query.setValue(builder.literal(node.getValue().charAt(1)));
        return true;
    }

    public boolean visit(JpqlTrimCharacter node, CriteriaHolder query) {
        query.setValue(builder.literal(node.getValue().charAt(1)));
        return true;
    }


    private <V, W extends Collection<V>> void buildMemberOf(JpqlMemberOf node, CriteriaHolder query) {
        if (query.isValueOfType(Expression.class)) {
            Expression<V> memberExpression = query.<Expression<V>>getCurrentValue();
            node.jjtGetChild(1).visit(this, query);
            Expression<W> collectionExpression = query.<Expression<W>>getCurrentValue();
            query.setValue(builder.isMember(memberExpression, collectionExpression));
        } else {
            Object member = query.getValue();
            node.jjtGetChild(1).visit(this, query);
            Expression<Collection<Object>> collectionExpression
                    = query.<Expression<Collection<Object>>>getCurrentValue();
            query.setValue(builder.isMember(member, collectionExpression));
        }
    }

    private Alias getAlias(Node node) {
        if (node.jjtGetNumChildren() == 1) {
            return null;
        }
        return new Alias(node.jjtGetChild(1).toString());
    }

    private Predicate getPredicate(Node node, CriteriaHolder query) {
        node.visit(this, query);
        return getPredicate(query);
    }

    private Predicate getPredicate(CriteriaHolder query) {
        if (query.isValueOfType(Predicate.class)) {
            return query.<Predicate>getCurrentValue();
        } else if (query.isValueOfType(Expression.class)) {
            return builder.isTrue(query.<Expression<Boolean>>getCurrentValue());
        } else if (query.isValueOfType(Boolean.class)) {
            return builder.isTrue(builder.literal(query.<Boolean>getCurrentValue()));
        } else {
            throw new IllegalStateException("Expected type boolean, but was "
                                            + query.getValue().getClass().getSimpleName());
        }
    }

    private Expression<?> getExpression(Node node, CriteriaHolder query) {
        node.visit(this, query);
        return getExpression(query);
    }

    private Expression<?> getExpression(CriteriaHolder query) {
        if (query.isValueOfType(Expression.class)) {
            return query.<Expression<?>>getCurrentValue();
        } else if (query.getValue() != null) {
            return builder.literal(query.getValue());
        } else {
            return null;
        }
    }

    private Expression<String> getStringExpression(Node node, CriteriaHolder query) {
        node.visit(this, query);
        return getStringExpression(query);
    }

    private Expression<String> getStringExpression(CriteriaHolder query) {
        if (query.isValueOfType(Expression.class)) {
            return query.<Expression<String>>getCurrentValue();
        } else if (query.isValueOfType(String.class)) {
            return builder.literal(query.<String>getCurrentValue());
        } else {
            throw new IllegalStateException("Expected type String, but was "
                                            + query.getValue().getClass().getSimpleName());
        }
    }

    private Expression<Character> getCharacterExpression(Node node, CriteriaHolder query) {
        node.visit(this, query);
        return getCharacterExpression(query);
    }

    private Expression<Character> getCharacterExpression(CriteriaHolder query) {
        if (query.isValueOfType(Expression.class)) {
            return query.<Expression<Character>>getCurrentValue();
        } else if (query.isValueOfType(Character.class)) {
            return builder.literal(query.<Character>getCurrentValue());
        } else {
            throw new IllegalStateException("Expected type char, but was "
                                            + query.getValue().getClass().getSimpleName());
        }
    }

    private Expression<Integer> getIntegerExpression(Node node, CriteriaHolder query) {
        node.visit(this, query);
        return getIntegerExpression(query);
    }

    private Expression<Integer> getIntegerExpression(CriteriaHolder query) {
        if (query.isValueOfType(Expression.class)) {
            return query.<Expression<Integer>>getCurrentValue();
        } else if (query.isValueOfType(Integer.class)) {
            return builder.literal(query.<Integer>getCurrentValue());
        } else {
            throw new IllegalStateException("Expected type int, but was "
                                            + query.getValue().getClass().getSimpleName());
        }
    }

    private Expression<Number> getNumberExpression(Node node, CriteriaHolder query) {
        node.visit(this, query);
        return getNumberExpression(query);
    }

    private Expression<Number> getNumberExpression(CriteriaHolder query) {
        if (query.isValueOfType(Expression.class)) {
            return query.<Expression<Number>>getCurrentValue();
        } else if (query.isValueOfType(Number.class)) {
            return builder.literal(query.<Number>getCurrentValue());
        } else {
            throw new IllegalStateException("Expected type Number, but was "
                                            + query.getValue().getClass().getSimpleName());
        }
    }

    private Expression<Comparable<Object>> getComparableExpression(Node node, CriteriaHolder query) {
        node.visit(this, query);
        return getComparableExpression(query);
    }

    private Expression<Comparable<Object>> getComparableExpression(CriteriaHolder query) {
        if (query.isValueOfType(Expression.class)) {
            return query.<Expression<Comparable<Object>>>getCurrentValue();
        } else if (query.isValueOfType(Comparable.class)) {
            return builder.literal(query.<Comparable<Object>>getCurrentValue());
        } else {
            throw new IllegalStateException("Expected type Number, but was "
                                            + query.getValue().getClass().getSimpleName());
        }
    }
    */
}
