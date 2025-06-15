package com.shubham.nosignal.utils

/**
 * Utility class for formatting network speed units
 */
object UnitFormatter {
    
    /**
     * Format speed in bytes per second to human readable format
     * @param bytesPerSecond Speed in bytes per second
     * @param useBits Whether to display in bits instead of bytes
     * @return Formatted string with appropriate unit
     */
    fun formatSpeed(bytesPerSecond: Float, useBits: Boolean = false): String {
        val value = if (useBits) bytesPerSecond * 8 else bytesPerSecond
        val unit = if (useBits) "bps" else "B/s"
        
        return when {
            value >= 1_000_000_000 -> String.format("%.2f G%s", value / 1_000_000_000, unit)
            value >= 1_000_000 -> String.format("%.2f M%s", value / 1_000_000, unit)
            value >= 1_000 -> String.format("%.1f K%s", value / 1_000, unit)
            else -> String.format("%.0f %s", value, unit)
        }
    }
    
    /**
     * Format speed in bytes per second to human readable format (Double overload for compatibility)
     * @param bytesPerSecond Speed in bytes per second
     * @param useBits Whether to display in bits instead of bytes
     * @return Formatted string with appropriate unit
     */
    fun formatSpeed(bytesPerSecond: Double, useBits: Boolean = false): String {
        return formatSpeed(bytesPerSecond.toFloat(), useBits)
    }
    
    /**
     * Format data usage in bytes to human readable format
     * @param bytes Data in bytes
     * @return Formatted string with appropriate unit
     */
    fun formatDataUsage(bytes: Double): String {
        return when {
            bytes >= 1_073_741_824 -> String.format("%.2f GB", bytes / 1_073_741_824) // 1024^3
            bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576) // 1024^2
            bytes >= 1_024 -> String.format("%.0f KB", bytes / 1_024)
            else -> String.format("%.0f B", bytes)
        }
    }
    
    /**
     * Convert bytes per second to appropriate scale for graphing
     * @param bytesPerSecond Speed in bytes per second
     * @param useBits Whether to use bits instead of bytes
     * @return Scaled value for graphing
     */
    fun getScaledValue(bytesPerSecond: Double, useBits: Boolean = false): Float {
        val value = if (useBits) bytesPerSecond * 8 else bytesPerSecond
        return (value / 1_000).toFloat() // Convert to KB/s or Kbps for graphing
    }
    
    /**
     * Get the unit label for graph axis
     * @param useBits Whether to use bits instead of bytes
     * @return Unit label string
     */
    fun getGraphUnit(useBits: Boolean = false): String {
        return if (useBits) "Kbps" else "KB/s"
    }
}
