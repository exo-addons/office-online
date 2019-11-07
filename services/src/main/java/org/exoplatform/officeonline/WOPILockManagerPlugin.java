package org.exoplatform.officeonline;

import javax.jcr.Node;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

public class WOPILockManagerPlugin extends BaseComponentPlugin {

  protected ExoCache<String, FileLock> locks;

  protected static final String        CACHE_NAME = "officeonline.locks.Cache".intern();

  public WOPILockManagerPlugin(CacheService cacheService) {
    this.locks = cacheService.getCacheInstance(CACHE_NAME);
  }

  public void lock(Node node, String lockId) {
    // TODO: Implement
  }

  public FileLock getLock(Node node) {
    // TODO: Implement
    return null;
  }

  public void unlock(Node node, String lockId) {
    // TODO: Implement
  }

  public void refreshLock(Node node, String lockId) {
    // TODO: Implement
  }

}
