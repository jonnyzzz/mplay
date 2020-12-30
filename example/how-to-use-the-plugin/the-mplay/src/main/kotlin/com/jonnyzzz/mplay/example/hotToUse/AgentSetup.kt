package com.jonnyzzz.mplay.example.hotToUse

import com.jonnyzzz.mplay.config.MPlayConfig
import com.jonnyzzz.mplay.config.MPlayConfiguration
import com.jonnyzzz.mplay.example.howToUse.ExampleService
import com.jonnyzzz.mplay.example.howToUse.SomeWeirdBase

@MPlayConfig
object Setup : MPlayConfiguration<ExampleService> {
    override val upperLimit: Class<*>
        get() = SomeWeirdBase::class.java
}

