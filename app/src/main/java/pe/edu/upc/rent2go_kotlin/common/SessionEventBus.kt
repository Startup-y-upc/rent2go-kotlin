package pe.edu.upc.rent2go_kotlin.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionEventBus {
    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun emit(event: SessionEvent) {
        _events.tryEmit(event)
    }
}

enum class SessionEvent {
    SESSION_EXPIRED
}
