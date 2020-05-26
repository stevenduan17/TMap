package com.steven.tmap.sample

import android.graphics.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.steven.tmap.IO
import com.steven.tmap.RouteHelper
import com.steven.tmap.isPointInPolygon
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

        val path = Path()
        path.moveTo(start!!.x, start!!.y)
        result.forEach {
            path.lineTo(flags[it].x, flags[it].y)
        }
        path.lineTo(end!!.x, end!!.y)


        val startIcon = BitmapFactory.decodeResource(resources, R.drawable.point)
        val endIcon = BitmapFactory.decodeResource(resources, R.drawable.marker_active)

        routeLayer?.changePath(start!!, end!!, path) ?: tMap.addLayer(
            RouteLayer(
                path,
                startIcon,
                start!!,
                endIcon,
                end!!
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

       /* Toast.makeText(this, "开始导航", Toast.LENGTH_SHORT).show()

        var i = 0
        val r = getIntercptedRoute(routers)
        MAIN.postDelayed(object : Runnable {
            override fun run() {
                if (i == r.size - 1) {
                    MAIN.removeCallbacks(this)
                } else {
//                    changePath(routers, r[i])
                    locationLayer?.setCurrentLocation(r[i], true)
                    i++
                    MAIN.postDelayed(this, 2000)
                }
            }
        }, 2000)*/
    }

/*
    //TODO  someProblems .
    private fun changePath(
        routers: MutableList<PointF>,
        currentPosition: PointF
    ) {
        IO.execute {
            val p = getNavigationRoute(routers, currentPosition)
            MAIN.post {
                p?.let {
                    routeLayer?.changePath(it.second, routers.last(), p.first)
                } ?: toast("到达终点")
            }
        }
    }*/
}