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

import java.io.StringWriter;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.akiban.sql.parser.QueryTreeNode;

/**
 * Encapsulates functionality common to most Matcher objects in this library.
 * 
 * @author kkoster
 *
 */
public abstract class QueryTreeNodeMatcher extends TypeSafeMatcher<QueryTreeNode> {

    @Override
    protected void describeMismatchSafely(QueryTreeNode item, Description mismatchDescription) {
        if (item instanceof QueryTreeNode) {
            final StringWriter writer = new StringWriter();
            ((QueryTreeNode)item).treePrint(writer);
            mismatchDescription.appendText("was ").appendText(writer.toString());
        } else {
            super.describeMismatchSafely(item, mismatchDescription);
        }        
    }
        
}
