package edu.tyut.webviewlearn.spi

import edu.tyut.webviewlearn.hybrid.Hybrid
import java.util.ServiceLoader

internal object HybridServiceManager {
    internal fun getHybrids(): Map<String, Hybrid> {
        val hybrids: ServiceLoader<Hybrid> = ServiceLoader.load<Hybrid>(Hybrid::class.java)
        return hybrids.associate { hybrid: Hybrid ->
            hybrid.getName() to hybrid
        }
    }
}