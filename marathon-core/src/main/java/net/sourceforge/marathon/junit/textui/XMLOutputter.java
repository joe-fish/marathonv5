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
package net.sourceforge.marathon.junit.textui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.runner.BaseTestRunner;
import net.sourceforge.marathon.checklist.CheckList;
import net.sourceforge.marathon.junit.MarathonTestCase;
import net.sourceforge.marathon.runtime.api.Constants;

public class XMLOutputter implements IOutputter {
    public XMLOutputter() {
        super();
    }

    @Override public void output(Writer writer, Test testSuite, Map<Test, MarathonTestResult> testOutputMap) {
        try {
            writer.write("<?xml version=\"1.0\" ?>\n");
            String reportDir = new File(System.getProperty(Constants.PROP_REPORT_DIR)).getName();
            writer.write("<test projectname='" + System.getProperty(Constants.PROP_PROJECT_NAME, "") + "' " + "reportdir='"
                    + reportDir + "' " + ">\n");
            printResult("", writer, testSuite, testOutputMap);
            writer.write("</test>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printResult(String indent, Writer writer, Test test, Map<Test, MarathonTestResult> testOutputMap)
            throws IOException {
        if (test instanceof TestSuite) {
            TestSuite suite = (TestSuite) test;
            writer.write(indent + "<testsuite name=\"" + suite.getName() + "\" >\n");
            Enumeration<Test> testsEnum = suite.tests();
            while (testsEnum.hasMoreElements()) {
                printResult(indent + "  ", writer, testsEnum.nextElement(), testOutputMap);
            }
            writer.write(indent + "</testsuite>\n");
        } else {
            MarathonTestResult result = testOutputMap.get(test);
            writeResultXML(indent, writer, result, test);
        }
    }

    private void writeResultXML(String indent, Writer writer, MarathonTestResult result, Test test) throws IOException {
        if (result == null) {
            return;
        }
        String durationStr = NumberFormat.getInstance().format(result.getDuration());
        int status = result.getStatus();
        StringBuilder xml = new StringBuilder();
        xml.append(indent);
        xml.append("<testcase name=\"");
        xml.append(result.getTestName());
        xml.append("\" status=\"");
        xml.append(status);
        xml.append("\" time=\"");
        xml.append(durationStr);
        xml.append("\" >\n");
        if (test instanceof MarathonTestCase) {
            MarathonTestCase mtestcase = (MarathonTestCase) test;
            ArrayList<CheckList> checklists = mtestcase.getChecklists();
            if (checklists.size() > 0) {
                int index = 1;
                for (CheckList checkList : checklists) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    checkList.saveXML(indent, baos, index++);
                    xml.append(new String(baos.toByteArray()));
                }
            }
        }
        if (status == MarathonTestResult.STATUS_PASS) {
            xml.append(indent).append("</testcase>\n");
        } else {
            String stackTrace = " ";
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                stackTrace = BaseTestRunner.getFilteredTrace(throwable);
            }
            String captureDir = System.getProperty(Constants.PROP_IMAGE_CAPTURE_DIR);
            if (captureDir != null && test instanceof MarathonTestCase) {
                File[] files = ((MarathonTestCase) test).getScreenCaptures();
                List<File> fileList = new ArrayList<File>();
                for (File file : files) {
                    fileList.add(file);
                }
                /**
                 * We have to sort them, because they are not guaranteed to be
                 * in any particular order, and tend to be in reverse order
                 * (ordered by newest to oldest file)
                 */
                Collections.sort(fileList);
                if (fileList.size() > 0) {
                    xml.append("<screen_captures>");
                    Iterator<File> it = fileList.iterator();
                    while (it.hasNext()) {
                        File file = it.next();
                        xml.append("<screen_capture file=\"").append(file.getName()).append("\"/>");
                    }
                    xml.append("</screen_captures>");
                }
            }
            xml.append("<![CDATA[").append(stackTrace);
            xml.append(indent).append("]]></testcase>\n");
        }
        writer.write(xml.toString());
    }
}
