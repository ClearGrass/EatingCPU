package com.cleargrass.app.eattingcpu

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.telecom.Call
import android.text.method.ScrollingMovementMethod
import android.widget.SeekBar
import com.cleargrass.app.eattingcpu.PiService.Companion.ACTION_PI
import com.cleargrass.app.eattingcpu.PiService.Companion.EXTRA_PI
import com.cleargrass.app.eattingcpu.PiService.Companion.SPEED_MAX
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader


class MainActivity : Activity() {
    lateinit var cpuInfoHandler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        stop.setOnClickListener {
            PiService.stopAction(this)
        }
        start.setOnClickListener {
            PiService.startAction(this, speed_bar.progress)
        }
        clear.setOnClickListener {
            PiService.clearAction(this)
        }
        pi_is.movementMethod = ScrollingMovementMethod()
        speed_bar.max = PiService.SPEED_MAX
        speed_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val speed = progress
                val sleepTime = PiService.sleepFromSpeed(speed)
                if (speed >= SPEED_MAX) {
                    speed_is.text = "Speed: Max   Sleep: $sleepTime"
                } else {
                    speed_is.text = "Speed: $speed   Sleep: $sleepTime"
                }
                PiService.changeSpeed(this@MainActivity, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
        speed_bar.progress = PiService.SPEED_MAX
        cpuInfoHandler = Handler(Handler.Callback {
            if (it.what == 0) {
                startGetCupTask()
            } else if (it.what == 10) {
                val cpus = it.obj as List<Cpu>

                val cpuView = listOf(cpu0, cpu1, cpu2, cpu3)
                for (i in 0..3) {
                    cpuView[i].text = cpus[i].toString()
                }
                cpuInfoHandler.sendEmptyMessageDelayed(0, 1000)
//            } else if (it.what == 100) {
//                loop_count.text = "Loop: $0"
            }
            true;
        })
        cpuInfoHandler.sendEmptyMessage(0)
    }

    private fun startGetCupTask() {
        Thread({
            val cpus =(0..3).map {cpuId ->
                Cpu(CpuUtil.getMinCpuFreq(cpuId), CpuUtil.getMaxCpuFreq(cpuId), CpuUtil.getCurCpuFreq(cpuId))
            }
            cpuInfoHandler.removeMessages(10)
            cpuInfoHandler.sendMessage(cpuInfoHandler.obtainMessage(10, cpus))
        }).start()
    }

    override fun onResume() {
        super.onResume()
        getSharedPreferences("info", Context.MODE_PRIVATE).let {
            val speed = it.getInt("speed", 10000)
            val sleepTime = it.getFloat("sleep", PiService.sleepFromSpeed(speed));
            if (speed >= SPEED_MAX) {
                speed_is.text = "Speed: Max   Sleep: $sleepTime"
            } else {
                speed_is.text = "Speed: $speed   Sleep: $sleepTime"
            }
            speed_bar.progress = speed
        }
        registerReceiver(rev, IntentFilter(ACTION_PI))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(rev)
    }

    override fun onDestroy() {
        super.onDestroy()
        cpuInfoHandler.removeMessages(0)
        cpuInfoHandler.removeMessages(10)
    }

    var rev = object: BroadcastReceiver () {
        override fun onReceive(context: Context?, intent: Intent?) {
            pi_is.text = intent?.getStringExtra(EXTRA_PI) ?: "3.14"
        }
    }


//    fun createThread2() {
//        var y = 1.0
//        var base = 0
//        Thread({
//            while (isStart) {
//                piValue = BigDecimal(3).multiply(BigDecimal(2).pow(base)).multiply(BigDecimal(y))    /// BigDecimal.valueOf(3 * Math.pow(2.0, base.toDouble()) * y)
//                y = Math.sqrt(2-Math.sqrt(4-y*y));
//                base += 1
//                Thread.sleep(100)
//            }
//        }).start()
//    }


}
