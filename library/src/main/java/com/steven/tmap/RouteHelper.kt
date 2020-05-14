package com.steven.tmap

import android.graphics.Point
import android.graphics.PointF
import com.steven.tmap.algorithm.getBestPathByGenetic
import com.steven.tmap.algorithm.getShortestPath
import java.util.*

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
            for (i in 0 until topology.size - 1) {
                val p1 = nodes[topology[i].x]
                val p2 = nodes[topology[i].y]
                if (!isObtuseAnglePointAndLine(point, p1, p2)) {
                    val minDis = getDistanceFromPointToLine(point, p1, p2)
                    if (min > minDis) {
                        p = getIntersectionFromPointToLine(point, p1, p2)
                        min = minDis
                        pos1 = topology[i].x
                        pos2 = topology[i].y
                    }
                }
            }
            // get intersection
            nodes.add(p!!)
            topology.add(Point(pos1, nodes.size - 1))
            topology.add(Point(pos2, nodes.size - 1))
        }
    }

}