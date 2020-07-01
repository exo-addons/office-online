package org.exoplatform.officeonline;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.ecm.utils.lock.LockUtil;
import org.exoplatform.officeonline.exception.LockMismatchException;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.services.cms.lock.LockService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * The Class WOPILockManagerPlugin is used to manage locks for WOPI.
 */
public class WOPILockManagerPlugin extends BaseComponentPlugin {

  /** The locks. */
  protected ExoCache<String, FileLock> locks;

  /** The Constant CACHE_NAME. */
  protected static final String        CACHE_NAME           = "officeonline.locks.Cache".intern();

  /** The Constant MIX_LOCKABLE. */
  protected static final String        MIX_LOCKABLE         = "mix:lockable";

  /** The Constant LOG. */
  protected static final Log           LOG                  = ExoLogger.getLogger(WOPILockManagerPlugin.class);

  /** The Constant LOCK_EXPIRES. */
  protected static final long          LOCK_EXPIRES         = 30 * 60000;

  /** The Constant EXPIRES_DELAY. */
  protected static final long          EXPIRES_DELAY        = 5 * 60000;

  /** The session providers. */
  protected SessionProviderService     sessionProviders;

  /** The jcr service. */
  protected RepositoryService          jcrService;

  /** The lock service. */
  protected LockService                lockService;

  /** The executor for removing expired locks. */
  protected ScheduledExecutorService   expiresExecutor      = Executors.newScheduledThreadPool(1);

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
   * Locks the node with provided lockId, stores the lock in the cache.
   * Refreshes the lock if the node is locked and lockIds matches.
   *
   * @param node the node
   * @param lockId the lock id
   * @throws RepositoryException the repository exception
   * @throws LockMismatchException the lock mismatch exception
   */
  public void lock(Node node, String lockId) throws RepositoryException, LockMismatchException {
    if (!node.isNodeType(MIX_LOCKABLE)) {
      node.addMixin(MIX_LOCKABLE);
      node.save();
    }

    if (!node.isLocked()) {
      lockNode(node, lockId);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Node successfully locked. UUID: {}, lockId: {}", node.getUUID(), lockId);
      }
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
        if (LOG.isDebugEnabled()) {
          LOG.debug("Lock refreshed. UUID: {}, lockId: {}", node.getUUID(), lockId);
        }
      } else {
        throw new LockMismatchException("Provided lock doesn't match lock on file", fileLock.getLockId());
      }
    }
  }

  /**
   * Gets the lock from cache.
   *
   * @param node the node
   * @return the lock or null, if the node isn't locked
   * @throws RepositoryException the repository exception
   */
  public FileLock getLock(Node node) throws RepositoryException {
    return node.isLocked() ? locks.get(node.getUUID()) : null;
  }

  /**
   * Unlocks the node with provided lockId.
   *
   * @param node the node
   * @param lockId the lock id
   * @param workspace the workspace
   * @throws RepositoryException the repository exception
   * @throws LockMismatchException if the node isn't locked of provided lockId doesn't match the current one
   */
  public void unlock(Node node, String lockId, String workspace) throws RepositoryException, LockMismatchException {
    FileLock fileLock = locks.get(node.getUUID());
    if (fileLock != null) {
      if (lockId.equals(fileLock.getLockId())) {
        getUserSession(workspace).addLockToken(fileLock.getLockToken());
        unlockNode(node);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Lock removed from node UUID: {}, lockId: {}", node.getUUID(), lockId);
        }
      } else {
        throw new LockMismatchException("Provided lockId doesn't match lock on the file", fileLock.getLockId());
      }
    } else {
      throw new LockMismatchException("File isn't locked", "");
    }
  }

  /**
   * Refreshes lock by prolonging the lock expires.
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
        if (LOG.isDebugEnabled()) {
          LOG.debug("Lock refreshed. UUID: {}, lockId: {}", node.getUUID(), lockId);
        }
      } else {
        throw new LockMismatchException("Provided lockId doesn't match lock on the file", fileLock.getLockId());
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
   * Gets the system session.
   *
   * @return the system session
   * @throws RepositoryException the repository exception
   */
  protected Session getSystemSession() throws RepositoryException {
    String workspace = jcrService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName();
    SessionProvider sp = SessionProvider.createSystemProvider();
    return sp.getSession(workspace, jcrService.getCurrentRepository());
  }

  /**
   * Removes the expired locks or locks with expiration time less than EXPIRES_DELAY.
   */
  protected void removeExpired() {
    try {
      locks.select(new CachedObjectSelector<String, FileLock>() {

        @Override
        public boolean select(String key, ObjectCacheInfo<? extends FileLock> ocinfo) {
          FileLock lock = ocinfo.get();
          return lock != null ? (lock.getExpires() - System.currentTimeMillis() < EXPIRES_DELAY) : false;
        }

        @Override
        public void onSelect(ExoCache<? extends String, ? extends FileLock> cache,
                             String fileId,
                             ObjectCacheInfo<? extends FileLock> ocinfo) throws Exception {
          FileLock lock = ocinfo.get();
          try {
            Session session = getSystemSession();
            session.addLockToken(lock.getLockToken());
            Node node = session.getNodeByUUID(fileId);
            if (node.isLocked()) {
              unlockNode(node);
              if (LOG.isDebugEnabled()) {
                LOG.debug("Node unlocked (lock expired). UUID: {}", fileId);
              }
            }
          } catch (RepositoryException e) {
            LOG.warn("Cannot unlock node. UUID {}, {}", fileId, e.getMessage());
          }
        }
      });
    } catch (Exception e) {
      LOG.error("Cannot unlock expired nodes", e);
    }
  }

  /**
   * Lock node.
   *
   * @param node the node
   * @param lockId the lock id
   * @throws RepositoryException the repository exception
   */
  private void lockNode(Node node, String lockId) throws RepositoryException {
    Lock lock = node.lock(true, false);
    try {
      LockUtil.keepLock(lock);
    } catch (Exception e) {
      LOG.warn("Cannot keep lock in lock service", e.getMessage());
    }
    long expires = System.currentTimeMillis() + LOCK_EXPIRES;
    FileLock fileLock = new FileLock(lockId, lock.getLockToken(), expires);
    locks.put(lock.getNode().getUUID(), fileLock);
  }

  /**
   * Unlock node.
   *
   * @param node the node
   * @throws RepositoryException the repository exception
   */
  private void unlockNode(Node node) throws RepositoryException {
    node.unlock();
    locks.remove(node.getUUID());
    try {
      LockUtil.removeLock(node);
    } catch (Exception e) {
      LOG.warn("Cannot remove lock in lock service", e.getMessage());
    }
  }

}
