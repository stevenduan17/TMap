package com.steven.tmap.algorithm

/**
 * @author Steven
 * @version 1.0
 * @since 2020/5/14
 */

/**
 * Get the shortest path between two points.
 */
internal fun getShortestPath(start: Int, end: Int, matrix: Array<FloatArray>): MutableList<Int> {
    return Floyd.getInstance().getShortestPath(start, end, matrix)
}

/**
 * Get the best path between some points.
 */
internal fun getBestPathBetweenPoints(matrix: Array<FloatArray>): MutableList<Int> {
    return TSP.getInstance().tsp(matrix)
}


/**
 * Get the best path between some points by genetic algorithm.
 */
internal fun getBestPathByGenetic(matrix: Array<FloatArray>): MutableList<Int> {
    val genetic = Genetic.getInstance()
    genetic.autoNextGeneration = true
    genetic.setMaxGeneration(200)
    val best = genetic.tsp(matrix)
    return best.toMutableList()
}