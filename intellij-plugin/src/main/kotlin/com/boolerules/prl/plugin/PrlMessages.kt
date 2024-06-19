package com.boolerules.prl.plugin

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val PRL_MESSAGES = "messages.PrlMessages"

object PrlMessages : DynamicBundle(PRL_MESSAGES) {

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = PRL_MESSAGES) key: String, vararg params: Any) =
        getMessage(key, *params)

    @Suppress("unused")
    @JvmStatic
    fun messagePointer(@PropertyKey(resourceBundle = PRL_MESSAGES) key: String, vararg params: Any) =
        getLazyMessage(key, *params)
}
