package com.babelsoftware.lifetools.model

import com.babelsoftware.lifetools.R

enum class AppLanguage(val code: String, val displayNameResId: Int) {
    TURKISH("tr", R.string.language_turkish),
    ENGLISH("en", R.string.language_english);

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return values().find { it.code == code } ?: TURKISH
        }
    }
}