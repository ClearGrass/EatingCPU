package com.cleargrass.app.eattingcpu

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigDecimal
import java.math.RoundingMode

private const val ACTION_START = "com.cleargrass.app.eattingcpu.action.START"
private const val ACTION_STOP = "com.cleargrass.app.eattingcpu.action.STOP"
private const val ACTION_SPEED = "com.cleargrass.app.eattingcpu.action.SPEED"
private const val ACTION_CLEAR = "com.cleargrass.app.eattingcpu.action.CLEAR"

private const val EXTRA_SPEED = "com.cleargrass.app.eattingcpu.extra.SPEED";

class PiService : IntentService("PiService") {
    var isStart = false;
    var speed = 5000
        set(value) {
            field = value
            sleepTime  = sleepFromSpeed(speed)
            getSharedPreferences("info", Context.MODE_PRIVATE).edit().putInt("speed", speed).putFloat("sleep", sleepTime).apply()
        }

    var sleepTime  = sleepFromSpeed(speed)
    var piValue = BigDecimal.valueOf((4f * (1f - 1f / 3 + 1f / 5 - 1f / 7 + 1f / 9)).toDouble())
        @SuppressLint("SetTextI18n")
        set(value) {
            field = value
//            runOnUiThread({
//                pi_is.text = value.toString()
//            })
        }
    var base = 11
    var loop = 0
    override fun onHandleIntent(intent: Intent?) {
        val mg = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Thread({
            while (isStart) {
                val piIntent = Intent(ACTION_PI)
                piIntent.putExtra(EXTRA_PI, piValue.toString())
                sendBroadcast(piIntent)
                Thread.sleep(1000)
            }
        }).start()
        while (isStart) {
            val now = piValue
            val x = BigDecimal.valueOf(4).divide(BigDecimal(base), 2000, RoundingMode.HALF_UP)
            piValue = now + if ((base / 2)  % 2 == 1) {  // 1 , 0 , 1
                -x
            } else {
                x
            }
//            cpuInfoHandler.sendEmptyMessage(100)


            val mgCompat = NotificationManagerCompat.from(this);

            builder?.apply {
                val bgt = NotificationCompat.BigTextStyle(this)
                setSmallIcon(R.drawable.pi_icon)
                setStyle(bgt).setContentText(piValue.toString())
                        .setSubText("loop: $loop")
            }?.let {
                mg.notify(1000, it.build().apply {
                    flags = Notification.FLAG_ONGOING_EVENT
                })
            }
            Log.d("pi", "π ≈ ${BigDecimal(piValue.toDouble()).setScale(10, RoundingMode.HALF_UP)} \nsleep $sleepTime loop $loop base $base)")
            loop ++
            base += 2
            Thread.sleep(sleepTime.toLong())
        }
        val piIntent = Intent(ACTION_PI)
        piIntent.putExtra(EXTRA_PI, piValue.toString())
        sendBroadcast(piIntent)
    }

    override fun onCreate() {
        super.onCreate()
        piValue.setScale(100000)
        val mg = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mg.createNotificationChannel(NotificationChannel("pi", "π", NotificationManager.IMPORTANCE_MIN).apply {
                setShowBadge(false)
            })
        }
    }

    private var builder: NotificationCompat.Builder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return super.onStartCommand(intent, flags, startId)
        Log.d("pi", "onStartCommand " + intent.action)
        if (intent.action.equals(ACTION_START)) {
            speed = intent.getIntExtra(EXTRA_SPEED, SPEED_MAX)
            if (isStart == false) {
                isStart = true
                piValue = BigDecimal.valueOf((4f * (1f - 1f / 3 + 1f / 5 - 1f / 7 + 1f / 9)).toDouble())
                base = 11
                loop = 0
            }
            builder = NotificationCompat.Builder(this, "pi").apply {
                val bgt = NotificationCompat.BigTextStyle(this)
                        .setBigContentTitle("π ≈")
                        .setSummaryText("$piValue...")
                setSmallIcon(R.drawable.pi_icon)
                setStyle(bgt).setContentTitle("π ≈")
                        .setSubText("loop: $loop")
                        .setContentText(piValue.toString())
                val stopintent = Intent(this@PiService, PiService::class.java).apply {
                    action = ACTION_STOP
                }
                addAction(0, "STOP", PendingIntent.getService(this@PiService, 0, stopintent, 0))
                setContentIntent(PendingIntent.getActivity(this@PiService, 10, Intent(this@PiService, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
            }
            startForeground(1000, builder!!.build())
        } else if (intent.action.equals(ACTION_STOP)){
            isStart = false
        } else if (intent.action.equals(ACTION_SPEED)){
            speed = intent.getIntExtra(EXTRA_SPEED, SPEED_MAX)
            Log.d("pi", "spped changed: $speed -> sleep $sleepTime")
        } else if (intent.action.equals(ACTION_CLEAR)) {
        }
        return super.onStartCommand(intent, flags, startId)
    }

    companion object {
        @JvmStatic
        val ACTION_PI = "com.cleargrass.app.eattingcpu.action.PI"
        @JvmStatic
        val EXTRA_PI = "com.cleargrass.app.eattingcpu.extra.PI";
        @JvmStatic
        val SLEEP_MIN = 20f
        @JvmStatic
        val SLEEP_MAX = 500f
        @JvmStatic
        val SPEED_MAX = 10000
        var sleepA = (SLEEP_MAX - SLEEP_MIN) / SPEED_MAX
        var sleepB = SLEEP_MIN

        @JvmStatic
        fun sleepFromSpeed(speed: Int) : Float{
            return sleepA * (SPEED_MAX - speed) + sleepB
        }

        @JvmStatic
        fun startAction(context: Context, progress: Int) {
            val intent = Intent(context, PiService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SPEED, progress)
            }
            context.startService(intent)
        }

        @JvmStatic
        fun stopAction(context: Context) {
            val intent = Intent(context, PiService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        @JvmStatic
        fun changeSpeed(context: Context, progress: Int) {
            val intent = Intent(context, PiService::class.java).apply {
                action = ACTION_SPEED
                putExtra(EXTRA_SPEED, progress)
            }
            context.startService(intent)
        }
        @JvmStatic
        fun clearAction(context: Context) {
            val intent = Intent(context, PiService::class.java).apply {
                action = ACTION_CLEAR
            }
            context.startService(intent)
        }
    }
}
