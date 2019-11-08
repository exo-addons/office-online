package org.exoplatform.officeonline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.officeonline.exception.LockMismatchException;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cms.lock.LockService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

// TODO: Auto-generated Javadoc
/**
 * The Class WOPILockManagerPlugin.
 */
public class WOPILockManagerPlugin extends BaseComponentPlugin {

  /** The locks. */
  protected ExoCache<String, FileLock> locks;

  /** The Constant CACHE_NAME. */
  protected static final String        CACHE_NAME      = "officeonline.locks.Cache".intern();

  /** The Constant LOG. */
  protected static final Log           LOG             = ExoLogger.getLogger(WOPILockManagerPlugin.class);

  /** The Constant LOCK_EXPIRES. */
  protected static final long          LOCK_EXPIRES    = 30 * 60000;

  /** The Constant EXPIRES_DELAY. */
  protected static final long          EXPIRES_DELAY   = 5 * 60000;

  /** The session providers. */
  protected SessionProviderService     sessionProviders;

  /** The jcr service. */
  protected RepositoryService          jcrService;

  /** The lock service. */
  protected LockService                lockService;

  /** The executor for removing expired locks. */
  protected ScheduledExecutorService   expiresExecutor = Executors.newScheduledThreadPool(1);

  /**
   * Instantiates a new WOPI lock manager plugin.
   *
   * @param cacheService the cache service
   * @param sessionProviders the session providers
   * @param jcrService the jcr service
   * @param lockService the lock service
   */
  public WOPILockManagerPlugin(CacheService cacheService,
                               SessionProviderService sessionProviders,
                               RepositoryService jcrService,
                               LockService lockService) {
    this.locks = cacheService.getCacheInstance(CACHE_NAME);
    this.sessionProviders = sessionProviders;
    this.jcrService = jcrService;
    this.lockService = lockService;
    expiresExecutor.scheduleAtFixedRate(() -> removeExpired(), EXPIRES_DELAY, EXPIRES_DELAY, TimeUnit.MILLISECONDS);
  }

  /**
   * Lock.
   *
   * @param node the node
   * @param lockId the lock id
   * @throws RepositoryException the repository exception
   * @throws LockMismatchException the lock mismatch exception
   */
  public void lock(Node node, String lockId) throws RepositoryException, LockMismatchException {
    if (!node.isLocked()) {
      Lock lock = node.lock(true, false);
      long expires = System.currentTimeMillis() + LOCK_EXPIRES;
      FileLock fileLock = new FileLock(node.getUUID(), lockId, lock.getLockToken(), expires);
      locks.put(node.getUUID(), fileLock);
      LOG.debug("Node successfully locked. UUID: {}, lockId: {}", node.getUUID(), lockId);
    } else {
      FileLock fileLock = locks.get(node.getUUID());
      // File locked by someone else
      if (fileLock == null) {
        String lockToken = "";
        try {
          lockToken = lockService.getLockToken(node);
        } catch (Exception e) {
          LOG.error("Cannot get lock token from node. UUID: {}. {}", node.getUUID(), e.getMessage());
        }
        throw new LockMismatchException("File locked by other service", lockToken);
      }
      if (lockId.equals(fileLock.getLockId())) {
        fileLock.setExpires(System.currentTimeMillis() + LOCK_EXPIRES);
        LOG.debug("Lock refreshed. UUID: {}, lockId: {}", node.getUUID(), lockId);
      } else {
        throw new LockMismatchException("Provided lock doesn't match lock on file", fileLock.getLockId());
      }
    }
  }

  /**
   * Gets the lock.
   *
   * @param node the node
   * @return the lock
   * @throws RepositoryException the repository exception
   */
  public FileLock getLock(Node node) throws RepositoryException {
    return node.isLocked() ? locks.get(node.getUUID()) : null;
  }

  /**
   * Unlock.
   *
   * @param node the node
   * @param lockId the lock id
   * @param workspace the workspace
   * @throws RepositoryException the repository exception
   * @throws LockMismatchException the lock mismatch exception
   */
  public void unlock(Node node, String lockId, String workspace) throws RepositoryException, LockMismatchException {
    FileLock fileLock = locks.get(node.getUUID());
    if (fileLock != null) {
      if (lockId.equals(fileLock.getLockId())) {
        getUserSession(workspace).addLockToken(fileLock.getLockToken());
        node.unlock();
        locks.remove(node.getUUID());
        LOG.debug("Lock removed from node UUID: {}, lockId: {}", node.getUUID(), lockId);
      } else {
        throw new LockMismatchException("Provided lockId doesn't match lock on the file", fileLock.getLockToken());
      }
    } else {
      throw new LockMismatchException("File isn't locked", "");
    }
  }


  /**
   * Refresh lock.
   *
   * @param node the node
   * @param lockId the lock id
   * @throws RepositoryException the repository exception
   * @throws LockMismatchException the lock mismatch exception
   */
  public void refreshLock(Node node, String lockId) throws RepositoryException, LockMismatchException {
    FileLock fileLock = locks.get(node.getUUID());
    if (fileLock != null) {
      if (lockId.equals(fileLock.getLockId())) {
        fileLock.setExpires(System.currentTimeMillis() + LOCK_EXPIRES);
        LOG.debug("Lock refreshed. UUID: {}, lockId: {}", node.getUUID(), lockId);
      } else {
        throw new LockMismatchException("Provided lockId doesn't match lock on the file", fileLock.getLockToken());
      }
    } else {
      throw new LockMismatchException("File isn't locked", "");
    }
  }

  /**
   * Gets the user session.
   *
   * @param workspace the workspace
   * @return the user session
   * @throws RepositoryException the repository exception
   */
  protected Session getUserSession(String workspace) throws RepositoryException {
    if (workspace == null) {
      workspace = jcrService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName();
    }
    SessionProvider sp = sessionProviders.getSessionProvider(null);
    return sp.getSession(workspace, jcrService.getCurrentRepository());
  }

  /**
   * Removes the expired.
   */
  protected void removeExpired() {
    List<? extends FileLock> fileLocks = new ArrayList<FileLock>();
    try {
      fileLocks = locks.getCachedObjects();
    } catch (Exception e) {
      LOG.error("Cannot get cached locks from ExoCache", e);
      return;
    }

    fileLocks.forEach(lock -> {
      if ((lock.getExpires() - System.currentTimeMillis()) < EXPIRES_DELAY) {
        try {
          Session session = getUserSession(null);
          session.addLockToken(lock.getLockToken());
          Node node = session.getNodeByUUID(lock.getFileId());
          node.unlock();
          locks.remove(lock.getFileId());
          LOG.debug("Node unlocked (lock expired). UUID: {}", lock.getFileId());
        } catch (RepositoryException e) {
          LOG.warn("Cannot unlock node. UUID {}, {}", lock.getFileId(), e.getMessage());
        }
      }
    });
  }
}
