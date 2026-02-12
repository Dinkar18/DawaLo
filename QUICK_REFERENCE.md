# 🚀 MedNutriTrack - Quick Reference

## Start Backend
```bash
cd /Users/aryadk/AndroidStudioProjects/MedNutriTrack-Backend
mvn spring-boot:run
```
**URL**: http://localhost:8080

## Test API
```bash
./test-api.sh
```

## Run Android
1. Open `/DawaLo` in Android Studio
2. Run (Shift + F10)

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /api/auth/register | No | Register user |
| POST | /api/auth/login | No | Login |
| POST | /api/medicines | Yes | Add medicine |
| GET | /api/medicines | Yes | Get medicines |
| DELETE | /api/medicines/{id} | Yes | Delete medicine |
| POST | /api/nutrition/log | Yes | Log food |
| GET | /api/nutrition/today | Yes | Today's logs |
| GET | /api/nutrition/summary | Yes | Protein/calories |

## Quick Test
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"phone":"9876543210","password":"Test@123","name":"Test","age":25,"gender":"MALE","weight":70,"height":175,"goal":"MUSCLE_GAIN","dietType":"VEGETARIAN","activityLevel":"MODERATELY_ACTIVE","languageCode":"hi"}'

# Login (save token)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"9876543210","password":"Test@123"}'

# Add Medicine (use token from login)
curl -X POST http://localhost:8080/api/medicines \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"name":"Vitamin D","dosage":"1 tablet","frequency":"Daily","times":["09:00:00"],"startDate":"2026-02-11","isActive":true}'
```

## Architecture

**Backend**: Controller → Service → Repository → Database
**Android**: Fragment → ViewModel → UseCase → Repository → (API + Room)

## SOLID Principles
- ✅ Single Responsibility
- ✅ Open/Closed
- ✅ Liskov Substitution
- ✅ Interface Segregation
- ✅ Dependency Inversion

## Design Patterns
- Repository, Factory, Singleton, Observer, Strategy

## Documentation
- `API_ENDPOINTS.md` - API reference
- `API_TESTING_GUIDE.md` - Testing guide
- `ARCHITECTURE.md` - Architecture details
- `FINAL_SUMMARY.md` - Complete overview

## Status
✅ Backend: COMPLETE
✅ Android: COMPLETE
✅ Integration: COMPLETE
✅ Testing: COMPLETE
✅ Documentation: COMPLETE

**READY TO TEST!** 🎉
