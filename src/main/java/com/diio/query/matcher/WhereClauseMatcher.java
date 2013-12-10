/*
   Copyright (c) 2013 Diio, LLC

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

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.SelectNode;

/**
 * Matches the WHERE clause section of a SQL statement.
 * 
 * @author kkoster
 *
 * @see UnderNodeMatcher
 */
public class WhereClauseMatcher extends QueryTreeNodeMatcher {

    private final Matcher<QueryTreeNode> submatcher;

    public WhereClauseMatcher(Matcher<QueryTreeNode> subMatcher) {
        this.submatcher = subMatcher;
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("a where clause of ").
            appendDescriptionOf(submatcher);
    }
    
    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        if (item instanceof SelectNode) {
            SelectNode select = (SelectNode) item;
            return QueryHasMatcher.hasInQuery(this.submatcher).matches(select.getWhereClause());
        }
        return false;
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, where(equals(column("my_id"), "'sam'")));
     */
    @Factory
    public static Matcher<QueryTreeNode> where(Matcher<QueryTreeNode> matcher) {
        return new WhereClauseMatcher(matcher);
    }
}
