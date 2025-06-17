# Clean Architecture - Room Storage Implementation

This directory contains the complete Room storage layer implementation following Clean Architecture principles.

## 🏗️ Architecture Overview

```
UI Layer (SpeedTestScreen)
    ↓
ViewModel (SpeedTestViewModelNew)
    ↓ 
Repository (SpeedTestRepository)
    ↓
DAO (SpeedTestDao)
    ↓
Entity (SpeedTestResultEntity)
    ↓
Database (AppDatabase)
```

## 📁 File Structure

```
app/src/main/kotlin/com/shubham/nosignal/
├── data/
│   ├── database/
│   │   ├── AppDatabase.kt           # Main Room database
│   │   ├── dao/
│   │   │   ├── SpeedTestDao.kt      # Data access interface
│   │   │   └── SpeedSnapshotDao.kt  # Existing snapshot DAO
│   │   └── entity/
│   │       ├── SpeedTestResultEntity.kt  # Room entity
│   │       └── SpeedSnapshotEntity.kt    # Existing snapshot entity
│   ├── repository/
│   │   └── SpeedTestRepository.kt   # Business logic layer
│   └── mapper/
│       └── SpeedTestMapper.kt       # Entity ↔ Domain mapping
├── domain/
│   └── model/
│       └── SpeedTestResult.kt       # Clean domain model
└── ui/
    ├── SpeedTestViewModelNew.kt     # New Clean Architecture ViewModel
    └── components/
        └── SpeedTestPopup.kt        # Updated UI component
```

## 🔧 Components Explained

### 1. **Entity** (`SpeedTestResultEntity.kt`)
- Room database entity with `@Entity` annotation
- Contains raw data fields exactly as stored in database
- Separate from UI concerns

### 2. **DAO** (`SpeedTestDao.kt`)
- Data Access Object interface
- Defines database operations (insert, query, delete)
- Returns `Flow<>` for reactive UI updates
- Automatically maintains 10-result limit

### 3. **Repository** (`SpeedTestRepository.kt`)
- Business logic layer between ViewModel and DAO
- Handles data operations and business rules
- Provides clean abstraction over database operations

### 4. **Domain Model** (`SpeedTestResult.kt`)
- Pure domain model without Room annotations
- Used in UI layer and business logic
- Contains computed properties for UI display

### 5. **Mapper** (`SpeedTestMapper.kt`)
- Extension functions for converting between layers
- `toEntity()`: Domain → Database
- `toDomain()`: Database → Domain
- Maintains separation of concerns

### 6. **Database** (`AppDatabase.kt`)
- Main Room database singleton
- Version 3 with new entity
- Includes migration strategy

### 7. **ViewModel** (`SpeedTestViewModelNew.kt`)
- Uses repository pattern
- Converts between domain and database models
- Maintains UI state and business logic

## 🚀 Usage Examples

### Storing a Result
```kotlin
val result = SpeedTestResult(
    timestamp = System.currentTimeMillis(),
    networkType = "Wi-Fi",
    ispName = "Comcast",
    downloadBps = 50_000_000.0,
    uploadBps = 25_000_000.0,
    // ... other fields
)

viewModel.storeResult(result)
```

### Observing Results
```kotlin
// In your Composable
val results by viewModel.recentResults.collectAsState(initial = emptyList())
val latestResult by viewModel.latestResult.collectAsState(initial = null)
```

### Repository Operations
```kotlin
// In ViewModel
private val repo = SpeedTestRepository(dao)

// Save with automatic cleanup
suspend fun saveResult(result: SpeedTestResult) {
    repo.saveResult(result.toEntity())
}

// Get reactive data
val results: Flow<List<SpeedTestResult>> = repo.getRecentResults().map { entities ->
    entities.toDomain()
}
```

## 🎯 Key Benefits

1. **Separation of Concerns**: UI, business logic, and data layers are separated
2. **Testability**: Each layer can be unit tested independently
3. **Maintainability**: Changes in one layer don't affect others
4. **Reactive UI**: Automatic UI updates using `Flow` and `StateFlow`
5. **Data Integrity**: Repository ensures consistent business rules
6. **Type Safety**: Compile-time type checking between layers
7. **Clean Code**: Clear responsibilities and single purpose per class

## 📊 Database Schema

### SpeedTestResultEntity Table
```sql
CREATE TABLE speed_test_results_entity (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp INTEGER NOT NULL,
    networkType TEXT NOT NULL,
    ispName TEXT NOT NULL,
    downloadBps REAL NOT NULL,
    uploadBps REAL NOT NULL,
    unloadedLatencyMs REAL NOT NULL,
    loadedDownloadLatencyMs REAL,
    loadedUploadLatencyMs REAL,
    unloadedJitterMs REAL,
    loadedDownloadJitterMs REAL,
    loadedUploadJitterMs REAL,
    packetLossPercent REAL,
    aimScoreStreaming TEXT,
    aimScoreGaming TEXT,
    aimScoreRTC TEXT
);
```

## 🔄 Data Flow

1. **User triggers speed test** → UI Layer
2. **ViewModel processes request** → Business Logic
3. **Repository saves result** → Data Layer  
4. **DAO inserts to database** → Persistence
5. **Database emits change** → Reactive Updates
6. **UI automatically updates** → User sees results

## 🎮 Integration with UI

The SpeedTestPopup component now uses the new Clean Architecture:

```kotlin
SpeedTestPopup(
    showPopup = showPopup,
    onDismiss = { showPopup = false },
    onTestCancel = { showPopup = false },
    onTestDone = { result -> 
        // Automatically saved via viewModel.storeResult()
        showPopup = false 
    },
    isDarkMode = isDarkMode,
    viewModel = speedTestViewModelNew // Uses new ViewModel
)
```

## 🚧 Next Steps

1. **Replace dummy data** with actual Cloudflare API integration
2. **Add ISP detection** logic for real network information
3. **Implement proper error handling** and retry mechanisms
4. **Add data analytics** and reporting features
5. **Optimize database queries** for large datasets

This implementation provides a solid foundation for the speed test feature with clean, maintainable, and testable code that follows Android development best practices. 