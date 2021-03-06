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
package net.sourceforge.marathon.runtime.api;

import java.io.File;
import java.util.Properties;

import net.sourceforge.marathon.runtime.api.Constants.MarathonMode;

/**
 * The operating environment of the application under test. In java terms this
 * corresponds roughly to the a java virtual machine
 */
public interface IMarathonRuntime {
    /**
     * creates a byte-compiled script inside the runtime. The result is
     * guaranteed to have been at least syntax level validated
     *
     * @param content
     *            - the contents of the script to create
     * @param filename
     *            - filename of the script, used for generating debug output and
     *            stack traces.
     * @return - a Script which
     */
    IScript createScript(MarathonMode playing, IConsole console, String scriptText, String absolutePath, boolean b, boolean c,
            Properties dataVariables);

    /**
     * begin capturing system events that are happening inside this runtime, and
     * send them to the recorder
     *
     * @param recorder
     *            - the objects to which system events will be sent.
     */
    void startRecording(IRecorder recorder);

    /**
     * if events were being recorded, they will no longer be recorded after this
     * method is called.
     */
    void stopRecording();

    /**
     * Start the application
     */
    void startApplication();

    /**
     * Stop the application
     */
    void stopApplication();

    /**
     * kill this runtime. it will no longer be available to create scripts after
     * this method has been called
     */
    void destroy();

    /**
     * Get the available Module functions
     *
     * @return the list of Module functions in a tree structure
     */
    Module getModuleFunctions();

    /**
     * Set the raw recording mode
     *
     * @param selected
     */
    void setRawRecording(boolean selected);

    /**
     * Evaluate the expression and return the result
     *
     * @param code
     * @return evaluated expression value
     */
    String evaluate(String code);

    WindowId getTopWindowId();

    File getScreenCapture();

    void insertScript(String function);

    void releaseInterpreters();

}
