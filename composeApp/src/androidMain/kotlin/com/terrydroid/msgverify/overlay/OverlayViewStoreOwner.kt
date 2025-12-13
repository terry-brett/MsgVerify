package com.terrydroid.msgverify.overlay

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/**
 * A view model store owner to make overlay service to use compose
 */
internal class OverlayViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore
        get() = ViewModelStore()

}
