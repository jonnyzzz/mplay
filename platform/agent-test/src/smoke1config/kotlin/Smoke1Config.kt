@file:Suppress("unused")

package com.jonnyzzz.mplay.agent.smoke1confg

import com.jonnyzzz.mplay.agent.smoke1.SmokeTestClass2
import com.jonnyzzz.mplay.config.MPlayConfig
import com.jonnyzzz.mplay.config.MPlayConfiguration
import java.util.function.Consumer

@MPlayConfig
class SmokeTestClass2Config<W>(
    val wc: Consumer<W>
) : MPlayConfiguration<SmokeTestClass2<W>> {

    fun visitTypeParameter0(w: W) = w
}
