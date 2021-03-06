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
package net.sourceforge.marathon.javaagent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ChooserHelper {

    private static final String homeDir;
    private static final String cwd;
    private static final String marathonDir;

    static {
        homeDir = getRealPath(System.getProperty("user.home", null));
        cwd = getRealPath(System.getProperty("user.dir", null));
        marathonDir = getRealPath(System.getProperty("marathon.project.dir", null));
    }

    private static String getRealPath(String path) {
        if (path == null) {
            return "";
        }
        try {
            return new File(path).getCanonicalPath();
        } catch (IOException e) {
            return "";
        }
    }

    public static File[] decode(String text) {
        ArrayList<File> files = new ArrayList<File>();
        StringTokenizer tokenizer = new StringTokenizer(text, File.pathSeparator);
        while (tokenizer.hasMoreElements()) {
            File file = decodeFile((String) tokenizer.nextElement());
            files.add(file);
        }
        return files.toArray(new File[files.size()]);
    }

    public static File decodeFile(String path) {
        String prefix = "";
        if (path.startsWith("#M")) {
            prefix = marathonDir;
            path = path.substring(2);
        } else if (path.startsWith("#C")) {
            prefix = cwd;
            path = path.substring(2);
        } else if (path.startsWith("#H")) {
            prefix = homeDir;
            path = path.substring(2);
        }

        return new File(prefix + path.replace('/', File.separatorChar));
    }

    public static String encode(File[] selectedfiles) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < selectedfiles.length; i++) {
            String encode = encode(selectedfiles[i]);
            if (encode != null) {
                buffer.append(encode);
            }
            if (i < selectedfiles.length - 1) {
                buffer.append(File.pathSeparator);
            }
        }
        return buffer.toString();
    }

    public static String encode(File file) {
        String path;
        try {
            path = file.getCanonicalPath();

            String prefix = "";
            if (marathonDir != null && path.startsWith(marathonDir)) {
                prefix = "#M";
                path = path.substring(marathonDir.length());
            } else if (cwd != null && path.startsWith(cwd)) {
                prefix = "#C";
                path = path.substring(cwd.length());
            } else if (homeDir != null && path.startsWith(homeDir)) {
                prefix = "#H";
                path = path.substring(homeDir.length());
            }
            return (prefix + path).replace(File.separatorChar, '/');

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
