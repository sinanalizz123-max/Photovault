package com.photovault.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.photovault.PhotoVaultApp;
import com.photovault.database.AppDatabase;
import com.photovault.database.entity.Account;
import com.photovault.database.entity.FaceCluster;
import com.photovault.database.entity.MediaItem;
import com.photovault.database.entity.Rule;
import com.photovault.ui.albums.AlbumAdapter;
import com.photovault.ui.faces.FaceAdapter;
import com.photovault.ui.photos.PhotoAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shared ViewModel for Photos, Albums, Faces tabs and Rules screen.
 */
public class PhotosViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private final MutableLiveData<List<PhotoAdapter.PhotoItem>> localPhotos  = new MutableLiveData<>();
    private final MutableLiveData<List<PhotoAdapter.PhotoItem>> cloudPhotos  = new MutableLiveData<>();
    private final MutableLiveData<List<AlbumAdapter.AlbumItem>> albums       = new MutableLiveData<>();
    private final MutableLiveData<List<FaceAdapter.FaceItem>>   namedClusters   = new MutableLiveData<>();
    private final MutableLiveData<List<FaceAdapter.FaceItem>>   unnamedClusters = new MutableLiveData<>();

    private boolean albumCloudMode = false;
    private Long activeAccountFilter = null;

    public PhotosViewModel(Application app) {
        super(app);
        db = PhotoVaultApp.getInstance().getDatabase();
        refresh();
    }

    // ── Refresh ──────────────────────────────────────────────────────────────

    public void refresh() {
        loadLocalPhotos();
        loadAlbums();
        loadFaceClusters();
    }

    // ── Photos ────────────────────────────────────────────────────────────────

    private void loadLocalPhotos() {
        executor.execute(() -> {
            List<MediaItem> items = db.mediaItemDao().getPage(200, 0);
            List<PhotoAdapter.PhotoItem> result = new ArrayList<>();
            for (MediaItem m : items) {
                PhotoAdapter.PhotoItem pi = new PhotoAdapter.PhotoItem();
                pi.mediaId     = m.id;
                pi.localPath   = m.localPath;
                pi.isVideo     = m.isVideo();
                pi.uploadStatus = -1; // default local only
                result.add(pi);
            }
            localPhotos.postValue(result);
        });
    }

    public LiveData<List<PhotoAdapter.PhotoItem>> getLocalPhotos() {
        return localPhotos;
    }

    public LiveData<List<PhotoAdapter.PhotoItem>> getCloudPhotos() {
        return cloudPhotos;
    }

    public void filterByAccount(Long accountId) {
        this.activeAccountFilter = accountId;
        // Re-query cloud photos filtered by account
    }

    // ── Albums ────────────────────────────────────────────────────────────────

    public void setAlbumMode(boolean cloud) {
        this.albumCloudMode = cloud;
        loadAlbums();
    }

    private void loadAlbums() {
        executor.execute(() -> {
            // In production: query real albums
            // Here: return stub list so UI can render
            albums.postValue(new ArrayList<>());
        });
    }

    public LiveData<List<AlbumAdapter.AlbumItem>> getAlbums() {
        return albums;
    }

    // ── Face clusters ─────────────────────────────────────────────────────────

    public LiveData<List<FaceAdapter.FaceItem>> getNamedClusters() {
        return namedClusters;
    }

    public LiveData<List<FaceAdapter.FaceItem>> getUnnamedClusters() {
        return unnamedClusters;
    }

    private void loadFaceClusters() {
        executor.execute(() -> {
            List<FaceCluster> named   = new ArrayList<>();
            List<FaceCluster> unnamed = new ArrayList<>();

            List<FaceCluster> all = new ArrayList<>(); // stub
            for (FaceCluster c : all) {
                if (c.isNamed()) named.add(c); else unnamed.add(c);
            }

            namedClusters.postValue(mapClusters(named));
            unnamedClusters.postValue(mapClusters(unnamed));
        });
    }

    private List<FaceAdapter.FaceItem> mapClusters(List<FaceCluster> clusters) {
        List<FaceAdapter.FaceItem> items = new ArrayList<>();
        for (FaceCluster c : clusters) {
            FaceAdapter.FaceItem fi = new FaceAdapter.FaceItem();
            fi.clusterId       = c.id;
            fi.name            = c.displayName();
            fi.initials        = c.initials();
            fi.photoCount      = c.photoCount;
            fi.isNamed         = c.isNamed();
            fi.isLowConfidence = c.isLowConfidence();
            fi.coverPhotoPath  = c.coverPhotoPath;
            items.add(fi);
        }
        return items;
    }

    public LiveData<FaceCluster> getCluster(long clusterId) {
        MutableLiveData<FaceCluster> result = new MutableLiveData<>();
        executor.execute(() -> result.postValue(db.faceClusterDao().getByIdSync(clusterId)));
        return result;
    }

    public LiveData<List<MediaItem>> getPhotosByCluster(long clusterId) {
        return db.mediaItemDao().getByFaceCluster(clusterId);
    }

    public void renameCluster(long clusterId, String name) {
        executor.execute(() -> db.faceClusterDao().setName(clusterId, name));
    }

    public void deleteCluster(long clusterId) {
        executor.execute(() -> {
            FaceCluster c = db.faceClusterDao().getByIdSync(clusterId);
            if (c != null) db.faceClusterDao().delete(c);
        });
    }

    // ── Rules ─────────────────────────────────────────────────────────────────

    public LiveData<List<Rule>> getAllRules() {
        return db.ruleDao().getAll();
    }

    public LiveData<Rule> getRuleById(long id) {
        MutableLiveData<Rule> result = new MutableLiveData<>();
        executor.execute(() -> result.postValue(db.ruleDao().getByIdSync(id)));
        return result;
    }

    public void insertRule(Rule rule) {
        executor.execute(() -> {
            rule.priorityOrder = db.ruleDao().getMaxPriority() + 1;
            db.ruleDao().insert(rule);
        });
    }

    public void updateRule(Rule rule) {
        executor.execute(() -> db.ruleDao().update(rule));
    }

    public void deleteRule(long ruleId, boolean removeFromCloud) {
        executor.execute(() -> {
            Rule rule = db.ruleDao().getByIdSync(ruleId);
            if (rule != null) {
                if (removeFromCloud) {
                    // TODO: Google Photos API deletion for upload_targets linked to this rule
                }
                db.ruleDao().delete(rule);
            }
        });
    }

    public void reorderRules(List<Rule> ordered) {
        executor.execute(() -> {
            for (int i = 0; i < ordered.size(); i++) {
                db.ruleDao().updatePriority(ordered.get(i).id, i);
            }
        });
    }

    // ── Routing rule for face cluster ─────────────────────────────────────────

    public LiveData<Rule> getRoutingRuleForCluster(long clusterId) {
        MutableLiveData<Rule> result = new MutableLiveData<>();
        executor.execute(() -> {
            List<Rule> rules = db.ruleDao().getByType(Rule.TYPE_FACE);
            for (Rule r : rules) {
                if (r.conditionValue.equals(String.valueOf(clusterId))) {
                    result.postValue(r);
                    return;
                }
            }
            result.postValue(null);
        });
        return result;
    }

    // ── Accounts ──────────────────────────────────────────────────────────────

    public LiveData<List<Account>> getAccounts() {
        return db.accountDao().getAllActive();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
