package com.gymtracker.backend.service.impl

import com.gymtracker.backend.config.ApiConfig
import com.gymtracker.backend.service.api.dto.ExternalExerciseDto
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponentsBuilder

@Service
class FetchDbServiceImpl(
    private val apiConfig: ApiConfig,
    private val restClient: RestClient
) {

    companion object {
        private val logger =
            LoggerFactory.getLogger(FetchDbServiceImpl::class.java)
    }

    fun getExerciseDto(): List<ExternalExerciseDto>? {
        val uri = UriComponentsBuilder
            .fromUriString(apiConfig.url)
            .path("/dist/exercises.json")
            .build()
            .toUri()

        return try {
            restClient.get()
                .uri(uri)
                .retrieve()
                .body(
                    object : ParameterizedTypeReference<List<ExternalExerciseDto>>() {}
                )
        } catch (e: Exception) {
            logger.error("Error fetching exercises. URL={}", uri, e)
            null
        }
    }



}
