/**
 * Copyright (c) 2009 Juwi MacMillan Group GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.juwimm.cms.search.res;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.springframework.beans.factory.annotation.Autowired;

import de.juwimm.cms.model.DocumentHbmDao;
import de.juwimm.cms.model.UnitHbm;
import de.juwimm.cms.search.res.pdf.LucenePDFDocument;

/**
 * @author <a href="mailto:carsten.schalm@juwimm.com">Carsten Schalm</a>
 * company Juwi|MacMillan Group Gmbh, Walsrode, Germany
 * @version $Id$
 */
public class PDFDocumentLocator {
	public static final String MIME_TYPE = "application/pdf";
	private static Logger log = Logger.getLogger(PDFDocumentLocator.class);
	@Autowired
	private DocumentHbmDao documentHbmDao;

	public Document getDocument(de.juwimm.cms.model.DocumentHbm document) {
		Document doc = null;

		InputStream bis = new ByteArrayInputStream(documentHbmDao.getDocumentContent(document.getDocumentId()));
		try {
			doc = LucenePDFDocument.getDocument(bis);
		} catch (IOException e) {
			if (log.isInfoEnabled()) log.info("Error indexing document " + document.getDocumentId() + " (" + document.getDocumentName() + ")" + " document may be password-protected: " + e.getMessage());
			if (log.isDebugEnabled()) log.debug(e.getMessage(), e);
			return null;
		}
		doc.add(new Field("documentId", document.getDocumentId().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("uid", document.getDocumentId().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		String docName = document.getDocumentName();
		if (docName == null) docName = "";
		doc.add(new Field("documentName", docName, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("title", docName, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("unitId", document.getUnit().getUnitId().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("unitName", document.getUnit().getName(), Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("mimeType", document.getMimeType(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("timeStamp", document.getTimeStamp().toString(), Field.Store.YES, Field.Index.NO));

		return doc;
	}

	public Document getResource(de.juwimm.cms.model.DocumentHbm document) {
		Document resource = new Document();
		InputStream bis = new ByteArrayInputStream(documentHbmDao.getDocumentContent(document.getDocumentId()));

		try {
			String content = "";
			if(document.getPassword()!=null){
				content = LucenePDFDocument.getPdfContent(bis,document.getPassword());
			} else {
				content = LucenePDFDocument.getPdfContent(bis);
			}
			if (content == null) return null;
			resource.add(new Field("contents", content, Field.Store.YES, Field.Index.ANALYZED));
			int summarySize = Math.min(content.length(), 500);
			String summary = content.substring(0, summarySize);
			resource.add(new Field("summary", summary, Field.Store.YES, Field.Index.NO));
		} catch (IOException e) {
			if (log.isInfoEnabled()) log.info("Error indexing document " + document.getDocumentId() + " (" + document.getDocumentName() + ")" + " document may be password-protected: " + e.getMessage());
			if (log.isDebugEnabled()) log.debug(e.getMessage(), e);
			return null;
		}
		resource.add(new Field("documentId", document.getDocumentId().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		resource.add(new Field("uid", document.getDocumentId().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		String docName = document.getDocumentName();
		if (docName == null) docName = "";
		resource.add(new Field("documentName", docName, Field.Store.YES, Field.Index.ANALYZED));
		resource.add(new Field("title", docName, Field.Store.YES, Field.Index.ANALYZED));
		resource.add(new Field("unitId", document.getUnit().getUnitId().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		resource.add(new Field("unitName", document.getUnit().getName(), Field.Store.YES, Field.Index.ANALYZED));
		resource.add(new Field("mimeType", document.getMimeType(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		resource.add(new Field("timeStamp", document.getTimeStamp().toString(), Field.Store.YES, Field.Index.NO));

		UnitHbm unitHbm=document.getUnit();
		if(unitHbm==null){
			unitHbm=document.getViewComponent().getViewComponentUnit().getAssignedUnit();
		}
		resource.add(new Field("siteId", unitHbm.getSite().getSiteId().toString(), Field.Store.YES, Field.Index.ANALYZED));

		return resource;
	}

	public Document getExternalResource(String url, InputStream in) {
		Document resource = new Document();
		resource.add(new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
		resource.add(new Field("uid", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
		try {
			String content = LucenePDFDocument.getPdfContent(in);
			if (content == null) return resource;
			resource.add(new Field("contents", content, Field.Store.YES, Field.Index.ANALYZED));
			int summarySize = Math.min(content.length(), 500);
			String summary = content.substring(0, summarySize);
			resource.add(new Field("summary", summary, Field.Store.YES, Field.Index.NO));
			in.close();
		} catch (IOException e) {
			if (log.isInfoEnabled()) log.info("Error indexing url " + url + " document may be password-protected: " + e.getMessage());
			if (log.isDebugEnabled()) log.debug(e.getMessage(), e);
			return null;
		}

		return resource;
	}

}
