package com.example.frontend_happygreen.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for date formatting
 */
object DateUtils {

    /**
     * Formats a date string from ISO format to a user-friendly format
     * @param dateString The date string in ISO format (nullable)
     * @return A formatted date string or "Data non disponibile" if null/empty
     */
    fun formatDate(dateString: String?): String {
        if (dateString == null || dateString.isEmpty()) {
            return "Data non disponibile"
        }

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)

            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Gets the current date/time as an ISO formatted string
     * @return Current date/time in ISO format
     */
    fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(Date())
    }

    /**
     * Formats a date string for comments (shows relative time)
     * @param dateString The date string in ISO format
     * @return A formatted relative time string (e.g. "2h", "3d")
     */
    fun formatCommentDate(dateString: String?): String {
        if (dateString == null || dateString.isEmpty()) {
            return "Ora"
        }

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString) ?: return dateString

            val now = Date()
            val diffInMillis = now.time - date.time
            val diffInMinutes = diffInMillis / (1000 * 60)
            val diffInHours = diffInMinutes / 60
            val diffInDays = diffInHours / 24

            when {
                diffInMinutes < 1 -> "Ora"
                diffInMinutes < 60 -> "${diffInMinutes}m"
                diffInHours < 24 -> "${diffInHours}h"
                diffInDays < 7 -> "${diffInDays}g"
                else -> {
                    val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                    outputFormat.format(date)
                }
            }
        } catch (e: Exception) {
            "Ora"
        }
    }
}