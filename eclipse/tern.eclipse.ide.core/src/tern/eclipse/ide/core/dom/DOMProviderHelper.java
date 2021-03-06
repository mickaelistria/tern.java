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
package tern.eclipse.ide.core.dom;

public class DOMProviderHelper {

	private static IDOMProvider provider = DefaultDOMProvider.INSTANCE;

	public static void setProvider(IDOMProvider provider) {
		DOMProviderHelper.provider = provider;
	}

	public static IDOMProvider getProvider() {
		return provider;
	}
}
