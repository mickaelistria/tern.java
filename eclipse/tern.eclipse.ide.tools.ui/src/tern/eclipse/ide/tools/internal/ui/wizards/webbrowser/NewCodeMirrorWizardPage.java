/**
 *  Copyright (c) 2013-2014 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package tern.eclipse.ide.tools.internal.ui.wizards.webbrowser;

import tern.eclipse.ide.core.utils.FileUtils;
import tern.eclipse.ide.tools.core.webbrowser.codemirror.CodeMirrorOptions;
import tern.eclipse.ide.tools.internal.ui.TernToolsUIMessages;
import tern.eclipse.ide.tools.internal.ui.wizards.NewFileWizardPage;

/**
 * Page to fill CodeMirror information.
 * 
 */
public class NewCodeMirrorWizardPage extends
		NewFileWizardPage<CodeMirrorOptions> {

	private static final String PAGE = "NewCodeMirrorWizardPage";

	public NewCodeMirrorWizardPage() {
		super(PAGE, FileUtils.HTML_EXTENSION);
		setTitle(TernToolsUIMessages.NewCodeMirrorWizardPage_title);
		setDescription(TernToolsUIMessages.NewCodeMirrorWizardPage_description);
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		getFileText().setText("index.html");
	}

	@Override
	protected void updateModel(CodeMirrorOptions options) {
		//options.setBaseURL("http://codemirror.net/");
		//options.setTernBaseURL("http://ternjs.net/");
		// options.setBaseURL("file://C:/Documents and Settings/azerr/git/tern.java/core/tern.server.nodejs/node_modules/tern/node_modules/codemirror/");
		// options.setTernBaseURL("file://C:/Documents and Settings/azerr/git/tern.java/core/tern.server.nodejs/node_modules/tern/");
		options.setEditorContent("var elt = document.getElementById('xxx');");
	}

}
