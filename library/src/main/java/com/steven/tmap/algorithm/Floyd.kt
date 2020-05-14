package com.steven.tmap.algorithm

/**
 * Floyd 插点法
 *
 * @author Steven
 * @version 1.0
 * @since 2020/5/13
 */
internal class Floyd {

    companion object {
        private var mInstance: Floyd? = null
        private val MAX = Float.MAX_VALUE

        @JvmStatic
        fun getInstance() = mInstance ?: synchronized(this) {
            mInstance ?: Floyd().also { mInstance = it }
        }
    }

    private lateinit var dist: Array<FloatArray>
    private lateinit var path: Array<IntArray>
    private lateinit var result: MutableList<Int>

    fun getShortestPath(start: Int, end: Int, matrix: Array<FloatArray>): MutableList<Int> {
        this.dist = Array(matrix.size) { FloatArray(matrix.size) }
        this.path = Array(matrix.size) { IntArray(matrix.size) }
        this.result = mutableListOf()
        floyd(matrix)
        result.add(start)
        findPath(start, end)
        result.add(end)
        return result
    }

    private fun floyd(matrix: Array<FloatArray>) {
        val size = matrix.size
        for (i in 0 until size) {
            for (j in 0 until size) {
                path[i][j] = -1
                dist[i][j] = matrix[i][j]
            }
        }
        for (k in 0 until size) {
            for (i in 0 until size) {
                for (j in 0 until size) {
                    if (dist[i][k] != MAX && dist[k][j] != MAX
                        && dist[i][k] + dist[k][j] < dist[i][j]
                    ) {
                        dist[i][j] = dist[i][k] + dist[k][j]
                        path[i][j] = k
                    }
                }
            }
        }
    }

    private fun findPath(start: Int, end: Int) {
        val k = path[start][end]
        if (k == -1) return
        findPath(start, k)
        result.add(k)
        findPath(k, end)
    }
}