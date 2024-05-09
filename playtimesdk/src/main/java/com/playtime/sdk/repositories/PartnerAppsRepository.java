package com.playtime.sdk.repositories;

import android.content.Context;
import android.os.AsyncTask;
import com.playtime.sdk.utils.Logger;

import com.playtime.sdk.async.UpdateInstalledOfferStatusAsync;
import com.playtime.sdk.database.AppDatabase;
import com.playtime.sdk.database.PartnerApps;
import com.playtime.sdk.database.PartnerAppsDao;
import com.playtime.sdk.utils.CommonUtils;

import java.util.List;

public class PartnerAppsRepository {
    private PartnerAppsDao mDataDao;
    private static Context context;

    public PartnerAppsRepository(Context c) {
        this.mDataDao = AppDatabase.getInstance(c).partnerAppsDao();
        context = c;
    }

    public void insert(PartnerApps dataItem) {
        new insertAsyncTask(mDataDao).execute(dataItem);
    }

    private static class insertAsyncTask extends AsyncTask<PartnerApps, Void, Void> {
        private PartnerAppsDao mAsyncTaskDao;

        insertAsyncTask(PartnerAppsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final PartnerApps... params) {
//            if (mAsyncTaskDao.isPartnerAppExist(params[0].task_offer_id) == 0) {
//                params[0].is_installed = 0;
            long result = mAsyncTaskDao.insert(params[0]);
            if (result > 0) {
                Logger.getInstance().e("OFFER DATA", "OFFER DATA INSERTED");
            }
//        }
            return null;
        }
    }

    public void insertAll(List<PartnerApps> dataItem) {
        new insertAllAsyncTask(mDataDao).execute(dataItem);
    }

    private class insertAllAsyncTask extends AsyncTask<List<PartnerApps>, Void, Void> {
        private PartnerAppsDao mAsyncTaskDao;

        insertAllAsyncTask(PartnerAppsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final List<PartnerApps>... params) {
            mAsyncTaskDao.insertAll(params[0]);
            return null;
        }
    }

    public void deleteItem(PartnerApps dataItem) {
        new deleteAsyncTask(mDataDao).execute(dataItem);
    }

    private static class deleteAsyncTask extends AsyncTask<PartnerApps, Void, Void> {
        private PartnerAppsDao mAsyncTaskDao;

        deleteAsyncTask(PartnerAppsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final PartnerApps... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    public void deleteItemById(Integer idItem) {
        new deleteByIdAsyncTask(mDataDao).execute(idItem);
    }

    private static class deleteByIdAsyncTask extends AsyncTask<Integer, Void, Void> {
        private PartnerAppsDao mAsyncTaskDao;

        deleteByIdAsyncTask(PartnerAppsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Integer... params) {
            mAsyncTaskDao.deleteById(params[0]);
            return null;
        }
    }

    public void deleteMultipleById(int[] idItem) {
        new deleteMultipleByIdAsyncTask(mDataDao).execute(idItem);
    }

    private static class deleteMultipleByIdAsyncTask extends AsyncTask<int[], Void, Void> {
        private PartnerAppsDao mAsyncTaskDao;

        deleteMultipleByIdAsyncTask(PartnerAppsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final int[]... params) {
            mAsyncTaskDao.deleteMultipleByIds(params[0]);
            return null;
        }
    }

    public void updatePartnerApp(PartnerApps obj) {
        new UpdatePartnerApp(mDataDao).execute(obj);
    }

    private static class UpdatePartnerApp extends AsyncTask<PartnerApps, Void, Void> {
        private PartnerAppsDao mAsyncTaskDao;

        UpdatePartnerApp(PartnerAppsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final PartnerApps... params) {
            mAsyncTaskDao.update(params[0]);
            Logger.getInstance().e("INSTALL DATA UPDATED ==>", "INSTALL DATA UPDATED IN LOCAL DB");
            return null;
        }
    }

    public void checkIsPartnerApp(String packageName, String udid, String appId, String gaId) {
        new UpdateInstallTimeIfPackageExistInPartnerApp(mDataDao).execute(new String[]{packageName, udid, appId, gaId});
    }

    private static class UpdateInstallTimeIfPackageExistInPartnerApp extends AsyncTask<String, Void, Void> {
        private PartnerAppsDao mAsyncTaskDao;
        private String packageId;
        private String appId;
        private String udid;
        private String gaid;
        private PartnerApps objApp;

        UpdateInstallTimeIfPackageExistInPartnerApp(PartnerAppsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
            packageId = params[0];
            appId = params[1];
            udid = params[2];
            gaid = params[3];
            objApp = mAsyncTaskDao.getPartnerAppByPackageId(params[0]);
            Logger.getInstance().e("CHECK PARTNER APP ==>", "IS PARTNER APP?: " + packageId + " OBJ: "+objApp);
            if (objApp != null && !CommonUtils.isStringNullOrEmpty(objApp.package_id) && objApp.package_id.equals(packageId)) {
                Logger.getInstance().e("CHECK PARTNER APP ==>", "THIS IS PARTNER APP: " + packageId);
                new UpdateInstalledOfferStatusAsync(context, packageId, appId, udid, gaid, objApp);
            }else{
                Logger.getInstance().e("CHECK PARTNER APP ==>", "THIS IS NOT A PARTNER APP: " + packageId);
            }
            return null;
        }
    }
}
