package com.playtime.sdk.repositories;

import android.content.Context;
import android.os.AsyncTask;

import com.playtime.sdk.database.AppDatabase;
import com.playtime.sdk.database.PartnerAppTargets;
import com.playtime.sdk.database.PartnerAppTargetsDao;
import com.playtime.sdk.database.PartnerApps;
import com.playtime.sdk.database.PartnerAppTargetsDao;

import java.util.List;

public class PartnerAppTargetsRepository {
    private PartnerAppTargetsDao mDataDao;

    public PartnerAppTargetsRepository(Context c) {
        this.mDataDao = AppDatabase.getInstance(c).partnerAppTargetsDao();
    }

    public void insert(PartnerAppTargets dataItem) {
        new insertAsyncTask(mDataDao).execute(dataItem);
    }

    private static class insertAsyncTask extends AsyncTask<PartnerAppTargets, Void, Void> {
        private PartnerAppTargetsDao mAsyncTaskDao;

        insertAsyncTask(PartnerAppTargetsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final PartnerAppTargets... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public void insertAll(List<PartnerAppTargets> dataItem) {
        new insertAllAsyncTask(mDataDao).execute(dataItem);
    }

    private class insertAllAsyncTask extends AsyncTask<List<PartnerAppTargets>, Void, Void> {
        private PartnerAppTargetsDao mAsyncTaskDao;

        insertAllAsyncTask(PartnerAppTargetsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final List<PartnerAppTargets>... params) {
            mAsyncTaskDao.insertAll(params[0]);
            return null;
        }
    }

    public void deleteItem(PartnerAppTargets dataItem) {
        new deleteAsyncTask(mDataDao).execute(dataItem);
    }

    private static class deleteAsyncTask extends AsyncTask<PartnerAppTargets, Void, Void> {
        private PartnerAppTargetsDao mAsyncTaskDao;

        deleteAsyncTask(PartnerAppTargetsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final PartnerAppTargets... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    public void deleteItemById(Integer idItem) {
        new deleteByIdAsyncTask(mDataDao).execute(idItem);
    }

    private static class deleteByIdAsyncTask extends AsyncTask<Integer, Void, Void> {
        private PartnerAppTargetsDao mAsyncTaskDao;

        deleteByIdAsyncTask(PartnerAppTargetsDao dao) {
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
        private PartnerAppTargetsDao mAsyncTaskDao;

        deleteMultipleByIdAsyncTask(PartnerAppTargetsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final int[]... params) {
            mAsyncTaskDao.deleteMultipleByIds(params[0]);
            return null;
        }
    }

    public void deleteMultipleByTaskOfferId(int idItem) {
        new deleteMultipleByTaskOfferIdAsyncTask(mDataDao).execute(idItem);
    }

    private static class deleteMultipleByTaskOfferIdAsyncTask extends AsyncTask<Integer, Void, Void> {
        private PartnerAppTargetsDao mAsyncTaskDao;

        deleteMultipleByTaskOfferIdAsyncTask(PartnerAppTargetsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Integer... params) {
            mAsyncTaskDao.deleteMultipleByTaskOfferId(params[0]);
            return null;
        }
    }
}
