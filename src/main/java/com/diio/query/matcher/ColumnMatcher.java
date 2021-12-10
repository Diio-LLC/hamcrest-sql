/*
   Copyright (c) 2022 Cirium

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.diio.query.matcher;

import static com.diio.query.matcher.LiteralMatcher.integralLiteral;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.QueryTreeNode;

/**
 * Matches a reference to a column, which may optionally be qualified by a table name. E.g.
 * both "my_table.my_column" and "my_column" can be matched.
 * 
 * TODO: does not match column names which are qualified by both a schema and a table name.
 * 
 * @author kkoster
 *
 */
public class ColumnMatcher extends QueryTreeNodeMatcher {

    private final String columnName;
    private final String tableName;

    public ColumnMatcher(String name) {
        if (name.contains(".")) {
            String parts[] = name.split("\\.");
            if (parts.length != 2) {
                //TODO support schemas (2 periods present)
                throw new RuntimeException("More than 1 period found in column name " + name);
            }
            tableName = parts[0];
            columnName = parts[1];
        } else {
            columnName = name;
            tableName = null;
        }
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("a column with name ");
        if (tableName != null) {
            description.appendText(tableName).appendText(".");
        }
        description.appendText(columnName);
    }

    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        if (item instanceof ColumnReference) {
            ColumnReference col = (ColumnReference) item;
            if (tableName != null && !tableName.equalsIgnoreCase(col.getTableName())) {
                return false;
            }
            return columnName.equalsIgnoreCase(col.getColumnName());
        }
        return false;
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, column("my_id")));
     */
    @Factory
    public static ColumnMatcher column(String name) {
        return new ColumnMatcher(name);
    }

    /**
     * Syntactic sugar! Useful in combination with a ListOfNodeMatcher.
     *
     * For example, assertThat(query, columns("my_col", "my_col2", "my_col3")));
     */
    @Factory
    public static ColumnMatcher[] columns(String... names) {
        ColumnMatcher[] toReturn = new ColumnMatcher[names.length];
        for (int i = 0; i < toReturn.length; i++) {
            toReturn[i] = column(names[i]);
        }
        return toReturn;
    }

    public Matcher<QueryTreeNode> equalTo(Matcher<QueryTreeNode> matcher) {
        return BinaryOperatorNodeMatcher.equalTo(this, matcher);
    }

    public Matcher<QueryTreeNode> equalToLiteral(String literal) {
        return BinaryOperatorNodeMatcher.equalTo(this, new LiteralMatcher<String>(literal));
    }

    public Matcher<QueryTreeNode> notNull() {
        return UnaryOperatorNodeMatcher.not(UnaryOperatorNodeMatcher.isNull(this));
    }

    public Matcher<QueryTreeNode> equalToLiteral(int literal) {
        return BinaryOperatorNodeMatcher.equalTo(this, new LiteralMatcher<Integer>(literal));
    }

    public Matcher<QueryTreeNode> notEqualToLiteral(int literal) {
        //Note: UnaryOperatorNodeMatcher.not(equalToLiteral(literal)) produces a matcher which matches different parse trees than this one
//        return UnaryOperatorNodeMatcher.not(equalToLiteral(literal));
        return BinaryOperatorNodeMatcher.notEqualTo(this, LiteralMatcher.literal(literal));
    }

    public Matcher<QueryTreeNode> lessThen(int literal) {
        return BinaryOperatorNodeMatcher.lessThan(this, integralLiteral(literal));
    }

    public Matcher<QueryTreeNode> greaterThan(int literal) {
        return BinaryOperatorNodeMatcher.greaterThan(this, integralLiteral(literal));
    }

    public Matcher<QueryTreeNode> is(boolean bool) {
        return BinaryOperatorNodeMatcher.is(this, new LiteralMatcher<Boolean>(bool));
    }

    // FIXME: Akiban SQL Parser fails on all bitwise operations so trying to fool it for now by using %/mod operation instead (just for tests to pass).
    public Matcher<QueryTreeNode> bitwiseAndUsingMod(Number literal) {
        return BinaryOperatorNodeMatcher.bitwiseAndUsingMod(this, LiteralMatcher.<Number>literal(literal));
    }

    public Matcher<QueryTreeNode> in(ListOfNodeMatcher matchers) {
        return InMatcher.in(this, matchers);
    }

    public Matcher<QueryTreeNode> inOrEqual(ListOfNodeMatcher matchers) {
        if (matchers.getSubMatchers().length > 1) {
            return in(matchers);
        } else {
            return equalTo(matchers.getSubMatchers()[0]);
        }
    }
}
