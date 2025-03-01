package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.event.Events

class TimerSlot(
    private val events: Events
) : Timers {

    private var timer: Timer? = null

    override fun start(name: String, restart: Boolean): Boolean {
        val start = TimerStart(name, restart)
        events.emit(start)
        if (start.cancelled) {
            return false
        }
        if (timer != null) {
            events.emit(TimerStop(timer!!.name, logout = false))
        }
        this.timer = Timer(name, start.interval)
        return true
    }

    override fun contains(name: String): Boolean {
        return timer?.name == name
    }

    override fun run() {
        val timer = timer ?: return
        if (!timer.ready()) {
            return
        }
        timer.reset()
        val tick = TimerTick(timer.name)
        events.emit(tick)
        if (tick.cancelled) {
            events.emit(TimerStop(timer.name, logout = false))
            this.timer = null
        }
    }

    override fun stop(name: String) {
        if (contains(name)) {
            events.emit(TimerStop(timer!!.name, logout = false))
            timer = null
        }
    }

    override fun clear(name: String): Boolean {
        if (contains(name)) {
            timer = null
            return true
        }
        return false
    }

    override fun clearAll() {
        timer = null
    }

    override fun stopAll() {
        if (timer != null) {
            events.emit(TimerStop(timer!!.name, logout = true))
        }
        timer = null
    }
}