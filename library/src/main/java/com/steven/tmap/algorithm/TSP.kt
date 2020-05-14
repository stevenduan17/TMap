package com.steven.tmap.algorithm

import java.util.*

/**
 * @author Steven
 * @version 1.0
 * @since 2020/5/14
 */
internal class TSP {

    companion object {
        private var instance: TSP? = null

        @JvmStatic
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: TSP().also { instance = it }
        }
    }

    fun tsp(matrix: Array<FloatArray>): MutableList<Int> {
        val stack = ArrayDeque<Int>()
        val list = mutableListOf<Int>()
        val nodesCount = matrix[0].size
        val visited = IntArray(nodesCount)
        visited[0] = 1
        stack.push(0)
        var element: Int
        var dst = 0
        var i: Int
        var minFlag = false
        list.add(0)
        while (!stack.isEmpty()) {
            element = stack.peek()
            i = 0
            var min: Float = Float.MAX_VALUE
            while (i < nodesCount) {
                if (matrix[element][i] < Float.MAX_VALUE
                    && visited[i] == 0
                    && min > matrix[element][i]
                ) {
                    min = matrix[element][i]
                    dst = i
                    minFlag = true
                }
                i++
            }
            if (minFlag) {
                visited[dst] = 1
                stack.push(dst)
                list.add(dst)
                minFlag = false
                continue
            }
            stack.pop()
        }
        return list
    }
}