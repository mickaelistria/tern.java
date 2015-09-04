/**
 *  Copyright (c) 2013-2015 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package tern.eclipse.ide.ui.properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.eclipsesource.json.JsonObject;

import tern.EcmaVersion;
import tern.TernException;
import tern.eclipse.ide.core.IIDETernProject;
import tern.eclipse.ide.core.IWorkingCopy;
import tern.eclipse.ide.core.IWorkingCopyListener;
import tern.eclipse.ide.internal.ui.TernUIMessages;
import tern.eclipse.ide.internal.ui.Trace;
import tern.eclipse.ide.ui.ImageResource;
import tern.eclipse.ide.ui.viewers.ECMAVersionLabelProvider;
import tern.server.ITernModule;
import tern.server.ITernModuleConfigurable;
import tern.server.TernPlugin;

/**
 * Tern Main page for project properties used to updat e.tern-project for :
 * 
 * <ul>
 * <li>ECMAScript info : ecmaVersion + es_modules tern plugin.</li>
 * <li>JSDoc info : doc_comment + strong option.</li>
 * </ul>
 * 
 * 
 */
public class TernMainPropertyPage extends AbstractTernPropertyPage implements IWorkingCopyListener {

	private static final String STRONG_OPTION = "strong";

	public static final String PROP_ID = "tern.eclipse.ide.ui.properties";

	private Button useESModules;
	private Button useJSDoc;
	private Button jsdocStrong;
	private ComboViewer ecmaVersionViewer;

	public TernMainPropertyPage() {
		setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_LOGO));
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(4, 4, true, true));
		composite.setLayout(new GridLayout());
		IWorkingCopy workingCopy = getWorkingCopy();
		// ECMAScript contents
		createECMAScriptComplianceContents(composite, workingCopy);
		// JSDoc contents
		createJSDocContents(composite, workingCopy);
		workingCopy.addWorkingCopyListener(this);
		return composite;
	}

	/**
	 * Create "ECMAScript Compliance" contents panel.
	 * 
	 * @param parent
	 * @param workingCopy
	 */
	private void createECMAScriptComplianceContents(final Composite parent, final IWorkingCopy workingCopy) {

		Group ecmaGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		ecmaGroup.setText(TernUIMessages.TernMainPropertyPage_ecmaGroup_label);
		ecmaGroup.setLayout(new GridLayout(2, false));
		ecmaGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Ecma Version
		Label versionLabel = new Label(ecmaGroup, SWT.NONE);
		versionLabel.setText(TernUIMessages.TernMainPropertyPage_ecmaVersion);
		versionLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ecmaVersionViewer = new ComboViewer(ecmaGroup, SWT.READ_ONLY);
		ecmaVersionViewer.setContentProvider(ArrayContentProvider.getInstance());
		ecmaVersionViewer.setLabelProvider(ECMAVersionLabelProvider.INSTANCE);
		ecmaVersionViewer.setInput(EcmaVersion.values());
		ecmaVersionViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateUseESModules(workingCopy);
			}
		});

		// Use ES modules?
		useESModules = new Button(ecmaGroup, SWT.CHECK);
		useESModules.setText(TernUIMessages.TernMainPropertyPage_useESModules);
		useESModules.setEnabled(false);

		// Update ES version
		ecmaVersionViewer.setSelection(new StructuredSelection(workingCopy.getEcmaVersion()));
		// Update use ES modules?
		useESModules.setSelection(workingCopy.hasCheckedTernModule(TernPlugin.es_modules.getName()));
		useESModules.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (useESModules.getSelection()) {
					workingCopy.getCheckedModules().add(TernPlugin.es_modules);
				} else {
					workingCopy.getCheckedModules().remove(TernPlugin.es_modules);
				}
			}
		});
	}

	private void updateUseESModules(IWorkingCopy workingCopy) {
		EcmaVersion ecmaVersion = getEcmaVersion();
		useESModules.setEnabled(ecmaVersion.getVersion() >= 6);
		if (!useESModules.isEnabled()) {
			useESModules.setSelection(false);
		}
		workingCopy.setEcmaVersion(ecmaVersion);
		
	}

	protected EcmaVersion getEcmaVersion() {
		IStructuredSelection selection = (IStructuredSelection) ecmaVersionViewer.getSelection();
		EcmaVersion version = (EcmaVersion) selection.getFirstElement();
		return version;
	}

	/**
	 * Create "JSDoc" contents panel.
	 * 
	 * @param parent
	 * @param workingCopy
	 */
	private void createJSDocContents(Composite parent, final IWorkingCopy workingCopy) {
		Group jsdocGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		jsdocGroup.setText(TernUIMessages.TernMainPropertyPage_jsdocGroup_label);
		jsdocGroup.setLayout(new GridLayout());
		jsdocGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Use JSDoc?
		useJSDoc = new Button(jsdocGroup, SWT.CHECK);
		useJSDoc.setText(TernUIMessages.TernMainPropertyPage_useJSDoc);
		useJSDoc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Strong.
		jsdocStrong = new Button(jsdocGroup, SWT.CHECK);
		jsdocStrong.setText(TernUIMessages.TernMainPropertyPage_JSDocStrong);
		jsdocStrong.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		useJSDoc.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = useJSDoc.getSelection();
				if (enabled) {
					workingCopy.getCheckedModules().add(getDocComment(workingCopy));
				} else {
					workingCopy.getCheckedModules().remove(getDocComment(workingCopy));
				}
				jsdocStrong.setEnabled(enabled);
			}
		});

		jsdocStrong.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ITernModuleConfigurable docComment = getDocComment(workingCopy);
				JsonObject options = null;
				if (jsdocStrong.getSelection()) {
					options = new JsonObject();
					options.set(STRONG_OPTION, true);
				}
				docComment.setOptions(options);

			}
		});

		if (workingCopy.hasCheckedTernModule(TernPlugin.doc_comment.getName())) {
			useJSDoc.setSelection(true);
			jsdocStrong.setEnabled(true);
			JsonObject options = getDocComment(workingCopy).getOptions();
			if (options != null) {
				jsdocStrong.setSelection(options.getBoolean(STRONG_OPTION, false));
			}
		} else {
			jsdocStrong.setEnabled(false);
		}
	}

	@Override
	protected void doPerformOk() throws Exception {
		// Do nothing
	}

	@Override
	public void moduleSelectionChanged(ITernModule module, boolean selected) {
		if (TernPlugin.doc_comment.getName().equals(module.getName())) {
			useJSDoc.setSelection(selected);
		} else if (TernPlugin.es_modules.getName().equals(module.getName())) {
			useESModules.setSelection(selected);
		}
	}

	protected ITernModuleConfigurable getDocComment(IWorkingCopy workingCopy) {
		try {
			return (ITernModuleConfigurable) workingCopy.getTernModule(TernPlugin.doc_comment.getName());
		} catch (TernException e) {
			Trace.trace(Trace.SEVERE, "Error while getting tern module from working copy", e);
		}
		return null;
	}

}