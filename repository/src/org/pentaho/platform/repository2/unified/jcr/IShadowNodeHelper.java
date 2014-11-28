package org.pentaho.platform.repository2.unified.jcr;

import org.pentaho.platform.api.repository2.unified.RepositoryFile;

/**
 * @author Andrey Khayrutdinov
 */
public interface IShadowNodeHelper {

  boolean isVisibleFor( String filePath, String user );

  boolean createShadowNodeForFile( String catalog, String fileName );

  boolean removeShadowNodeFor( String filePath );

  String getJcrPath();

  RepositoryFile getJcrFolder();
}
