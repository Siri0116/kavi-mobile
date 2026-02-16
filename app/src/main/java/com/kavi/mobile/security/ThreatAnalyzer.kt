package com.kavi.mobile.security

import android.content.Context
import com.kavi.mobile.security.detectors.*

/**
 * Threat Analyzer - The Brain of Kavi's Security
 * Aggregates data from all detectors and calculates risk scores.
 */
class ThreatAnalyzer(private val context: Context) {

    private val appScanner = AppScanner(context)
    private val permissionScanner = PermissionScanner(context)
    private val accessibilityWatcher = AccessibilityWatcher(context)
    private val overlayWatcher = OverlayWatcher(context)
    private val micCameraMonitor = MicCameraMonitor(context)
    private val backgroundServiceWatcher = BackgroundServiceWatcher(context)

    data class SecurityReport(
        val threatLevel: ThreatLevel,
        val totalScore: Int,
        val threats: List<DetectedThreat>,
        val summary: String
    )

    data class DetectedThreat(
        val type: ThreatType,
        val severity: ThreatSeverity,
        val description: String,
        val packageName: String?,
        val score: Int
    )

    enum class ThreatLevel {
        SAFE, LOW, MEDIUM, HIGH, CRITICAL
    }

    enum class ThreatSeverity {
        INFO, WARNING, DANGEROUS, CRITICAL
    }

    enum class ThreatType {
        ACCESSIBILITY_ABUSE,
        SPYWARE_COMBO,
        OVERLAY_ATTACK,
        MIC_USAGE,
        SIDELOADED_APP,
        PERSISTENT_SERVICE,
        SYSTEM_SETTING
    }

    /**
     * Run full analysis and generate report
     */
    fun analyze(): SecurityReport {
        val threats = mutableListOf<DetectedThreat>()
        var totalScore = 0

        // 1. Check Accessibility Abuse (Highest Risk)
        val accessibilityRisks = accessibilityWatcher.checkEnabledServices()
        for (risk in accessibilityRisks) {
            threats.add(DetectedThreat(
                type = ThreatType.ACCESSIBILITY_ABUSE,
                severity = ThreatSeverity.CRITICAL,
                description = "Unknown app '${risk.serviceName}' has full control of your screen via Accessibility.",
                packageName = risk.packageName,
                score = 40
            ))
            totalScore += 40
        }

        // 2. Check Risky Permission Combos
        val permissionRisks = permissionScanner.scanForDangerousCombos()
        for ((pkg, desc) in permissionRisks) {
            threats.add(DetectedThreat(
                type = ThreatType.SPYWARE_COMBO,
                severity = ThreatSeverity.DANGEROUS,
                description = desc,
                packageName = pkg,
                score = 30
            ))
            totalScore += 30
        }

        // 3. Check Overlay Permissions
        val overlayApps = overlayWatcher.getAppsWithOverlayPermission()
        for (pkg in overlayApps) {
            // Check if it also has mic (already covered in permission scanner) or just overlay
            // If just overlay, it's medium risk (potential phishing)
            val permissions = permissionScanner.getAppPermissions(pkg)
            if (permissions?.hasMic == false) { // If it has mic, it's already flagged above as High Risk
                threats.add(DetectedThreat(
                    type = ThreatType.OVERLAY_ATTACK,
                    severity = ThreatSeverity.WARNING,
                    description = "App '$pkg' can draw over other apps.",
                    packageName = pkg,
                    score = 20
                ))
                totalScore += 20
            }
        }

        // 4. Check Active Mic Usage
        val micUsage = micCameraMonitor.isMicActive()
        if (micUsage.isActive) {
            val appName = micUsage.packageName ?: "Unknown App"
            threats.add(DetectedThreat(
                type = ThreatType.MIC_USAGE,
                severity = ThreatSeverity.WARNING,
                description = "Microphone is currently active by $appName.",
                packageName = null,
                score = 15
            ))
            totalScore += 15
        }

        // 5. Check Sideloaded Apps (Lower risk, but worth noting)
        val installedApps = appScanner.getInstalledApps()
        for (app in installedApps) {
            // Only check recently installed? Or all? Let's check all for now but give low score
            if (appScanner.isSideloaded(app.packageName) && !app.isSystemApp) {
                threats.add(DetectedThreat(
                    type = ThreatType.SIDELOADED_APP,
                    severity = ThreatSeverity.INFO,
                    description = "App '${app.appName}' was installed from an unknown source.",
                    packageName = app.packageName,
                    score = 10
                ))
                totalScore += 10
            }
        }

        // Calculate Threat Level
        val level = when {
            totalScore >= 60 -> ThreatLevel.CRITICAL
            totalScore >= 40 -> ThreatLevel.HIGH
            totalScore >= 20 -> ThreatLevel.MEDIUM
            totalScore > 0 -> ThreatLevel.LOW
            else -> ThreatLevel.SAFE
        }

        return SecurityReport(
            threatLevel = level,
            totalScore = totalScore,
            threats = threats.sortedByDescending { it.score },
            summary = generateSummary(level, threats)
        )
    }

    private fun generateSummary(level: ThreatLevel, threats: List<DetectedThreat>): String {
        if (threats.isEmpty()) return "Device is secure."
        
        val count = threats.size
        val topThreat = threats.first()
        
        return "Threat Level: $level. Found $count issues. Top concern: ${topThreat.description}"
    }
}
