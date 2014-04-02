package com.diio.query.matcher;

import static com.diio.query.matcher.ColumnMatcher.column;
import static com.diio.query.matcher.QueryHasMatcher.hasInQuery;
import static com.diio.query.matcher.TrimOperatorNodeMatcher.trim;
import static com.diio.query.matcher.TrimOperatorNodeMatcher.trimLeft;
import static com.diio.query.matcher.TrimOperatorNodeMatcher.trimRight;
import static com.diio.query.matcher.UnderNodeMatcher.result;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.StatementNode;

public class TrimOperatorNodeMatcherTest {
    @Test
    public void shouldMatchTrimFunction() throws StandardException {
        StatementNode statement = new SQLParser().parseStatement("SELECT TRIM(foo) FROM MyTable");

        assertThat(statement, hasInQuery(result(trim(column("foo")))));
    }

    @Test
    public void shouldMatchTrimWithCharFunction() throws StandardException {
        StatementNode statement = new SQLParser().parseStatement("SELECT TRIM('0' FROM foo) FROM MyTable");

        assertThat(statement, hasInQuery(result(trim(column("foo"), '0'))));
    }

    @Test
    public void shouldMatchTrimLeftWithCharFunction() throws StandardException {
        StatementNode statement = new SQLParser().parseStatement("SELECT TRIM(LEADING '0' FROM foo) FROM MyTable");

        assertThat(statement, hasInQuery(result(trimLeft(column("foo"), '0'))));
    }

    @Test
    public void shouldMatchTrimRightWithCharFunction() throws StandardException {
        StatementNode statement = new SQLParser().parseStatement("SELECT TRIM(TRAILING '0' FROM foo) FROM MyTable");

        assertThat(statement, hasInQuery(result(trimRight(column("foo"), '0'))));
    }
}
