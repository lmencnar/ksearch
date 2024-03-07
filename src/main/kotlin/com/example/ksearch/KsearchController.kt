package com.example.ksearch

import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.json.JsonData
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayOutputStream
import java.util.ArrayList

inline fun <reified T> T.logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}
@RestController
class KsearchController(private val client: ElasticsearchClientWrapper) {

    val logger = logger()

    @GetMapping("/hello")
    fun hello(): List<String> =
            arrayOf("hello", "world").asList()

    @GetMapping("/search")
    fun search(): List<String> {

        val result = ArrayList<String>()
        val searchRequest = buildRequest("Rome", "London",
            "2010-03-03T23:32:19.000+01:00", "2025-03-03T23:32:19.000+01:00")

        val searchResponse = client.search(searchRequest, JsonData::class.java)

        logger.info(convertToString(searchRequest))

        val iter = searchResponse.hits().hits().listIterator()
        while(iter.hasNext()) {
            iter.next().source()?.toJson()?.asJsonObject()?.let { result.add(it.getString("Carrier")) }
        }

        return result
    }


    private fun convertToString(searchRequest :SearchRequest): String {
        val baos = ByteArrayOutputStream()
        val mapper = JacksonJsonpMapper()
        val generator = mapper.jsonProvider().createGenerator(baos)
        mapper.serialize(searchRequest, generator)
        generator.close()
        return baos.toString()
    }

    private fun buildSimpleRequest(): SearchRequest = SearchRequest.of { s ->
        s
            .index("kibana_sample_data_flights")
            .query { q -> q
                    .matchAll { t-> t
                    }
            }
    }

    private fun buildRequest(originCity :String, destCity :String, startTime :String, endTime :String): SearchRequest = SearchRequest.of { s -> s
        .index("kibana_sample_data_flights")
        .size(100)
        .query { q -> q
            .bool { b -> b
                .must { m -> m
                    .bool { b -> b
                        .must{ s -> s
                            .term { t -> t
                                .field("OriginCityName")
                                .value(originCity)
                            }
                        }
                        .must { s -> s
                            .term { t -> t
                                .field("DestCityName")
                                .value(destCity)
                            }
                        }
                    }
                }
                .must { m -> m
                    .bool { b -> b
                        .must { s -> s
                            .range { r -> r
                                .field("timestamp")
                                .gte(JsonData.of(startTime))
                            }
                        }
                        .must { s -> s
                            .range { r -> r
                                .field("timestamp")
                                .lte(JsonData.of(endTime))
                            }
                        }
                    }
                }
            }
        }
    }
}