fun <T> List<T>.getSafe(index: Int): T? {
    return if (index in indices) this[index] else null
}
