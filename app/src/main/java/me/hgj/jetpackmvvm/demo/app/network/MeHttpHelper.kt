package me.hgj.jetpackmvvm.demo.app.network

import me.hgj.jetpackmvvm.demo.app.network.callback.BaseAppCallBack
import me.hgj.jetpackmvvm.demo.app.network.callback.CallBack
import me.hgj.jetpackmvvm.demo.app.network.request.GetRequest
import me.hgj.jetpackmvvm.demo.app.network.request.HttpParams
import me.hgj.jetpackmvvm.demo.app.network.request.PostRequest
import me.hgj.jetpackmvvm.demo.data.model.bean.ApiPagerResponse
import me.hgj.jetpackmvvm.demo.data.model.bean.ApiResponse
import me.hgj.jetpackmvvm.demo.data.model.bean.AriticleResponse
import java.lang.reflect.Type

/**
 * Author by Ouyangle, Date on 2022/5/6.
 * PS: Not easy to write code, please indicate.
 */
class MeHttpHelper {
    companion object {
        val instance: MeHttpHelper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MeHttpHelper()
        }
    }

    suspend fun <V> get(
        url: String,
        httpParams: HttpParams?,
        callBack: Class<V>
    ): ApiResponse<V> {
        var getRequest: GetRequest = MeHttp.get(url)
        if (httpParams != null) {
            getRequest = getRequest.params(httpParams)!!
        }
        return getRequest.execute(callBack)
    }

    suspend fun <V> get(
        url: String,
        httpParams: HttpParams?,
        type: Type
    ): ApiResponse<V> {
        var getRequest: GetRequest = MeHttp.get(url)
        if (httpParams != null) {
            getRequest = getRequest.params(httpParams)!!
        }
        return getRequest.execute(type)
    }

    suspend fun <V> get(
        url: String,
        httpParams: HttpParams?,
        callBack: BaseAppCallBack<V>
    ): ApiResponse<V> {
        var getRequest: GetRequest = MeHttp.get(url)
        if (httpParams != null) {
            getRequest = getRequest.params(httpParams)!!
        }
        return getRequest.execute(callBack)
    }


    suspend fun <V> post(
        url: String,
        httpParams: HttpParams?,
        callBack: Class<V>
    ): ApiResponse<V> {
        var postRequest: PostRequest = MeHttp.post(url)
        if (httpParams != null) {
            postRequest = postRequest.params(httpParams)!!
        }
        return postRequest.execute(callBack)
    }

    suspend fun <V> post(
        url: String,
        httpParams: HttpParams?,
        callBack: BaseAppCallBack<V>
    ): ApiResponse<V> {
        var postRequest: PostRequest = MeHttp.post(url)
        if (httpParams != null) {
            postRequest = postRequest.params(httpParams)!!
        }
        return postRequest.execute(callBack)
    }

    suspend fun <V> post(
        url: String,
        httpParams: HttpParams?,
        type: Type
    ): ApiResponse<V> {
        var postRequest: PostRequest = MeHttp.post(url)
        if (httpParams != null) {
            postRequest = postRequest.params(httpParams)!!
        }
        return postRequest.execute(type)
    }


}