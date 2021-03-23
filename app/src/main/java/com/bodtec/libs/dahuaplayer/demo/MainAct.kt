package com.bodtec.libs.dahuaplayer.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bodtec.module.dahuaplayer.BodtecDahuaHelper
import kotlinx.android.synthetic.main.act_main.*

class MainAct : AppCompatActivity() {

    private val mToken = "S4NbecfYB1CCT8JFQHEM7P_Y5524jUq1yNQtu+FruA87jL8ok9J5GP2mfM3ngXouKgri3lHI8uiymNDhDYI6AMocMh50p+/tRLJJID1McGgRcXlfKI9ZPSFWTqv9h3volnuhHVob/vpB45bPxlX0zIV"
    private val mSnCode = "33010662001320101725"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)
        mPlayerBtn.setOnClickListener {
            BodtecDahuaHelper.getInstance().open(this@MainAct, mToken, mSnCode)
        }
    }
}