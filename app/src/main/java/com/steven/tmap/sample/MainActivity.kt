package com.steven.tmap.sample

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Path
import android.graphics.PointF
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.steven.tmap.layer.LocationLayer
import com.steven.tmap.layer.MarkerLayer
import com.steven.tmap.layer.OutlineLayer
import com.steven.tmap.marker.Marker
import com.steven.tmap.marker.OnMarkerCheckedListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnMarkerCheckedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val list = listOf(
            PointF(0f, 0f),
            PointF(30f, 40f),
            PointF(90f, 20f),
            PointF(300f, 400f),
            PointF(200f, 600f)
        )

        tMap.addLayer(OutlineLayer().apply {
            setOutline(mutableListOf(Path().apply {
                for (p in list) {
                    lineTo(p.x, p.y)
                }
            }), Color.BLUE, 2F)
        })

        val markerActive = BitmapFactory.decodeResource(resources, R.drawable.marker_active)
        val icon = BitmapFactory.decodeResource(resources, R.drawable.point)
        val markerLayer = MarkerLayer(markerActive).apply {
            addMarkers(
                mutableListOf(
                    Marker(PointF(60F, 90F), icon),
                    Marker(PointF(100F, 130F), icon),
                    Marker(PointF(180F, 200F), icon),
                    Marker(PointF(200F, 500F), icon)
                )
            )
            onMarkerCheckedListener = this@MainActivity
        }
        tMap.addLayer(markerLayer)


        val locationLayer = LocationLayer().apply {
            locationRadius = toPx(6)
            navigateLine = list.reversed()
//            setCurrentLocation(PointF(280F, 380F))
        }
        tMap.addLayer(locationLayer)

        val locations = listOf(
            PointF(190f, 600f),
            PointF(290f, 400f),
            PointF(260f, 350f),
            PointF(80f, 20f),
            PointF(10f, 40f),
            PointF(0f, 0f)
        )

        val handler = Handler()
        var i = 0
        handler.postDelayed(object : Runnable {
            override fun run() {
                locationLayer.setCurrentLocation(locations[i], true)
                if (i == locations.size - 1) {
                    handler.removeCallbacks(this)
                } else {
                    i++
                }
                handler.postDelayed(this, 3000L)
            }
        }, 3000L)

    }

    override fun onMarkerChecked(marker: Marker) {
        Toast.makeText(this, marker.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.scale) {
            tMap.setCurrentZoom(1.5F)
        } else if (item?.itemId == R.id.nav) {
            startActivity(Intent(applicationContext, NavActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("SameParameterValue")
    private fun toPx(dp: Int) = dp * resources.displayMetrics.density + 0.5F
}
