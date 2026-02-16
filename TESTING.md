# Kavi-Mobile Testing Guide

## Pre-Testing Checklist

Before testing, ensure:
- [ ] APK installed on Android device
- [ ] All permissions granted
- [ ] Internet connection available
- [ ] Microphone working
- [ ] Device running Android 8.0+

---

## Phase 1: Basic Functionality Tests

### Test 1.1: App Launch
- [ ] App icon appears in app drawer
- [ ] App opens without crashing
- [ ] UI displays correctly
- [ ] No error messages on startup

### Test 1.2: Permission Handling
- [ ] Permission dialog appears on first run
- [ ] All permissions can be granted
- [ ] App handles denied permissions gracefully
- [ ] Can grant permissions later in Settings

### Test 1.3: UI Elements
- [ ] Kavi logo displays
- [ ] Microphone button visible
- [ ] Status text shows "Ready"
- [ ] Command card displays
- [ ] Start Service button visible

---

## Phase 2: Voice Recognition Tests

### Test 2.1: Basic Voice Input
1. Tap microphone button
2. Status should show "Listening..."
3. Progress bar appears
4. Speak clearly: "Hello"
5. **Expected**: Text appears in command card

**Result**: ✅ Pass / ❌ Fail

### Test 2.2: Command Recognition
Test each command:

| Command | Expected Result | Status |
|---------|----------------|--------|
| "Open Chrome" | Chrome launches | ⬜ |
| "Open Instagram" | Instagram launches | ⬜ |
| "Open Camera" | Camera app opens | ⬜ |
| "Take a photo" | Camera app opens | ⬜ |

### Test 2.3: Error Handling
- [ ] No speech: Shows appropriate message
- [ ] Network error: Handles gracefully
- [ ] Background noise: Filters correctly
- [ ] Multiple commands: Processes first one

---

## Phase 3: App Launching Tests

### Test 3.1: Pre-mapped Apps
Test launching these apps:

| App Name | Voice Command | Installed? | Launches? |
|----------|---------------|------------|-----------|
| Instagram | "Open Instagram" | ⬜ | ⬜ |
| WhatsApp | "Open WhatsApp" | ⬜ | ⬜ |
| Chrome | "Open Chrome" | ⬜ | ⬜ |
| YouTube | "Open YouTube" | ⬜ | ⬜ |
| Gmail | "Open Gmail" | ⬜ | ⬜ |
| Maps | "Open Maps" | ⬜ | ⬜ |
| Camera | "Open Camera" | ⬜ | ⬜ |
| Settings | "Open Settings" | ⬜ | ⬜ |

### Test 3.2: Fuzzy Search
- [ ] "Open Insta" → Opens Instagram
- [ ] "Launch Chrome" → Opens Chrome
- [ ] "Start YouTube" → Opens YouTube

### Test 3.3: App Not Found
- [ ] "Open NonExistentApp" → Shows error message
- [ ] Error message is user-friendly

---

## Phase 4: Phone Control Tests

### Test 4.1: Call Functionality
**Prerequisites**: Add test contacts to phone

| Contact | Command | Calls? |
|---------|---------|--------|
| Mom | "Call Mom" | ⬜ |
| Dad | "Call Dad" | ⬜ |
| Friend | "Call [Name]" | ⬜ |

### Test 4.2: Contact Lookup
- [ ] Finds exact match
- [ ] Finds partial match
- [ ] Shows error if contact not found

### Test 4.3: Dialer
- [ ] "Open dialer" → Opens phone app
- [ ] Dialer opens without number

---

## Phase 5: Camera Tests

### Test 5.1: Photo Capture
- [ ] "Take a photo" → Camera opens
- [ ] "Take a picture" → Camera opens
- [ ] Camera app is in photo mode

### Test 5.2: Video Recording
- [ ] "Record a video" → Camera opens
- [ ] "Take a video" → Camera opens
- [ ] Camera app is in video mode

---

## Phase 6: Navigation Tests

### Test 6.1: Location Navigation
| Command | Expected |
|---------|----------|
| "Navigate to home" | Maps opens with "home" |
| "Directions to work" | Maps opens with "work" |
| "Map to Times Square" | Maps opens with location |

### Test 6.2: Maps Integration
- [ ] Google Maps opens
- [ ] Location is pre-filled
- [ ] Can start navigation

---

## Phase 7: Accessibility Service Tests

### Test 7.1: Service Activation
1. Go to Settings → Accessibility
2. Find "Kavi Mobile"
3. Toggle ON
4. **Expected**: Service activates

**Result**: ✅ Pass / ❌ Fail

### Test 7.2: UI Automation
- [ ] Service can read screen content
- [ ] Service can detect UI elements
- [ ] No crashes when service is active

---

## Phase 8: Background Service Tests

### Test 8.1: Service Start
1. Tap "Start Background Service"
2. **Expected**: Notification appears
3. Notification shows "Kavi is listening"

**Result**: ✅ Pass / ❌ Fail

### Test 8.2: Service Persistence
- [ ] Service runs after minimizing app
- [ ] Service survives screen lock
- [ ] Notification remains visible
- [ ] Can tap notification to open app

### Test 8.3: Service Stop
- [ ] Can stop service from notification
- [ ] Service stops when app is force-closed

---

## Phase 9: Battery & Performance Tests

### Test 9.1: Battery Usage
1. Run background service for 1 hour
2. Check battery usage in Settings
3. **Expected**: <5% drain per hour

**Result**: ___% per hour

### Test 9.2: Memory Usage
- [ ] App uses <100MB RAM
- [ ] No memory leaks after extended use
- [ ] App doesn't slow down device

### Test 9.3: Response Time
- [ ] Voice recognition: <2 seconds
- [ ] Command execution: <1 second
- [ ] App launch: <3 seconds

---

## Phase 10: Edge Cases & Error Handling

### Test 10.1: No Internet
- [ ] Voice recognition shows error
- [ ] Error message is clear
- [ ] App doesn't crash

### Test 10.2: Airplane Mode
- [ ] App handles gracefully
- [ ] Shows appropriate message

### Test 10.3: Low Battery
- [ ] App continues to work
- [ ] Background service may be killed (expected)

### Test 10.4: Multiple Rapid Commands
- [ ] Processes commands sequentially
- [ ] Doesn't crash
- [ ] Doesn't skip commands

---

## Phase 11: Real-World Usage Tests

### Test 11.1: Daily Tasks
Perform these tasks using only voice:

- [ ] Open social media app
- [ ] Make a phone call
- [ ] Take a selfie
- [ ] Navigate to a location
- [ ] Set an alarm
- [ ] Search the web

### Test 11.2: Different Environments
Test voice recognition in:

- [ ] Quiet room (ideal)
- [ ] Moderate noise (TV on)
- [ ] Loud environment (music playing)
- [ ] Outdoor (wind, traffic)

### Test 11.3: Different Accents/Voices
- [ ] Clear pronunciation
- [ ] Fast speech
- [ ] Slow speech
- [ ] Different accent

---

## Bug Reporting Template

If you find a bug, document it:

```
Bug ID: #___
Date: ___________
Device: ___________
Android Version: ___________

Steps to Reproduce:
1. 
2. 
3. 

Expected Result:


Actual Result:


Error Messages:


Screenshots: (if applicable)
```

---

## Test Results Summary

### Overall Status
- Total Tests: ___
- Passed: ___
- Failed: ___
- Skipped: ___

### Critical Issues
1. 
2. 
3. 

### Minor Issues
1. 
2. 
3. 

### Recommendations
1. 
2. 
3. 

---

## Sign-Off

**Tester Name**: ___________
**Date**: ___________
**Signature**: ___________

**Status**: ⬜ Ready for Production / ⬜ Needs Fixes / ⬜ Major Issues

---

**Testing Complete! 🎉**
