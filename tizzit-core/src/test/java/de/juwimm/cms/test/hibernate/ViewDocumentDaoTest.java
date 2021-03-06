package de.juwimm.cms.test.hibernate;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;

import de.juwimm.cms.authorization.model.UserHbm;
import de.juwimm.cms.authorization.model.UserHbmDao;
import de.juwimm.cms.authorization.model.UserHbmImpl;
import de.juwimm.cms.model.SiteHbm;
import de.juwimm.cms.model.SiteHbmDao;
import de.juwimm.cms.model.SiteHbmImpl;
import de.juwimm.cms.model.UnitHbm;
import de.juwimm.cms.model.UnitHbmImpl;
import de.juwimm.cms.model.ViewComponentHbm;
import de.juwimm.cms.model.ViewComponentHbmImpl;
import de.juwimm.cms.model.ViewDocumentHbm;
import de.juwimm.cms.model.ViewDocumentHbmDao;
import de.juwimm.cms.model.ViewDocumentHbmDaoImpl;
import de.juwimm.cms.model.ViewDocumentHbmImpl;

public class ViewDocumentDaoTest extends HbmTestImpl {

	@Autowired
	ViewDocumentHbmDao viewDocumentDao;
	@Autowired
	SiteHbmDao siteDao;
	UserHbmDao userDao;

	public void initializeServiceBeans() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onSetUp() throws Exception {
		super.onSetUp();
		mockAuthetication();
		init();

		userDao = EasyMock.createMock(UserHbmDao.class);
		((ViewDocumentHbmDaoImpl) viewDocumentDao).setUserHbmDao(userDao);
	}



	public void init() {
		ViewDocumentHbm viewDocument = new ViewDocumentHbmImpl();
		ViewComponentHbm viewComponent = new ViewComponentHbmImpl();
		SiteHbm site = new SiteHbmImpl();

		site.setSiteId(1);
		site.setName("testSite");
		insertSite(site);

		viewComponent.setViewComponentId(1);

		viewDocument.setViewDocumentId(1);
		viewDocument.setLanguage("testLanguage");
		viewDocument.setSite(site);
		viewDocument.setViewType("testViewType");
		insertViewDocument(viewDocument);
	}

	/**
	 * Test Load
	 */
	public void testLoad() {
		ViewDocumentHbm viewDocument = viewDocumentDao.load(1);
		Assert.assertNotNull(viewDocument);
		Assert.assertEquals("testLanguage", viewDocument.getLanguage());
		Assert.assertEquals("testViewType", viewDocument.getViewType());

	}

	/**
	 * Test Create
	 * expect: assign id and properties
	 */
	public void testCreate() {
		ViewDocumentHbm viewDocument = new ViewDocumentHbmImpl();

		UnitHbm unit = new UnitHbmImpl();
		unit.setUnitId(1);
		unit.setName("testName");

		SiteHbm site = siteDao.load(1);
		site.setRootUnit(unit);

		UserHbm user = new UserHbmImpl();
		user.setUserId("testUser");
		user.setActiveSite(site);

		ViewComponentHbm viewComponent = new ViewComponentHbmImpl();
		viewComponent.setViewComponentId(1);

		viewDocument.setViewComponent(viewComponent);
		viewDocument.setLanguage("testLanguage");
		viewDocument.setViewType("testViewType");

		try {
			EasyMock.expect(userDao.load(EasyMock.eq("testUser"))).andReturn(user);
		} catch (Exception e) {
			Assert.assertTrue(false);
		}

		EasyMock.replay(userDao);

		try {
			viewDocument = viewDocumentDao.create(viewDocument);
			Assert.assertNotNull(viewDocument.getViewDocumentId());
			Assert.assertEquals("testLanguage", viewDocument.getLanguage());
			Assert.assertEquals("testViewType", viewDocument.getViewType());
		} catch (Exception e) {
			Assert.assertTrue(false);
		}

		EasyMock.verify(userDao);
	}

}
