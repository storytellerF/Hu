package com.storyteller_f.hu_library

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * 没有navigatorHeight 应该跳过
 */
sealed class HuPanel(val navigatorHeight: Int?) {
    class None(navigatorHeight: Int?) : HuPanel(navigatorHeight)

    /**
     * 为空代表还没有从insetListener 中获取高度，但是当前应该是输入法状态
     */
    class Ime(val height: Int?, navigatorHeight: Int?) : HuPanel(navigatorHeight)

    /**
     * panel 的高度永远不是空
     */
    class Panel(val index: Int, val height: Int, navigatorHeight: Int?) : HuPanel(navigatorHeight)
}

fun Context.showKeyboard(editText: EditText?) {
    val imm = getSystemService(InputMethodManager::class.java)
    editText?.requestFocus()
    imm?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT, object : ResultReceiver(
        Handler(
            Looper.getMainLooper()
        )
    ) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            super.onReceiveResult(resultCode, resultData)
        }
    })
}

fun Context.hideKeyboard() {
    if (this is Activity) {
        hideKeyboard()
    }
}

fun Activity.hideKeyboard() {
    if (window != null) {
        getSystemService(InputMethodManager::class.java)?.hideSoftInputFromWindow(
            window.decorView.windowToken,
            0
        )
    }
}
