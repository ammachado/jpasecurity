/*
 * Copyright 2008 - 2016 Arne Limburg
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
package org.jpasecurity.jpql.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jpasecurity.Path;
import org.jpasecurity.access.SecurePersistenceUnitUtil;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import org.jpasecurity.persistence.mapping.ManagedTypeFilter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static org.jpasecurity.util.Validate.notNull;

/**
 * This implementation of the {@link JpqlVisitorAdapter} evaluates queries in memory,
 * storing the result in the specified {@link QueryEvaluationParameters}.
 *
 * If the evaluation cannot be performed due to missing information the result is set to <quote>undefined</quote>.
 * To evaluate subselect-query, pluggable implementations of {@link SubQueryEvaluator} are used.
 *
 * @author Arne Limburg
 */
public class QueryEvaluatorImpl extends JpqlVisitorAdapter<QueryEvaluationParameters> implements QueryEvaluator {

    public static final int DECIMAL_PRECISION = 100;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final JpqlCompiler compiler;
    private final SecurePersistenceUnitUtil util;
    private final SubQueryEvaluator[] subselectEvaluators;

    public QueryEvaluatorImpl(JpqlCompiler compiler, SecurePersistenceUnitUtil util, SubQueryEvaluator... evaluators) {
        super(null);
        this.compiler = notNull(JpqlCompiler.class, compiler);
        this.util = notNull(SecurePersistenceUnitUtil.class, util);
        this.subselectEvaluators = evaluators;
        for (SubQueryEvaluator subselectEvaluator: evaluators) {
            subselectEvaluator.setQueryEvaluator(this);
        }
    }

    @Override
    public boolean canEvaluate(ParserRuleContext node, QueryEvaluationParameters parameters) {
        try {
            evaluate(node, parameters);
            return true;
        } catch (NotEvaluatableException e) {
            return false;
        }
    }

    @Override
    public <R> R evaluate(ParserRuleContext node, QueryEvaluationParameters parameters) throws NotEvaluatableException {
        setDefaultResult(parameters);
        this.visit(node);
        return parameters.getResult();
    }

    @Override
    public QueryEvaluationParameters visitSelectClause(JpqlParser.SelectClauseContext ctx) {
        defaultResult().setResultUndefined();
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitFromClause(JpqlParser.FromClauseContext ctx) {
        defaultResult().setResultUndefined();
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitGroupByClause(JpqlParser.GroupByClauseContext ctx) {
        defaultResult().setResultUndefined();
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitHavingClause(JpqlParser.HavingClauseContext ctx) {
        defaultResult().setResultUndefined();
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitOrderByClause(JpqlParser.OrderByClauseContext ctx) {
        defaultResult().setResultUndefined();
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitNegatedPredicate(JpqlParser.NegatedPredicateContext ctx) {
        visit(ctx.predicate());
        try {
            defaultResult().setResult(!((Boolean) defaultResult().getResult()));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitOrPredicate(JpqlParser.OrPredicateContext ctx) {
        boolean undefined = false;
        visit(ctx.predicate(0));
        try {
            if (defaultResult().resultIsBoolean() && defaultResult().<Boolean>getResult()) {
                return stopVisitingChildren();
            }
        } catch (NotEvaluatableException e) {
            undefined = true;
        }

        visit(ctx.predicate(1));
        try {
            if (defaultResult().resultIsBoolean() && defaultResult().<Boolean>getResult()) {
                return stopVisitingChildren();
            }
        } catch (NotEvaluatableException e) {
            undefined = true;
        }

        if (undefined) {
            defaultResult().setResultUndefined();
        } else {
            defaultResult().setResult(false);
        }

        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitAndPredicate(JpqlParser.AndPredicateContext ctx) {
        boolean undefined = false;
        boolean result = true;
        visit(ctx.predicate(0));
        try {
            result = defaultResult().<Boolean>getResult();
        } catch (NotEvaluatableException e) {
            undefined = true;
        }

        visit(ctx.predicate(0));
        try {
            result &= defaultResult().<Boolean>getResult();
        } catch (NotEvaluatableException e) {
            undefined = true;
        }
        if (undefined) {
            defaultResult().setResultUndefined();
        } else {
            defaultResult().setResult(result);
        }
        return stopVisitingChildren();
    }

    /*
    @Override
    public boolean visit(JpqlPath node, QueryEvaluationParameters data) {
        try {
            node.getChild(0).visit(this, data);
            Path path = new Path(node.toString());
            if (path.hasSubpath()) {
                PathEvaluator pathEvaluator = new MappedPathEvaluator(data.getMetamodel(), util);
                data.setResult(pathEvaluator.evaluate(data.getResult(), path.getSubpath()));
            }
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
        }
        return false;
    }

    @Override
    public boolean visit(JpqlCollectionValuedPath node, QueryEvaluationParameters data) {
        try {
            node.getChild(0).visit(this, data);
            Path path = new Path(node.toString());
            if (path.hasSubpath()) {
                PathEvaluator pathEvaluator = new MappedPathEvaluator(data.getMetamodel(), util);
                data.setResult(pathEvaluator.evaluateAll(Collections.singleton(data.getResult()), path.getSubpath()));
            }
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
        }
        return false;
    }
    */

    @Override
    public QueryEvaluationParameters visitBetweenPredicate(JpqlParser.BetweenPredicateContext ctx) {
        try {
            visit(ctx.expression(0));
            Comparable<Object> value = defaultResult().getResult();
            visit(ctx.expression(1));
            Comparable<Object> lower;
            try {
                lower = defaultResult().getResult();
            } catch (NotEvaluatableException e) {
                lower = null;
            }
            visit(ctx.expression(2));
            Comparable<Object> upper;
            try {
                upper = defaultResult().getResult();
            } catch (NotEvaluatableException e) {
                upper = null;
            }
            if ((lower != null && lower.compareTo(value) > 0) || (upper != null && upper.compareTo(value) < 0)) {
                defaultResult().setResult(false);
            } else if (lower == null || upper == null) {
                defaultResult().setResultUndefined();
            } else {
                defaultResult().setResult(true);
            }
            if (!defaultResult().isResultUndefined() && ctx.NOT() != null) {
                boolean result = defaultResult().getResult();
                defaultResult().setResult(!result);
            }
        } catch (ClassCastException e) {
            defaultResult().setResultUndefined();
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitInPredicate(JpqlParser.InPredicateContext ctx) {
        try {
            Object expression = visit(ctx.expression()).getResult();
            Collection<?> inList = getResultCollection(visit(ctx.inList()));
        } catch (NotEvaluatableException e) {
            //
        }
        return super.visitInPredicate(ctx);
    }

    /*
    @Override
    public boolean visit(JpqlIn node, QueryEvaluationParameters data) {
        Object value;
        try {
            node.getChild(0).visit(this, data);
            value = convert(data.getResult());
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
            return false;
        }
        boolean undefined = false;
        Collection<Object> values = new ArrayList<>();
        for (int i = 1; i < node.getChildCount(); i++) {
            node.getChild(i).visit(this, data);
            try {
                if (data.getResult() instanceof Collection) {
                    values.addAll(convertAll(data.<Collection<?>>getResult()));
                } else {
                    values.add(convert(data.getResult()));
                }
            } catch (NotEvaluatableException e) {
                undefined = true;
            }
        }
        if (values.contains(value)) {
            data.setResult(Boolean.TRUE);
        } else if (undefined) {
            data.setResultUndefined();
        } else {
            data.setResult(Boolean.FALSE);
        }
        return false;
    }
    */

    @Override
    public QueryEvaluationParameters visitLikePredicate(JpqlParser.LikePredicateContext ctx) {
        try {
            visit(ctx.expression(0));
            String text = defaultResult().getResult();
            visit(ctx.expression(1));
            String pattern = defaultResult().getResult();
            defaultResult().setResult(text.matches(createRegularExpression(pattern)));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitIsNullPredicate(JpqlParser.IsNullPredicateContext ctx) {
        try {
            visit(ctx.expression());
            if (defaultResult().getResult() instanceof Collection) {
                defaultResult().setResult(((Collection<?>)defaultResult().getResult()).isEmpty());
            } else {
                defaultResult().setResult(defaultResult().getResult() == null);
            }
            if (!defaultResult().isResultUndefined() && ctx.NOT() != null) {
                boolean result = defaultResult().getResult();
                defaultResult().setResult(!result);
            }
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitIsEmptyPredicate(JpqlParser.IsEmptyPredicateContext ctx) {
        try {
            visit(ctx.expression());
            Collection<?> result = defaultResult().getResult();
            defaultResult().setResult(result == null || result.isEmpty());
            if (!defaultResult().isResultUndefined() && ctx.NOT() != null) {
                boolean r = defaultResult().getResult();
                defaultResult().setResult(!r);
            }
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitMemberOfPredicate(JpqlParser.MemberOfPredicateContext ctx) {
        try {
            visit(ctx.expression());
            Object value = defaultResult().getResult();
            visit(ctx.path());
            defaultResult().setResult(((Collection<?>)defaultResult().getResult()).contains(value));
            if (!defaultResult().isResultUndefined() && ctx.NOT() != null) {
                boolean r = defaultResult().getResult();
                defaultResult().setResult(!r);
            }
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitEqualityPredicate(JpqlParser.EqualityPredicateContext ctx) {
        try {
            Object value1 = convert(visit(ctx.expression(0)).getResult());
            Path rightHandSide = new Path(ctx.expression(1).getText());
            if (value1 == null) {
                defaultResult().setResult(false);
            } else if (rightHandSide.isEnumValue()) {
                Object value2 = rightHandSide.getEnumValue();
                defaultResult().setResult(value1.equals(value2));
            } else {
                Object value2 = convert(visit(ctx.expression(1)).getResult());
                defaultResult().setResult(Objects.equals(value1, value2));
            }
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitInequalityPredicate(JpqlParser.InequalityPredicateContext ctx) {
        try {
            Object value1 = visit(ctx.expression(0)).getResult();
            Path rightHandSide = new Path(ctx.expression(1).getText());
            if ((value1 == null || value1 instanceof Enum) && rightHandSide.isEnumValue()) {
                Object value2 = rightHandSide.getEnumValue();
                defaultResult().setResult(!value2.equals(value1));
            } else {
                Object value2 = convert(visit(ctx.expression(1)).getResult());
                defaultResult().setResult(!Objects.equals(value1, value2));
            }
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitGreaterThanPredicate(JpqlParser.GreaterThanPredicateContext ctx) {
        try {
            Comparable<Object> value1 = visit(ctx.expression(0)).getResult();
            Comparable<Object> value2 = visit(ctx.expression(1)).getResult();
            defaultResult().setResult(value1.compareTo(value2) > 0);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitGreaterThanOrEqualPredicate(JpqlParser.GreaterThanOrEqualPredicateContext ctx) {
        try {
            Comparable<Object> value1 = visit(ctx.expression(0)).getResult();
            Comparable<Object> value2 = visit(ctx.expression(1)).getResult();
            defaultResult().setResult(value1.compareTo(value2) >= 0);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitLessThanPredicate(JpqlParser.LessThanPredicateContext ctx) {
        try {
            Comparable<Object> value1 = visit(ctx.expression(0)).getResult();
            Comparable<Object> value2 = visit(ctx.expression(1)).getResult();
            defaultResult().setResult(value1.compareTo(value2) < 0);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitLessThanOrEqualPredicate(JpqlParser.LessThanOrEqualPredicateContext ctx) {
        try {
            Comparable<Object> value1 = visit(ctx.expression(0)).getResult();
            Comparable<Object> value2 = visit(ctx.expression(1)).getResult();
            defaultResult().setResult(value1.compareTo(value2) <= 0);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitAdditionExpression(JpqlParser.AdditionExpressionContext ctx) {
        try {
            BigDecimal value1 = new BigDecimal(visit(ctx.expression(0)).getResult().toString());
            BigDecimal value2 = new BigDecimal(visit(ctx.expression(1)).getResult().toString());
            defaultResult().setResult(value1.add(value2));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitSubtractionExpression(JpqlParser.SubtractionExpressionContext ctx) {
        try {
            BigDecimal value1 = new BigDecimal(visit(ctx.expression(0)).getResult().toString());
            BigDecimal value2 = new BigDecimal(visit(ctx.expression(1)).getResult().toString());
            defaultResult().setResult(value1.subtract(value2));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitMultiplicationExpression(JpqlParser.MultiplicationExpressionContext ctx) {
        try {
            BigDecimal value1 = new BigDecimal(visit(ctx.expression(0)).getResult().toString());
            BigDecimal value2 = new BigDecimal(visit(ctx.expression(1)).getResult().toString());
            defaultResult().setResult(value1.multiply(value2));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitDivisionExpression(JpqlParser.DivisionExpressionContext ctx) {
        try {
            BigDecimal value1 = new BigDecimal(visit(ctx.expression(0)).getResult().toString());
            BigDecimal value2 = new BigDecimal(visit(ctx.expression(1)).getResult().toString());
            defaultResult().setResult(value1.divide(value2));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitUnaryMinusExpression(JpqlParser.UnaryMinusExpressionContext ctx) {
        try {
            BigDecimal value1 = new BigDecimal(visit(ctx.expression()).getResult().toString());
            defaultResult().setResult(value1.multiply(new BigDecimal(-1)));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitUnaryPlusExpression(JpqlParser.UnaryPlusExpressionContext ctx) {
        try {
            defaultResult().setResult(new BigDecimal(visit(ctx.expression()).getResult().toString()));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitConcatFunction(JpqlParser.ConcatFunctionContext ctx) {
        try {visit(ctx.expression(0));
            StringBuilder sb = new StringBuilder(defaultResult().<String>getResult());
            for (int i = 1; i < ctx.expression().size(); i++) {
                visit(ctx.expression(i));
                sb.append(defaultResult().getResult());
            }
            defaultResult().setResult(sb.toString());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitSubstringFunction(JpqlParser.SubstringFunctionContext ctx) {
        try {
            visit(ctx.expression());
            String text = defaultResult().getResult();
            visit(ctx.substringFunctionStartArgument());
            int fromIndex = new BigDecimal(defaultResult().getResult().toString()).intValue();
            int toIndex = 0;
            if (ctx.substringFunctionLengthArgument() != null) {
                visit(ctx.substringFunctionLengthArgument());
                toIndex = new BigDecimal(defaultResult().getResult().toString()).intValue();
            }
            defaultResult().setResult(text.substring(fromIndex, toIndex));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitTrimFunction(JpqlParser.TrimFunctionContext ctx) {
        try {
            boolean leading = true;
            boolean trailing = true;
            boolean trimSpecificationPresent = ctx.trimSpecification() != null;
            if (ctx.trimSpecification() != null) {
                switch (ctx.trimSpecification().getText().toLowerCase()) {
                    case "leading":
                        trailing = false;
                        break;
                    case "trailing":
                        leading = false;
                        break;
                }
            }
            char trimCharacter = ' ';
            if (trimSpecificationPresent && ctx.trimCharacter() != null) {
                visit(ctx.trimCharacter());
                trimCharacter = ((String)defaultResult().getResult()).charAt(0);
            }
            visit(ctx.expression());
            String text = defaultResult().getResult();
            StringBuilder builder = new StringBuilder(text);
            if (leading) {
                while (builder.charAt(0) == trimCharacter) {
                    builder.deleteCharAt(0);
                }
            }
            if (trailing) {
                while (builder.charAt(builder.length() - 1) == trimCharacter) {
                    builder.deleteCharAt(builder.length() - 1);
                }
            }
            defaultResult().setResult(builder.toString());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitLowerFunction(JpqlParser.LowerFunctionContext ctx) {
        try {
            defaultResult().setResult(visit(ctx.expression()).getResult().toString().toLowerCase());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitUpperFunction(JpqlParser.UpperFunctionContext ctx) {
        try {
            defaultResult().setResult(visit(ctx.expression()).getResult().toString().toUpperCase());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitLengthFunction(JpqlParser.LengthFunctionContext ctx) {
        try {
            defaultResult().setResult(visit(ctx.expression()).getResult().toString().length());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitLocateFunction(JpqlParser.LocateFunctionContext ctx) {
        try {
            String text = visit(ctx.locateFunctionSubstrArgument()).getResult().toString();
            String substring = visit(ctx.locateFunctionStringArgument()).getResult().toString();
            int start = 0;
            if (ctx.locateFunctionStartArgument() != null) {
                start = new BigInteger(visit(ctx.locateFunctionStartArgument()).getResult().toString()).intValue();
            }
            defaultResult().setResult(text.indexOf(substring, start));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitAbsFunction(JpqlParser.AbsFunctionContext ctx) {
        try {
            defaultResult().setResult(new BigDecimal(visit(ctx.expression()).getResult().toString()).abs());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitSqrtFunction(JpqlParser.SqrtFunctionContext ctx) {
        try {
            final String value = visit(ctx.expression()).getResult().toString();
            defaultResult().setResult(Math.sqrt(new BigDecimal(value).doubleValue()));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitModFunction(JpqlParser.ModFunctionContext ctx) {
        try {
            int i1 = Integer.parseInt(visit(ctx.modDividendArgument()).getResult().toString());
            int i2 = Integer.parseInt(visit(ctx.modDivisorArgument()).getResult().toString());
            defaultResult().setResult(i1 % i2);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitSizeFunction(JpqlParser.SizeFunctionContext ctx) {
        try {
            defaultResult().setResult(((Collection<?>)visit(ctx.path()).getResult()).size());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitCurrentDateFunction(JpqlParser.CurrentDateFunctionContext ctx) {
        defaultResult().setResult(new Date(new java.util.Date().getTime()));
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitCurrentTimeFunction(JpqlParser.CurrentTimeFunctionContext ctx) {
        defaultResult().setResult(new Time(new java.util.Date().getTime()));
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitCurrentTimestampFunction(JpqlParser.CurrentTimestampFunctionContext ctx) {
        defaultResult().setResult(new Timestamp(new java.util.Date().getTime()));
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitMainEntityPersisterReference(JpqlParser.MainEntityPersisterReferenceContext ctx) {
        defaultResult().setResult(ManagedTypeFilter.forModel(defaultResult().getMetamodel()).filter(ctx.getText().trim()).getJavaType());
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitStringLiteral(JpqlParser.StringLiteralContext ctx) {
        defaultResult().setResult(ctx.getText());
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitCharacterLiteral(JpqlParser.CharacterLiteralContext ctx) {
        defaultResult().setResult(ctx.getText());
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitIntegerLiteral(JpqlParser.IntegerLiteralContext ctx) {
        defaultResult().setResult(Integer.valueOf(ctx.getText()));
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitLongLiteral(JpqlParser.LongLiteralContext ctx) {
        defaultResult().setResult(Long.valueOf(ctx.getText()));
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitBigIntegerLiteral(JpqlParser.BigIntegerLiteralContext ctx) {
        defaultResult().setResult(new BigInteger(ctx.getText()));
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitFloatLiteral(JpqlParser.FloatLiteralContext ctx) {
        defaultResult().setResult(Float.valueOf(ctx.getText()));
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitDoubleLiteral(JpqlParser.DoubleLiteralContext ctx) {
        defaultResult().setResult(Double.valueOf(ctx.getText()));
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitBigDecimalLiteral(JpqlParser.BigDecimalLiteralContext ctx) {
        defaultResult().setResult(new BigDecimal(ctx.getText()));
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitHexLiteral(JpqlParser.HexLiteralContext ctx) {
        defaultResult().setResult(Integer.parseInt(ctx.getText(), 16));
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitOctalLiteral(JpqlParser.OctalLiteralContext ctx) {
        defaultResult().setResult(Integer.parseInt(ctx.getText(), 8));
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitNullLiteral(JpqlParser.NullLiteralContext ctx) {
        defaultResult().setResult(null);
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitBooleanLiteral(JpqlParser.BooleanLiteralContext ctx) {
        defaultResult().setResult(Boolean.valueOf(ctx.getText()));
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitTimestampLiteral(JpqlParser.TimestampLiteralContext ctx) {
        defaultResult().setResult(Timestamp.valueOf(ctx.getText()));
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitDateLiteral(JpqlParser.DateLiteralContext ctx) {
        defaultResult().setResult(java.sql.Date.valueOf(ctx.getText()));
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitTimeLiteral(JpqlParser.TimeLiteralContext ctx) {
        defaultResult().setResult(Time.valueOf(ctx.getText()));
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitNamedParameter(JpqlParser.NamedParameterContext ctx) {
        try {
            defaultResult().setResult(defaultResult().getNamedParameterValue(ctx.identifier().getText()));
        } catch (NotEvaluatableException e) {
            defaultResult().setResultUndefined();
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitPositionalParameter(JpqlParser.PositionalParameterContext ctx) {
        try {
            defaultResult().setResult(defaultResult()
                    .getPositionalParameterValue(Integer.valueOf(ctx.INTEGER_LITERAL().getText())));
        } catch (NotEvaluatableException e) {
            defaultResult().setResultUndefined();
        }
        return stopVisitingChildren();
    }

    /*
    @Override
    public boolean visit(JpqlIdentificationVariable node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        try {
            data.setResult(data.getAliasValue(new Alias(node.getValue())));
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
        }
        return false;
    }

    @Override
    public boolean visit(JpqlEscapeCharacter node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(node.getValue());
        return false;
    }

    @Override
    public boolean visit(JpqlTrimCharacter node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(node.getValue().substring(1, node.getValue().length() - 1)); //trim quotes
        return false;
    }

    @Override
    public boolean visit(JpqlExists node, QueryEvaluationParameters data) {
        try {
            node.getChild(0).visit(this, data);
            data.setResult(!((Collection<?>)data.getResult()).isEmpty());
        } catch (NotEvaluatableException e) {
            //result is undefined
        }
        return false;
    }

    @Override
    public boolean visit(JpqlSubselect node, QueryEvaluationParameters evaluationParameters) {
        JpqlCompiledStatement subselect = compiler.compile(node);
        for (SubQueryEvaluator subselectEvaluator: subselectEvaluators) {
            if (subselectEvaluator.canEvaluate(node, evaluationParameters)) {
                try {
                    evaluationParameters.setResult(subselectEvaluator.evaluate(subselect, evaluationParameters));
                    return false;
                } catch (NotEvaluatableException e) {
                    evaluationParameters.setResultUndefined();
                }
            }
        }
        return false;
    }

    @Override
    public boolean visit(JpqlKey node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        node.getChild(0).visit(this, data);
        try {
            Map<?, ?> result = data.getResult();
            if (result != null) {
                data.setResult(result.keySet());
            }
        } catch (NotEvaluatableException e) {
            // ignore
        }
        return false;
    }

    @Override
    public boolean visit(JpqlValue node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        node.getChild(0).visit(this, data);
        try {
            Map<?, ?> result = data.getResult();
            if (result != null) {
                data.setResult(result.values());
            }
        } catch (NotEvaluatableException e) {
            // ignore
        }
        return false;
    }

    @Override
    public boolean visit(JpqlEntry node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        node.getChild(0).visit(this, data);
        try {
            Map<?, ?> result = data.getResult();
            if (result != null) {
                if (result.isEmpty()) {
                    data.setResult(null);
                } else {
                    data.setResult(result.entrySet());
                }
            }
        } catch (NotEvaluatableException e) {
            // ignore
        }
        return false;
    }

    @Override
    public boolean visit(JpqlType node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        node.getChild(0).visit(this, data);
        try {
            data.setResult(forModel(data.getMetamodel()).filter(data.getResult().getClass()).getJavaType());
        } catch (NotEvaluatableException e) {
            // ignore
        }
        return false;
    }

    @Override
    public boolean visit(JpqlCase node, QueryEvaluationParameters data) {
        for (int i = 0; i < node.getChildCount(); i++) {
            Node child = node.getChild(i);
            if (child instanceof JpqlWhen) {
                try {
                    child.visit(this, data);
                    if (data.<Boolean>getResult()) {
                        child.getChild(1).visit(this, data);
                        return false;
                    }
                } catch (NotEvaluatableException e) {
                    data.setResultUndefined();
                    return false;
                }
            }
        }
        return visit(node.getChild(node.getChildCount() - 1));
    }
    */

    @Override
    public QueryEvaluationParameters visitCoalesce(JpqlParser.CoalesceContext ctx) {
        for (int i = 0; i < ctx.expression().size(); i++) {
            this.visit(ctx.expression(i));
            try {
                if (defaultResult().getResult() != null) {
                    return stopVisitingChildren();
                }
            } catch (NotEvaluatableException e) {
                // result is undefined
                return stopVisitingChildren();
            }
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitNullIf(JpqlParser.NullIfContext ctx) {
        try {
            visit(ctx.expression(0));
            Object result = defaultResult().getResult();
            if (result == null) {
                // result will stay null
                return stopVisitingChildren();
            }
            visit(ctx.expression(1));
            if (result.equals(defaultResult().getResult())) {
                defaultResult().setResult(null);
            } else {
                defaultResult().setResult(result);
            }
        } catch (NotEvaluatableException e1) {
            // result is undefined
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitSimpleCaseWhen(JpqlParser.SimpleCaseWhenContext ctx) {
        try {
            JpqlParser.SimpleCaseStatementContext caseStatement = (JpqlParser.SimpleCaseStatementContext)ctx.parent;
            Object searchExpression = visit(caseStatement.expression()).getResult();
            Object when = visit(ctx.expression(0)).getResult();
            Object then = visit(ctx.expression(1)).getResult();
            if (Objects.equals(searchExpression, when)) {
                defaultResult().setResult(then);
            } else {
                defaultResult().setResultUndefined();
            }
        } catch (NotEvaluatableException e) {
            defaultResult().setResultUndefined();
        }
        return stopVisitingChildren();
    }

    @Override
    public QueryEvaluationParameters visitSearchedCaseWhen(JpqlParser.SearchedCaseWhenContext ctx) {
        try {
            boolean when = visit(ctx.predicate()).getResult();
            Object then = visit(ctx.expression()).getResult();
            if (when) {
                defaultResult().setResult(then);
            } else {
                defaultResult().setResultUndefined();
            }
        } catch (NotEvaluatableException e) {
            defaultResult().setResultUndefined();
        }
        return stopVisitingChildren();
    }

    protected Collection<?> getResultCollection(Object result) {
        if (result == null) {
            return Collections.emptySet();
        } else if (result instanceof Collection) {
            return (Collection<?>) result;
        } else {
            return Collections.singleton(result);
        }
    }

    private String createRegularExpression(String pattern) {
        StringBuilder regularExpressionBuilder = new StringBuilder();
        int index = 0;
        int specialCharacterIndex = indexOfSpecialCharacter(pattern, index);
        appendSubPattern(regularExpressionBuilder, pattern, index, specialCharacterIndex);
        while (specialCharacterIndex < pattern.length()) {
            index = specialCharacterIndex;
            if (pattern.charAt(index) == '\\') {
                index++;
            }
            if (pattern.charAt(index) == '_') {
                regularExpressionBuilder.append('.');
                index++;
            } else { //if (pattern.charAt(index) == '%') {
                regularExpressionBuilder.append(".*");
                index++;
            }
            specialCharacterIndex = indexOfSpecialCharacter(pattern, index);
            appendSubPattern(regularExpressionBuilder, pattern, index, specialCharacterIndex);
        }
        return regularExpressionBuilder.toString();
    }

    /**
     * Returns the index of the next special character within the specified pattern
     * starting at the specified index or the length of the pattern, if no special character is present.
     */
    private int indexOfSpecialCharacter(String pattern, int startIndex) {
        int i1 = pattern.indexOf("\\_", startIndex);
        int i2 = pattern.indexOf("_", startIndex);
        int i3 = pattern.indexOf("\\%", startIndex);
        int i4 = pattern.indexOf("%", startIndex);
        int min = pattern.length();
        if (i1 > -1) {
            min = i1;
        }
        if (i2 > -1 && i2 < min) {
            min = i2;
        }
        if (i3 > -1 && i3 < min) {
            min = i3;
        }
        if (i4 > -1 && i4 < min) {
            min = i4;
        }
        return min;
    }

    private void appendSubPattern(StringBuilder regularExpression, String pattern, int startIndex, int endIndex) {
        String subpattern = pattern.substring(startIndex, endIndex);
        if (subpattern.length() > 0) {
            regularExpression.append("\\Q").append(subpattern).append("\\E");
        }
    }

    private Collection<?> convertAll(Collection<?> collection) {
        Collection<Object> result = new ArrayList<>(collection.size());
        for (Object value: collection) {
            result.add(convert(value));
        }
        return result;
    }

    private Object convert(Object object) {
        if (!(object instanceof Number) || object instanceof BigDecimal) {
            return object;
        } else if (object instanceof Float) {
            return BigDecimal.valueOf((Float) object);
        } else if (object instanceof Double) {
            return BigDecimal.valueOf((Double) object);
        } else {
            return new BigDecimal(object.toString());
        }
    }
}
