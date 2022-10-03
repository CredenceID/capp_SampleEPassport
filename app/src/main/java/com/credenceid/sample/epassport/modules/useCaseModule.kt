package com.credenceid.sample.epassport.modules

import com.credenceid.sample.epassport.eco.mrzscanner.domains.ReadIcaoMrzUseCase
import org.koin.dsl.module

val useCaseModule by lazy {
    module {
        single { ReadIcaoMrzUseCase() }
    }
}
