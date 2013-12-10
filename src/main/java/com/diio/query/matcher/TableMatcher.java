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

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.FromTable;
import com.akiban.sql.parser.QueryTreeNode;

/**
 * Matches a single table name.
 * 
 * @author kkoster
 *
 */
public class TableMatcher extends QueryTreeNodeMatcher {

    private final String tableName;

    public TableMatcher(String name) {
        tableName = name;
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("a table with name ").appendText(tableName);
    }

    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        if (item instanceof FromTable) {
            FromTable col = (FromTable) item;
            try {
                return tableName.equalsIgnoreCase(col.getExposedName());
            } catch (StandardException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, table("my_table")));
     */
    @Factory
    public static Matcher<QueryTreeNode> table(String name) {
        return new TableMatcher(name);
    }
}
