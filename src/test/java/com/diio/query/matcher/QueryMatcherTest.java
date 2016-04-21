package com.diio.query.matcher;

import static com.diio.query.matcher.AggregateMatcher.aggregate;
import static com.diio.query.matcher.AggregateMatcher.sum;
import static com.diio.query.matcher.BetweenMatcher.between;
import static com.diio.query.matcher.BinaryOperatorNodeMatcher.andRelation;
import static com.diio.query.matcher.BinaryOperatorNodeMatcher.equalTo;
import static com.diio.query.matcher.BinaryOperatorNodeMatcher.minus;
import static com.diio.query.matcher.BinaryOperatorNodeMatcher.orRelation;
import static com.diio.query.matcher.BinaryOperatorNodeMatcher.plus;
import static com.diio.query.matcher.BinaryOperatorNodeMatcher.relation;
import static com.diio.query.matcher.CaseStatementMatcher.cased;
import static com.diio.query.matcher.ColumnMatcher.column;
import static com.diio.query.matcher.ColumnMatcher.columns;
import static com.diio.query.matcher.FromSubqueryMatcher.fromSubquery;
import static com.diio.query.matcher.FunctionMatcher.function;
import static com.diio.query.matcher.InMatcher.in;
import static com.diio.query.matcher.ListOfNodeMatcher.exactSequence;
import static com.diio.query.matcher.ListOfNodeMatcher.ordered;
import static com.diio.query.matcher.ListOfNodeMatcher.subsequence;
import static com.diio.query.matcher.LiteralMatcher.literal;
import static com.diio.query.matcher.QueryHasMatcher.hasInQuery;
import static com.diio.query.matcher.TableMatcher.table;
import static com.diio.query.matcher.TernaryMatcher.like;
import static com.diio.query.matcher.UnderNodeMatcher.groupBy;
import static com.diio.query.matcher.UnderNodeMatcher.orderBy;
import static com.diio.query.matcher.UnderNodeMatcher.result;
import static com.diio.query.matcher.UpperLowerFunctionMatcher.lower;
import static com.diio.query.matcher.UpperLowerFunctionMatcher.upper;
import static com.diio.query.matcher.WhereClauseMatcher.where;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.hamcrest.core.IsAnything;
import org.junit.Before;
import org.junit.Test;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.types.TypeId;

/**
 * Self-test for query Matcher classes.
 *
 * @author kkoster
 */
public class QueryMatcherTest {

    private StatementNode statement;
    private StatementNode subqueryTopStatement;
    private StatementNode caseStatement;
    private String sqlWithFunctions;
    private StatementNode statementWithFunctions;
    private StatementNode inSingleItemListStatement;
    private StatementNode inTwoItemListStatement;
    private StatementNode orWithMultipleElementsStatement;

    private static StatementNode getParseTree(String rawSQL) {
        SQLParser sqlParser = new SQLParser();
        try {
            return sqlParser.parseStatement(rawSQL);
        } catch (StandardException exc) {
            throw new RuntimeException("invalid or unrecognized sql:\n\n" + rawSQL + "\n\n, preprocessedSQL:\n\n" + rawSQL, exc);
        }
    }
    
    @Before
    public void setUp() throws StandardException {
        String simpleSql = "SELECT foo, SUM(bar)" +
                " FROM MyTable" +
                " WHERE (foo='john' AND bar IS NOT NULL) OR (foo='jesse')" +
                " GROUP BY foo, just_in_group_by" +
                " ORDER BY just_in_order_by";
        statement = getParseTree(simpleSql);
        
        String subquerySql = "SELECT standard_table.col1, nested_table.col2, AVG(standard_table.col3)" +
        		" FROM standard_table, " +
        		"   (SELECT inner_col AS col2, unselected_col FROM my_inner WHERE inner_col3>55 GROUP BY col2, unselected_col) nested_table" +
        		" WHERE nested_table.col2=standard_table.join_col" +
        		" GROUP BY nested_table.col2, standard_table.col1" +
        		" ORDER BY AVG(standard_table.col3)";
        subqueryTopStatement = getParseTree(subquerySql);
        
        String caseSql = "SELECT CASE WHEN(foo='my foo literal') THEN 2 ELSE 3 END " +
                " FROM MyTable" +
                " WHERE bar = 55";
        caseStatement = getParseTree(caseSql);
        
        sqlWithFunctions = "SELECT FLOOR(foo), TRIM(LEADING '0' FROM foo) FROM MyTable WHERE ROUND(bar, 0) > 10";
        statementWithFunctions = getParseTree(sqlWithFunctions);

        inSingleItemListStatement = getParseTree("SELECT foo FROM MyTable WHERE bar IN ('my_string_literal')");
        inTwoItemListStatement = getParseTree("SELECT foo FROM MyTable WHERE bar IN ('my_string_literal', 'my_second_literal')");
        orWithMultipleElementsStatement = getParseTree("SELECT x, y, z FROM MyTable WHERE ((a = 1) or (b = 2) or (c = 3))");
    }

    @Test
    public void hasInQueryLooksAtAllClauses() {
        assertThat(statement, hasInQuery(column("foo")));
        assertThat(statement, hasInQuery(column("just_in_group_by")));
        assertThat(statement, hasInQuery(column("just_in_order_by")));
    }
    
    @Test
    public void positiveAssertionOnNarrowingToResultList() {
        assertThat(statement, hasInQuery(result(column("foo"))));        
    }
    
    @Test(expected = AssertionError.class)
    public void failingAssertionOnResultNarrowingSelection() {
        assertThat(statement, hasInQuery(result(column("just_in_group_by"))));        
    }

    @Test(expected = AssertionError.class)
    public void notFoundColumn() {
        assertThat(statement, hasInQuery(column("i_am_not_there")));
    }
    
    @Test(expected = AssertionError.class)
    public void notFoundInResultColumn() {
        assertThat(statement, hasInQuery(result(column("i_am_not_there"))));
    }
    
    @Test
    public void aggregateColumnInResult() {
        assertThat(statement, hasInQuery(result(aggregate("SUM", column("bar")))));
    }
    
    @Test
    public void fromTablePassing() {
        assertThat(statement, hasInQuery(table("MyTable")));
        assertThat("case-insensitive", statement, hasInQuery(table("mYtable")));
    }
        
    @Test(expected = AssertionError.class)
    public void fromTableFailing() {
        assertThat(statement, hasInQuery(table("MyBlah")));
    }
        
    @Test
    public void whereClause() {
        assertThat(statement, hasInQuery(where(column("foo"))));
        assertThat(statement, hasInQuery(where(column("foo").equalToLiteral("john"))));
    }
    
    @Test
    public void andInWhereClause() {
        assertThat(statement, hasInQuery(where(andRelation(column("foo").equalToLiteral("john"), column("bar").notNull()))));
    }
    
    @Test
    public void orInWhereClauseWithTwoElements() {
        assertThat(statement, hasInQuery(where(
                orRelation(
                        andRelation(column("foo").equalToLiteral("john"), column("bar").notNull()),
                        column("foo").equalToLiteral("jesse")
                        )
                )));
    }
    
    @Test
    public void orInWhereClauseWithMoreThanTwoElements() {
        // JR
        assertThat(orWithMultipleElementsStatement, hasInQuery(where(
                orRelation(
                        orRelation(column("a").equalToLiteral(1), column("b").equalToLiteral(2)),
                        column("c").equalToLiteral(3)
                )
        )));
    }    
    
    
    @Test
    public void notNullInWhereClause() {
        assertThat(statement, hasInQuery(where(column("bar").notNull())));
    }
    
    @Test
    public void groupByPassing() {
        assertThat(statement, hasInQuery(groupBy(column("foo"))));
        assertThat(statement, hasInQuery(groupBy(column("just_in_group_by"))));
        assertThat(statement, hasInQuery(groupBy()));
    }
    
    @Test(expected = AssertionError.class)
    public void groupByFailing() {
        assertThat(statement, hasInQuery(groupBy(column("just_in_order_by"))));        
    }
    
    @Test
    public void fromSubqueryMatching() {
        QueryTreeNode subqueryStatement = hasInQuery(fromSubquery("nested_table")).getMatch(subqueryTopStatement);
        assertNotNull(subqueryStatement);
    }
    
    @Test
    public void noSubqueryMatching() {
        assertNull(hasInQuery(fromSubquery("nested_table_which_doesnt_exist")).getMatch(subqueryTopStatement));        
    }
    
    @Test
    public void fullyQualifiedColumnName() {
        assertThat(subqueryTopStatement, hasInQuery(result(column("nested_table.col2"))));
    }
    
    @Test
    public void fullyQualifiedColumnNameDoesntMatch() {
        assertFalse(hasInQuery(result(column("nested_table_which_isnt_there.col2"))).matches(subqueryTopStatement));
    }
    
    @Test
    public void orderByMatchingColumn() {
        assertThat(statement, hasInQuery(orderBy(column("just_in_order_by"))));                
    }

    @Test
    public void orderByMatchingExistence() {
        assertThat(statement, hasInQuery(orderBy()));                
    }

    @Test
    public void distinguishBetweenConditionInResultListAndWhereClause() {
        assertTrue(hasInQuery(where(column("bar").equalToLiteral(55))).matches(caseStatement));
        assertFalse(hasInQuery(where(column("foo").equalToLiteral("my foo literal"))).matches(caseStatement));
    }
    
    @Test
    public void conditionalStatementMatcher() {
        assertThat("Just matching WHEN condition", caseStatement, hasInQuery(cased(column("foo").equalToLiteral("my foo literal"))));
        assertThat("Matching WHEN condition and THEN ELSE", caseStatement, 
                hasInQuery(cased(column("foo").equalToLiteral("my foo literal"),
                        literal(2), literal(3))));
    }
    
    @Test
    public void caseStatementMatcherFailingOnMismatchedThen() {
        assertFalse(hasInQuery(cased(column("foo").equalToLiteral("my foo literal"), literal(88), literal(3))).matches(caseStatement));
    }
    
    @Test
    public void descriptionOnCaseStatementMatcher() {
        final Matcher<QueryTreeNode> cased = cased(column("foo").equalToLiteral("my foo literal"), literal(88), literal(3));
        final StringDescription description = new StringDescription();
        cased.describeTo(description);
        assertEquals("a CASE statement clause of a relational expression of a column with name foo = a literal with value my foo literal " +
        		"THEN a literal with value 88 ELSE a literal with value 3", 
                description.toString());
    }
    
    @Test
    public void caseStatementMatcherFailingOnMismatchedElse() {
        assertThat(caseStatement, not(hasInQuery(cased(column("foo").equalToLiteral("my foo literal"), literal(2), literal(88)))));
    }
    
    @Test
    public void caseStatementMatcherFailingOnMismatchedWhen() {
        assertFalse(hasInQuery(cased(column("foo").equalToLiteral("my bar literal"), literal(2), literal(3))).matches(caseStatement));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void orderedColumns() {
        assertThat(statement, hasInQuery(result(ordered(column("foo"), aggregate("SUM", column("bar"))))));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void noOrderedColumnsIsTrueDegenerately() {
        assertThat(statement, hasInQuery(ordered()));
    }
    
    @Test
    public void orderedColumnsIsFalseIfColumnsOutOfOrder() {
        @SuppressWarnings("unchecked")
        final QueryHasMatcher matcher = hasInQuery(result(ordered(aggregate("SUM", column("bar")), column("foo"))));
        assertFalse(matcher.matches(statement));
        final StringDescription descr = new StringDescription();
        matcher.describeTo(descr);
        assertEquals("Query that has a selection result of [an aggregated SUM column with a column with name bar, a column with name foo] in order", 
                descr.toString());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void orderedColumnsWithGapsBetweenIsOkay() {
        assertThat(subqueryTopStatement, hasInQuery(ordered(column("standard_table.col1"), column("standard_table.col3"))));        
    }
    
    @Test
    public void subsequenceFailsOnGaps() {
        @SuppressWarnings("unchecked")
        final QueryHasMatcher matcher = hasInQuery(subsequence(column("standard_table.col1"), column("standard_table.col3")));
        assertFalse(matcher.matches(subqueryTopStatement));
        final StringDescription descr = new StringDescription();
        matcher.describeTo(descr);
        assertEquals("Query that has [a column with name standard_table.col1, a column with name standard_table.col3] in a subsequence without gaps", 
                descr.toString());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void subsequenceSuccess() {
        assertThat(subqueryTopStatement, hasInQuery(subsequence(
                column("standard_table.col1"), 
                column("nested_table.col2"), 
                column("standard_table.col3"))));
        assertThat(subqueryTopStatement, hasInQuery(subsequence(
                column("standard_table.col1"), 
                column("nested_table.col2"))));
        assertThat(subqueryTopStatement, hasInQuery(subsequence(
                column("nested_table.col2"), 
                column("standard_table.col3"))));
        assertThat(subqueryTopStatement, hasInQuery(subsequence(
                column("nested_table.col2"))));
    }    
    
    @Test
    public void subsequenceSuccessWithColumnsSyntacticSugar() {
        assertThat(subqueryTopStatement, hasInQuery(subsequence(
                columns("standard_table.col1", "nested_table.col2", "standard_table.col3"))));
    }    
    
    @Test
    public void exactFailsOnMissingFirstMatch() {
        @SuppressWarnings("unchecked")
        final QueryHasMatcher matcher = hasInQuery(exactSequence(column("nested_table.col2"), column("standard_table.col3")));
        assertFalse(matcher.matches(subqueryTopStatement));
        final StringDescription descr = new StringDescription();
        matcher.describeTo(descr);
        assertEquals("Query that has [a column with name nested_table.col2, a column with name standard_table.col3] in exact sequence", 
                descr.toString());
    }

    @Test
    public void exactFailsOnMissingLastMatch() {
        @SuppressWarnings("unchecked")
        final QueryHasMatcher matcher = hasInQuery(exactSequence(column("standard_table.col1"), column("nested_table.col2")));
        assertFalse(matcher.matches(subqueryTopStatement));
        final StringDescription descr = new StringDescription();
        matcher.describeTo(descr);
        assertEquals("Query that has [a column with name standard_table.col1, a column with name nested_table.col2] in exact sequence", 
                descr.toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void exactSuccess() {
        assertThat(subqueryTopStatement, hasInQuery(exactSequence(column("standard_table.col1"), 
                column("nested_table.col2"), 
                column("standard_table.col3"))));
    }
    
    @Test
    public void notEqualToLiteral() {
        String notEqualSql = "SELECT foo " +
                " FROM MyTable" +
                " WHERE bar != 55";
        final StatementNode notEqualsTree = getParseTree(notEqualSql);
        assertThat(notEqualsTree, hasInQuery(where(column("bar").notEqualToLiteral(55))));
    }
    
    @Test
    public void notEqualToLiteralAlternateOperator() {
        String notEqualSql = "SELECT foo " +
                " FROM MyTable" +
                " WHERE bar <> 55";
        final StatementNode notEqualsTree = getParseTree(notEqualSql);
        assertThat(notEqualsTree, hasInQuery(where(column("bar").notEqualToLiteral(55))));
    }    
    
    @Test
    public void notEqualToLiteralDoesntMatchEqualsNestedUnderNot() {
        String notEqualSql = "SELECT foo " +
                " FROM MyTable" +
                " WHERE NOT (bar = 55)";
        final StatementNode notEqualsTree = getParseTree(notEqualSql);
        final QueryHasMatcher matcher = hasInQuery(where(column("bar").notEqualToLiteral(55)));
        assertFalse(matcher.matches(notEqualsTree));
    }
    
    @Test
    public void additionMatcher() {
        String notEqualSql = "SELECT foo " +
                " FROM MyTable" +
                " WHERE bar = foo + 55";
        final StatementNode parseTree = getParseTree(notEqualSql);
        assertThat(parseTree, hasInQuery(where(relation(column("bar"), "=", plus(column("foo"), literal(55))))));
    } 

    @Test
    public void minusMatcher() {
        String notEqualSql = "SELECT foo " +
                " FROM MyTable" +
                " WHERE bar = foo - 55";
        final StatementNode parseTree = getParseTree(notEqualSql);
        assertThat(parseTree, hasInQuery(where(relation(column("bar"), "=", minus(column("foo"), literal(55))))));
    } 
    
    @SuppressWarnings("unchecked")
    @Test
    public void functionMatcherOnZeroOneAndTwoArguments() {
        assertThat(statementWithFunctions, hasInQuery(result(function("FLOOR"))));        
        assertThat(statementWithFunctions, hasInQuery(result(function("FLOOR", column("foo")))));        
        assertThat(statementWithFunctions, hasInQuery(where(function("ROUND", column("bar"), literal(0)))));
    }
    
    @Test
    public void functionMatcherOnUpperAndLowerFunctions() {
        sqlWithFunctions = "SELECT UPPER(foo), LOWER(bar) FROM MyTable";
        statementWithFunctions = getParseTree(sqlWithFunctions);        
        
        assertThat(statementWithFunctions, hasInQuery(result(upper(new IsAnything<QueryTreeNode>()))));        
        assertThat(statementWithFunctions, hasInQuery(result(upper(column("foo")))));        
        assertThat(statementWithFunctions, hasInQuery(result(lower(column("bar")))));
        
        //no such column
        assertThat(statementWithFunctions, not(hasInQuery(where(lower(column("barry"))))));
    }

    /**
     sqlWithFunctions = "SELECT FLOOR(foo),  WHERE ROUND(bar, 0) > 10";
     statementWithFunctions = getParseTree(sqlWithFunctions);
     */
    @SuppressWarnings("unchecked")
    @Test
    public void functionMatcherDoesNotMatchFunctionName() {
        assertFalse(hasInQuery(result(function("FLOO"))).matches(statementWithFunctions));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void functionMatcherDoesNotMatchNumberOfArguments() {
        assertFalse(hasInQuery(where(function("ROUND", column("bar")))).matches(statementWithFunctions));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void functionMatcherParameterMatcherDoesNotMatch() {
        assertFalse(hasInQuery(where(function("ROUND", column("barry"), literal(0)))).matches(statementWithFunctions));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void inMatcherOnOneSizeSet() {
        assertThat(inSingleItemListStatement, hasInQuery(where(in(column("bar"), ordered(literal("my_string_literal"))))));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void inMatcherOnOneSizeSetDoesntMatch() {
        assertThat(inSingleItemListStatement, not(hasInQuery(where(in(column("bar"), 
                ordered(literal("my_string_literal"), literal("my_string_literal_not_there")))))));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void inMatcherOnTwoSizeSet() {
        assertThat(inTwoItemListStatement, hasInQuery(where(in(column("bar"), 
                exactSequence(literal("my_string_literal"), literal("my_second_literal"))))));
        assertThat(inTwoItemListStatement, hasInQuery(where(in(column("bar"), 
                ordered(literal("my_string_literal"), literal("my_second_literal"))))));
        assertThat(inTwoItemListStatement, hasInQuery(where(in(column("bar"), 
                subsequence(literal("my_string_literal"), literal("my_second_literal"))))));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void inMatcherOnTwoSizeSetDoesntSpecifyEnoughItems() {
        assertThat(inTwoItemListStatement, not(hasInQuery(where(in(column("bar"), 
                exactSequence(literal("my_string_literal")))))));
    }
    
    @Test
    public void timeLiteralMatch() {
        String timeLiteralSql = "SELECT my_col " +
                " FROM MyTable" +
                " WHERE time_col = TIME '01:00:00'";
        StatementNode timeLiteralStatement = getParseTree(timeLiteralSql);
        Matcher<QueryTreeNode> matcherWithType = CastMatcher.cast(TypeId.TIME_ID, literal("01:00:00"));
        Matcher<QueryTreeNode> matcherWithOutType = CastMatcher.castedValue(literal("01:00:00"));
        Matcher<QueryTreeNode> matcherFromFactory = LiteralMatcher.timeLiteral("01:00:00");
        assertThat(timeLiteralStatement, hasInQuery(where(equalTo(column("time_col"), matcherWithType))));        
        assertThat(timeLiteralStatement, hasInQuery(where(equalTo(column("time_col"), matcherFromFactory))));        
        assertThat(timeLiteralStatement, hasInQuery(where(equalTo(column("time_col"), matcherWithOutType))));
    }
    
    @Test
    public void likeMatch() {
        String likeSql = "SELECT my_col " +
                " FROM MyTable" +
                " WHERE name_col LIKE '%Doe%'";
        StatementNode timeLiteralStatement = getParseTree(likeSql);
        assertThat(timeLiteralStatement, hasInQuery(where(like(column("name_col"), literal("%Doe%")))));
    }
    
    @Test
    public void betweenMatch() {
        String betweenSql = "SELECT my_col " +
                " FROM MyTable" +
                " WHERE date_col BETWEEN date '1992-07-01' AND date '1994-12-31'";
        StatementNode betweenStatement = getParseTree(betweenSql);
        ColumnMatcher dateColMatcher = column("date_col");
        assertThat(betweenStatement, hasInQuery(where(between(
                dateColMatcher, 
                LiteralMatcher.dateLiteral("1992-07-01"), 
                LiteralMatcher.dateLiteral("1994-12-31")))));
    }
    
    @Test
    public void sumMatches() {
        String sumSql = "SELECT sum(my_col) FROM MyTable";
        StatementNode sumStatement = getParseTree(sumSql);
        ColumnMatcher colMatcher = column("my_col");
        assertThat(sumStatement, hasInQuery(result(sum(colMatcher))));
    }

    @Test(expected=AssertionError.class)
    public void sumDoesntMatch() {
        String sumSql = "SELECT MIN(my_col) FROM MyTable";
        StatementNode sumStatement = getParseTree(sumSql);
        ColumnMatcher colMatcher = column("my_col");
        assertThat(sumStatement, hasInQuery(result(sum(colMatcher))));
    }
}
