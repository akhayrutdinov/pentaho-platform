package org.pentaho.platform.repository2.unified.jcr;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;

import java.util.concurrent.Callable;

import static org.pentaho.platform.api.repository2.unified.RepositoryFile.SEPARATOR;

/**
 * @author Andrey Khayrutdinov
 */
public class JcrShadowNodeHelper implements IShadowNodeHelper {
  private static final Log logger = LogFactory.getLog( JcrShadowNodeHelper.class );

  private final IUnifiedRepository unifiedRepository;
  private final IRepositoryFileAclDao repositoryFileAclDao;
  private final String jcrPath;

  public JcrShadowNodeHelper( IUnifiedRepository unifiedRepository,
                              IRepositoryFileAclDao repositoryFileAclDao,
                              String jcrPath) {
    this.unifiedRepository = unifiedRepository;
    this.repositoryFileAclDao = repositoryFileAclDao;
    this.jcrPath = jcrPath;
  }

  @Override public String getJcrPath() {
    return jcrPath;
  }

  @Override public RepositoryFile getJcrFolder() {
    return unifiedRepository.getFile( jcrPath );
  }

  // todo Khayrutdinov : to verify
  @Override public boolean isVisibleFor( final String filePath, String user ) {
    String fullPath = getJcrPath() + SEPARATOR + filePath;
    Callable<Boolean> check = checkPathCommand( fullPath );
    try {
      Boolean visibleForAdmin = SecurityHelper.getInstance().runAsSystem( check );
      return !visibleForAdmin || check.call();
    } catch ( Exception e ) {
      logger.error( e );
      throw new RuntimeException( e );
    }
  }

  @Override public boolean createShadowNodeForFile( String catalog, String fileName) {
    try {
      return SecurityHelper.getInstance().runAsSystem( createFileCommand( catalog, fileName ) );
    } catch ( Exception e ) {
      logger.error( e );
      throw new RuntimeException( e );
    }
  }

  @Override public boolean removeShadowNodeFor( String filePath ) {
    try {
      return SecurityHelper.getInstance().runAsSystem( removePathCommand( getJcrPath() + SEPARATOR + filePath ) );
    } catch ( Exception e ) {
      logger.error( e );
      throw new RuntimeException( e );
    }
  }


  static RepositoryFile purFile( String filename ) {
    return new RepositoryFile.Builder( filename ).build();
  }

  static RepositoryFile purFolder( String name ) {
    return new RepositoryFile.Builder( name ).folder( true ).build();
  }

  private Callable<Boolean> checkPathCommand( String fullPath ) {
    return new CheckPathCommand( unifiedRepository, fullPath );
  }

  private Callable<Boolean> removePathCommand( String fullPath ) {
    return new RemovePathCommand( unifiedRepository, fullPath );
  }

  private Callable<Boolean> createFileCommand(String catalog, String fileName) {
    return new CreateFileCommand( unifiedRepository, getJcrPath(), catalog, fileName );
  }

  private static class CheckPathCommand implements Callable<Boolean> {
    private final IUnifiedRepository unifiedRepository;
    private final String fullPath;

    public CheckPathCommand( IUnifiedRepository unifiedRepository, String fullPath ) {
      this.unifiedRepository = unifiedRepository;
      this.fullPath = fullPath;
    }

    @Override public Boolean call() throws Exception {
      return unifiedRepository.getFile( fullPath ) == null;
    }
  }

  private static class RemovePathCommand implements Callable<Boolean> {
    private final IUnifiedRepository unifiedRepository;
    private final String fullPath;

    public RemovePathCommand( IUnifiedRepository unifiedRepository, String fullPath ) {
      this.unifiedRepository = unifiedRepository;
      this.fullPath = fullPath;
    }

    @Override public Boolean call() throws Exception {
      RepositoryFile file = unifiedRepository.getFile( fullPath );
      if (file != null) {
        unifiedRepository.deleteFile( file.getId(), true, "Removing shadow node for path: " + fullPath );
      }
      return Boolean.TRUE;
    }
  }

  private static class CreateFileCommand implements Callable<Boolean> {
    private final IUnifiedRepository unifiedRepository;
    private final String jcrPath;
    private final String catalog;
    private final String fileName;

    public CreateFileCommand( IUnifiedRepository unifiedRepository, String jcrPath, String catalog,
                              String fileName ) {
      this.unifiedRepository = unifiedRepository;
      this.jcrPath = jcrPath;
      this.catalog = catalog;
      this.fileName = fileName;
    }

    @Override public Boolean call() throws Exception {
      RepositoryFile jcrFolder = unifiedRepository.getFile( jcrPath );

      RepositoryFile folder;
      if ( StringUtils.isNotEmpty( catalog )) {
        folder = unifiedRepository.createFolder( jcrFolder.getId(), purFolder( catalog ), "" );
      } else {
        folder = jcrFolder;
      }

      RepositoryFile result = unifiedRepository.createFile( folder.getId(), purFile( fileName ),
        new NodeRepositoryFileData( new DataNode( "acl" ) ), "" );

      return result != null;
    }
  }
}