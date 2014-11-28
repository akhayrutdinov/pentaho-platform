package org.pentaho.platform.plugin.services.importexport.legacy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.jcr.IShadowNodeHelper;
import org.pentaho.platform.repository2.unified.jcr.IShadowNodeHelperProvider;
import org.pentaho.platform.repository2.unified.jcr.JcrShadowNodeHelper;
import org.pentaho.platform.repository2.unified.jcr.ShadowNodeHelperProviderImpl;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.pentaho.platform.api.repository2.unified.RepositoryFile.SEPARATOR;
import static org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper.*;

/**
 * An integration test for {@link org.pentaho.platform.plugin.services.importexport.legacy
 * .MondrianCatalogRepositoryHelper} class
 */
@RunWith( SpringJUnit4ClassRunner.class )
public class MondrianCatalogRepositoryHelperTest extends DefaultUnifiedRepositoryBase {

  public static final String ETC_SHADOWED_FOLDER = ClientRepositoryPaths.getEtcFolderPath() + SEPARATOR + "shadowed";
  public static final String ETC_SHADOWED_MONDRIAN_FOLDER = ETC_SHADOWED_FOLDER + SEPARATOR + MONDRIAN;

  private static final String TEST_CATALOG = "tst";
  private static final String ETC_TEST_CATALOG = ETC_MONDRIAN_JCR_FOLDER + SEPARATOR + TEST_CATALOG;
  private static final String ETC_SHADOWED_TEST_CATALOG = ETC_SHADOWED_MONDRIAN_FOLDER + SEPARATOR + TEST_CATALOG;

  private MondrianCatalogRepositoryHelper helper;

  @Override @Before
  public void setUp() throws Exception {
    super.setUp();

    loginAsSysTenantAdmin();

    final ITenant defaultTenant = tenantManager.createTenant( systemTenant, TenantUtils.getDefaultTenant(), tenantAdminRoleName,
      tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( defaultTenant, singleTenantAdminUserName, PASSWORD, "", new String[]{ tenantAdminRoleName } );

    repo.createFolder( repo.getFile( "/etc" ).getId(), new RepositoryFile.Builder( MONDRIAN ).folder( true ).build(),
      "" );

    helper = new MondrianCatalogRepositoryHelper( repo );
  }

  @Test
  public void shadowedNodeIsCreatedWithMondrianSchema() throws Exception {
    helper.addHostedCatalog( new ByteArrayInputStream( new byte[ 0 ] ), TEST_CATALOG, "" );

    assertNotNull( repo.getFile( ETC_TEST_CATALOG + SEPARATOR + "schema.xml" ) );
    assertNotNull( repo.getFile( ETC_SHADOWED_TEST_CATALOG + SEPARATOR + "schema.xml" ) );
  }

  @Test
  public void shadowedNodeIsCreatedWithMondrianSchemaTwice() throws Exception {
    final String ANOTHER_TEST_CATALOG = TEST_CATALOG + TEST_CATALOG;

    helper.addHostedCatalog( new ByteArrayInputStream( new byte[ 0 ] ), TEST_CATALOG, "" );
    helper.addHostedCatalog( new ByteArrayInputStream( new byte[ 0 ] ), ANOTHER_TEST_CATALOG, "" );

    assertNotNull( repo.getFile( ETC_SHADOWED_TEST_CATALOG + SEPARATOR + "schema.xml" ) );
    assertNotNull( repo.getFile( ETC_SHADOWED_TEST_CATALOG + TEST_CATALOG + SEPARATOR
        + "schema.xml" ) );
  }

  @Test
  public void shadowedNodeIsRemovedWithMondrianSchema() throws Exception {
    helper.addHostedCatalog( new ByteArrayInputStream( new byte[ 0 ] ), TEST_CATALOG, "" );
    helper.deleteCatalog( TEST_CATALOG );

    assertNull( repo.getFile( ETC_SHADOWED_TEST_CATALOG + SEPARATOR + "schema.xml" ) );
    assertNotNull( repo.getFile( ETC_SHADOWED_MONDRIAN_FOLDER ) );
  }

  @Test
  public void removedMondrianSchemaWithoutShadowedNode() throws Exception {
    helper.addHostedCatalog( new ByteArrayInputStream( new byte[ 0 ] ), TEST_CATALOG, "" );

    repo.deleteFile( repo.getFile( ETC_SHADOWED_MONDRIAN_FOLDER ).getId(), TEST_CATALOG );
    assertNull( repo.getFile( ETC_SHADOWED_TEST_CATALOG ) );

    helper.deleteCatalog( TEST_CATALOG );

    assertNull( repo.getFile( ETC_TEST_CATALOG ) );
  }

  @Test
  public void updateMondrianSchema() throws Exception {
    helper.addHostedCatalog( new ByteArrayInputStream( new byte[ 0 ] ), TEST_CATALOG, "" );
    helper.addHostedCatalog( new ByteArrayInputStream( "".getBytes() ), TEST_CATALOG, "" );

    assertNotNull( repo.getFile( ETC_SHADOWED_TEST_CATALOG + SEPARATOR + "schema.xml" ) );
  }
}
