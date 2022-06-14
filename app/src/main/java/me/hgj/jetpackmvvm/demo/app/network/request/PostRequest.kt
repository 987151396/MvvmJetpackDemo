package me.hgj.jetpackmvvm.demo.app.network.request

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import me.hgj.jetpackmvvm.demo.app.network.ApiResultFunc
import me.hgj.jetpackmvvm.demo.app.network.callback.CallBack
import me.hgj.jetpackmvvm.demo.app.network.callback.CallBackProxy
import me.hgj.jetpackmvvm.demo.app.network.callback.CallClazzProxy
import me.hgj.jetpackmvvm.demo.data.model.bean.ApiResponse
import okhttp3.ResponseBody
import java.lang.reflect.Type

/**
 * Author by Ouyangle, Date on 2022/5/7.
 * PS: Not easy to write code, please indicate.
 */
class PostRequest (url: String?) : BaseBodyRequest<PostRequest>(url){

    suspend fun <T> execute(clazz: Class<T>): ApiResponse<T> {
        return execute(object : CallClazzProxy<ApiResponse<T>, T>(clazz) {})
    }

    suspend fun <T> execute(type: Type?): ApiResponse<T> {
        return execute(object : CallClazzProxy<ApiResponse<T>?, T>(type) {})
    }

    suspend fun <T> execute(callBack: CallBack<T>?): ApiResponse<T> {
        return execute(object : CallBackProxy<ApiResponse<T>?, T>(callBack) {})
    }

    @SuppressLint("NewApi")
    suspend fun <T> execute(proxy: CallBackProxy<out ApiResponse<T>?, T>): ApiResponse<T> {
        Log.d("dafdasfadsfads","typeName" + proxy.type .typeName)
        var responseBody = generateRequest()
        var s = responseBody.string()
        var apiResultFunc = ApiResultFunc<T>(proxy.type,s)
        return apiResultFunc.apply(responseBody)!!
    }

    @SuppressLint("NewApi")
    private suspend fun <T> execute(proxy: CallClazzProxy<out ApiResponse<T>?, T>): ApiResponse<T> {
        Log.d("dafdasfadsfads","typeName" + proxy.type .typeName)
        var responseBody = generateRequest()
        var s = responseBody.string()
        var apiResultFunc = ApiResultFunc<T>(proxy.type,s)
        return apiResultFunc.apply(responseBody)!!
    }

    public override suspend fun generateRequest(): ResponseBody {
        return super.generateRequest()
    }
}
