package me.hgj.jetpackmvvm.demo.app.network.request

import me.hgj.jetpackmvvm.demo.app.network.Utils.Utils
import me.hgj.jetpackmvvm.demo.app.network.apiService
import me.hgj.jetpackmvvm.demo.app.network.body.ProgressResponseCallBack
import me.hgj.jetpackmvvm.demo.app.network.body.RequestBodyUtils
import me.hgj.jetpackmvvm.demo.app.network.body.UploadProgressRequestBody
import me.hgj.jetpackmvvm.demo.app.network.request.HttpParams.FileWrapper
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import java.io.File
import java.io.InputStream
import java.util.ArrayList
import java.util.HashMap

/**
 * Author by Ouyangle, Date on 2022/5/7.
 * PS: Not easy to write code, please indicate.
 */
abstract class BaseBodyRequest <R : BaseBodyRequest<R>>(url: String?) : BaseRequest<R>(url) {
    protected var str : String? = null//上传的文本内容
    protected var mediaType : MediaType? = null//上传的文本内容
    protected var json : String? = null//上传的Json
    protected var bs : ByteArray? = null//上传的字节数据
    protected var object_: Any? = null//上传的对象
    protected var requestBody : RequestBody? = null//自定义的请求体

    enum class UploadType {
        /**
         * MultipartBody.Part方式上传
         */
        PART,

        /**
         * Map RequestBody方式上传
         */
        BODY
    }

    private var currentUploadType = UploadType.PART

    open fun requestBody(requestBody: RequestBody?): R {
        this.requestBody = requestBody
        return this as R
    }

    /**
     * 注意使用该方法上传字符串会清空实体中其他所有的参数，头信息不清除
     */
    open fun upString(string: String): R {
        str = string
        mediaType = MediaType.parse("text/plain")
        return this as R
    }

    open fun upString(string: String, mediaType: String?): R {
        str = string
        Utils.checkNotNull(mediaType, "mediaType==null")
        this.mediaType = MediaType.parse(mediaType)
        return this as R
    }

    open fun upObject(@Body obj: Any): R {
        object_ = obj
        return this as R
    }

    /**
     * 注意使用该方法上传字符串会清空实体中其他所有的参数，头信息不清除
     */
    open fun upJson(json: String?): R {
        this.json = json
        return this as R
    }

    /**
     * 注意使用该方法上传字符串会清空实体中其他所有的参数，头信息不清除
     */
    open fun upBytes(bs: ByteArray?): R {
        this.bs = bs
        return this as R
    }


    open fun params(key: String?, file: File?, responseCallBack: ProgressResponseCallBack?): R {
        params.put(key, file, responseCallBack)
        return this as R
    }

    open fun params(
        key: String?,
        stream: InputStream?,
        fileName: String?,
        responseCallBack: ProgressResponseCallBack?
    ): R {
        params.put(key, stream, fileName, responseCallBack)
        return this as R
    }

    open fun params(
        key: String?,
        bytes: ByteArray?,
        fileName: String?,
        responseCallBack: ProgressResponseCallBack?
    ): R {
        params.put(key, bytes, fileName, responseCallBack)
        return this as R
    }


    open fun addFileParams(
        key: String?,
        files: List<File?>?,
        responseCallBack: ProgressResponseCallBack?
    ): R {
        params.putFileParams(key, files, responseCallBack)
        return this as R
    }

    open fun addFileWrapperParams(key: String?, fileWrappers: List<FileWrapper<*>>): R {
        params.putFileWrapperParams(key, fileWrappers)
        return this as R
    }

    open fun params(
        key: String?,
        file: File?,
        fileName: String?,
        responseCallBack: ProgressResponseCallBack?
    ): R {
        params.put(key, file, fileName, responseCallBack)
        return this as R
    }


    open fun <T> params(
        key: String?,
        file: T,
        fileName: String?,
        contentType: MediaType?,
        responseCallBack: ProgressResponseCallBack?
    ): R {
        params.put(key, file, fileName, contentType, responseCallBack)
        return this as R
    }

    /**
     * 上传文件的方式，默认part方式上传
     */
    open fun <T> uploadType(uploadtype: UploadType): R {
        currentUploadType = uploadtype
        return this as R
    }

    override suspend fun generateRequest(): ResponseBody {
        if (requestBody != null) { //自定义的请求体
            return apiService.postBody(url, requestBody)
        } else if (json != null) { //上传的Json
            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
            return apiService.postJson(url, body)
        } else if (this.object_ != null) { //自定义的请求object
            return apiService.postBody(url, object_)
        } else if (this.str != null) { //上传的文本内容
            val body: RequestBody = RequestBody.create(mediaType, this.str)
            return apiService.postBody(url, body)
        } else if (bs != null) { //上传的字节数据
            val body = RequestBody.create(MediaType.parse("application/octet-stream"), bs)
            return apiService.postBody(url, body)
        }
        return if (params.fileParamsMap.isEmpty()) {
            apiService.post(url, params.urlParamsMap)
        } else {
            if (currentUploadType == UploadType.PART) { //part方式上传
                uploadFilesWithParts()
            } else { //body方式上传
                uploadFilesWithBodys()
            }
        }
    }

    protected suspend fun uploadFilesWithParts(): ResponseBody {
        val parts: MutableList<MultipartBody.Part> = ArrayList()
        //拼接参数键值对
        for ((key, value) in params.urlParamsMap.entries) {
            parts.add(MultipartBody.Part.createFormData(key, value))
        }
        //拼接文件
        for ((key, fileValues) in params.fileParamsMap.entries) {
            for (fileWrapper in fileValues) {
                val part: MultipartBody.Part = addFile(key, fileWrapper)!!
                parts.add(part)
            }
        }
        return apiService.uploadFiles(url, parts)
    }

    //文件方式
    private fun addFile(key: String, fileWrapper: FileWrapper<*>): MultipartBody.Part? {
        //MediaType.parse("application/octet-stream", file)
        val requestBody = getRequestBody(fileWrapper)
        Utils.checkNotNull(
            requestBody,
            "requestBody==null fileWrapper.file must is File/InputStream/byte[]"
        )
        //包装RequestBody，在其内部实现上传进度监听
        return if (fileWrapper.responseCallBack != null) {
            val uploadProgressRequestBody =
                UploadProgressRequestBody(requestBody, fileWrapper.responseCallBack)
            MultipartBody.Part.createFormData(
                key,
                fileWrapper.fileName,
                uploadProgressRequestBody
            )
        } else {
            MultipartBody.Part.createFormData(key, fileWrapper.fileName, requestBody)
        }
    }

    protected suspend fun uploadFilesWithBodys() : ResponseBody {
        val mBodyMap: MutableMap<String, RequestBody> = HashMap()
        //拼接参数键值对
        for ((key, value) in params.urlParamsMap.entries) {
            val body = RequestBody.create(MediaType.parse("text/plain"), value)
            mBodyMap[key] = body
        }
        //拼接文件
        for ((key, fileValues) in params.fileParamsMap.entries) {
            for (fileWrapper in fileValues) {
                val requestBody: RequestBody = getRequestBody(fileWrapper)!!
                val uploadProgressRequestBody =
                    UploadProgressRequestBody(requestBody, fileWrapper.responseCallBack)
                mBodyMap[key] = uploadProgressRequestBody
            }
        }
        return apiService.uploadFiles(url, mBodyMap)
    }

    private fun getRequestBody(fileWrapper: FileWrapper<*>): RequestBody? {
        var requestBody: RequestBody? = null
        if (fileWrapper.file is File) {
            requestBody = RequestBody.create(fileWrapper.contentType, fileWrapper.file as File)
        } else if (fileWrapper.file is InputStream) {
            //requestBody = RequestBodyUtils.create(RequestBodyUtils.MEDIA_TYPE_MARKDOWN, (InputStream) fileWrapper.file);
            requestBody =
                RequestBodyUtils.create(fileWrapper.contentType, fileWrapper.file as InputStream)
        } else if (fileWrapper.file is ByteArray) {
            requestBody = RequestBody.create(fileWrapper.contentType, fileWrapper.file as ByteArray)
        }
        return requestBody
    }

}