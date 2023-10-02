import kotlin.reflect.KProperty

class LiveData<T>(initialValue: T) {
    private val observers = mutableSetOf<(T) -> Unit>()
    private var value: T = initialValue

    fun getValue(): T = value

    fun setValue(newValue: T) {
        value = newValue
        observers.forEach { observer -> observer(value) }
    }

    fun addObserver(observer: (T) -> Unit) {
        observers.add(observer)
        observer(value)
    }

    fun removeObserver(observer: (T) -> Unit) {
        observers.remove(observer)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }
}


