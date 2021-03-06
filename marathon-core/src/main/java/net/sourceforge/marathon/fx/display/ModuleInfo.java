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
package net.sourceforge.marathon.fx.display;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ModuleInfo {

    private List<String> moduleDirs;
    private List<ModuleDirElement> moduleDirElements = new ArrayList<>();
    private ObservableList<ModuleFileElement> moduleFileElements = FXCollections.observableArrayList();
    private boolean needModuleFile = true;
    private String moduleFunctionName;
    private String modulefunctionDescription;
    private ModuleDirElement moduleDirElement;
    private String fileName;
    private String suffix = ".rb";
    private String title;;

    public static class ModuleDirElement {
        private File file;
        @SuppressWarnings("unused") private String prefix;

        public ModuleDirElement(File file, String prefix) {
            this.file = file;
            this.prefix = prefix;
        }

        @Override public String toString() {
            return file.getName();
        }

        public File getFile() {
            return file;
        }
    }

    public class ModuleFileElement {

        private File file;

        public ModuleFileElement(File file) {
            this.file = file;
        }

        @Override public String toString() {
            return file.getName();
        }
    }

    public ModuleInfo(String title, List<String> moduleDirs, String suffix) {
        this(title, moduleDirs, suffix, true);
    }

    public ModuleInfo(String title, List<String> moduleDirs, String suffix, boolean needModuleFile) {
        this.title = title;
        this.moduleDirs = moduleDirs;
        this.suffix = suffix;
        this.needModuleFile = needModuleFile;
        createModuleDir();
    }

    private void createModuleDir() {
        for (String dir : moduleDirs) {
            moduleDirElements.add(new ModuleDirElement(new File(dir), ""));
        }
    }

    public String getTitle() {
        return title;
    }

    public List<ModuleDirElement> getModuleDirElements() {
        return moduleDirElements;
    }

    public boolean isNeedModuleFile() {
        return needModuleFile;
    }

    public void setModuleFunctionName(String moduleFunctionName) {
        this.moduleFunctionName = moduleFunctionName;
    }

    public void setModuleFunctionDescription(String modulefunctionDescription) {
        this.modulefunctionDescription = modulefunctionDescription;
    }

    public void setModuleDirElement(ModuleDirElement moduleDirElement) {
        this.moduleDirElement = moduleDirElement;
    }

    public void setModuleFile(String fileName) {
        this.fileName = fileName;
    }

    public String getModuleFunctionName() {
        return moduleFunctionName;
    }

    public String getModulefunctionDescription() {
        return modulefunctionDescription;
    }

    public ModuleDirElement getModuleDirElement() {
        return moduleDirElement;
    }

    public String getFileName() {
        if (fileName.endsWith(suffix)) {
            return fileName;
        } else {
            return fileName + suffix;
        }
    }

    public ObservableList<ModuleFileElement> getModuleFileElements() {
        return moduleFileElements;
    }

    public void populateFiles(ModuleDirElement selectedItem) {
        File file = selectedItem.getFile();
        file.list(new FilenameFilter() {
            @Override public boolean accept(File dir, String name) {
                File f = new File(dir, name);
                boolean b = f.isFile() && name.endsWith(suffix);
                if (b) {
                    moduleFileElements.add(new ModuleFileElement(f));
                }
                return b;
            }
        });
    }
}
