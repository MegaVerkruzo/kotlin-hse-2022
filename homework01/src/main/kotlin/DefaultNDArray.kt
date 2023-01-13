interface NDArray : SizeAware, DimensionAware {

    /*
     * Получаем значение по индексу point
     *
     * Если размерность point не равна размерности NDArray
     * бросаем IllegalPointDimensionException
     *
     * Если позиция по любой из размерностей некорректна с точки зрения
     * размерности NDArray, бросаем IllegalPointCoordinateException
     */
    fun at(point: Point): Int

    /*
     * Устанавливаем значение по индексу point
     *
     * Если размерность point не равна размерности NDArray
     * бросаем IllegalPointDimensionException
     *
     * Если позиция по любой из размерностей некорректна с точки зрения
     * размерности NDArray, бросаем IllegalPointCoordinateException
     */
    fun set(point: Point, value: Int)

    /*
     * Копируем текущий NDArray
     *
     */
    fun copy(): NDArray

    /*
     * Создаем view для текущего NDArray
     *
     * Ожидается, что будет создан новая реализация интерфейса.
     * Но она не должна быть видна в коде, использующем эту библиотеку как внешний артефакт
     *
     * Должна быть возможность делать view над view.
     *
     * In-place-изменения над view любого порядка видна в оригинале и во всех view
     *
     * Проблемы thread-safety игнорируем
     */
    fun view(): NDArray

    /*
     * In-place сложение
     *
     * Размерность other либо идентична текущей, либо на 1 меньше
     * Если она на 1 меньше, то по всем позициям, кроме "лишней", она должна совпадать
     *
     * Если размерности совпадают, то делаем поэлементное сложение
     *
     * Если размерность other на 1 меньше, то для каждой позиции последней размерности мы
     * делаем поэлементное сложение
     *
     * Например, если размерность this - (10, 3), а размерность other - (10), то мы для три раза прибавим
     * other к каждому срезу последней размерности
     *
     * Аналогично, если размерность this - (10, 3, 5), а размерность other - (10, 5), то мы для пять раз прибавим
     * other к каждому срезу последней размерности
     */
    fun add(other: NDArray)

    /*
     * Умножение матриц. Immutable-операция. Возвращаем NDArray
     *
     * Требования к размерности - как для умножения матриц.
     *
     * This - обязательно двумерная
     *
     * other - может быть двумерной, с подходящей размерностью, равной 1 или просто вектором
     *
     * Возвращаем новую матрицу (NDArray размерности 2)
     *
     */
    fun dot(other: NDArray): NDArray
}

/*
 * Базовая реализация NDArray
 *
 * Конструкторы должны быть недоступны клиенту
 *
 * Инициализация - через factory-методы ones(shape: Shape), zeros(shape: Shape) и метод copy
 */
class DefaultNDArray private constructor(private val shape: Shape, private val data: IntArray) : NDArray {
    override val size: Int
        get() = shape.size
    override val ndim: Int
        get() = shape.ndim

    private fun findIndexInData(point: Point): Int {
        if (point.ndim != ndim) throw NDArrayException.IllegalPointDimensionException(point.ndim, ndim)
        (0 until point.ndim).forEach {
            if (point.dim(it) !in 0 until shape.dim(it)) throw NDArrayException.IllegalPointCoordinateException(
                it,
                point.dim(it),
                shape.dim(it)
            )
        }

        // findIndexInDataOrNull: Int
        var indexInData = 0
        var sizeWithPreviousDimensions = 1
        (0 until ndim).forEach { index ->
            indexInData += point.dim(index) * sizeWithPreviousDimensions
            sizeWithPreviousDimensions *= shape.dim(index)
        }
        return indexInData
    }

    private fun getByIndexOrNull(index: Int): Int? = data.getOrNull(index)

    private fun addByIndexOrNothing(index: Int, value: Int) {
        if (index !in (0 until size)) return
        data[index] += value
    }

    override fun at(point: Point): Int = data[findIndexInData(point)]

    override fun set(point: Point, value: Int) {
        data[findIndexInData(point)] = value
    }

    override fun copy(): NDArray = DefaultNDArray(shape, data.copyOf())

    override fun view(): NDArray = this

    override fun add(other: NDArray) {
        if (ndim - other.ndim > 1 || ndim < other.ndim) throw NDArrayException.IllegalOperationShapeSizeException(
            Operation.ADD,
            ndim,
            other.ndim
        )
        (0 until other.ndim).forEach {
            if (dim(it) != other.dim(it)) throw NDArrayException.IllegalOperationShapeSizeException(
                Operation.ADD,
                ndim,
                other.ndim
            )
        }
        if (ndim == other.ndim + 1) {
            val sizeWithoutLastDimension: Int = size / shape.dim(ndim - 1)
            (0 until shape.dim(ndim - 1)).forEach { indexOfLastDimension ->
                (0 until sizeWithoutLastDimension).forEach { indexOfFirstDimensions ->
                    data[indexOfLastDimension * sizeWithoutLastDimension + indexOfFirstDimensions] +=
                        (other as DefaultNDArray).getByIndexOrNull(indexOfFirstDimensions)
                            ?: throw NDArrayException.IllegalOperationShapeSizeException(
                                Operation.ADD,
                                ndim,
                                other.ndim
                            )
                }
            }
        } else {
            (0 until size).forEach {
                data[it] += (other as DefaultNDArray).getByIndexOrNull(it) ?: throw NDArrayException.IllegalOperationShapeSizeException(
                    Operation.ADD,
                    ndim,
                    other.ndim
                )
            }
        }
    }

    override fun dot(other: NDArray): NDArray {
        // Checking that tables has correct sizes
        if (!(ndim == 2 && other.ndim <= 2 && shape.dim(1) == other.dim(0))) throw NDArrayException.IllegalOperationShapeSizeException(
            Operation.DOT,
            ndim,
            other.ndim
        )
        val countTables: Int = if (other.ndim >= 2) other.dim(1) else 1
        val countK: Int = shape.dim(1)
        val result: DefaultNDArray = zeros(DefaultShape(shape.dim(0), countTables))
        for (i in (0 until shape.dim(0))) {
            for (j in (0 until countTables)) {
                for (k in (0 until countK)) {
                    val indexOfNewTable: Int = i * countTables + j
                    result.addByIndexOrNothing(indexOfNewTable,
                        (result.getByIndexOrNull(indexOfNewTable) ?: 0)
                                + data[i * countK + k] * ((other as DefaultNDArray).getByIndexOrNull(j + i * countTables) ?: 0)
                    )
                }
            }
        }
        return result
    }

    override fun dim(i: Int): Int = shape.dim(i)

    companion object {
        fun ones(shape: Shape): DefaultNDArray = DefaultNDArray(shape, IntArray(shape.size) { 1 })

        fun zeros(shape: Shape): DefaultNDArray = DefaultNDArray(shape, IntArray(shape.size) { 0 })
    }

}

sealed class NDArrayException(reason: String = "") : Exception(reason) {
    class IllegalPointCoordinateException(indexDimension: Int, pointSize: Int, shapeSize: Int) : NDArrayException(
        "On Dimension with index = $indexDimension, point has size = $pointSize, but shape's size is $shapeSize"
    )

    class IllegalOperationShapeSizeException(operation: Operation, currentNDim: Int, otherNDim: Int) : NDArrayException(
        "In operation ${operation.text} currentShape ndim is $currentNDim and other's ndim is $otherNDim"
    )

    class IllegalPointDimensionException(pointDimension: Int, shapeDimension: Int) :
        NDArrayException("pointDimension = $pointDimension isn't equal shapeDimension = $shapeDimension")
}

enum class Operation(val text: String) {
    ADD("Add"),
    DOT("Dot")
}