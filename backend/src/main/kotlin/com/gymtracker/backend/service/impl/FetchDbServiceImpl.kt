package com.gymtracker.backend.service.impl

import com.gymtracker.backend.config.ApiConfig
import com.gymtracker.backend.service.api.dto.ExternalExerciseDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponentsBuilder
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue

@Service
class FetchDbServiceImpl(
    private val apiConfig: ApiConfig,
    private val restClient: RestClient,
    private val jsonMapper: JsonMapper
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
            // raw.githubusercontent.com serves JSON as text/plain, so parse the body ourselves.
            val body = restClient.get()
                .uri(uri)
                .retrieve()
                .body(String::class.java)
                ?: return null
            jsonMapper.readValue<List<ExternalExerciseDto>>(body)
        } catch (e: Exception) {
            logger.error("Error fetching exercises. URL={}", uri, e)
            null
        }
    }



}
