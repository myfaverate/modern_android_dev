package edu.tyut.hiltlearn.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.tyut.hiltlearn.di.bean.BindElectricEngine
import edu.tyut.hiltlearn.di.bean.BindGasEngine
import edu.tyut.hiltlearn.di.bean.ElectricEngine
import edu.tyut.hiltlearn.di.bean.Engine
import edu.tyut.hiltlearn.di.bean.GasEngine

@Module
@InstallIn(value = [SingletonComponent::class])
internal abstract class EngineModule {
    @BindGasEngine
    @Binds
    internal abstract fun bindGasEngine(gasEngine: GasEngine): Engine
    @BindElectricEngine
    @Binds
    internal abstract fun bindElectricEngine(electricEngine: ElectricEngine): Engine
}