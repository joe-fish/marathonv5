/*******************************************************************************
 * Copyright 2016 Jalian Systems Pvt. Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.sourceforge.marathon.javaagent.components;

import java.awt.BorderLayout;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sun.swingset3.demos.table.TableDemo;

import net.sourceforge.marathon.javaagent.IJavaAgent;
import net.sourceforge.marathon.javaagent.IJavaElement;
import net.sourceforge.marathon.javaagent.JavaAgent;
import net.sourceforge.marathon.javaagent.JavaElementFactory;
import net.sourceforge.marathon.testhelpers.ComponentUtils;

@Test public class JTableHeaderJavaElementTest extends JavaElementTest {
    protected JFrame frame;
    private IJavaAgent driver;

    @BeforeMethod public void showDialog() throws Throwable {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() {
                frame = new JFrame(JTableHeaderJavaElementTest.class.getSimpleName());
                frame.setName("frame-" + JTableHeaderJavaElementTest.class.getSimpleName());
                TableDemo demo = new TableDemo();
                demo.start();
                frame.getContentPane().add(demo, BorderLayout.CENTER);
                frame.pack();
                frame.setAlwaysOnTop(true);
                frame.setVisible(true);
            }
        });
        driver = new JavaAgent();
        JavaElementFactory.add(JTableHeader.class, JTableHeaderJavaElement.class);
    }

    @AfterMethod public void disposeDriver() throws Throwable {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() {
                frame.setVisible(false);
                frame.dispose();
            }
        });
    }

    public void tableHeaderItems() throws Throwable {
        IJavaElement tableHeader = driver.findElementByTagName("table-header");
        AssertJUnit.assertEquals("[\"Year\",\"Award Category\",\"Movie Title\",\"Nominee(s)\"]", tableHeader.getText());
    }

    public void tableHeaderColumnCount() {
        IJavaElement tableHeader = driver.findElementByTagName("table-header");
        AssertJUnit.assertEquals("4", tableHeader.getAttribute("count"));
    }

    public void tableHeaderItem() throws Throwable {
        IJavaElement tableHeader = driver.findElementByTagName("table-header");
        IJavaElement prop = marathon_select_by_properties(tableHeader, "Movie Title", false);
        AssertJUnit.assertEquals("Movie Title", prop.getText());
    }

    public void tableHeaderItemWithProperties() {
        Properties p = new Properties();
        p.put("item", "4");
        IJavaElement tableHeader = driver.findElementByTagName("table-header");
        IJavaElement prop = marathon_select_by_properties(tableHeader, p, false);
        AssertJUnit.assertEquals("Nominee(s)", prop.getText());
    }

    public void tableHeaderItemWithProperties2() {
        Properties p = new Properties();
        p.put("text", "Award Category");
        IJavaElement tableHeader = driver.findElementByTagName("table-header");
        IJavaElement prop = marathon_select_by_properties(tableHeader, p, false);
        AssertJUnit.assertEquals("Award Category", prop.getText());
    }

    public void assertContent() {
        IJavaElement tableHeader = driver.findElementByTagName("table-header");
        AssertJUnit.assertEquals("[[\"Year\",\"Award Category\",\"Movie Title\",\"Nominee(s)\"]]",
                tableHeader.getAttribute("content"));
    }

    public void assertContentWithDuplicates() {
        IJavaElement tableHeader = driver.findElementByTagName("table-header");
        siw(new Runnable() {
            @Override public void run() {
                JTable table = (JTable) ComponentUtils.findComponent(JTable.class, frame);
                table.getColumnModel().getColumn(1).setHeaderValue("Movie Title");
            }
        });
        AssertJUnit.assertEquals("[[\"Year\",\"Movie Title\",\"Movie Title(1)\",\"Nominee(s)\"]]",
                tableHeader.getAttribute("content"));
    }

    public void assertContentWithMultipleDuplicates() {
        IJavaElement tableHeader = driver.findElementByTagName("table-header");
        siw(new Runnable() {
            @Override public void run() {
                JTable table = (JTable) ComponentUtils.findComponent(JTable.class, frame);
                table.getColumnModel().getColumn(1).setHeaderValue("Movie Title");
                table.getColumnModel().getColumn(3).setHeaderValue("Movie Title");
            }
        });
        AssertJUnit.assertEquals("[[\"Year\",\"Movie Title\",\"Movie Title(1)\",\"Movie Title(2)\"]]",
                tableHeader.getAttribute("content"));
    }
}
