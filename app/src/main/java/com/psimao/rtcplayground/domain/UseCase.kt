package com.psimao.rtcplayground.domain

import kotlinx.coroutines.experimental.Deferred

abstract class UseCase<P, T> {

    abstract fun execute(params: P? = null): Deferred<T?>

    abstract fun observe(params: P? = null, next: ((T) -> Unit)? = null)

    open fun dispose() {}
}