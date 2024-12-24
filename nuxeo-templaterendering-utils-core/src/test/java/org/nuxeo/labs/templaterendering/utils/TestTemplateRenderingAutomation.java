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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;

import jakarta.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.audit.test.AuditFeature;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.context.ContextService;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.template.api.adapters.TemplateBasedDocument;
import org.nuxeo.template.api.adapters.TemplateSourceDocument;
import org.nuxeo.template.processors.xdocreport.ZipXmlHelper;

/**
 * 
 * 
 */
@RunWith(FeaturesRunner.class)
@Features({AuditFeature.class, AutomationFeature.class})
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("org.nuxeo.labs.templaterendering.utils.nuxeo-templaterendering-utils-core")
@Deploy("org.nuxeo.template.manager.api")
@Deploy("org.nuxeo.template.manager")
@Deploy("org.nuxeo.template.manager.xdocreport")
@Deploy("org.nuxeo.labs.templaterendering.utils.nuxeo-templaterendering-utils-core:automation-scripting-contrib.xml")
public class TestTemplateRenderingAutomation {

    protected static final String TEMPLATE_NAME = "mytestTemplate";

    protected static final String TEST_DOC_TITlE = "My Test Doc";

    protected static final String TEST_DOC_DESCRIPTION = "My Test Doc Description";

    // Not really a constant.
    protected String TEMPLATE_BASED_DOC_PATH;

    @Inject
    CoreSession session;

    @Inject
    AutomationService automationService;

    @Inject
    ContextService ctxService;

    // Basic copy paste of org.nuxeo.ecm.platform.template.tests.SimpleTemplateDocTestCase#setupTestDocs
    // templateFile is a local resource file, "TemplateRendering/automation-simple.docx"
    protected TemplateBasedDocument createTemplateAndTestDoc(String templateFile) {

        DocumentModel root = session.getRootDocument();
        DocumentModel templateDoc = session.createDocumentModel(root.getPathAsString(), "templatedDoc",
                "TemplateSource");
        templateDoc.setPropertyValue("dc:title", "MyTemplate");
        templateDoc.setPropertyValue("tmpl:templateName", TEMPLATE_NAME);

        File f = FileUtils.getResourceFileFromContext(templateFile);
        templateDoc.setPropertyValue("file:content", new FileBlob(f));

        templateDoc = session.createDocument(templateDoc);

        TemplateSourceDocument templateSource = templateDoc.getAdapter(TemplateSourceDocument.class);
        assertNotNull(templateSource);
        assertEquals(TEMPLATE_NAME, templateSource.getName());

        DocumentModel testDoc = session.createDocumentModel(root.getPathAsString(), "templatedBasedDoc",
                "TemplateBasedFile");
        testDoc.setPropertyValue("dc:title", TEST_DOC_TITlE);
        testDoc.setPropertyValue("dc:description", TEST_DOC_DESCRIPTION);

        testDoc = session.createDocument(testDoc);
        TEMPLATE_BASED_DOC_PATH = testDoc.getPath().toString();

        // associate doc and template
        TemplateBasedDocument adapter = testDoc.getAdapter(TemplateBasedDocument.class);
        assertNotNull(adapter);

        adapter.setTemplate(templateDoc, true);

        session.save();

        return adapter;

    }

    @Test
    public void testAutomationReturnsString() throws Exception {
        // See this template to check what is supposed to be in the created document.
        TemplateBasedDocument adapter = createTemplateAndTestDoc("TemplateRendering/automation-simple.docx");
        DocumentModel testDoc = adapter.getAdaptedDoc();
        assertNotNull(testDoc);

        Blob newBlob = adapter.renderAndStoreAsAttachment(TEMPLATE_NAME, true);
        assertNotNull(newBlob);

        String xmlContent = ZipXmlHelper.readXMLContent(newBlob, ZipXmlHelper.DOCX_MAIN_FILE);

        // See javascript.test1
        String expected = "AUTOMATION RESULT: " + TEST_DOC_TITlE + ", " + TEMPLATE_BASED_DOC_PATH + ", "
                + TEST_DOC_DESCRIPTION;
        assertTrue(xmlContent.contains(expected));
    }

    @Test
    public void testAutomationReturnsArray() throws Exception {
        // See this template to check what is supposed to be in the created document.
        TemplateBasedDocument adapter = createTemplateAndTestDoc("TemplateRendering/automation-return-array.docx");
        DocumentModel testDoc = adapter.getAdaptedDoc();
        assertNotNull(testDoc);

        Blob newBlob = adapter.renderAndStoreAsAttachment(TEMPLATE_NAME, true);
        assertNotNull(newBlob);

        String textContent = blobToText(newBlob);

        // See javascript.test2
        // Output is in bullet lists, we just test the values are there
        assertTrue(textContent.contains(TEST_DOC_TITlE));
        assertTrue(textContent.contains(TEMPLATE_BASED_DOC_PATH));
        assertTrue(textContent.contains(TEST_DOC_DESCRIPTION));
    }

    @Test
    public void testAutomationReturnsArrayOfObjects() throws Exception {
        // See this template to check what is supposed to be in the created document.
        TemplateBasedDocument adapter = createTemplateAndTestDoc("TemplateRendering/automation-return-arrayOfObjects.docx");
        DocumentModel testDoc = adapter.getAdaptedDoc();
        assertNotNull(testDoc);

        Blob newBlob = adapter.renderAndStoreAsAttachment(TEMPLATE_NAME, true);
        assertNotNull(newBlob);
        
        String textContent = blobToText(newBlob);
        // See javascript.test2
        // Output is in bullet lists, we just test the values are there
        assertTrue(textContent.contains("TITLE: " + TEST_DOC_TITlE));
        assertTrue(textContent.contains("PATH: " + TEMPLATE_BASED_DOC_PATH));
        assertTrue(textContent.contains("DESCRIPTION: " + TEST_DOC_DESCRIPTION));
    }

    @Test
    public void testAutomationWithAParam() throws Exception {
        // See this template to check what is supposed to be in the created document.
        TemplateBasedDocument adapter = createTemplateAndTestDoc("TemplateRendering/automation-with-parameter.docx");
        DocumentModel testDoc = adapter.getAdaptedDoc();
        assertNotNull(testDoc);

        Blob newBlob = adapter.renderAndStoreAsAttachment(TEMPLATE_NAME, true);
        assertNotNull(newBlob);

        String textContent = blobToText(newBlob);
        // See javascript.test4 and the template. Parameter is a JSON object as string
        assertTrue(textContent.contains("1, hello, true"));
    }
    
    protected String blobToText(Blob blob) throws Exception {
        SimpleBlobHolder blobHolder = new SimpleBlobHolder(blob);
        ConversionService conversionService = Framework.getService(ConversionService.class);
        BlobHolder resultBlob = conversionService.convert("any2text", blobHolder, null);
        String text;
        text = new String(resultBlob.getBlob().getByteArray(), StandardCharsets.UTF_8);
        
        return text;
    }

}
