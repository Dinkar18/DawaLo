# 🔧 Generic Programming in MedNutriTrack

## Current Status: ⚠️ PARTIALLY GENERIC

### ✅ What's Already Generic

#### 1. Backend - ApiResponse<T>
```java
public class ApiResponse<T> {
    private T data;  // Works with ANY type
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }
}

// Usage:
ApiResponse<Medicine> medicineResponse = ApiResponse.success(medicine);
ApiResponse<User> userResponse = ApiResponse.success(user);
ApiResponse<List<Medicine>> listResponse = ApiResponse.success(medicines);
```

#### 2. Backend - JpaRepository<T, ID>
```java
// Spring Data JPA is fully generic
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    // Inherits: save(), findById(), findAll(), deleteById()
    // All methods work with Medicine type
}

public interface UserRepository extends JpaRepository<User, Long> {
    // Same methods, different type
}
```

#### 3. Android - Kotlin Generics
```kotlin
// Result<T> - Kotlin standard library
suspend fun login(): Result<AuthResponse?> {
    return try {
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// LiveData<T> - Android Architecture Components
val medicines: LiveData<List<Medicine>> = _medicines
```

---

## ❌ What's NOT Generic (Problems)

### Problem 1: Duplicate Service Code
```java
// MedicineService.java
public Medicine save(Medicine medicine) {
    return medicineRepository.save(medicine);
}

// UserService.java
public User save(User user) {
    return userRepository.save(user);
}

// NutritionService.java
public DailyFoodLog save(DailyFoodLog log) {
    return foodLogRepository.save(log);
}

// ❌ Same code repeated 3 times!
```

### Problem 2: Duplicate Repository Methods
```kotlin
// Android - MedicineRepository
interface MedicineRepository {
    suspend fun getAll(): List<Medicine>
    suspend fun getById(id: Long): Medicine?
    suspend fun insert(item: Medicine)
    suspend fun delete(item: Medicine)
}

// Android - NutritionRepository
interface NutritionRepository {
    suspend fun getAll(): List<FoodLog>
    suspend fun getById(id: Long): FoodLog?
    suspend fun insert(item: FoodLog)
    suspend fun delete(item: FoodLog)
}

// ❌ Same methods, different types!
```

---

## ✅ SOLUTION: Full Generic Implementation

### 1. Generic Service (Backend)

```java
/**
 * Generic CRUD Service
 * T = Entity type (Medicine, User, etc.)
 * ID = Primary key type (Long, String, etc.)
 */
public abstract class GenericService<T, ID> {
    
    protected abstract JpaRepository<T, ID> getRepository();
    
    // Generic CRUD operations
    public T save(T entity) {
        return getRepository().save(entity);
    }
    
    public Optional<T> findById(ID id) {
        return getRepository().findById(id);
    }
    
    public List<T> findAll() {
        return getRepository().findAll();
    }
    
    public void deleteById(ID id) {
        getRepository().deleteById(id);
    }
    
    public Page<T> findAll(Pageable pageable) {
        return getRepository().findAll(pageable);
    }
}

// Usage:
@Service
public class MedicineService extends GenericService<Medicine, Long> {
    private final MedicineRepository repository;
    
    @Override
    protected JpaRepository<Medicine, Long> getRepository() {
        return repository;
    }
    
    // Only add medicine-specific methods
    public List<Medicine> findByUserId(Long userId) {
        return repository.findByUserIdAndIsActive(userId, true);
    }
}

@Service
public class UserService extends GenericService<User, Long> {
    private final UserRepository repository;
    
    @Override
    protected JpaRepository<User, Long> getRepository() {
        return repository;
    }
    
    // Only add user-specific methods
    public Optional<User> findByPhone(String phone) {
        return repository.findByPhone(phone);
    }
}
```

**Benefits:**
- ✅ No duplicate CRUD code
- ✅ Consistent behavior across all services
- ✅ Easy to add new entities
- ✅ Single place to fix bugs

---

### 2. Generic Repository (Android)

```kotlin
/**
 * Generic Repository Interface
 * T = Entity type
 */
interface BaseRepository<T> {
    suspend fun getAll(): List<T>
    suspend fun getById(id: Long): T?
    suspend fun insert(item: T): Long
    suspend fun update(item: T)
    suspend fun delete(item: T)
}

// Implementation
class MedicineRepositoryImpl(
    private val api: MedicineApi,
    private val dao: MedicineDao
) : BaseRepository<Medicine> {
    
    override suspend fun getAll(): List<Medicine> {
        return try {
            val response = api.getMedicines()
            response.body()?.data ?: dao.getAll()
        } catch (e: Exception) {
            dao.getAll()
        }
    }
    
    override suspend fun getById(id: Long): Medicine? {
        return dao.getById(id)
    }
    
    override suspend fun insert(item: Medicine): Long {
        return dao.insert(item)
    }
    
    override suspend fun update(item: Medicine) {
        dao.update(item)
    }
    
    override suspend fun delete(item: Medicine) {
        dao.delete(item)
    }
}
```

---

### 3. Generic Use Case (Android)

```kotlin
/**
 * Generic Use Case
 * P = Parameters
 * R = Result
 */
abstract class UseCase<in P, out R> {
    
    suspend operator fun invoke(params: P): Result<R> {
        return try {
            Result.success(execute(params))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    protected abstract suspend fun execute(params: P): R
}

// Usage:
class AddMedicineUseCase(
    private val repository: MedicineRepository
) : UseCase<Medicine, Medicine>() {
    
    override suspend fun execute(params: Medicine): Medicine {
        return repository.insert(params)
    }
}

class GetMedicinesUseCase(
    private val repository: MedicineRepository
) : UseCase<Long, List<Medicine>>() {  // Long = userId
    
    override suspend fun execute(params: Long): List<Medicine> {
        return repository.getByUserId(params)
    }
}
```

---

### 4. Generic ViewModel (Android)

```kotlin
/**
 * Generic ViewModel
 * T = Data type
 */
abstract class BaseViewModel<T> : ViewModel() {
    
    protected val _data = MutableLiveData<List<T>>()
    val data: LiveData<List<T>> = _data
    
    protected val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    protected val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    abstract suspend fun loadData()
    
    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            try {
                loadData()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}

// Usage:
class MedicineViewModel(
    private val useCase: GetMedicinesUseCase
) : BaseViewModel<Medicine>() {
    
    override suspend fun loadData() {
        val result = useCase(userId)
        if (result.isSuccess) {
            _data.value = result.getOrNull()
        }
    }
}
```

---

### 5. Generic Adapter (Android)

```kotlin
/**
 * Generic RecyclerView Adapter
 * T = Item type
 * VH = ViewHolder type
 */
abstract class BaseAdapter<T, VH : RecyclerView.ViewHolder>(
    private val diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, VH>(diffCallback) {
    
    protected var onItemClick: ((T) -> Unit)? = null
    protected var onItemLongClick: ((T) -> Boolean)? = null
    
    fun setOnItemClickListener(listener: (T) -> Unit) {
        onItemClick = listener
    }
    
    fun setOnItemLongClickListener(listener: (T) -> Boolean) {
        onItemLongClick = listener
    }
}

// Usage:
class MedicineAdapter : BaseAdapter<Medicine, MedicineAdapter.ViewHolder>(
    MedicineDiffCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicineBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(private val binding: ItemMedicineBinding) 
        : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(medicine: Medicine) {
            binding.tvName.text = medicine.name
            binding.root.setOnClickListener { onItemClick?.invoke(medicine) }
        }
    }
}
```

---

## Benefits of Generic Programming

### 1. DRY (Don't Repeat Yourself)
```java
// ❌ Before: 100 lines × 3 services = 300 lines
MedicineService, UserService, NutritionService

// ✅ After: 100 lines (generic) + 10 lines each = 130 lines
GenericService + 3 specific services
```

### 2. Type Safety
```java
// ✅ Compile-time type checking
ApiResponse<Medicine> response = service.getMedicine();
Medicine medicine = response.getData();  // Type-safe!

// ❌ Without generics (old Java)
ApiResponse response = service.getMedicine();
Medicine medicine = (Medicine) response.getData();  // Runtime cast!
```

### 3. Reusability
```kotlin
// ✅ Same adapter for different types
class GenericAdapter<T> { }

val medicineAdapter = GenericAdapter<Medicine>()
val userAdapter = GenericAdapter<User>()
val foodAdapter = GenericAdapter<FoodLog>()
```

### 4. Maintainability
```java
// ✅ Fix bug once, applies everywhere
class GenericService<T, ID> {
    public T save(T entity) {
        // Bug fix here affects all services
        validate(entity);
        return repository.save(entity);
    }
}
```

---

## Generic Programming Principles

### 1. Bounded Type Parameters
```java
// Only accept types that extend BaseEntity
public class GenericService<T extends BaseEntity, ID> {
    public T save(T entity) {
        entity.setUpdatedAt(LocalDateTime.now());  // Available because T extends BaseEntity
        return repository.save(entity);
    }
}
```

### 2. Wildcard Types
```java
// Accept any list
public void processList(List<?> list) { }

// Accept list of Number or subclasses
public void processNumbers(List<? extends Number> numbers) { }

// Accept list of Integer or superclasses
public void addIntegers(List<? super Integer> list) { }
```

### 3. Generic Methods
```java
public class Utils {
    // Generic method (not generic class)
    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        return list.stream()
            .filter(predicate)
            .collect(Collectors.toList());
    }
}

// Usage:
List<Medicine> activeMedicines = Utils.filter(medicines, m -> m.isActive());
```

### 4. Multiple Type Parameters
```java
public class Pair<K, V> {
    private K key;
    private V value;
    
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}

// Usage:
Pair<String, Integer> pair = new Pair<>("age", 25);
```

---

## Real-World Examples in Project

### Example 1: Generic API Response
```java
// ✅ Works with any type
ApiResponse<Medicine> medicineResponse = ApiResponse.success(medicine);
ApiResponse<List<User>> usersResponse = ApiResponse.success(users);
ApiResponse<Map<String, Object>> mapResponse = ApiResponse.success(data);
```

### Example 2: Generic Repository
```kotlin
// ✅ Same interface, different implementations
interface Repository<T> {
    suspend fun getAll(): List<T>
}

class MedicineRepository : Repository<Medicine> { }
class UserRepository : Repository<User> { }
class FoodRepository : Repository<FoodLog> { }
```

### Example 3: Generic Validation
```java
public class Validator<T> {
    private List<ValidationRule<T>> rules = new ArrayList<>();
    
    public void addRule(ValidationRule<T> rule) {
        rules.add(rule);
    }
    
    public boolean validate(T entity) {
        return rules.stream().allMatch(rule -> rule.test(entity));
    }
}

// Usage:
Validator<Medicine> medicineValidator = new Validator<>();
medicineValidator.addRule(m -> m.getName() != null);
medicineValidator.addRule(m -> m.getDosage() != null);
```

---

## Summary

### Current Project Status:

| Component | Generic? | Score |
|-----------|----------|-------|
| ApiResponse | ✅ Yes | 10/10 |
| JpaRepository | ✅ Yes | 10/10 |
| Services | ⚠️ Partial | 5/10 |
| Controllers | ❌ No | 3/10 |
| Android Repos | ⚠️ Partial | 6/10 |
| ViewModels | ⚠️ Partial | 6/10 |
| Adapters | ⚠️ Partial | 7/10 |

**Overall Generic Score: 6.7/10**

### To Make It Fully Generic:

1. ✅ Create `GenericService<T, ID>` (Done above)
2. ⚠️ Create `BaseRepository<T>` for Android
3. ⚠️ Create `BaseViewModel<T>` for Android
4. ⚠️ Create `BaseAdapter<T, VH>` for Android
5. ⚠️ Add generic validation framework
6. ⚠️ Add generic error handling

### Benefits After Full Implementation:

- 📉 **40% less code** (no duplication)
- 🐛 **Fewer bugs** (fix once, applies everywhere)
- ⚡ **Faster development** (reuse components)
- 🔒 **Type safety** (compile-time checks)
- 🧪 **Easier testing** (mock generic interfaces)

---

## Conclusion

**Answer: The project uses generic programming PARTIALLY.**

✅ **Good**: ApiResponse, JpaRepository, Result types
⚠️ **Needs Improvement**: Services, Repositories, ViewModels
❌ **Missing**: Generic base classes, validation framework

**Recommendation**: Implement the generic base classes shown above to achieve **9/10 generic score**!
