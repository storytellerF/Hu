package com.storyteller_f.hu_library

import android.animation.ValueAnimator
import android.content.Context
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private val savedImeHeight = intPreferencesKey("imeHeight")

class HuState(
    private val lifecycleOwner: LifecycleOwner,
    private val context: Context,
    private val content: ConstraintLayout,
    private val panelContainer: FrameLayout,
    private val inputBox: EditText
) {
    val panelState = MutableLiveData<HuPanel>()
    private val currentPanelState get() = panelState.value ?: HuPanel.None(null)

    // 获取屏幕密度
    private val density = context.resources.displayMetrics.density

    // 将 dp 值转换为 px 像素值
    private val minimumImHeight = (MINIMUM_HEIGHT_DP * density).toInt()

    init {
        setup()
    }

    private fun setup() {
        observeIme()
        imeAni()
        observePanelState(panelContainer, content)
    }

    private fun observeIme() {
        ViewCompat.setOnApplyWindowInsetsListener(inputBox) { _, inset ->
            val imeHeight = inset.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navHeight = inset.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            Log.i(TAG, "imeAni: $imeHeight $navHeight")
            if (imeHeight > 0) {
                /**
                 * 某些手机：比如三星，在切换导航键的情况下imeHeight 的值不为0，即使当时没有输入法弹出
                 */
                if (imeHeight > minimumImHeight) {//必需大与panel 最小高度
                    lifecycleOwner.lifecycleScope.launch {
                        context.dataStore.edit {
                            it[savedImeHeight] = imeHeight
                        }
                    }
                }
                panelState.value = HuPanel.Ime(imeHeight, navHeight)
            } else if (currentPanelState !is HuPanel.Panel) {
                panelState.value = HuPanel.None(navHeight)//关闭
            }
            inset
        }
    }

    private fun imeAni() {
        content.imeAnimation(friends = listOf(panelContainer)) {
            inputBox.y.toInt()
        }
    }


    private fun observePanelState(panelContainer: FrameLayout, content: ConstraintLayout) {
        panelState.observe(lifecycleOwner) {
            val navHeight = it.navigatorHeight ?: return@observe

            val aniOffset = when (it) {
                is HuPanel.Ime -> it.height
                is HuPanel.Panel -> it.height
                else -> navHeight
            }
            panelContainer.updateLayoutParams {
                height = aniOffset
            }
            content.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = aniOffset
            }
            /**
             * 输入框抬高
             */
            val bottomInputUp = it is HuPanel.Ime || it is HuPanel.Panel

            panelContainer.isVisible = bottomInputUp
        }
    }

    /**
     *
     * @param index 如果当前是panel 会判断索引是否相同，如果相同，相当与切换到输入法。如果是-1，代表关闭panel
     */
    fun switchPanel(index: Int) {
        if (index == -1) {
            closePanel()
        } else {
            lifecycleOwner.lifecycleScope.launch {
                val height = context.dataStore.data.first()[savedImeHeight]
                switchToPanel(index, height)
            }
        }
    }

    private fun closePanel() {
        val state = currentPanelState
        val navigatorHeight = currentPanelState.navigatorHeight ?: return
        panelState.value = HuPanel.None(navigatorHeight)
        when (state) {
            is HuPanel.Ime -> {
                context.hideKeyboard()
            }
            is HuPanel.Panel -> {
                state.height
                doPanelAnimation(-state.height + navigatorHeight)
            }
            else -> {

            }
        }
    }

    /**
     * 如果当前是panel 会判断索引是否相同，如果相同，相当与切换到输入法
     */
    private fun switchToPanel(index: Int, height: Int?) {
        Log.d(TAG, "switchPanel() called with: index = $index, height = $height")
        val fallbackImeHeight = height?.takeIf { it > minimumImHeight } ?: minimumImHeight
        val state = currentPanelState
        val navigatorHeight = state.navigatorHeight ?: return
        when (state) {
            is HuPanel.Ime -> {
                //一般ime 这里取到的高度不是空
                panelState.value = HuPanel.Panel(
                    index, state.height, navigatorHeight
                )//复用当前输入法的高度
                context.hideKeyboard()
            }

            is HuPanel.Panel -> {
                if (index == state.index) {
                    panelState.value =
                        HuPanel.Ime(state.height, navigatorHeight)//panel 的高度可能是不正确的，以后续的inset 获取
                    context.showKeyboard(inputBox)
                } else {
                    //切换到另一个panel
                    panelState.value = HuPanel.Panel(index, state.height, navigatorHeight)
                    context.hideKeyboard()
                }
            }

            is HuPanel.None -> {
                panelState.value =
                    HuPanel.Panel(index, fallbackImeHeight, navigatorHeight)
                //执行动画
                doPanelAnimation(fallbackImeHeight - navigatorHeight)
            }
        }
    }

    private fun doPanelAnimation(start: Int) {
        ValueAnimator.ofInt(start, 0).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                val current = it.animatedValue as Int
                panelContainer.translationY = current.toFloat()
                content.translationY = current.toFloat()
            }
            start()
        }
    }

    companion object {
        private const val TAG = "HuState"
        const val MINIMUM_HEIGHT_DP = 200
    }
}