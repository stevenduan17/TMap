package com.steven.tmap.sample

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Path
import android.graphics.PointF
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.steven.tmap.*
import com.steven.tmap.layer.LocationLayer
import com.steven.tmap.layer.OutlineLayer
import com.steven.tmap.layer.RouteLayer
import kotlinx.android.synthetic.main.activity_nav.*

/**
 * @author Steven
 * @version 1.0
 * @since 2020/5/12
 */
class NavActivity : AppCompatActivity() {

    private var start: PointF? = null
    private var end: PointF? = null

    private var flag = false

    private var routeLayer: RouteLayer? = null

    private var locationLayer: LocationLayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav)
        supportActionBar?.title = "导航"

        tMap.addLayer(OutlineLayer().apply {
            setOutline(outline, Color.BLUE, 3F)
            onClick = {
                val p = tMap.getRealCoordinateWithScreenXY(it[0], it[1])
                if (flag && start == null) {
                    start = PointF(p[0], p[1])
                } else if (flag && end == null) {
                    flag = false
                    end = PointF(p[0], p[1])
                    //nav route
                    setupNav()
                }
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        flag = true
        start = null
        end = null
        return super.onOptionsItemSelected(item)
    }

    private fun setupNav() {
        if (start == null || end == null) return

        RouteHelper.init(flags.size, topology.size)

        val result = RouteHelper.getShortestPathBetweenPoints(
            start!!, end!!, flags, topology
        )

        val routers = mutableListOf<PointF>()
        routers.add(start!!)
        routers.addAll(result.map { flags[it] })
        routers.add(end!!)

        descTextView.visibility = View.VISIBLE
        IO.execute {
            val distance = getDistance(routers)
            MAIN.post {
                descTextView.text = String.format("全程 %.2fm,现在开始导航！", distance)
            }
        }


        val path = getPath(routers)

        val startIcon = BitmapFactory.decodeResource(resources, R.drawable.point)
        val endIcon = BitmapFactory.decodeResource(resources, R.drawable.marker_active)

        routeLayer?.updatePaths(
            hashMapOf(path to Color.MAGENTA),
            startIcon = startIcon to start!!,
            endIcon = endIcon to end!!
        ) ?: tMap.addLayer(
            RouteLayer(
                hashMapOf(path to Color.MAGENTA),
                startIcon to start!!,
                endIcon to end!!
            ).also { this.routeLayer = it }
        )

        if (locationLayer == null) {
            locationLayer = LocationLayer().apply {
                navigateLine = routers
            }
            tMap.addLayer(locationLayer!!)
        } else {
            locationLayer?.navigateLine = routers
        }


        var i = 0
        val r = getIntercptedRoute(routers)
        MAIN.postDelayed(object : Runnable {
            override fun run() {
                if (i == r.size) {
                    MAIN.removeCallbacks(this)
                } else {
                    setCurrentPosition(r[i], routers)
                    i++
                    MAIN.postDelayed(this, 2000)
                }

            }
        }, 2000)
    }

    private fun setCurrentPosition(
        point: PointF,
        routers: MutableList<PointF>
    ) {
        IO.execute {
            //计算已经走过和未走过的路线
            var distance = Float.MAX_VALUE
            var i = -1
            routers.forEachIndexed { index, p ->
                val d = getDistance(point, p)
                if (d < distance) {
                    distance = d
                    i = index
                }
            }

            if (i >= 0) {
                val doneRoute = mutableListOf<PointF>()
                val undoneRoute = mutableListOf<PointF>()

                val result: PointF
                if (i == 0) {
                    result =
                        getShortestIntersectionFromPointToLine(point, routers[i], routers[i + 1])
                    if (routers[0].equals(result.x, result.y)) {
                        //处于起点 ignore
                        undoneRoute.addAll(routers)
                    } else {
                        doneRoute.add(routers.first())
                        doneRoute.add(result)
                        undoneRoute.add(result)
                        undoneRoute.addAll(routers.subList(1, routers.size))
                    }
                } else if (i == routers.size - 1) {
                    Log.d("NAV", "arrived to end.")
                    result =
                        getShortestIntersectionFromPointToLine(point, routers[i - 1], routers[i])
                    if (routers[i].equals(result.x, result.y)) {
                        Log.d("NAV", "nav arrived.")
                        //已经到达终点
                        doneRoute.addAll(routers)
                        undoneRoute.clear()
                    } else {
                        doneRoute.addAll(routers.subList(0, routers.size - 1))
                        doneRoute.add(result)
                        undoneRoute.add(result)
                        undoneRoute.add(routers.last())
                    }

                } else {
                    result =
                        getShortestIntersectionFromPointToLine(point, routers[i], routers[i + 1])

                    val result1 =
                        getShortestIntersectionFromPointToLine(point, routers[i - 1], routers[i])

                    if (getDistance(point, result) > getDistance(point, result1)) {
                        doneRoute.addAll(routers.subList(0, i))
                        doneRoute.add(result1)
                        undoneRoute.add(result1)
                        undoneRoute.addAll(routers.subList(i, routers.size))
                    } else {
                        doneRoute.addAll(routers.subList(0, i + 1))
                        doneRoute.add(result)
                        undoneRoute.add(result)
                        undoneRoute.addAll(routers.subList(i + 1, routers.size))
                    }
                }

                val remainDistance = getDistance(undoneRoute)
                Log.d("NAV", "remain distance $remainDistance")
                MAIN.post {
                    descTextView.text = if (remainDistance < 0.5f) "您已到达终点,导航结束！"
                    else String.format("全程剩余 %.2fm！", remainDistance)
                    routeLayer?.updatePaths(
                        hashMapOf(
                            getPath(doneRoute) to Color.DKGRAY,
                            getPath(undoneRoute) to Color.MAGENTA
                        ), ignoreEnds = true
                    )
                }
            }
        }

        locationLayer?.setCurrentLocation(point, true) { degree ->
            Log.d("NAV", "degree: $degree")
            getDegreeDesc(degree)
        }
    }

    private fun getDegreeDesc(degree: Double) {
        val desc = if (degree in 45.0..135.0) {
            "左转"
        } else if (degree > 135 && degree < 180) {
            "向后转"
        } else if (degree <= -45 && degree >= -135) {
            "右转"
        } else if (degree >= -180 && degree < -135) {
            "向后转"
        } else null

        desc?.let { toast(it) }
    }
}