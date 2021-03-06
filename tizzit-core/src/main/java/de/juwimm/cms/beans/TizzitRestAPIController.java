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
// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 */
package de.juwimm.cms.beans;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tizzit.plugins.server.transformer.BaseContentHandler;
import org.tizzit.util.XercesHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import de.juwimm.cms.model.HostHbmDao;
import de.juwimm.cms.vo.ViewComponentValue;

@Controller
public class TizzitRestAPIController {
	private static Logger log = Logger.getLogger(TizzitRestAPIController.class);

	@Autowired
	private WebServiceSpring webSpringBean;
	@Autowired
	private HostHbmDao hostHbmDao;

	public void setWebSpringBean(WebServiceSpring webSpringBean) {
		log.info("connecting webservice...");
		this.webSpringBean = webSpringBean;
	}

	@RequestMapping(value = "/unitinfoxml/{unitid}", method = RequestMethod.GET)
	@ResponseBody
	public String unitInfoXml(@PathVariable int unitid) {
		if (log.isDebugEnabled()) log.debug("/unitinfoxml/" + unitid);
		String sb = null;
		try {
			sb = webSpringBean.getUnitInfoXml(unitid);
		} catch (Exception e) {
			log.warn("Error calling getUnitInfoXml on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/memberlist/{siteId}/{unitId}/{firstname}/{lastname}", method = RequestMethod.GET)
	@ResponseBody
	public String getMembersList(@PathVariable int siteId, @PathVariable int unitId, @PathVariable String firstname, @PathVariable String lastname) {
		if (log.isDebugEnabled()) log.debug("/memberlist/" + siteId);
		String sb = null;
		try {
			sb = webSpringBean.getMembersList(siteId, unitId, firstname, lastname);
		} catch (Exception e) {
			log.warn("Error calling getMembersList on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/navigationxml/{refVcId}/{since}/{depth}/{getPUBLSVersion}", method = RequestMethod.GET)
	@ResponseBody
	public String getNavigationXml(@PathVariable int refVcId, @PathVariable String since, @PathVariable int depth, @PathVariable boolean getPUBLSVersion) throws Exception {
		return this.getNavigationXml(refVcId, since, depth, getPUBLSVersion, -1);
	}

	@RequestMapping(value = "/navigationxml/{refVcId}/{since}/{depth}/{getPUBLSVersion}/{showType}", method = RequestMethod.GET)
	@ResponseBody
	public String getNavigationXml(@PathVariable int refVcId, @PathVariable String since, @PathVariable int depth, @PathVariable boolean getPUBLSVersion, @PathVariable int showType) throws Exception {
		if (log.isDebugEnabled()) log.debug("/navigationxml/" + refVcId);
		String sb = null;
		/*try {
			sb = webSpringBean.getNavigationXml(refVcId, since, depth, getPUBLSVersion);
		} catch (Exception e) {
			log.warn("Error calling getNavigationXml on webservicespring");
		}
		return sb;
		 */
		int ifDistanceToNavigationRoot = -1;
		boolean showOnlyAuthorized = false;

		if (ifDistanceToNavigationRoot == -1 || webSpringBean.getNavigationRootDistance4VCId(refVcId) >= ifDistanceToNavigationRoot) {
			ViewComponentValue viewComponentValue = webSpringBean.getViewComponent4Id(refVcId);
			String navigationXml = webSpringBean.getNavigationXml(refVcId, since, depth, null, getPUBLSVersion,showOnlyAuthorized, false, showType);
			if (navigationXml != null && !navigationXml.isEmpty()) {
				Document docNavigationXml = XercesHelper.string2Dom(navigationXml);
				// add axis 
				String viewComponentXPath = "//viewcomponent[@id=\"" + refVcId + "\"]";
				if (log.isDebugEnabled()) log.debug("Resolving Navigation Axis: " + viewComponentXPath);
				Node found = XercesHelper.findNode(docNavigationXml, viewComponentXPath);
				if (found != null) {
					if (log.isDebugEnabled()) log.debug("Found Axis in viewComponentId " + refVcId);
					this.setAxisToRootAttributes(found);
				} else {
					ViewComponentValue axisVcl = webSpringBean.getViewComponent4Id(viewComponentValue.getParentId());
					while (axisVcl != null) {
						found = XercesHelper.findNode(docNavigationXml, "//viewcomponent[@id=\"" + axisVcl.getViewComponentId() + "\"]");
						if (found != null) {
							if (log.isDebugEnabled()) log.debug("Found Axis in axisVcl " + axisVcl.getViewComponentId());
							this.setAxisToRootAttributes(found);
							break;
						}
						axisVcl = axisVcl.getParentId() == null ? null : webSpringBean.getViewComponent4Id(axisVcl.getParentId());
					}
				}
				// filter safeGuard
				if (showOnlyAuthorized) {
					/*try {
						String allNavigationXml = XercesHelper.doc2String(docNavigationXml);
						String filteredNavigationXml = this.webSpringBean.filterNavigation(allNavigationXml, safeguardMap);
						if (log.isDebugEnabled()) {
							log.debug("allNavigationXml\n" + allNavigationXml);
							log.debug("filteredNavigationXml\n" + filteredNavigationXml);
						}
						docNavigationXml = XercesHelper.string2Dom(filteredNavigationXml);
					} catch (Exception e) {
						log.error("Error filtering navigation with SafeGuard: " + e.getMessage(), e);
					}*/
				}
				sb = XercesHelper.doc2String(docNavigationXml);
			}
		}
		return sb;
	}

	private void setAxisToRootAttributes(Node found) {
		Node changeNode = found;
		while (changeNode != null && changeNode instanceof Element && changeNode.getNodeName().equalsIgnoreCase("viewcomponent")) {
			((Element) changeNode).setAttribute("onAxisToRoot", "true");
			changeNode = changeNode.getParentNode();
		}
	}

	@RequestMapping(value = "/navigationbackwardxml/{refVcId}/{since}/{dontShowFirst}/{getPUBLSVersion}", method = RequestMethod.GET)
	@ResponseBody
	public String getNavigationBackwardXml(@PathVariable int refVcId, @PathVariable String since, @PathVariable int dontShowFirst, @PathVariable boolean getPUBLSVersion) {
		if (log.isDebugEnabled()) log.debug("/navigationbackwardxml/" + refVcId);
		String sb = null;
		try {
			sb = webSpringBean.getNavigationBackwardXml(refVcId, since, dontShowFirst, getPUBLSVersion,0);
		} catch (Exception e) {
			log.warn("Error calling getNavigationBackwardXml on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/content/{vcId}/{getPUBLSVersion}", method = RequestMethod.GET)
	@ResponseBody
	public String getContent(@PathVariable int vcId, @PathVariable boolean getPUBLSVersion) {
		if (log.isDebugEnabled()) log.debug("/content/" + vcId);
		String sb = null;
		try {
			sb = webSpringBean.getContent(vcId, getPUBLSVersion);
		} catch (Exception e) {
			log.warn("Error calling getContent on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/contentparsed/{vcId}/{getPUBLSVersion}", method = RequestMethod.GET)
	@ResponseBody
	public String getContentParsed(@PathVariable int vcId, @PathVariable boolean getPUBLSVersion) {
		if (log.isDebugEnabled()) log.debug("/contentparsed/" + vcId);
		String sb = null;
		try {
			sb = webSpringBean.getContent(vcId, getPUBLSVersion);
		} catch (Exception e) {
			log.warn("Error calling getContent on webservicespring");
		}

		StringWriter sw = new StringWriter();
		if (sb != null) {
			try {
				XMLReader parser = XMLReaderFactory.createXMLReader();
				XMLWriter xw = new XMLWriter(sw);
				PluginManagement pm = new PluginManagement();
				BaseContentHandler transformer = new BaseContentHandler(pm, xw, webSpringBean, vcId, getPUBLSVersion);

				parser.setContentHandler(transformer);

				InputStream stream = new ByteArrayInputStream(sb.getBytes());
				InputSource inputSource = new InputSource(stream);

				long parseTime = System.currentTimeMillis();

				parser.parse(inputSource);
				parseTime = System.currentTimeMillis() - parseTime;
				log.debug("Parse time: " + parseTime + "ms");
			} catch (SAXException saxe) {
				log.warn("Error while parsing content of: " + vcId, saxe);
			} catch (IOException ioe) {
				log.warn("Error while parsing content of: " + vcId, ioe);
			}
		}
		return sw.toString().substring(38);
	}

	@RequestMapping(value = "/mandatordir/{hostName}", method = RequestMethod.GET)
	@ResponseBody
	public String getMandatorDir(@PathVariable String hostName) {
		if (log.isDebugEnabled()) log.debug("/mandatordir/" + hostName);
		String sb = null;
		try {
			sb = webSpringBean.getMandatorDir(hostName);
		} catch (Exception e) {
			log.warn("Error calling getMandatorDir on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/resolveShortLink/{hostName}", method = RequestMethod.GET)
	@ResponseBody
	public String resolveShortLink(@PathVariable String hostName, @PathVariable String requestPath) {
		if (log.isDebugEnabled()) log.debug("/resolveShortLink/" + hostName);
		String sb = null;
		try {
			sb = webSpringBean.resolveShortLink(hostName, requestPath);
		} catch (Exception e) {
			log.warn("Error calling resolveShortLink on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/startpage/{hostName}", method = RequestMethod.GET)
	@ResponseBody
	public String getStartPage(@PathVariable String hostName) {
		if (log.isDebugEnabled()) log.debug("/startpage/" + hostName);
		String sb = null;
		try {
			sb = webSpringBean.getStartPage(hostName);
		} catch (Exception e) {
			log.warn("Error calling getStartPage on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/path4unit/{unitId}/{viewDocumentId}", method = RequestMethod.GET)
	@ResponseBody
	public String getPath4Unit(@PathVariable int unitId, @PathVariable int viewDocumentId) {
		if (log.isDebugEnabled()) log.debug("/path4unit/" + unitId);
		String sb = null;
		try {
			sb = webSpringBean.getPath4Unit(unitId, viewDocumentId);
		} catch (Exception e) {
			log.warn("Error calling getPath4Unit on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/path4viewcomponent/{viewComponentId}", method = RequestMethod.GET)
	@ResponseBody
	public String getPath4ViewComponent(@PathVariable int viewComponentId) {
		if (log.isDebugEnabled()) log.debug("/path4viewcomponent/" + viewComponentId);
		String sb = null;
		try {
			sb = webSpringBean.getPath4ViewComponent(viewComponentId);
		} catch (Exception e) {
			log.warn("Error calling getPath4ViewComponent on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/mimetype4picture/{pictureId}", method = RequestMethod.GET)
	@ResponseBody
	public String getMimetype4Picture(@PathVariable int pictureId) {
		if (log.isDebugEnabled()) log.debug("/mimetype4picture/" + pictureId);
		String sb = null;
		try {
			sb = webSpringBean.getMimetype4Picture(pictureId);
		} catch (Exception e) {
			log.warn("Error calling getMimetype4Picture on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/documentname/{documentId}", method = RequestMethod.GET)
	@ResponseBody
	public String getDocumentName(@PathVariable int documentId) {
		if (log.isDebugEnabled()) log.debug("/documentname/" + documentId);
		String sb = null;
		try {
			sb = webSpringBean.getDocumentName(documentId);
		} catch (Exception e) {
			log.warn("Error calling getDocumentName on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/mimetypefordocument/{documentId}", method = RequestMethod.GET)
	@ResponseBody
	public String getMimetype4Document(@PathVariable int documentId) {
		if (log.isDebugEnabled()) log.debug("/mimetypefordocument/" + documentId);
		String sb = null;
		try {
			sb = webSpringBean.getMimetype4Document(documentId);
		} catch (Exception e) {
			log.warn("Error calling getMimetype4Document on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/safegueardloginpath/{viewComponentId}", method = RequestMethod.GET)
	@ResponseBody
	public String getSafeguardLoginPath(@PathVariable int viewComponentId) {
		if (log.isDebugEnabled()) log.debug("/safegueardloginpath/" + viewComponentId);
		String sb = null;
		try {
			sb = webSpringBean.getSafeguardLoginPath(viewComponentId);
		} catch (Exception e) {
			log.warn("Error calling getSafeguardLoginPath on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/safeguardrealmidandtype/{viewComponentId}", method = RequestMethod.GET)
	@ResponseBody
	public String getSafeguardRealmIdAndType(@PathVariable int viewComponentId) {
		if (log.isDebugEnabled()) log.debug("/safeguardrealmidandtype/" + viewComponentId);
		String sb = null;
		try {
			sb = webSpringBean.getSafeguardRealmIdAndType(viewComponentId);
		} catch (Exception e) {
			log.warn("Error calling getSafeguardRealmIdAndType on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/allunitsxml/{siteId}", method = RequestMethod.GET)
	@ResponseBody
	public String getAllUnitsXml(@PathVariable int siteId) {
		if (log.isDebugEnabled()) log.debug("/allunitsxml/" + siteId);
		String sb = null;
		try {
			sb = webSpringBean.getAllUnitsXml(siteId);
		} catch (Exception e) {
			log.warn("Error calling getAllUnitsXml on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/allunitlistxml/{siteId}", method = RequestMethod.GET)
	@ResponseBody
	public String getUnitListXml(@PathVariable int siteId) {
		if (log.isDebugEnabled()) log.debug("/allunitlistxml/" + siteId);
		String sb = null;
		try {
			sb = webSpringBean.getUnitListXml(siteId);
		} catch (Exception e) {
			log.warn("Error calling getUnitListXml on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/heading/{contentId}/{liveServer}", method = RequestMethod.GET)
	//	@ResponseBody
	public String getHeading(@PathVariable int contentId, @PathVariable boolean liveServer) {
		if (log.isDebugEnabled()) log.debug("/heading/" + contentId);
		String sb = null;
		try {
			sb = webSpringBean.getHeading(contentId, liveServer);
		} catch (Exception e) {
			log.warn("Error calling getHeading on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/defaultlanguage/{siteId}", method = RequestMethod.GET)
	@ResponseBody
	public String getDefaultLanguage(@PathVariable int siteId) {
		if (log.isDebugEnabled()) log.debug("/defaultLanguage/" + siteId);
		String sb = null;
		try {
			sb = webSpringBean.getDefaultLanguage(siteId);
		} catch (Exception e) {
			log.warn("Error calling getDefaultLanguage on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/includecontent/{currentViewComponentId}/{includeUnit}/{includeBy}/{getPublsVersion}/{xPathQuery}", method = RequestMethod.GET)
	@ResponseBody
	public String getIncludeContent(@PathVariable int currentViewComponentId, @PathVariable boolean includeUnit, @PathVariable String includeBy, @PathVariable boolean getPublsVersion, @PathVariable String xPathQuery) {
		if (log.isDebugEnabled()) log.debug("/includecontent/" + currentViewComponentId);
		String sb = null;
		try {
			sb = webSpringBean.getIncludeContent(currentViewComponentId, includeUnit, includeBy, getPublsVersion, xPathQuery);
		} catch (Exception e) {
			log.warn("Error calling getIncludeContent on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/includeteaser/{currentViewComponentId}/{getPublsVersion}/{teaserRequest}", method = RequestMethod.GET)
	@ResponseBody
	public String getIncludeTeaser(@PathVariable int currentViewComponentId, @PathVariable boolean getPublsVersion, @PathVariable String teaserRequest) {
		if (log.isDebugEnabled()) log.debug("/includeTeaser/" + currentViewComponentId);
		String sb = null;
		try {
			sb = webSpringBean.getIncludeTeaser(currentViewComponentId, getPublsVersion, teaserRequest);
		} catch (Exception e) {
			log.warn("Error calling getIncludeTeaser on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/unitinfoxml/{unitId}", method = RequestMethod.GET)
	@ResponseBody
	public String getUnitInfoXml(@PathVariable int unitId) {
		if (log.isDebugEnabled()) log.debug("/unitinfoxml/" + unitId);
		String sb = null;
		try {
			sb = webSpringBean.getUnitInfoXml(unitId);
		} catch (Exception e) {
			log.warn("Error calling getUnitInfoXml on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/lastmodifiedpages/{viewComponentId}/{unitId}/{numberOfPages}/{getPublsVersion}", method = RequestMethod.GET)
	@ResponseBody
	public String getLastModifiedPages(@PathVariable int viewComponentId, @PathVariable int unitId, @PathVariable int numberOfPages, @PathVariable boolean getPublsVersion) {
		if (log.isDebugEnabled()) log.debug("/lastmodifiedpages/" + viewComponentId);
		String sb = null;
		try {
			sb = webSpringBean.getLastModifiedPages(viewComponentId, unitId, numberOfPages, getPublsVersion);
		} catch (Exception e) {
			log.warn("Error calling getLastModifiedPages on webservicespring");
		}
		return sb;
	}

	@RequestMapping(value = "/getmodifieddate4cache/{viewComponentId}/{hostName}", method = RequestMethod.GET)
	@ResponseBody
	public long getModifiedDate4Cache(@PathVariable int viewComponentId, @PathVariable String hostName) {
		if (log.isDebugEnabled()) log.debug("/getmodifieddate4cache/" + viewComponentId);
		long date = 0;
		try {
			date = webSpringBean.getModifiedDate4Cache(viewComponentId, hostName);
		} catch (Exception e) {
			log.warn("Error calling getModifiedDate4Cache on webservicespring");
		}
		return date;
	}

	@RequestMapping(value = "/getmodifieddate4cache", method = RequestMethod.GET)
	@ResponseBody
	public long getModifiedDate4Cache(@RequestParam(value = "host") String hostName, @RequestParam(value = "requestPath") String requestPath) {
		if (log.isDebugEnabled()) log.debug("/getmodifieddate4cache/");
		long date = 0;
		try {
			String viewType = "browser";
			Map<String, String> propertyMap = this.webSpringBean.getSitemapParameters(hostName, requestPath, viewType, null, null, new HashMap<String, String>());
			Integer viewComponentId = Integer.getInteger(propertyMap.get("viewComponentId"));
			date = webSpringBean.getModifiedDate4Cache(viewComponentId, propertyMap.get("hostName"));
		} catch (Exception e) {
			log.warn("Error calling getModifiedDate4Cache on webservicespring");
		}
		return date;
	}

	@RequestMapping(value = "/synchronizegrailsuserroles", method = RequestMethod.POST)
	public void synchronizeGrailsUserRoles(@RequestBody String roles) {
		if (log.isDebugEnabled()) log.debug("/synchronizegrailsuserroles/");
		// get the role names from the body and write them to an array
		int anzRoles = 10;
		String[] grailsRoles = new String[anzRoles];
		webSpringBean.syncGrailsRoles(grailsRoles);
	}

	//http://localhost:8080/remote/action?host=www.hsg-wennigsen-gehrden.de&requestPath=de/UnsereMannschaften&safeguardUsername=null&safeguardPassword=null
	@RequestMapping(value = "/action", method = RequestMethod.GET)
	@ResponseBody
	public String getAction(@RequestParam(value = "host") String hostName, @RequestParam(value = "requestPath") String requestPath, @RequestParam(value = "safeguardUsername", required = false) String safeguardUsername, @RequestParam(value = "safeguardPassword", required = false) String safeguardPassword) throws Exception {
		// first check for some redirects for this host
		String viewType = "browser"; // This can be browser for now 

		Map<String, String> propertyMap = this.webSpringBean.getSitemapParameters(hostName, requestPath, viewType, safeguardUsername, safeguardPassword, new HashMap<String, String>());

		StringBuffer sb = new StringBuffer();

		sb.append("<root>");
		sb.append("<params>");

		//sb.append("<siteName>" + site.getName() + "</siteName>");
		//sb.append("<siteShort>" + site.getShortName() + "</siteShort>");

		for (String name : propertyMap.keySet()) {
			String val = propertyMap.get(name);
			sb.append("<" + name + ">" + val + "</" + name + ">");
		}

		sb.append("</params>");
		sb.append("<content>");
		sb.append(getContentParsed(Integer.parseInt(propertyMap.get("viewComponentId")), Boolean.parseBoolean(propertyMap.get("hostIsLiveserver"))));
		sb.append("</content>");
		sb.append("</root>");

		return sb.toString();
	}

	//	public String resolveRedirect(String hostName, String requestPath, Set<String> formerHostsSet) {
	//
	//	}
	//	
	//	public String filterNavigation(String navigationXml, Map safeGuardMap) {
	//		
	//	}
	//
	//	public PersonValue getPerson(Long personId){
	//
	//	}
	//
	//	//@RequestMapping(value = "/navigationage/{refVcID}/{since}/{depth}/{getPUBLSVersion}", method = RequestMethod.GET)
	//	public Date getNavigationAge(Integer refVcId, String since, int depth, boolean getPUBLSVersion){
	//
	//	}
	//
	//	public DepartmentValue getDepartment(Long departmentId){
	//
	//	}
	//
	//	////@RequestMapping(value="/document/{documentId}", method=RequestMethod.GET)
	//	public byte[] getDocument(Integer documentId){
	//
	//	}
	//
	//	public Integer getSiteForHost(String hostName){
	//
	//	}
	//
	//	public SiteValue getSiteValueForHost(String hostName){
	//
	//	}
	//
	//	public Collection getAllSites(){
	//
	//	}
	//
	//	public ViewComponentValue getViewComponent4Id(Integer viewComponentId){
	//
	//	}
	//
	//	public SiteValue getSite4VCId(Integer viewComponentId){
	//
	//	}
	//
	//	public int getNavigationRootDistance4VCId(Integer viewComponentId){
	//
	//	}
	//
	//	public ViewDocumentValue getViewDocument4ViewComponentId(Integer viewComponentId){
	//
	//	}
	//
	//	public ViewDocumentValue[] getViewDocuments4Site(Integer siteId){
	//
	//	}
	//
	//	public UnitValue getUnit(Integer unitId){
	//
	//	}
	//
	//	public AddressValue getAddress(Long addressId){
	//
	//	}
	//
	//	public TalktimeValue getTalktime(Long talktimeId){
	//
	//	}
	//
	//	public byte[] getThumbnail(Integer pictureId){
	//
	//	}
	//
	//	public byte[] getPreview(Integer pictureId){
	//
	//	}
	//

	@RequestMapping(value = "/picture/{pictureId}", method = RequestMethod.GET)
	@ResponseBody
	public byte[] getPicture(@PathVariable Integer pictureId) throws Exception {
		return webSpringBean.getPicture(pictureId);
	}
	//
	//	public Long getTimestamp4Picture(Integer pictureId){
	//
	//	}
	//
	//	public Long getTimestamp4Document(Integer documentId){
	//
	//	}
	//
	//
	//	public boolean hasPublishContentVersion(Integer viewComponentId){
	//
	//	}
	//
	//	public UnitValue getUnit4ViewComponent(Integer viewComponentId){
	//
	//	}
	//
	//	public SiteValue getSite4Unit(Integer unitId){
	//
	//	}
	//
	//	public boolean isVisibleForLanguageVersion(ViewComponentValue viewComponentValue, boolean liveServer){
	//
	//	}
	//
	//	public PictureValue getPictureValue(Integer pictureId){
	//
	//	}
	//
	//	public Integer getUnitIdForViewComponent(Integer viewComponentId){
	//
	//	}
	//
	//	public ViewComponentValue getViewComponent4Unit(Integer unitId, Integer viewDocumentId){
	//
	//	}
	//
	//	public ContentValue getContent(Integer contentId){
	//
	//	}
	//
	//
	//	public long getMaxSiteLastModifiedDate(){
	//
	//	}
	//
	//	public Boolean getLiveserver(String hostName) {
	//
	//	}

}
