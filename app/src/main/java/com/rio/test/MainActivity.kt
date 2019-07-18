package com.rio.test

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val REQUEST_OVERLAY_PERMISSION = 1
    private var m_utils:CommonUtils? = null
    private var m_selectApp:InstalledApps? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 権限要求
        if(!checkOverlayPermission()) requestOverlayPermission()

        // インストール済み、起動可能アプリ一覧作成
        m_utils = CommonUtils()
        val installedApps = m_utils!!.getInstalledAppInfo(this)
        val adapter = AppListAdapter(this, installedApps)
        listView1.setAdapter(adapter);

        // アプリ選択時
        listView1.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            m_selectApp = installedApps.get(position)
            sampleapp_icon.setImageBitmap(m_utils!!.byte2Bitmap(m_selectApp!!.appIcon!!))
        }

        buttonServiceStart.setOnClickListener {
            if(checkOverlayPermission()) {
                if(m_selectApp != null){
                    val serviceIntent = Intent(this, SampleService::class.java)
                    serviceIntent.putExtra("appinfo",m_selectApp)
                    startForegroundService(serviceIntent)
                }else{
                    Toast.makeText(applicationContext, "アプリを選択して下さい", Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(applicationContext, "権限を付与して下さい", Toast.LENGTH_LONG).show()
                requestOverlayPermission()
            }
        }

        buttonServiceStop.setOnClickListener {
            val serviceIntent = Intent(this, SampleService::class.java)
            stopService(serviceIntent);
        }
    }

    fun Context.checkOverlayPermission(): Boolean {
        if (Build.VERSION.SDK_INT < 23) {
            return true
        }
        return Settings.canDrawOverlays(this)
    }

    public fun Activity.requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${getPackageName()}"));
        this.startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_OVERLAY_PERMISSION -> if (checkOverlayPermission()) {
                // 権限付与済み
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // アプリ一覧のAdapter
    private class AppListAdapter(context: Context, dataList: List<InstalledApps>) : ArrayAdapter<InstalledApps>(context, com.rio.test.R.layout.app_list) {

        private val mInflater: LayoutInflater

        init {
            mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            addAll(dataList)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView

            var holder = ViewHolder()

            if (convertView == null) {
                convertView = mInflater.inflate(com.rio.test.R.layout.app_list, parent, false)
                holder.textLabel = convertView!!.findViewById(com.rio.test.R.id.app_name)
                holder.imageIcon = convertView!!.findViewById(com.rio.test.R.id.app_icon) as ImageView
                convertView!!.setTag(holder)
            } else {
                holder = convertView!!.getTag() as ViewHolder
            }

            val data = getItem(position)
            val utils = CommonUtils()
            // ラベルとアイコンをリストビューに設定
            holder.textLabel!!.setText(data!!.appName)
            holder.imageIcon!!.setImageBitmap(utils.byte2Bitmap(data!!.appIcon!!))

            return convertView
        }
    }

    private class ViewHolder {
        internal var textLabel: TextView? = null
        internal var imageIcon: ImageView? = null
    }
}



fun Context.toast(message:String){
    Toast.makeText(applicationContext,message, Toast.LENGTH_SHORT).show()
}
