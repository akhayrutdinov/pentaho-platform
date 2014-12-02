package org.pentaho.platform.repository2.unified.jcr;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;

import static org.junit.Assert.*;

public class JcrShadowNodeHelperTest extends DefaultUnifiedRepositoryBase {

  private JcrShadowNodeHelper helper;
  private ITenant defaultTenant;

  @Override @Before
  public void setUp() throws Exception {
    super.setUp();

    loginAsSysTenantAdmin();
    defaultTenant = tenantManager.createTenant( systemTenant, TenantUtils.getDefaultTenant(), tenantAdminRoleName,
      tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( defaultTenant, USERNAME_SUZY, PASSWORD, "", new String[] { tenantAdminRoleName } );
    logout();

    login( USERNAME_SUZY, defaultTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    RepositoryFile publicOfSuzy = repo.getFile( ClientRepositoryPaths.getPublicFolderPath() );
    if (publicOfSuzy == null) {
      repo.createFolder( repo.getFile( "/" ).getId(), new RepositoryFile.Builder( "public" ).folder( true )
        .build(), "" );
    }
    logout();

    helper = new JcrShadowNodeHelper( repo, repositoryFileAclDao, "/etc" );
  }

  @Override
  @After
  public void tearDown() throws Exception {
    loginAsSysTenantAdmin();

    ITenant defaultTenant = tenantManager.getTenant( "/" + ServerRepositoryPaths.getPentahoRootFolderName() + "/" + TenantUtils.getDefaultTenant() );
    if ( defaultTenant != null ) {
      cleanupUserAndRoles( defaultTenant );
    }

    super.tearDown();
  }

  @Test
  public void isVisible() {
    login( USERNAME_SUZY, defaultTenant, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    RepositoryFile testFolder = repo.createFolder( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath(USERNAME_SUZY) ).getId(),
      new RepositoryFile.Builder( "test" ).folder( true ).build(), "");
    assertNotNull( testFolder );

    RepositoryFile testTxt = repo.createFile( testFolder.getId(), new RepositoryFile.Builder( "test.txt" ).build(),
      new NodeRepositoryFileData( new DataNode( "" ) ), "" );
    assertNotNull( testTxt );

    //assertTrue( helper.createShadowNodeForFile( null, "test.txt" ) );

    //assertTrue( helper.isVisibleFor( "test.txt", USERNAME_SUZY ) );
  }
}