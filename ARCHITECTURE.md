# 🏗️ MedNutriTrack - Clean Architecture & OOP Design

## Architecture Overview

### System Architecture (3-Tier)

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Android    │  │   Web App    │  │  iOS App     │     │
│  │   (Kotlin)   │  │   (React)    │  │  (Swift)     │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
└─────────┼──────────────────┼──────────────────┼────────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │ REST API (JSON)
┌─────────────────────────────▼────────────────────────────────┐
│                    BUSINESS LOGIC LAYER                       │
│  ┌────────────────────────────────────────────────────────┐ │
│  │           Spring Boot Application                      │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐ │ │
│  │  │ Controllers  │  │  Services    │  │ Repositories│ │ │
│  │  │ (REST API)   │→ │ (Business)   │→ │   (Data)    │ │ │
│  │  └──────────────┘  └──────────────┘  └─────────────┘ │ │
│  │  ┌──────────────┐  ┌──────────────┐                  │ │
│  │  │   Security   │  │     DTOs     │                  │ │
│  │  │  (JWT/Auth)  │  │  (Transfer)  │                  │ │
│  │  └──────────────┘  └──────────────┘                  │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────▼────────────────────────────────┘
                              │ JPA/Hibernate
┌─────────────────────────────▼────────────────────────────────┐
│                      DATA LAYER                               │
│  ┌────────────────────────────────────────────────────────┐ │
│  │              PostgreSQL Database                       │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────────────┐   │ │
│  │  │  users   │  │medicines │  │ daily_food_logs  │   │ │
│  │  └──────────┘  └──────────┘  └──────────────────┘   │ │
│  └────────────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────────────────┘
```

---

## SOLID Principles Implementation

### 1. Single Responsibility Principle (SRP)

**Each class has ONE reason to change**

#### Backend Example:
```java
// ❌ BAD: Multiple responsibilities
class UserService {
    void register(User user) { /* validation + save + email + logging */ }
}

// ✅ GOOD: Single responsibility
class UserService {
    void register(User user) { /* only business logic */ }
}

class UserValidator {
    void validate(User user) { /* only validation */ }
}

class EmailService {
    void sendWelcomeEmail(User user) { /* only email */ }
}
```

#### Android Example:
```kotlin
// ❌ BAD: Fragment doing everything
class MedicineFragment {
    fun addMedicine() { /* UI + validation + API + database */ }
}

// ✅ GOOD: Separated concerns
class MedicineFragment {  // Only UI
    fun addMedicine() { viewModel.addMedicine() }
}

class MedicineViewModel {  // Only presentation logic
    fun addMedicine() { useCase.execute() }
}

class AddMedicineUseCase {  // Only business logic
    suspend fun execute() { repository.save() }
}

class MedicineRepository {  // Only data access
    suspend fun save() { /* API + Room */ }
}
```

---

### 2. Open/Closed Principle (OCP)

**Open for extension, closed for modification**

#### Backend Example:
```java
// ✅ GOOD: Can add new calculators without modifying existing code
interface HealthCalculator {
    float calculate(User user);
}

class BMRCalculator implements HealthCalculator {
    float calculate(User user) { /* BMR formula */ }
}

class TDEECalculator implements HealthCalculator {
    float calculate(User user) { /* TDEE formula */ }
}

class BMICalculator implements HealthCalculator {
    float calculate(User user) { /* BMI formula */ }
}

// Add new calculator without changing existing code
class BodyFatCalculator implements HealthCalculator {
    float calculate(User user) { /* Body fat formula */ }
}
```

---

### 3. Liskov Substitution Principle (LSP)

**Subtypes must be substitutable for their base types**

#### Backend Example:
```java
// ✅ GOOD: All implementations can replace the interface
interface NotificationSender {
    void send(String message, User user);
}

class EmailNotification implements NotificationSender {
    void send(String message, User user) { /* send email */ }
}

class SMSNotification implements NotificationSender {
    void send(String message, User user) { /* send SMS */ }
}

class PushNotification implements NotificationSender {
    void send(String message, User user) { /* send push */ }
}

// Can use any implementation
NotificationSender sender = new EmailNotification();
sender.send("Welcome!", user);  // Works

sender = new SMSNotification();
sender.send("Welcome!", user);  // Also works
```

---

### 4. Interface Segregation Principle (ISP)

**Clients shouldn't depend on interfaces they don't use**

#### Backend Example:
```java
// ❌ BAD: Fat interface
interface UserOperations {
    void register();
    void login();
    void updateProfile();
    void deleteAccount();
    void exportData();
    void importData();
}

// ✅ GOOD: Segregated interfaces
interface Authenticatable {
    void register();
    void login();
}

interface ProfileManageable {
    void updateProfile();
    void deleteAccount();
}

interface DataPortable {
    void exportData();
    void importData();
}

// Classes implement only what they need
class AuthService implements Authenticatable { }
class ProfileService implements ProfileManageable { }
class DataService implements DataPortable { }
```

---

### 5. Dependency Inversion Principle (DIP)

**Depend on abstractions, not concretions**

#### Backend Example:
```java
// ❌ BAD: Depends on concrete class
class MedicineService {
    private PostgreSQLRepository repository = new PostgreSQLRepository();
}

// ✅ GOOD: Depends on abstraction
class MedicineService {
    private final MedicineRepository repository;  // Interface
    
    public MedicineService(MedicineRepository repository) {
        this.repository = repository;  // Injected
    }
}

// Can swap implementations
MedicineRepository repo = new PostgreSQLRepository();
// OR
MedicineRepository repo = new MongoDBRepository();
// OR
MedicineRepository repo = new InMemoryRepository();  // For testing
```

---

## Design Patterns Used

### 1. Repository Pattern

**Abstracts data access logic**

```kotlin
// Android
interface MedicineRepository {
    suspend fun getMedicines(): List<Medicine>
    suspend fun addMedicine(medicine: Medicine)
    suspend fun deleteMedicine(id: Long)
}

class MedicineRepositoryImpl(
    private val api: MedicineApi,
    private val dao: MedicineDao
) : MedicineRepository {
    
    override suspend fun getMedicines(): List<Medicine> {
        // Try API first
        return try {
            val response = api.getMedicines()
            if (response.isSuccessful) {
                val medicines = response.body()?.data ?: emptyList()
                dao.insertAll(medicines)  // Cache
                medicines
            } else {
                dao.getAll()  // Fallback to cache
            }
        } catch (e: Exception) {
            dao.getAll()  // Offline mode
        }
    }
}
```

### 2. Factory Pattern

**Creates objects without specifying exact class**

```java
// Backend
public class CalculatorFactory {
    public static HealthCalculator getCalculator(String type) {
        return switch (type) {
            case "BMR" -> new BMRCalculator();
            case "TDEE" -> new TDEECalculator();
            case "BMI" -> new BMICalculator();
            default -> throw new IllegalArgumentException("Unknown type");
        };
    }
}
```

### 3. Singleton Pattern

**Ensures only one instance exists**

```kotlin
// Android
object RetrofitClient {
    private var retrofit: Retrofit? = null
    
    fun getInstance(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .build()
        }
        return retrofit!!
    }
}
```

### 4. Observer Pattern

**LiveData/Flow for reactive updates**

```kotlin
// Android
class MedicineViewModel : ViewModel() {
    private val _medicines = MutableLiveData<List<Medicine>>()
    val medicines: LiveData<List<Medicine>> = _medicines
    
    fun loadMedicines() {
        viewModelScope.launch {
            _medicines.value = repository.getMedicines()
        }
    }
}

// Fragment observes
viewModel.medicines.observe(viewLifecycleOwner) { medicines ->
    adapter.submitList(medicines)  // UI updates automatically
}
```

### 5. Strategy Pattern

**Selects algorithm at runtime**

```java
// Backend
interface ProteinCalculationStrategy {
    float calculate(float weight, Goal goal);
}

class WeightLossStrategy implements ProteinCalculationStrategy {
    float calculate(float weight, Goal goal) {
        return weight * 1.2f;
    }
}

class MuscleGainStrategy implements ProteinCalculationStrategy {
    float calculate(float weight, Goal goal) {
        return weight * 1.6f;
    }
}

class ProteinCalculator {
    private ProteinCalculationStrategy strategy;
    
    void setStrategy(ProteinCalculationStrategy strategy) {
        this.strategy = strategy;
    }
    
    float calculate(User user) {
        return strategy.calculate(user.getWeight(), user.getGoal());
    }
}
```

---

## Layer Responsibilities

### Backend Layers

#### 1. Controller Layer
- **Responsibility**: Handle HTTP requests/responses
- **Should**: Validate input, call services, return DTOs
- **Should NOT**: Business logic, database access

```java
@RestController
@RequestMapping("/api/medicines")
public class MedicineController {
    private final MedicineService service;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Medicine>> add(@RequestBody Medicine medicine, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Medicine saved = service.addMedicine(userId, medicine);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }
}
```

#### 2. Service Layer
- **Responsibility**: Business logic
- **Should**: Validate business rules, orchestrate operations
- **Should NOT**: HTTP concerns, SQL queries

```java
@Service
public class MedicineService {
    private final MedicineRepository repository;
    private final UserRepository userRepository;
    
    public Medicine addMedicine(Long userId, Medicine medicine) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        medicine.setUser(user);
        return repository.save(medicine);
    }
}
```

#### 3. Repository Layer
- **Responsibility**: Data access
- **Should**: CRUD operations, queries
- **Should NOT**: Business logic

```java
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByUserIdAndIsActive(Long userId, Boolean isActive);
}
```

---

### Android Layers

#### 1. Presentation Layer (UI)
- **Fragments/Activities**: Display data, handle user input
- **ViewModels**: Presentation logic, state management

```kotlin
class MedicineFragment : Fragment() {
    private val viewModel: MedicineViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.medicines.observe(viewLifecycleOwner) { medicines ->
            adapter.submitList(medicines)
        }
        
        binding.fabAdd.setOnClickListener {
            showAddDialog()
        }
    }
}
```

#### 2. Domain Layer (Business Logic)
- **Use Cases**: Single business operation
- **Models**: Business entities

```kotlin
class AddMedicineUseCase(private val repository: MedicineRepository) {
    suspend operator fun invoke(medicine: Medicine): Result<Medicine> {
        return try {
            val saved = repository.addMedicine(medicine)
            Result.success(saved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### 3. Data Layer
- **Repositories**: Abstract data sources
- **Data Sources**: API, Database, Cache

```kotlin
class MedicineRepositoryImpl(
    private val remoteDataSource: MedicineApi,
    private val localDataSource: MedicineDao
) : MedicineRepository {
    
    override suspend fun addMedicine(medicine: Medicine): Medicine {
        // Save to API
        val response = remoteDataSource.addMedicine(medicine)
        val saved = response.body()?.data ?: throw Exception("Failed")
        
        // Cache locally
        localDataSource.insert(saved)
        
        return saved
    }
}
```

---

## OOP Concepts Applied

### 1. Encapsulation

**Hide internal details, expose only necessary**

```java
// Backend
@Entity
public class User {
    @Id
    private Long id;
    
    private String password;  // Private!
    
    // No getter for password
    public void setPassword(String password) {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());  // Hashed
    }
    
    public boolean checkPassword(String plainPassword) {
        return BCrypt.checkpw(plainPassword, this.password);
    }
}
```

### 2. Inheritance

**Reuse common behavior**

```kotlin
// Android
abstract class BaseFragment : Fragment() {
    protected lateinit var prefs: PreferenceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager(requireContext())
    }
    
    protected fun showLoading() { /* common loading */ }
    protected fun hideLoading() { /* common hide */ }
}

class MedicineFragment : BaseFragment() {
    // Inherits prefs, showLoading, hideLoading
}
```

### 3. Polymorphism

**Same interface, different implementations**

```java
// Backend
interface NotificationService {
    void notify(User user, String message);
}

class EmailNotificationService implements NotificationService {
    void notify(User user, String message) {
        // Send email
    }
}

class SMSNotificationService implements NotificationService {
    void notify(User user, String message) {
        // Send SMS
    }
}

// Use polymorphically
NotificationService service = getNotificationService(user.getPreference());
service.notify(user, "Medicine reminder");  // Calls correct implementation
```

### 4. Abstraction

**Hide complexity, show only essentials**

```kotlin
// Android
interface MedicineRepository {
    suspend fun getMedicines(): List<Medicine>
}

// Implementation hidden from ViewModel
class MedicineRepositoryImpl : MedicineRepository {
    override suspend fun getMedicines(): List<Medicine> {
        // Complex logic: API call, caching, error handling
        // ViewModel doesn't need to know
    }
}
```

---

## Testing Strategy

### Backend Tests

```java
@SpringBootTest
class MedicineServiceTest {
    
    @Mock
    private MedicineRepository repository;
    
    @InjectMocks
    private MedicineService service;
    
    @Test
    void testAddMedicine() {
        // Arrange
        Medicine medicine = new Medicine();
        when(repository.save(any())).thenReturn(medicine);
        
        // Act
        Medicine result = service.addMedicine(1L, medicine);
        
        // Assert
        assertNotNull(result);
        verify(repository, times(1)).save(medicine);
    }
}
```

### Android Tests

```kotlin
@Test
fun `addMedicine should save to repository`() = runTest {
    // Arrange
    val medicine = Medicine(name = "Test")
    val repository = mockk<MedicineRepository>()
    coEvery { repository.addMedicine(medicine) } returns medicine
    
    val useCase = AddMedicineUseCase(repository)
    
    // Act
    val result = useCase(medicine)
    
    // Assert
    assertTrue(result.isSuccess)
    coVerify { repository.addMedicine(medicine) }
}
```

---

## Benefits of This Architecture

### 1. Maintainability
- Easy to find and fix bugs
- Clear separation of concerns
- Each class has single purpose

### 2. Testability
- Can mock dependencies
- Test each layer independently
- Fast unit tests

### 3. Scalability
- Easy to add new features
- Can swap implementations
- Parallel development possible

### 4. Flexibility
- Change database without affecting business logic
- Change UI without affecting data layer
- Add new platforms (iOS, Web) easily

### 5. Reusability
- Use cases can be shared
- Repositories can be reused
- DTOs can be shared between platforms

---

## Code Quality Metrics

### Backend
- **Cyclomatic Complexity**: < 10 per method
- **Class Size**: < 300 lines
- **Method Size**: < 50 lines
- **Test Coverage**: > 80%

### Android
- **ViewModel**: No Android framework dependencies
- **Fragment**: < 200 lines
- **Use Case**: Single public method
- **Repository**: Interface + Implementation

---

## Future Improvements

1. **Add Caching Layer**: Redis for API responses
2. **Implement CQRS**: Separate read/write models
3. **Add Event Sourcing**: Track all state changes
4. **Microservices**: Split into Auth, Medicine, Nutrition services
5. **GraphQL**: Alternative to REST for flexible queries

---

## Summary

This architecture follows:
- ✅ SOLID principles
- ✅ Clean Architecture
- ✅ Design Patterns
- ✅ OOP best practices
- ✅ Separation of Concerns
- ✅ Dependency Injection
- ✅ Testability
- ✅ Maintainability

**Result**: Production-ready, scalable, maintainable codebase!
