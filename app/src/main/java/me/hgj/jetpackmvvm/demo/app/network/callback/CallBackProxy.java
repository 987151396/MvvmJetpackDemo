
package me.hgj.jetpackmvvm.demo.app.network.callback;

import android.util.Log;

import com.google.gson.internal.$Gson$Types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import me.hgj.jetpackmvvm.demo.app.network.Utils.Utils;
import me.hgj.jetpackmvvm.demo.data.model.bean.ApiResponse;
import me.hgj.jetpackmvvm.network.BaseResponse;
import okhttp3.ResponseBody;

/**
 * <p>描述：提供回调代理</p>
 * 主要用于可以自定义ApiResult<br>
 */
public abstract class CallBackProxy<T extends ApiResponse<R>, R> implements IType<T> {
    CallBack<R> mCallBack;

    public CallBackProxy(CallBack<R> callBack) {
        mCallBack = callBack;
    }

    public CallBack getCallBack() {
        return mCallBack;
    }

    @Override
    public Type getType() {//CallBack代理方式，获取需要解析的Type
        Type typeArguments = null;
        if (mCallBack != null) {
            Type rawType = mCallBack.getRawType();//如果用户的信息是返回List需单独处理
            Log.d("CallBackProxy","000000 ------ " + rawType + " ------ " + Utils.getClass(rawType, 0));
            if (List.class.isAssignableFrom(Utils.getClass(rawType, 0)) || Map.class.isAssignableFrom(Utils.getClass(rawType, 0))) {
                typeArguments = mCallBack.getType();
                Log.d("CallBackProxy","111111 ------ " + typeArguments);
            //} else if (CacheResult.class.isAssignableFrom(Utils.getClass(rawType, 0))) {
            //    Type type = mCallBack.getType();
            //    typeArguments = Utils.getParameterizedType(type, 0);
            } else {
                Type type = mCallBack.getType();
                //typeArguments = Utils.getClass(type, 0);
                typeArguments = type;
                Log.d("CallBackProxy","222222 ------ " + typeArguments + " ------ type : " + type + "+\n" + " Utils.getClass : " + Utils.getClass(type, 0));
            }
        }
        if (typeArguments == null) {
            typeArguments = ResponseBody.class;
        }
        Type rawType = Utils.findNeedType(getClass());
        if (rawType instanceof ParameterizedType) {
            rawType = ((ParameterizedType) rawType).getRawType();
        }
        Log.d("CallBackProxy","333333 ------ " + rawType + " ------ typeArguments : " + typeArguments);
        return $Gson$Types.newParameterizedTypeWithOwner(null, rawType, typeArguments);
    }
}
