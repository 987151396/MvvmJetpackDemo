package me.hgj.jetpackmvvm.demo.app.network

import me.hgj.jetpackmvvm.demo.app.network.request.GetRequest
import me.hgj.jetpackmvvm.demo.app.network.request.PostRequest

/**
 * Author by Ouyangle, Date on 2022/5/6.
 * PS: Not easy to write code, please indicate.
 */
class MeHttp {
    companion object{
        /**
         * get请求
         */
        fun get(url: String?): GetRequest {
            return GetRequest(url)
        }

        /**
         * post请求
         */
        fun post(url: String?): PostRequest {
            return PostRequest(url)
        }
    }
}