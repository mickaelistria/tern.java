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
package tern.eclipse.ide.internal.ui.wizards;

import org.eclipse.jface.viewers.ISelection;

import tern.eclipse.ide.core.generator.TernPluginOptions;
import tern.eclipse.ide.core.utils.FileUtils;
import tern.eclipse.ide.internal.ui.TernUIMessages;

/**
 * Page to fill tern plugin information.
 * 
 */
public class NewTernPluginWizardPage extends NewFileWizardPage {

	private static final String PAGE = "NewTernPluginWizardPage";

	private final TernPluginOptions options;

	public NewTernPluginWizardPage(TernPluginOptions options,
			ISelection selection) {
		super(PAGE, FileUtils.JS_EXTENSION, selection);
		this.options = options;
		setTitle(TernUIMessages.NewTernPluginWizardPage_title);
		setDescription(TernUIMessages.NewTernPluginWizardPage_description);
	}

	@Override
	protected void synchModel() {
		options.setPluginName(getName());
		options.setDefName(getName());
	}

}
