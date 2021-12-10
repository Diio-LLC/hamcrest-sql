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

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsAnything;

import com.akiban.sql.parser.FromSubquery;
import com.akiban.sql.parser.QueryTreeNode;

/**
 * Matches subqueries in FROM clauses that are aliased.
 * 
 * @author kkoster
 *
 */
public class FromSubqueryMatcher extends QueryTreeNodeMatcher {

    private final Matcher<QueryTreeNode> submatcher;
    private final String tableName;

    public FromSubqueryMatcher(Matcher<QueryTreeNode> subMatcher, String tableName) {
        this.submatcher = subMatcher;
        this.tableName = tableName;
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("a FromSubquery named ").
            appendText(tableName).
            appendText(" having ").
            appendDescriptionOf(submatcher);
    }
    
    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        if (item instanceof FromSubquery) {
            if (tableName.equals(((FromSubquery)item).getExposedName())) {                
                return QueryHasMatcher.hasInQuery(this.submatcher).matches(item);
            }
        }
        return false;
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(fromSubquery("my_inner", aggregate("SUM", column("my_id")))));
     */
    @Factory
    public static FromSubqueryMatcher fromSubquery(String tableName, Matcher<QueryTreeNode> matcher) {
        return new FromSubqueryMatcher(matcher, tableName);
    }
    
    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(fromSubquery("my_inner")));
     */
    @Factory
    public static FromSubqueryMatcher fromSubquery(String tableName) {
        return new FromSubqueryMatcher(new IsAnything<QueryTreeNode>(), tableName);
    }

}
