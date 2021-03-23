package com.bodtec.module.dahuaplayer.async;

import android.os.AsyncTask;
import androidx.lifecycle.LifecycleOwner;
import com.android.business.exception.BusinessErrorCode;
import com.android.business.exception.BusinessException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @description:
 * @author:111144
 * @date:2020/5/26
 */
public class AsyncBuilder<T> extends AsyncTask<String, Integer, T> {
    public static Executor executor = Executors.newCachedThreadPool();

    private BusinessNullRetTask businessNullRetTask;
    private ResultNullListener resultNullListener;
    private LoadingListener loadingListener;
    private BusinessTask<T> businessTask;
    private ResultListener<T> resultListener;
    BusinessException businessException;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (loadingListener != null) {
            loadingListener.beginLoading();
        }
    }

    @Override
    protected T doInBackground(String... strings) {

        if (businessNullRetTask != null) {
            try {
                businessNullRetTask.doInBackground();
            } catch (Exception e) {
                businessException = getException(e);
            }
        }

        if (businessTask != null) {
            try {
                return businessTask.doInBackground();
            } catch (Exception e) {
                businessException = getException(e);
            }
        }
        return null;
    }

    private BusinessException getException(Exception e) {
        if (e instanceof BusinessException) {
            return (BusinessException) e;
        } else {
            return new BusinessException(BusinessErrorCode.BEC_COMMON_UNKNOWN, e);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        //在主线程 显示线程任务执行的进度
    }

    @Override
    protected void onPostExecute(T result) {
        // 执行完毕后，则更新UI
        if (loadingListener != null) {
            loadingListener.finishLoading();
        }

        if (resultNullListener != null) {
            if (businessException == null) {
                resultNullListener.onSuccess();
            }else{
                resultNullListener.onError(businessException);
            }
        }

        if (resultListener != null) {
            if (businessException == null){
                resultListener.onSuccess(result);
            }else{
                resultListener.onError(businessException);
            }

        }
    }

    @Override
    protected void onCancelled(T t) {
        super.onCancelled(t);
    }


    public AsyncBuilder(BusinessNullRetTask businessTask) {
        this.businessNullRetTask = businessTask;
    }

    public AsyncBuilder(BusinessTask<T> businessTask) {
        this.businessTask = businessTask;
    }

    public static AsyncBuilder createTask(BusinessNullRetTask businessTask) {
        AsyncBuilder asyncBuilder = new AsyncBuilder(businessTask);
        return asyncBuilder;
    }

    public static <T> AsyncBuilder createTask(BusinessTask<T> businessTask) {
        AsyncBuilder<T> asyncBuilder = new AsyncBuilder<>(businessTask);
        return asyncBuilder;
    }

    public AsyncBuilder loading(LoadingListener loadingListener) {
        this.loadingListener = loadingListener;
        return this;
    }

    public interface BusinessNullRetTask {
        public void doInBackground() throws Exception;
    }

    public interface BusinessTask<T> {
        public T doInBackground() throws Exception;
    }

    public interface LoadingListener {
        public void beginLoading();

        public void finishLoading();
    }


    public interface ResultNullListener {
        public void onError(BusinessException e);

        public void onSuccess();
    }

    public interface ResultListener<T> {

        public void onError(BusinessException e);

        public void onSuccess(T t);
    }


}
