package com.steven.tmap.sample

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Path
import android.graphics.PointF
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.steven.tmap.RouteHelper
import com.steven.tmap.layer.OutlineLayer
import com.steven.tmap.layer.RouteLayer
import kotlinx.android.synthetic.main.activity_nav.*

/**
 * @author Steven
 * @version 1.0
 * @since 2020/5/12
 */
class NavActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav)
        supportActionBar?.title = "导航"


        tMap.addLayer(OutlineLayer().apply {
            setOutline(outline, Color.BLUE, 3F)
        })

        RouteHelper.init(flags.size, topology.size)

       val result=  RouteHelper.getShortestPathBetweenPoints(
            PointF(95f, 267f), PointF(603f, 385f), flags, topology
        )

        val path = Path()
        path.moveTo(95f, 267f)
        result.forEach {
            path.lineTo(flags[it].x, flags[it].y)
        }
        path.lineTo(603f, 385f)


        val startIcon = BitmapFactory.decodeResource(resources, R.drawable.marker_active)
        val endIcon = BitmapFactory.decodeResource(resources, R.drawable.marker_active)

        tMap.addLayer(
            RouteLayer(
                path,
                startIcon,
                PointF(95f, 267f),
                endIcon,
                PointF(603f, 385f)
            )
        )
    }
}