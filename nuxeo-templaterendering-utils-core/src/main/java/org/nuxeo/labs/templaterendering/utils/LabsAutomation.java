/*
 * (C) Copyright 2024 Hyland (http://hyland.com/)  and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Thibaud Arguillere
 */
package org.nuxeo.labs.templaterendering.utils;

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.template.api.context.DocumentWrapper;

/**
 * @since TODO
 */
public class LabsAutomation {

    protected final DocumentModel doc;

    protected final DocumentWrapper nxWrapper;

    public LabsAutomation(DocumentModel doc, DocumentWrapper nuxeoWrapper) {
        this.doc = doc;
        this.nxWrapper = nuxeoWrapper;
    }

    public Object run(String chainId) {

        return run(chainId, null);
    }

    public Object run(String chainId, String parameter) {

        AutomationService as = Framework.getService(AutomationService.class);
        OperationContext octx = new OperationContext(doc.getCoreSession());
        octx.setInput(doc);
        octx.put("automationTemplateParam", parameter);

        Object result = null;

        try {
            result = as.run(octx, chainId);
        } catch (OperationException e) {
            throw new NuxeoException(
                    "Error running chain <" + chainId + "> in the context of TemplateRendering Processor", e);
        }
        return result;
    }

}
