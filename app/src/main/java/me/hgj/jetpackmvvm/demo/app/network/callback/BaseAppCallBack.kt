package me.hgj.jetpackmvvm.demo.app.network.callback

import me.hgj.jetpackmvvm.demo.app.network.Utils.Utils
import java.lang.reflect.Type

/**
 * Author by Ouyangle, Date on 2022/5/8.
 * PS: Not easy to write code, please indicate.
 */
open class BaseAppCallBack<T> : CallBack<T>() {
    /*public abstract void onStart();

    public abstract void onCompleted();

    public abstract void onError(ApiException e);

    public abstract void onSuccess(T result);*/
    override fun getType(): Type? { //获取需要解析的泛型T类型
        return Utils.findNeedClass(javaClass)
    }

    override fun getRawType(): Type? { //获取需要解析的泛型T raw类型
        return Utils.findRawType(javaClass)
    }

}