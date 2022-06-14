package me.hgj.jetpackmvvm.demo.app.network.request

import me.hgj.jetpackmvvm.demo.data.model.bean.ApiResponse
import okhttp3.ResponseBody

/**
 * Author by Ouyangle, Date on 2022/5/6.
 * PS: Not easy to write code, please indicate.
 */
abstract class BaseRequest<R> constructor(var url: String?){

    protected var params: HttpParams = HttpParams()

    /**
     * 设置参数
     */
    open fun params(params: HttpParams?): R? {
        this.params.put(params)
        return this as R
    }

    open fun params(key: String?, value: String?): R? {
        params.put(key, value)
        return this as R
    }

    open fun params(map: Map<String?, String?>): R? {
        for ((key, value) in map) {
            params.put(key, value)
        }
        return this as R
    }

    open fun removeParam(key: String?): R? {
        params.remove(key)
        return this as R
    }

    open fun removeAllParams(): R? {
        params.clear()
        return this as R
    }
    protected abstract suspend fun generateRequest() : ResponseBody
}