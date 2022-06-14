package me.hgj.jetpackmvvm.demo.app.network

import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.hgj.jetpackmvvm.demo.app.network.Utils.Utils
import me.hgj.jetpackmvvm.demo.data.model.bean.ApiResponse
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Author by Ouyangle, Date on 2022/5/7.
 * PS: Not easy to write code, please indicate.
 */
class ApiResultFunc<T> constructor(var type : Type, var jsons : String) {
    var gson: Gson? = null
    init {
        gson = GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
            .registerTypeAdapterFactory(NullStringToEmptyAdapterFactory<T>()) //这里生成Gson时注册自定义处理String NULL值的TypeAdapter
            .serializeNulls()
            .create()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @kotlin.jvm.Throws(Exception::class)
    fun apply(responseBody: ResponseBody): ApiResponse<T>? {
        var apiResult: ApiResponse<T>? = ApiResponse(-1,"", Any() as T)

        if (type is ParameterizedType) { //自定义ApiResult
            val cls: Class<T> = (type as ParameterizedType).rawType as Class<T>
            if (ApiResponse::class.java.isAssignableFrom(cls)) {
                val params = (type as ParameterizedType).actualTypeArguments
                val clazz: Class<T> = Utils.getClass(params[0], 0) as Class<T>
                val rawType: Class<T> = Utils.getClass(type, 0) as Class<T>
                Log.d("ApiResultFunc", "params : $params")
                Log.d("ApiResultFunc", "clazz : ${clazz.name}")
                Log.d("ApiResultFunc", "rawType : ${rawType.name}")
                try {
                    var json = jsons
                    //增加是List<String>判断错误的问题
                    if (!MutableList::class.java.isAssignableFrom(rawType) && clazz == String::class.java) {
                        apiResult?.data = (json as T)
                        apiResult?.errorCode = 0
                        /* final Type type = Utils.getType(cls, 0);
                        ApiResult result = gson.fromJson(json, type);
                        if (result != null) {
                            apiResult = result;
                            apiResult.setData((T) json);
                        } else {
                            apiResult.setMsg("json is null");
                        }*/
                    } else {
                        Log.d("ApiResultFunc","type : " +  type.typeName)
                        //Log.d("ApiResultFunc", "json : $json")
                        val result: ApiResponse<T> = gson?.fromJson(json, type)!!
                        apiResult = result
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    apiResult?.errorMsg = e.message!!
                } finally {
                    responseBody.close()
                }
            } else {
                apiResult?.errorMsg = "ApiResult.class.isAssignableFrom(cls) err!!"
            }
        } else { //默认Apiresult
            try {
                val json = responseBody.string()
                val clazz: Class<T> = Utils.getClass(type, 0) as Class<T>
                if (clazz == String::class.java) {
                    //apiResult.setData((T) json);
                    //apiResult.setCode(0);
                    val result: ApiResponse<T>? = parseApiResult(json, apiResult)
                    if (result != null) {
                        apiResult = result
                        apiResult.data = (json as T)
                    } else {
                        apiResult?.errorMsg = "json is null"
                    }
                } else {
                    val result: ApiResponse<T>? = parseApiResult(json, apiResult)
                    if (result != null) {
                        apiResult = result
                        if (apiResult.data != null) {
                            val data: T = gson?.fromJson(apiResult.data.toString(), clazz)!!
                            apiResult.data = data
                        } else {
                            apiResult.errorMsg = "ApiResult's data is null"
                        }
                    } else {
                        apiResult?.errorMsg = "json is null"
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                apiResult?.errorMsg = e.message!!
            } catch (e: IOException) {
                e.printStackTrace()
                apiResult?.errorMsg =e.message!!
            } finally {
                responseBody.close()
            }
        }
        return apiResult
    }

    @kotlin.jvm.Throws(JSONException::class)
    private fun parseApiResult(json: String, apiResult: ApiResponse<T>?): ApiResponse<T>? {
        if (TextUtils.isEmpty(json)) return null
        val jsonObject = JSONObject(json)
        if (jsonObject.has("code")) {
            apiResult?.errorCode = jsonObject.getInt("code")
        }
        if (jsonObject.has("data")) {
            apiResult?.data = jsonObject.getString("data") as T
        }
        if (jsonObject.has("msg")) {
            apiResult?.errorMsg = jsonObject.getString("msg")
        }
        return apiResult
    }
}