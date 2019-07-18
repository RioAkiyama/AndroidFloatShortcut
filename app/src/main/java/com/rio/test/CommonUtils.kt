package com.rio.test

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import java.io.ByteArrayOutputStream



class CommonUtils {

    public fun getInstalledAppInfo(con : Context) : ArrayList<InstalledApps>{
        val pm = con.getPackageManager()
        val pckInfoList = pm.getInstalledApplications(PackageManager.GET_META_DATA) as List<ApplicationInfo>

        val appList = ArrayList<InstalledApps>()
        for (pckInfo in pckInfoList) {
            // 起動可能なアプリのみ取得
            if (pm.getLaunchIntentForPackage(pckInfo.packageName) != null) {
                val appData = InstalledApps()
                appData.packageName = pckInfo.packageName
                appData.className = pm.getLaunchIntentForPackage(pckInfo.packageName).component.className
                appData.appIcon = drawable2byte(pckInfo.loadIcon(pm))
                appData.appName = pckInfo.loadLabel(pm).toString()
                appList.add(appData)
            }
        }

        return appList
    }

    public fun byte2Bitmap(bytArray : ByteArray) : Bitmap{
        return BitmapFactory.decodeByteArray(bytArray, 0, bytArray.size)
    }

    public fun drawable2byte(image : Drawable) : ByteArray{
        val bitmap = getBitmapFromDrawable(image)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    public fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        val bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable.draw(canvas)
        return bmp
    }
}