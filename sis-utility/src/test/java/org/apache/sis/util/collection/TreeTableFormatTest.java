/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.util.collection;

import java.text.ParseException;
import org.junit.Test;
import org.apache.sis.test.TestCase;
import org.apache.sis.test.DependsOn;
import org.apache.sis.test.DependsOnMethod;

import static org.apache.sis.test.Assert.*;
import static org.apache.sis.util.collection.TableColumn.*;


/**
 * Tests the {@link TreeTableFormat} class.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-3.00)
 * @version 0.3
 * @module
 */
@DependsOn({
    DefaultTreeTableTest.class,
    org.apache.sis.io.TableFormatterTest.class
})
public final strictfp class TreeTableFormatTest extends TestCase {
    /**
     * Tests the formatting as a tree, with control on the indentation.
     */
    @Test
    public void testTreeFormat() {
        final DefaultTreeTable.Node root   = new DefaultTreeTable.Node("Node #1");
        final DefaultTreeTable.Node branch = new DefaultTreeTable.Node("Node #2");
        root.getChildren().add(branch);
        root.getChildren().add(new DefaultTreeTable.Node("Node #3"));
        branch.getChildren().add(new DefaultTreeTable.Node("Node #4"));

        final TreeTableFormat tf = new TreeTableFormat(null, null);
        tf.setVerticalLinePosition(2);
        assertMultilinesEquals(
                "Node #1\n" +
                "  ├─Node #2\n" +
                "  │   └─Node #4\n" +
                "  └─Node #3\n", tf.format(new DefaultTreeTable(root)));
    }

    /**
     * Tests the parsing of a tree. This method parses and reformats a tree,
     * and performs its check on the assumption that the tree formatting is
     * accurate.
     *
     * @throws ParseException Should never happen.
     */
    @Test
    @DependsOnMethod("testTreeFormat")
    public void testTreeParse() throws ParseException {
        final TreeTableFormat tf = new TreeTableFormat(null, null);
        tf.setVerticalLinePosition(0);
        final String text =
                "Node #1\n" +
                "├───Node #2\n" +
                "│   └───Node #4\n" +
                "└───Node #3\n";
        final TreeTable table = tf.parseObject(text);
        assertMultilinesEquals(text, tf.format(table));
    }

    /**
     * Tests the formatting of a tree table.
     */
    @Test
    @DependsOnMethod("testTreeFormat")
    public void testTreeTableFormat() {
        final TableColumn<Integer> valueA = new TableColumn<Integer>(Integer.class, "value #1");
        final TableColumn<String>  valueB = new TableColumn<String> (String .class, "value #2");
        final DefaultTreeTable table  = new DefaultTreeTable(NAME, valueA, valueB);
        final TreeTable.Node   root   = new DefaultTreeTable.Node(table);
        root.setValue(NAME, "Node #1");
        root.setValue(valueA, 10);
        root.setValue(valueB, "Value #1B");
        final TreeTable.Node branch1 = new DefaultTreeTable.Node(table);
        branch1.setValue(NAME, "Node #2");
        branch1.setValue(valueA, 20);
        root.getChildren().add(branch1);
        final TreeTable.Node branch2 = new DefaultTreeTable.Node(table);
        branch2.setValue(NAME, "Node #3");
        branch2.setValue(valueB, "Value #3B");
        root.getChildren().add(branch2);
        final TreeTable.Node leaf = new DefaultTreeTable.Node(table);
        leaf.setValue(NAME, "Node #4");
        leaf.setValue(valueA, 40);
        leaf.setValue(valueB, "val #4\twith tab\nand a new line");
        branch1.getChildren().add(leaf);
        table.setRoot(root);

        final TreeTableFormat tf = new TreeTableFormat(null, null);
        tf.setVerticalLinePosition(1);
        assertMultilinesEquals(
                "Node #1………………………… 10…… Value #1B\n" +
                " ├──Node #2……………… 20\n" +
                " │   └──Node #4…… 40…… val #4  with tab ¶ and a new line\n" +
                " └──Node #3……………… ………… Value #3B\n", tf.format(table));

        tf.setColumns(NAME, valueA);
        assertMultilinesEquals(
                "Node #1………………………… 10\n" +
                " ├──Node #2……………… 20\n" +
                " │   └──Node #4…… 40\n" +
                " └──Node #3\n", tf.format(table));

        tf.setColumns(NAME, valueB);
        assertMultilinesEquals(
                "Node #1………………………… Value #1B\n" +
                " ├──Node #2\n" +
                " │   └──Node #4…… val #4  with tab ¶ and a new line\n" +
                " └──Node #3……………… Value #3B\n", tf.format(table));
    }

    /**
     * Tests the parsing of a tree table. This method parses and reformats a tree table,
     * and performs its check on the assumption that the tree table formatting is accurate.
     *
     * @throws ParseException Should never happen.
     */
    @Test
    @DependsOnMethod("testTreeTableFormat")
    public void testTreeTableParse() throws ParseException {
        final TableColumn<Integer> valueA = new TableColumn<Integer>(Integer.class, "value #1");
        final TableColumn<String>  valueB = new TableColumn<String> (String .class, "value #2");
        final TreeTableFormat tf = new TreeTableFormat(null, null);
        tf.setColumns(NAME, valueA, valueB);
        tf.setVerticalLinePosition(1);
        final String text =
                "Node #1………………………… 10…… Value #1B\n" +
                " ├──Node #2……………… 20\n" +
                " │   └──Node #4…… 40…… Value #4B\n" +
                " └──Node #3……………… ………… Value #3B\n";
        final TreeTable table = tf.parseObject(text);
        assertMultilinesEquals(text, tf.format(table));
    }

    /**
     * Tests parsing and formatting using a different column separator.
     *
     * @throws ParseException Should never happen.
     */
    @Test
    @DependsOnMethod("testTreeTableParse")
    public void testAlternativeColumnSeparatorPattern() throws ParseException {
        final TableColumn<Integer> valueA = new TableColumn<Integer>(Integer.class, "value #1");
        final TableColumn<String>  valueB = new TableColumn<String> (String .class, "value #2");
        final TreeTableFormat tf = new TreeTableFormat(null, null);
        assertEquals("?……[…] ", tf.getColumnSeparatorPattern());
        tf.setColumns(NAME, valueA, valueB);
        tf.setVerticalLinePosition(1);
        /*
         * Test with all column separators.
         */
        tf.setColumnSeparatorPattern(" [ ]│ ");
        assertEquals(" [ ]│ ", tf.getColumnSeparatorPattern());
        final String text =
                "Node #1         │ 10 │ Value #1B\n" +
                " ├──Node #2     │ 20 │ \n" +
                " │   └──Node #4 │ 40 │ Value #4B\n" +
                " └──Node #3     │    │ Value #3B\n";
        final TreeTable table = tf.parseObject(text);
        assertMultilinesEquals(text, tf.format(table));
        /*
         * Test with omission of column separator for trailing null values.
         */
        tf.setColumnSeparatorPattern("? [ ]; ");
        assertMultilinesEquals(
                "Node #1         ; 10 ; Value #1B\n" +
                " ├──Node #2     ; 20\n" + // Column separator omitted here.
                " │   └──Node #4 ; 40 ; Value #4B\n" +
                " └──Node #3     ;    ; Value #3B\n", tf.format(table));
        /*
         * Test with regular expression at parsing time.
         */
        tf.setColumnSeparatorPattern("?……[…] /\\w*│+\\w*");
        assertEquals("?……[…] /\\w*│+\\w*", tf.getColumnSeparatorPattern());
        assertEquals(table, tf.parseObject(text));
    }
}