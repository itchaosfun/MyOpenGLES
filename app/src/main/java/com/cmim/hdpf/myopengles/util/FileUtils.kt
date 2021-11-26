package com.cmim.hdpf.myopengles.util

import android.content.Context
import android.util.Log
import java.io.*

object FileUtils {

    private const val TAG = "FileUtils"

    /**
     * 从assets目录中复制文件到本地sd卡缓存中
     * @param fileName as String
     */
    fun copyFileFromAssetsToCache(context: Context, fileName: String) {
        val assetManager = context.assets
        val file = File(context.getExternalFilesDir(null)?.absolutePath + File.separator + fileName)
        if (!file.exists()) {
            Log.i(TAG, "file not exist")
            try {
                val `in` = assetManager.open(fileName)
                val out =
                    FileOutputStream(context.getExternalFilesDir(null)?.absolutePath + File.separator + fileName)
                val buffer = ByteArray(1024)
                var read = `in`.read(buffer)
                while (read != -1) {
                    out.write(buffer, 0, read)
                    read = `in`.read(buffer)
                }
            } catch (e: Exception) {
                Log.i(TAG, "file copy error : " + e.message)
            }

        } else {
            Log.i(TAG, "$fileName is exsit")
        }
    }

    fun getAssetData(context: Context,fileName: String):String{
        val stringBuilder = StringBuilder();
        try {
            val inputStreamReader = context.assets.open(fileName);
            val bufferedReader = BufferedReader(InputStreamReader(inputStreamReader))
            var line: String? = null
            while (({ line = bufferedReader.readLine();line }()) != null) {
                stringBuilder.append(line)
                stringBuilder.append("\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }
}