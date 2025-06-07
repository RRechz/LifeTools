// File: data/GithubRelease.kt
package com.babelsoftware.lifetools.data // veya model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubRelease(
    @SerialName("tag_name")
    val tagName: String, // Örn: "v0.1.0-beta.2"

    @SerialName("assets")
    val assets: List<GithubAsset> = emptyList()
)

@Serializable
data class GithubAsset(
    @SerialName("name")
    val name: String, // Örn: "app-release.apk"

    @SerialName("browser_download_url")
    val downloadUrl: String
)