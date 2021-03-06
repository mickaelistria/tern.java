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
package tern.eclipse.ide.tools.internal.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * Tern UI messages.
 * 
 */
public final class TernToolsUIMessages extends NLS {

	private static final String BUNDLE_NAME = "tern.eclipse.ide.tools.internal.ui.TernToolsUIMessages"; //$NON-NLS-1$

	private static ResourceBundle fResourceBundle;

	// Buttons
	public static String Button_browse;

	// Wizard
	public static String NewFileWizardPage_name_text;
	public static String NewFileWizardPage_container_text;
	public static String NewFileWizardPage_fileName_text;

	public static String NewTernDefWizard_windowTitle;
	public static String NewTernDefWizardPage_title;
	public static String NewTernDefWizardPage_description;

	public static String NewTernPluginWizard_windowTitle;
	public static String NewTernPluginWizardPage_title;
	public static String NewTernPluginWizardPage_description;

	public static String TernDefsSelectionWizardPage_title;
	public static String TernDefsSelectionWizardPage_description;
	public static String TernPluginsSelectionWizardPage_title;
	public static String TernPluginsSelectionWizardPage_description;
	public static String NewCodeMirrorWizard_windowTitle;
	public static String NewCodeMirrorWizardPage_title;
	public static String NewCodeMirrorWizardPage_description;
	public static String NewAceWizard_windowTitle;
	public static String NewAceWizardPage_title;
	public static String NewAceWizardPage_description;	
	public static String NewOrionWizard_windowTitle;
	public static String NewOrionWizardPage_title;
	public static String NewOrionWizardPage_description;
	
	private TernToolsUIMessages() {
	}

	public static ResourceBundle getResourceBundle() {
		try {
			if (fResourceBundle == null)
				fResourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
		} catch (MissingResourceException x) {
			fResourceBundle = null;
		}
		return fResourceBundle;
	}

	static {
		NLS.initializeMessages(BUNDLE_NAME, TernToolsUIMessages.class);
	}
}
