/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.uriresolver;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.Callback;

public class ModifiedResourceHandler extends ResourceHandler {

	public ModifiedResourceHandler() {
		super();
	}

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		// 403 if user agent starts with Java/1. with https://lime.software/.
		// See https://github.com/redhat-developer/vscode-xml/issues/429#issuecomment-784875083
		String userAgent = request.getHeaders().get("User-Agent");
		if (userAgent != null && userAgent.startsWith("Java/1.")) {
			Response.writeError(request, response, callback, 403, userAgent + " is not allowed");
			return true;
		}
		return super.handle(request, response, callback);
	}

}
