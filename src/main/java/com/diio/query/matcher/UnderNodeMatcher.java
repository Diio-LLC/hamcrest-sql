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
import org.hamcrest.core.IsAnything;

import com.akiban.sql.parser.GroupByList;
import com.akiban.sql.parser.OrderByList;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.ResultColumnList;

/**
 * Used to match a top-level section of a SQL query, such as a GROUP BY section, the SELECT section, etc.
 * Note, the WHERE clause should be matched via the WhereClauseMatcher.
 * 
 * @author kkoster
 *
 * @see WhereClauseMatcher
 * @param <T> the type of QueryTreeNode that represents the section to be matched.
 */
public class UnderNodeMatcher<T extends QueryTreeNode> extends QueryTreeNodeMatcher {

    protected final Matcher<QueryTreeNode> submatcher;
    private final Class<? extends T> nodeClass;
    private final String descrPrefix;
    
    protected UnderNodeMatcher(Matcher<QueryTreeNode> submatcher, Class<? extends T> nodeClass, String describeToPrefix) {
        this.submatcher = submatcher;
        this.nodeClass = nodeClass;
        this.descrPrefix = describeToPrefix;
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText(descrPrefix).
            appendDescriptionOf(submatcher);
    }

    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        if (nodeClass.isAssignableFrom(item.getClass())) {
            return QueryHasMatcher.hasInQuery(this.submatcher).matches(item);
        }
        return false;
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, result(aggregate("SUM", column("my_id"))));
     */
    @Factory
    public static Matcher<QueryTreeNode> result(Matcher<QueryTreeNode> matcher) {
        return new UnderNodeMatcher<QueryTreeNode>(matcher, ResultColumnList.class, "a selection result of ");
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, groupBy(column("my_id")));
     */
    @Factory
    public static Matcher<QueryTreeNode> groupBy(Matcher<QueryTreeNode> matcher) {
        return new UnderNodeMatcher<QueryTreeNode>(matcher, GroupByList.class, "a group by of ");
    }


    /**
     * Syntactic sugar for the presence of a GROUP BY clause.
     *
     * For example, assertThat(query, groupBy());
     */
    @Factory
    public static Matcher<QueryTreeNode> groupBy() {
        return new UnderNodeMatcher<QueryTreeNode>(new IsAnything<QueryTreeNode>(), GroupByList.class, "a group by ");
    }


    /**
     * Syntactic sugar for the presence of an ORDER BY clause.
     *
     * For example, assertThat(query, groupBy());
     */
    @Factory
    public static Matcher<QueryTreeNode> orderBy() {
        return new UnderNodeMatcher<QueryTreeNode>(new IsAnything<QueryTreeNode>(), OrderByList.class, "an order by ");
    }


    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, orderBy(column("my_id")));
     */
    @Factory
    public static Matcher<QueryTreeNode> orderBy(Matcher<QueryTreeNode> matcher) {
        return new UnderNodeMatcher<QueryTreeNode>(matcher, OrderByList.class, "a order by of ");
    }

}
