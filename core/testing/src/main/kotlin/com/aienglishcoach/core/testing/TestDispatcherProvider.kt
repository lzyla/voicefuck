package com.aienglishcoach.core.testing

import com.aienglishcoach.core.common.dispatcher.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/** [DispatcherProvider] where every dispatcher is the same [TestDispatcher]. */
@Suppress("OPT_IN_USAGE")
class TestDispatcherProvider(
    testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : DispatcherProvider {
    override val io: CoroutineDispatcher = testDispatcher
    override val default: CoroutineDispatcher = testDispatcher
    override val main: CoroutineDispatcher = testDispatcher
}
