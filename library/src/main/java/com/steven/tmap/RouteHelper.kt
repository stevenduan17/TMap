package com.steven.tmap

import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
import com.steven.tmap.algorithm.getBestPathByGenetic
import com.steven.tmap.algorithm.getShortestPath
import java.util.*
import kotlin.math.pow

/**
 * @author Steven
 * @version 1.0
 * @since 2020/5/14
 */
object RouteHelper {

    private var nodeSize = 0
    private var topologySize = 0

    fun init(nodeSize: Int, topologySize: Int) {
        this.nodeSize = nodeSize
        this.topologySize = topologySize
    }

    fun getDistanceBetweenPoints(nodes: List<PointF>, indexList: List<Int>): Float {
        var distance = 0f
        for (i in 0 until indexList.size - 1) {
            distance += getDistance(nodes[indexList[i]], nodes[indexList[i + 1]])
        }
        return distance
    }

    fun getShortestPathBetweenPoints(
        start: Int,
        end: Int,
        nodes: MutableList<PointF>,
        topology: MutableList<Point>
    ): MutableList<Int> {
        val matrix = getMatrixNodes(nodes, topology)
        return getShortestPath(start, end, matrix)
    }

    fun getShortestPathBetweenPoints(
        start: PointF,
        end: PointF,
        nodes: MutableList<PointF>,
        topology: MutableList<Point>
    ): MutableList<Int> {
        if (nodeSize != nodes.size) {
            var value: Int = nodes.size - nodeSize
            for (i in 0 until value) {
                nodes.removeAt(nodes.size - 1)
            }
            value = topologySize
            for (i in 0 until value) {
                topology.removeAt(topology.size - 1)
            }
        }

        addPointToList(start, nodes, topology)
        addPointToList(end, nodes, topology)

        return getShortestPathBetweenPoints(nodes.size - 2, nodes.size - 1, nodes, topology)
    }

    fun getBestPathBetweenPoints(
        points: IntArray,
        nodes: MutableList<PointF>,
        topology: MutableList<Point>
    ): MutableList<Int> {
        // adjacency matrix
        val matrix = Array(points.size) { FloatArray(points.size) }
        for (i in matrix.indices) {
            for (j in i until matrix[i].size) {
                if (i == j) {
                    matrix[i][j] = Float.MAX_VALUE
                } else {
                    matrix[i][j] = getDistanceBetweenPoints(
                        nodes,
                        getShortestPathBetweenPoints(
                            points[i],
                            points[j], nodes, topology
                        )
                    )
                    matrix[j][i] = matrix[i][j]
                }
            }
        }

        // TSP to get best path
        val routeList: MutableList<Int> = mutableListOf()
        val result: List<Int> = getBestPathByGenetic(matrix)
        for (i in 0 until result.size - 1) {
            val size = routeList.size
            routeList.addAll(
                getShortestPathBetweenPoints(
                    points[result[i]], points[result[i + 1]], nodes, topology
                )
            )
            if (i != 0) {
                routeList.removeAt(size)
            }
        }
        return routeList
    }

    fun getBestPathBetweenPoints(
        points: List<PointF>,
        nodes: MutableList<PointF>,
        topology: MutableList<Point>
    ): MutableList<Int> {
        if (nodeSize != nodes.size) {
            var value: Int = nodes.size - nodeSize
            for (i in 0 until value) {
                nodes.removeAt(nodes.size - 1)
            }
            value = topology.size - topologySize
            for (i in 0 until value) {
                topology.removeAt(topology.size - 1)
            }
        }

        val ps = IntArray(points.size)
        for (i in points.indices) {
            addPointToList(points[i], nodes, topology)
            ps[i] = nodes.size - 1
        }

        return getBestPathBetweenPoints(ps, nodes, topology)
    }

    private fun getMatrixNodes(
        nodes: MutableList<PointF>,
        topology: MutableList<Point>
    ): Array<FloatArray> {
        val matrix = Array(nodes.size) { FloatArray(nodes.size) }
        for (i in matrix.indices) {
            Arrays.fill(matrix[i], Float.MAX_VALUE)
        }
        topology.forEach {
            matrix[it.x][it.y] = getDistance(nodes[it.x], nodes[it.y])
            matrix[it.y][it.x] = matrix[it.x][it.y]
        }
        return matrix
    }

    private fun addPointToList(
        point: PointF?,
        nodes: MutableList<PointF>,
        topology: MutableList<Point>
    ) {
        if (point != null) {
            var p: PointF? = null
            var pos1 = 0
            var pos2 = 0
            var min = Float.MAX_VALUE
            var nodesPointIndex: Int? = null
            for (i in 0 until topology.size - 1) {
                val p1 = nodes[topology[i].x]
                val p2 = nodes[topology[i].y]

                val result = getShortestDistanceFromPointToLine(point, p1, p2)
                if (result.distance < min) {
                    min = result.distance
                    when {
                        result.isA -> {
                            nodesPointIndex = topology[i].x
                            p = null
                        }
                        result.isB -> {
                            nodesPointIndex = topology[i].y
                            p = null
                        }
                        else -> {
                            // get intersection
                            p = getIntersectionFromPointToLine(point, p1, p2)
                            pos1 = topology[i].x
                            pos2 = topology[i].y
                            nodesPointIndex = null
                        }
                    }
                }
            }

            p?.let {
                nodes.add(it)
                topology.add(Point(pos1, nodes.size - 1))
                topology.add(Point(pos2, nodes.size - 1))
            }

            nodesPointIndex?.let {
                nodes.add(point)
                topology.add(Point(it, nodes.size - 1))
            }
        }
    }

    //TODO  some problems.
    fun getNavigationRoute(origin: List<PointF>, currentLocation: PointF): Pair<Path, PointF>? {
        if (origin.isEmpty() || origin.size < 2) return null
        var minIndex = 0
        var minDistance = Float.MAX_VALUE
        var distance: Float
        origin.forEachIndexed { index, point ->
            distance = getDistance(point, currentLocation)
            if (distance < minDistance) {
                minDistance = distance
                minIndex = index
            }
        }
        //verify end.
        if (minIndex < origin.size - 1) {
            val isObtuseToNext = minDistance.pow(2) +
                    getDistance(origin[minIndex], origin[minIndex + 1]).pow(2) <
                    getDistance(currentLocation, origin[minIndex + 1]).pow(2)
            if (isObtuseToNext) {
                //is obtuse, currentPosition in minIndex-1 to minIndex
                if (minIndex == 0) {
                    return Pair(Path().apply {
                        moveTo(origin[0].x, origin[0].y)
                        for (i in 1 until origin.size) {
                            lineTo(origin[i].x, origin[i].y)
                        }
                    }, origin[0])
                } else {
                    val intersection = getIntersectionFromPointToLine(
                        currentLocation, origin[minIndex - 1], origin[minIndex]
                    )
                    return Pair(
                        Path().apply {
                            moveTo(intersection.x, intersection.y)
                            for (i in minIndex until origin.size) {
                                lineTo(origin[minIndex].x, origin[minIndex].y)
                            }
                        }, intersection
                    )
                }
            } else {
                val intersection = getIntersectionFromPointToLine(
                    currentLocation, origin[minIndex + 1], origin[minIndex]
                )
                return Pair(Path().apply {
                    moveTo(intersection.x, intersection.y)
                    for (i in minIndex + 1 until origin.size) {
                        lineTo(origin[minIndex].x, origin[minIndex].y)
                    }
                }, intersection)
            }
        } else {
            val isObtuseToEnd = minDistance.pow(2) +
                    getDistance(origin[minIndex], origin[minIndex - 1]).pow(2) <=
                    getDistance(currentLocation, origin[minIndex - 1]).pow(2)
            return if (isObtuseToEnd) {
                //already to the end
                null
            } else {
                val intersection = getIntersectionFromPointToLine(
                    currentLocation, origin[minIndex], origin[minIndex - 1]
                )
                Pair(Path().apply {
                    moveTo(intersection.x, intersection.y)
                    lineTo(origin[minIndex].x, origin[minIndex].y)
                }, intersection)
            }
        }
    }

}