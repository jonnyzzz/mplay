@file:Suppress("unused")

package com.jonnyzzz.mplay.agent.smoke1confg

import com.jonnyzzz.mplay.agent.smoke1.SmokeTestClass2
import com.jonnyzzz.mplay.config.MPlayConfig
import com.jonnyzzz.mplay.config.MPlayConfiguration

@MPlayConfig
class SmokeTestClass2Config<W> : MPlayConfiguration<SmokeTestClass2<W>> {
    fun visitTypeParameter0(w: W) = w
}
