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
package tern.eclipse.ide.ui.viewers;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

import tern.eclipse.ide.core.scriptpath.ITernScriptPath;

public class TernScriptPathContentProvider extends ArrayContentProvider
		implements ITreeContentProvider {

	@Override
	public Object[] getChildren(Object element) {
		if ((element instanceof ITernScriptPath)) {
			return ((ITernScriptPath) element).getScriptResources().toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return element instanceof ITernScriptPath;
	}

}
