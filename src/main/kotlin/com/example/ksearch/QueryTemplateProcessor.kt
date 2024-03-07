package com.example.ksearch

import org.codehaus.jettison.json.JSONArray
import org.codehaus.jettison.json.JSONObject
import java.io.ByteArrayOutputStream

class QueryTemplateProcessor {

    fun replaceParamsInObject(templateObject :JSONObject, templateParams :JSONObject) :JSONObject {
        val finalObject = JSONObject()

        val names = templateObject.names()
        for(i in 0..names.length()-1) {
            val name = names.getString(i)
            var value = templateObject.get(name)
            when(value) {
                is String -> {
                    if(value.startsWith("$")) {
                        val paramName = value.substring(1)
                        if(templateParams.has(paramName)) {
                            value = templateParams.getString(paramName)
                        }
                    }
                    finalObject.put(name, value)
                }
                is JSONObject -> {
                    value = replaceParamsInObject(value, templateParams)
                    finalObject.put(name, value)
                }
                is JSONArray -> {

                }
            }
        }
        return finalObject
    }

    fun loadJsonFromFile(fileName : String) : JSONObject {

        return JSONObject(this::class.java.getResourceAsStream(fileName).use {it ->
            val baos = ByteArrayOutputStream()
            baos.writeBytes(it.readAllBytes())
            baos.toString()
        })
    }
}