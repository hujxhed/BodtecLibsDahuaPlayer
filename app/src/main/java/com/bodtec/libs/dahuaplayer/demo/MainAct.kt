package com.bodtec.libs.dahuaplayer.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bodtec.module.dahuaplayer.BodtecDahuaHelper
import kotlinx.android.synthetic.main.act_main.*

class MainAct : AppCompatActivity() {

    private val mToken = ""
    private val mSnCode = "33010662001320101725"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)
        mPlayerBtn.setOnClickListener {
            BodtecDahuaHelper.getInstance().open(this@MainAct, mToken, mSnCode)
        }
    }
}