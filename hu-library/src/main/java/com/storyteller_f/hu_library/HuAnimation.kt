package com.storyteller_f.hu_library

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat

inline fun View.imeAnimation(
    direction: Int = -1,
    friends: List<View> = listOf(),
    crossinline baseline: () -> Int
) {
    ViewCompat.setWindowInsetsAnimationCallback(this, object : WindowInsetsAnimationCompat.Callback(
        DISPATCH_MODE_STOP
    ) {
        var endPosition = 0
        var startPosition = 0
        override fun onPrepare(animation: WindowInsetsAnimationCompat) {
            super.onPrepare(animation)
            endPosition = baseline()
        }

        override fun onStart(
            animation: WindowInsetsAnimationCompat,
            bounds: WindowInsetsAnimationCompat.BoundsCompat
        ): WindowInsetsAnimationCompat.BoundsCompat {
            startPosition = baseline()
            return super.onStart(animation, bounds)
        }

        override fun onProgress(
            insets: WindowInsetsCompat,
            runningAnimations: MutableList<WindowInsetsAnimationCompat>
        ): WindowInsetsCompat {
            val fl = (endPosition - startPosition) * (runningAnimations.fraction() - 1) * direction
            translationY = fl
            friends.forEach {
                it.translationY = fl
            }
            return insets
        }

        override fun onEnd(animation: WindowInsetsAnimationCompat) {
            super.onEnd(animation)
            translationY = 0f
            friends.forEach {
                it.translationY = 0f
            }
        }

    })
}

fun List<WindowInsetsAnimationCompat>.fraction() = getOrNull(0)?.interpolatedFraction ?: 0f
