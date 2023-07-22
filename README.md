# Hu

## 使用

```kotlin
private val huState by lazy {
    HuState(
        this,
        this,
        binding.content,
        binding.panelContainer,
        binding.inputBox
    )
}
```

初始化

```kotlin
WindowCompat.setDecorFitsSystemWindows(window, false)
huState
setupPanelSwitch()
```

其中setupPanelSwitch 的实现

```kotlin
private fun setupPanelSwitch() {
    val list = listOf(binding.more)
    list.forEachIndexed { index, imageView ->
        imageView.setOnClickListener {
            huState.switchPanel(index)
        }
    }

    huState.panelState.observe(this) { currentPanel: com.storyteller_f.hu_library.HuPanel? ->
        list.forEachIndexed { index, imageView ->
            imageView.isSelected =
                currentPanel is com.storyteller_f.hu_library.HuPanel.Panel && currentPanel.index == index
        }
    }
}
```