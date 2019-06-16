package me.shetj.base.tools.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.Keep
import androidx.core.content.FileProvider
import me.shetj.base.base.BaseCallback
import me.shetj.base.tools.app.AppUtils
import me.shetj.base.tools.file.SDCardUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Keep
class ImageUtils {

    companion object {

        val GET_IMAGE_BY_CAMERA = 5001
        val GET_IMAGE_FROM_PHONE = 5002
        val CROP_IMAGE = 5003
        var imageUriFromCamera: Uri? = null
        var cropImageUri: Uri? = null
        private val imagePath = "shetj_base/image"

        /**
         * 创建一条图片地址uri,用于保存拍照后的照片
         *
         * @param context
         * @return 图片的uri
         */
        private fun createImagePathUri(context: Context?): Uri {
            val file = File(createImagePath())
            if (context == null) {
                throw NullPointerException()
            }
            val uri: Uri
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(context.applicationContext, AppUtils.appPackageName + ".FileProvider", file)
            } else {
                uri = Uri.fromFile(file)
            }
            return uri
        }
        @JvmStatic
        fun createImagePath(): String {
            val timeFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
            val time = System.currentTimeMillis()
            val imageName = timeFormatter.format(Date(time))
            return SDCardUtils.getPath(imagePath) + "/" + imageName + ".jpg"
        }
        @JvmStatic
        fun openCameraImage(activity: Activity) {
            ImageUtils.imageUriFromCamera = ImageUtils.createImagePathUri(activity)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUtils.imageUriFromCamera)
            activity.startActivityForResult(intent, ImageUtils.GET_IMAGE_BY_CAMERA)
        }
        @JvmStatic
        fun openLocalImage(activity: Activity) {
            val intent: Intent
            intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            activity.startActivityForResult(intent, ImageUtils.GET_IMAGE_FROM_PHONE)
        }
        @JvmStatic
        fun cropImage(activity: Activity, srcUri: Uri?) {
            ImageUtils.cropImageUri = Uri.fromFile(File(createImagePath()))
            val intent = Intent("com.android.camera.action.CROP")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            intent.setDataAndType(srcUri, "image/*")
            //裁剪图片的宽高比例
            intent.putExtra("aspectX", 1)
            intent.putExtra("aspectY", 1)
            intent.putExtra("crop", "true")
            //可裁剪
            intent.putExtra("scale", true)
            //支持缩放
            intent.putExtra("return-data", false)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUtils.cropImageUri)
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
            //输出图片格式
            intent.putExtra("noFaceDetection", true)
            //取消人脸识别
            activity.startActivityForResult(intent, CROP_IMAGE)
        }


        /**
         * 把uri转成file
         *
         * @param activity
         * @param uri
         * @return
         */
        fun getFileByUri(activity: Activity, uri: Uri): File? {
            var path: String? = null
            if ("file" == uri.scheme) {
                path = uri.encodedPath
                if (path != null) {
                    path = Uri.decode(path)
                    val cr = activity.contentResolver
                    val buff = StringBuffer()
                    buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=").append("'$path'").append(")")
                    val cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA), buff.toString(), null, null)
                    var index = 0
                    var dataIdx = 0
                    cur!!.moveToFirst()
                    while (!cur.isAfterLast) {
                        index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID)
                        index = cur.getInt(index)
                        dataIdx = cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                        path = cur.getString(dataIdx)
                        cur.moveToNext()
                    }
                    cur.close()
                    if (index == 0) {
                    } else {
                        val u = Uri.parse("content://media/external/images/media/$index")
                        println("temp uri is :$u")
                    }
                }
                if (path != null) {
                    return File(path)
                }
            } else if ("content" == uri.scheme) {
                // 4.2.2以后
                val proj = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = activity.contentResolver.query(uri, proj, null, null, null)
                if (cursor!!.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    path = cursor.getString(columnIndex)
                }
                cursor.close()
                return File(path!!)
            } else {
            }
            return null
        }

        /**
         * get图片添加文字
         * @param imageBitmap 图片
         * @param des 文字
         * @param textSize  文字大小
         * @return
         */
        @JvmStatic
        fun getShareingBitmap(imageBitmap: Bitmap, des: String, textSize: Int): Bitmap {
            val config = imageBitmap.config
            val sourceBitmapHeight = imageBitmap.height
            val sourceBitmapWidth = imageBitmap.width
            val paint = Paint()
            // 画笔颜色
            paint.color = Color.BLACK

            val textpaint = TextPaint(paint)
            // 文字大小
            textpaint.textSize = textSize.toFloat()
            // 抗锯齿
            textpaint.isAntiAlias = true

            val title_layout = StaticLayout(des, textpaint,
                    sourceBitmapWidth, Layout.Alignment.ALIGN_CENTER, 1f, 1f, true)

            val share_bitmap = Bitmap.createBitmap(sourceBitmapWidth, sourceBitmapHeight + title_layout.height, config)
            val canvas = Canvas(share_bitmap)

            canvas.drawColor(Color.WHITE)

            // 绘制图片
            canvas.drawBitmap(imageBitmap, 0f, 0f, paint)
            canvas.translate(0f, sourceBitmapHeight.toFloat())
            title_layout.draw(canvas)
            canvas.translate(0f, title_layout.height.toFloat())
            return share_bitmap
        }


        /**
         * 在 [Activity] onActivityResult 下使用
         * @param context
         * @param requestCode
         * @param data
         * @param callBack
         */
        @JvmStatic
        fun onActivityResult(context: Activity, requestCode: Int, data: Intent?, callBack: BaseCallback<String>?) {
            when (requestCode) {
                ImageUtils.GET_IMAGE_BY_CAMERA -> if (ImageUtils.imageUriFromCamera != null) {
                    // 对图片进行裁剪
                    ImageUtils.cropImage(context, ImageUtils.imageUriFromCamera)
                }
                ImageUtils.GET_IMAGE_FROM_PHONE -> if (data != null && data.data != null) {
                    ImageUtils.cropImage(context, data.data)
                }
                ImageUtils.CROP_IMAGE -> {
                    val path = ImageUtils.cropImageUri!!.path
                    if (ImageUtils.cropImageUri != null && File(path!!).exists()) {
                        callBack?.onSuccess(path)
                    } else {
                        callBack?.onFail()
                    }
                }
                else -> callBack?.onFail()
            }
        }


        /**
         * 获取圆形图片
         * @param bitmap
         * @return
         */
        fun getRoundImage(bitmap: Bitmap): Bitmap {
            val width = bitmap.width
            val height = bitmap.height
            var left = 0
            var top = 0
            var right = width
            var bottom = height
            var roundPx = (height / 2).toFloat()
            if (width > height) {
                left = (width - height) / 2
                top = 0
                right = left + height
                bottom = height
            } else if (height > width) {
                left = 0
                top = (height - width) / 2
                right = width
                bottom = top + width
                roundPx = (width / 2).toFloat()
            }

            val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val color = -0xbdbdbe
            val paint = Paint()
            val rect = Rect(left, top, right, bottom)
            val rectF = RectF(rect)
            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)
            return output
        }

        /**
         * 缩放图片
         *
         * @param src       源图片
         * @param newWidth  新宽度
         * @param newHeight 新高度
         * @param recycle   是否回收
         * @return 缩放后的图片
         */
        @JvmOverloads
        fun scale(src: Bitmap, newWidth: Int, newHeight: Int, recycle: Boolean = false): Bitmap? {
            if (isEmptyBitmap(src)) {
                return null
            }
            val ret = Bitmap.createScaledBitmap(src, newWidth, newHeight, true)
            if (recycle && !src.isRecycled) {
                src.recycle()
            }
            return ret
        }

        /**
         * 缩放图片
         *
         * @param src         源图片
         * @param scaleWidth  缩放宽度倍数
         * @param scaleHeight 缩放高度倍数
         * @param recycle     是否回收
         * @return 缩放后的图片
         */
        @JvmOverloads
        fun scale(src: Bitmap, scaleWidth: Float, scaleHeight: Float, recycle: Boolean = false): Bitmap? {
            if (isEmptyBitmap(src)) {
                return null
            }
            val matrix = Matrix()
            matrix.setScale(scaleWidth, scaleHeight)
            val ret = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
            if (recycle && !src.isRecycled) {
                src.recycle()
            }
            return ret
        }

        /**
         * 判断bitmap对象是否为空
         *
         * @param src 源图片
         * @return `true`: 是<br></br>`false`: 否
         */
        private fun isEmptyBitmap(src: Bitmap?): Boolean {
            return src == null || src.width == 0 || src.height == 0
        }

        /******************************~~~~~~~~~ 下方和压缩有关 ~~~~~~~~~ */

        /**
         * 按缩放压缩
         *
         * @param src       源图片
         * @param newWidth  新宽度
         * @param newHeight 新高度
         * @return 缩放压缩后的图片
         */
        fun compressByScale(src: Bitmap, newWidth: Int, newHeight: Int): Bitmap? {
            return scale(src, newWidth, newHeight, false)
        }

        /**
         * 按缩放压缩
         *
         * @param src       源图片
         * @param newWidth  新宽度
         * @param newHeight 新高度
         * @param recycle   是否回收
         * @return 缩放压缩后的图片
         */
        fun compressByScale(src: Bitmap, newWidth: Int, newHeight: Int, recycle: Boolean): Bitmap? {
            return scale(src, newWidth, newHeight, recycle)
        }

        /**
         * 按缩放压缩
         *
         * @param src         源图片
         * @param scaleWidth  缩放宽度倍数
         * @param scaleHeight 缩放高度倍数
         * @return 缩放压缩后的图片
         */
        fun compressByScale(src: Bitmap, scaleWidth: Float, scaleHeight: Float): Bitmap? {
            return scale(src, scaleWidth, scaleHeight, false)
        }

        /**
         * 按缩放压缩
         *
         * @param src         源图片
         * @param scaleWidth  缩放宽度倍数
         * @param scaleHeight 缩放高度倍数
         * @param recycle     是否回收
         * @return 缩放压缩后的图片
         */
        fun compressByScale(src: Bitmap, scaleWidth: Float, scaleHeight: Float, recycle: Boolean): Bitmap? {
            return scale(src, scaleWidth, scaleHeight, recycle)
        }

        /**
         * 按质量压缩
         *
         * @param src     源图片
         * @param quality 质量
         * @param recycle 是否回收
         * @return 质量压缩后的图片
         */
        @JvmOverloads
        fun compressByQuality(src: Bitmap, quality: Int, recycle: Boolean = false): Bitmap? {
            if (isEmptyBitmap(src) || quality < 0 || quality > 100) {
                return null
            }
            val baos = ByteArrayOutputStream()
            src.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            val bytes = baos.toByteArray()

            if (recycle && !src.isRecycled) {
                src.recycle()
            }
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        /**
         * 按质量压缩
         *
         * @param src         源图片
         * @param maxByteSize 允许最大值字节数
         * @param recycle     是否回收
         * @return 质量压缩压缩过的图片
         */
        @JvmOverloads
        fun compressByQuality(src: Bitmap, maxByteSize: Long, recycle: Boolean = false): Bitmap? {
            if (isEmptyBitmap(src) || maxByteSize <= 0) {
                return null
            }
            val baos = ByteArrayOutputStream()
            var quality = 100
            src.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            while (baos.toByteArray().size > maxByteSize && quality > 0) {
                baos.reset()
                quality -= 5
                src.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            }
            if (quality < 0) {
                return null
            }
            val bytes = baos.toByteArray()
            if (recycle && !src.isRecycled) {
                src.recycle()
            }
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        /**
         * 按采样大小压缩
         *
         * @param src        源图片
         * @param sampleSize 采样率大小
         * @param recycle    是否回收
         * @return 按采样率压缩后的图片
         */
        @JvmOverloads
        fun compressBySampleSize(src: Bitmap, sampleSize: Int, recycle: Boolean = false): Bitmap? {
            if (isEmptyBitmap(src)) {
                return null
            }
            val options = BitmapFactory.Options()
            options.inSampleSize = sampleSize
            val baos = ByteArrayOutputStream()
            src.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val bytes = baos.toByteArray()
            if (recycle && !src.isRecycled) {
                src.recycle()
            }
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        }
    }
}