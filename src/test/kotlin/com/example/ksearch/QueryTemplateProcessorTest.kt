package com.example.ksearch

import org.junit.jupiter.api.Test

class QueryTemplateProcessorTest {

    @Test
    fun loadJsonFromFile() {
        QueryTemplateProcessor().loadJsonFromFile("/queries/searchFlightsFromTo.json")
    }

    @Test
    fun replaceParamsInObject() {
        val templateObject = QueryTemplateProcessor().loadJsonFromFile("/queries/searchFlightsFromTo.json")
        val paramsObject = QueryTemplateProcessor().loadJsonFromFile("/queries/searchFlightsFromToParams.json")
        val queryObject = QueryTemplateProcessor().replaceParamsInObject(templateObject, paramsObject)
        queryObject.names()
    }
}