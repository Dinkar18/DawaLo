# Testing & Troubleshooting Guide

## ✅ Permissions Added

The app now requests all necessary permissions:

### 1. Notification Permission (Android 13+)
- Requested on app launch
- Required for showing notifications

### 2. Exact Alarm Permission (Android 12+)
- Requested when adding medicine
- Required for scheduling exact alarms
- Opens system settings

### 3. Foreground Service Permission
- Already in manifest
- Required for continuous alert

## 🧪 Step-by-Step Testing

### Step 1: Install App
```bash
cd /Users/aryadk/AndroidStudioProjects/DawaLo
./gradlew installDebug
```

### Step 2: First Launch - Grant Permissions
1. **Open app**
2. **Notification permission dialog** will appear
   - Tap "Allow"
3. **Exact alarm permission dialog** will appear
   - Tap "Grant"
   - Opens Settings → Alarms & reminders
   - Enable "MedNutriTrack"
   - Go back to app

### Step 3: Setup Profile
1. Enter name: "Test User"
2. Enter weight: "70"
3. Select goal: "Normal"
4. Tap "SAVE"

### Step 4: Add Medicine
1. Tap "MEDICINE" button
2. Tap FAB (+) button
3. Enter:
   - Medicine name: "Test Medicine"
   - Dosage: "1 tablet"
   - Frequency: "Daily"
4. Tap "ADD TIME"
5. **Set time to 1 minute from now**
   - Example: If current time is 16:20, set to 16:21
6. Tap "SAVE"
7. **Check for toast**: "Alarms scheduled for 1 times"

### Step 5: Wait for Alarm
1. **Keep phone unlocked** (for first test)
2. Wait 1 minute
3. **Expected behavior**:
   - Alarm sound starts (continuous)
   - Notification appears with "STOP" button
   - Voice says: "Time to take your medicine: Test Medicine"
   - After 10 seconds, voice repeats
   - Continues until you tap STOP

### Step 6: Stop Alert
1. Pull down notification shade
2. Tap "STOP" button
3. Everything should stop immediately

## 🔍 Troubleshooting

### Issue 1: No Notification Appears

**Check:**
1. Notification permission granted?
   - Settings → Apps → MedNutriTrack → Permissions → Notifications
2. Exact alarm permission granted?
   - Settings → Apps → MedNutriTrack → Alarms & reminders

**Fix:**
- Manually enable both permissions in Settings

### Issue 2: Notification Shows But No Sound/Voice

**Check:**
1. Phone not on silent mode
2. Volume is up
3. Do Not Disturb is OFF

**Check Logs:**
```bash
adb logcat | grep -E "MedicineAlarm|VoiceAlert|AlarmScheduler"
```

**Expected logs:**
```
AlarmScheduler: Alarm scheduled for Test Medicine at 2026-02-11 16:21:00
MedicineAlarmReceiver: Alarm received for: Test Medicine (ID: 1)
MedicineAlarmReceiver: Voice alert service started
VoiceAlertService: Service created
VoiceAlertService: Service started
VoiceAlertService: Starting alert for: Test Medicine
```

### Issue 3: Alarm Doesn't Trigger

**Possible causes:**
1. Exact alarm permission not granted
2. Battery optimization killing app
3. Time set in the past

**Fix:**
1. Grant exact alarm permission
2. Disable battery optimization:
   - Settings → Apps → MedNutriTrack → Battery → Unrestricted
3. Set time in future (at least 1 minute ahead)

### Issue 4: Service Crashes

**Check logs:**
```bash
adb logcat | grep -E "AndroidRuntime|FATAL"
```

**Common issues:**
- TTS not initialized
- MediaPlayer error
- Notification channel not created

## 📱 Testing on Different Android Versions

### Android 13+ (API 33+)
- Must grant notification permission
- Must grant exact alarm permission

### Android 12 (API 31-32)
- Must grant exact alarm permission
- Notification permission automatic

### Android 11 and below (API 30-)
- All permissions automatic
- Should work without dialogs

## 🔊 Testing Voice Alert

### Test Different Languages:
1. Go to Settings (TODO: implement)
2. Change language to Hindi
3. Add new medicine
4. Voice should speak in Hindi

### Test TTS:
1. Check if TTS engine installed:
   - Settings → Language & Input → Text-to-Speech
2. Test TTS:
   - Tap "Listen to an example"
3. If not working:
   - Download TTS data for your language

## 📊 Verify Alarm Scheduled

### Check via ADB:
```bash
adb shell dumpsys alarm | grep "com.dp.dawalo"
```

Should show scheduled alarms with times.

## 🐛 Debug Mode

### Enable verbose logging:
1. Open Android Studio
2. Run app with debugger
3. Check Logcat for:
   - "AlarmScheduler" - alarm scheduling
   - "MedicineAlarmReceiver" - alarm trigger
   - "VoiceAlertService" - service lifecycle

### Key log messages:
```
✅ Alarm scheduled for [Medicine] at [Time]
✅ Alarm received for: [Medicine]
✅ Voice alert service started
✅ Service created
✅ Service started
✅ Starting alert for: [Medicine]
```

## ✅ Success Checklist

- [ ] App installs successfully
- [ ] Notification permission granted
- [ ] Exact alarm permission granted
- [ ] Profile created
- [ ] Medicine added
- [ ] Toast shows "Alarms scheduled"
- [ ] Alarm triggers at scheduled time
- [ ] Notification appears
- [ ] Alarm sound plays continuously
- [ ] Voice speaks medicine name
- [ ] Voice repeats every 10 seconds
- [ ] STOP button works
- [ ] Everything stops when STOP pressed

## 🎯 Quick Test (30 seconds)

1. Install app
2. Grant all permissions
3. Setup profile
4. Add medicine with time = current time + 1 minute
5. Wait 1 minute
6. Should hear alarm + voice
7. Tap STOP

**If this works, everything is functioning correctly!**

## 📞 Still Not Working?

Share these details:
1. Android version
2. Device model
3. Logcat output (filter: MedicineAlarm, VoiceAlert, AlarmScheduler)
4. Screenshot of permissions screen
5. What happens vs what should happen
