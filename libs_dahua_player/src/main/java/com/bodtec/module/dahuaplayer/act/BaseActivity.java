package com.bodtec.module.dahuaplayer.act;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bodtec.module.dahuaplayer.ActivityTaskManager;
import com.bodtec.module.dahuaplayer.R;


/**
 * 作者：ZXD
 * 日期：2020/4/7
 * 描述：
 **/
public abstract class BaseActivity extends AppCompatActivity {

    protected abstract int initContentView();

    protected abstract void initView();//使用ButterKnife省略此处

    protected abstract void initData();

    protected abstract void doSomething();

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            /*当应用处于后台时，由于内存低而被系统回收时，由于Application被回收，并且会重启一个新的Application，
              但Task栈依然存在，当前存在的ActivityRecord(s)实例并未销毁,只是界面数据被回收。使之再次进入时避免现场恢复，
              清空栈，并跳转至启动页，另其重走启动流程
              */
            ActivityTaskManager.getInstance().finishAllActivity();
            Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            startActivity(intent);
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        ActivityTaskManager.getInstance().addActivity(this);
        setContentView(initContentView());
        mProgressDialog = new ProgressDialog(this, R.style.loading_style);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mProgressDialog.setCanceledOnTouchOutside(false);
        initView();
        initData();
        doSomething();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityTaskManager.getInstance().finishActivity(this);
    }

    protected void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    protected void showProgressDialog() {
        if (mProgressDialog != null && !mProgressDialog.isShowing()) {
            mProgressDialog.show();
            mProgressDialog.setContentView(R.layout.view_loading_layout);
        }
    }

    protected void showProgressDialog(String str) {
        if (mProgressDialog != null && !mProgressDialog.isShowing()) {
            mProgressDialog.show();
            mProgressDialog.setContentView(R.layout.view_loading_layout);
            TextView content = mProgressDialog.findViewById(R.id.tx_content);
            content.setText(str);
        }
    }

    public void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
