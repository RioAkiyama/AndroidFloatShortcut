package com.rio.test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton

class SampleService : Service() {
    private var m_view: View? = null
    private var m_wm: WindowManager? = null
    private var m_selectApp:InstalledApps? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        m_selectApp = intent!!.getSerializableExtra("appinfo") as InstalledApps
        sendNotification()
        createFloatIconButton()
        return START_STICKY
    }

    private fun sendNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = "Float Shortcut App"
        val id = "foreground_service_id"
        val notifyDescription = "app detailed information"

        if (manager.getNotificationChannel(id) == null) {
            val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
            mChannel.apply {
                description = notifyDescription
            }
            manager.createNotificationChannel(mChannel)
        }

        val notification = NotificationCompat.Builder(this,id).apply {
            setSmallIcon(R.drawable.ic_launcher_background)
            setContentTitle("Float Shortcut")
            setContentText("Now shortcut is available")
        }.build()

        startForeground(1, notification)
    }

    private fun createFloatIconButton() {
        m_wm = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams()
        val layoutInflater = LayoutInflater.from(this)

        params.let {
            //レイヤー設定
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                it.type = WindowManager.LayoutParams.TYPE_PHONE
            }
            it.format = PixelFormat.RGBA_8888
            it.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            it.gravity = Gravity.LEFT or Gravity.TOP
            it.x = 0
            it.y = 0
            it.width = WindowManager.LayoutParams.WRAP_CONTENT
            it.height = WindowManager.LayoutParams.WRAP_CONTENT
        }

        val utils = CommonUtils()
        m_view = layoutInflater.inflate(R.layout.overlay, null)
        m_wm!!.addView(m_view, params)
        val iconButton = m_view!!.findViewById(R.id.icon_button) as ImageButton
        iconButton.setImageBitmap(utils.byte2Bitmap(m_selectApp!!.appIcon!!))

        m_view!!.measure(
            View.MeasureSpec.makeMeasureSpec(
                0,
                View.MeasureSpec.UNSPECIFIED
            ), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        // iconボタンの移動
        iconButton.setOnTouchListener { _, event ->
            params.x = event.rawX.toInt() - iconButton.measuredWidth / 2
            params.y = event.rawY.toInt() - iconButton.measuredHeight / 2
            m_wm?.updateViewLayout(m_view, params)
            false
        }

        // タップされた場合
        iconButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.setClassName(m_selectApp!!.packageName, m_selectApp!!.className)
            startActivity(intent);
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        m_wm?.removeView(m_view);
        stopSelf()
    }
}