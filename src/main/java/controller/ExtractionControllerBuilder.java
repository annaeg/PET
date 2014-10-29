/**
* Copyright (c) 2014, Fabio Corubolo - University of Liverpool and Anna Eggers - Göttingen State and University Library
* The work has been developed in the PERICLES Project by Members of the PERICLES Consortium.
* This work was supported by the European Commission Seventh Framework Programme under Grant Agreement Number FP7- 601138 PERICLES.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at:   http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
* an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including without
* limitation, any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTIBITLY, or FITNESS FOR A PARTICULAR
* PURPOSE. In no event and under no legal theory, whether in tort (including negligence), contract, or otherwise,
* unless required by applicable law or agreed to in writing, shall any Contributor be liable for damages, including
* any direct, indirect, special, incidental, or consequential damages of any character arising as a result of this
* License or out of the use or inability to use the Work.
* See the License for the specific language governing permissions and limitation under the License.
*/
package controller;

/**
 * The ExtractionControllerBuilder creates an ExtractionController object. It is
 * developed with the builder pattern.
 * <p>
 * Builder call example: new ExtractionControllerBuilder().once().create();
 * 
 * @see ExtractionController
 */
public class ExtractionControllerBuilder {
	boolean graphic = true;
	protected boolean updateExtraction = true;
	public String storageSystem;

	/**
	 * This function has to be called as the last function of the chain. It will
	 * create the ExtractionController.
	 * 
	 * @return Returns the ExtractionController with the configuration specified
	 *         by the functions, that were called in the chain before this
	 *         function.
	 */
	public ExtractionController create() {
		return new ExtractionController(this);
	}

	/**
	 * Call this function to start a single extraction and no continuous
	 * extraction.
	 * 
	 * @return The altered builder.
	 */
	public ExtractionControllerBuilder once() {
		this.updateExtraction = false;
		return this;
	}

	/**
	 * Call this function to start the PET without any graphic. This will
	 * disable the GUI and the system tray icon. The GUI can be started
	 * afterwards from the CLI with the command "gui".
	 * 
	 * @return The altered builder.
	 */
	public ExtractionControllerBuilder headless() {
		this.graphic = false;
		return this;
	}
}