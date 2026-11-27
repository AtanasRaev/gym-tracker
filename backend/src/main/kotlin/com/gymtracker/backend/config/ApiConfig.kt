package com.gymtracker.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestClient

@ConfigurationProperties(prefix = "gym-api")
data class ApiConfig(
    var url: String
) {
    @Bean
    fun genericRestClient(): RestClient =
        RestClient.builder().baseUrl(url).requestFactory(
            org.springframework.http.client.SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(10_000)
                setReadTimeout(60_000)
            }
        ).build()
}