package com.gymtracker.backend.service.impl

import com.cloudinary.Cloudinary
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
class ExerciseImageUploader(private val environment: Environment) {
    private val client by lazy {
        Cloudinary(mapOf(
            "cloud_name" to environment.getRequiredProperty("CLOUDINARY_CLOUD_NAME"),
            "api_key" to environment.getRequiredProperty("CLOUDINARY_API_KEY"),
            "api_secret" to environment.getRequiredProperty("CLOUDINARY_API_SECRET"),
            "secure" to true
        ))
    }

    fun upload(url: String, publicId: String): String {
        val existing = try {
            client.api().resource(publicId, emptyMap<String, Any>())
        } catch (e: com.cloudinary.api.exceptions.NotFound) {
            null
        }
        if (existing != null) {
            val existingUrl = existing["secure_url"] as? String
            require(existing["public_id"] == publicId && existingUrl?.startsWith("https://") == true)
            return existingUrl
        }
        val result = client.uploader().upload(url, mapOf(
            "public_id" to publicId, "overwrite" to false,
            "unique_filename" to false, "resource_type" to "image"
        ))
        val secureUrl = result["secure_url"] as? String
        require(result["public_id"] == publicId && secureUrl?.startsWith("https://") == true) {
            "Invalid Cloudinary upload response"
        }
        return secureUrl
    }
}
