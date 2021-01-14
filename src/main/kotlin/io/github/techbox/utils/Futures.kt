package io.github.techbox.utils

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.*
import java.util.function.BiConsumer
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private class ContinuationConsumer<T>(
    @Volatile @JvmField var cont: Continuation<T>?
) : BiConsumer<T?, Throwable?> {
    override fun accept(result: T?, ex: Throwable?) {
        val cont = this.cont ?: return
        if (ex == null)
            @Suppress("UNCHECKED_CAST")
            cont.resume(result as T)
        else
            cont.resumeWithException((ex as? CompletionException)?.cause ?: ex)
    }
}

suspend fun <T> CompletionStage<T>.await(): T {
    // fast-path
    if (this is Future<*> && isDone) {
        try {
            @Suppress("UNCHECKED_CAST", "BlockingMethodInNonBlockingContext")
            return get() as T
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }

    return suspendCancellableCoroutine { cont: CancellableContinuation<T> ->
        val consumer = ContinuationConsumer(cont)
        whenComplete(consumer)

        cont.invokeOnCancellation {
            (this as? CompletableFuture<T>)?.cancel(false)
            consumer.cont = null // clears gc
        }
    }
}