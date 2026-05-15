# Introduction
This document is intended for developers who will maintain or further develop the app. This will provide an overview of the project structure, the design patterns utilized and the rationale behind key structural decisions to ensure a consistent coding style.

# Architecture 
This application is structured around a layered MVVM(Model-View-ViewModel) architecture, promoting a clean separation between the data infrastructure and the user interface.
The design and implementation are based on reccomended [Android "best practices"](https://developer.android.com/topic/architecture/recommendations). 

# OO Principles: High Cohesion and Low Coupling
By strictly separating responsibilities into four layers (Presentation, Domain, Data and Models), we achieve high cohesion within components and low coupling between layers.
* High Cohesion: Each component has a single, well-defined responsibilty.
  > Example: `PlantDataSource` class demonstrates high cohesion by focuing exclusively on loading and transforming plant data from a local CSV asset into domain specific `PlantInfo` objects. All methods within the class support this single responsibility, including parsing, normalization and mapping logic.
* Low Coupling: This application demonstrates low coupling through its MVVM-based architecture:
  * The UI layer (for example `LandingPageScreen` and `MapScreen`) only interacts with ViewModels and does not access data sources directly.
  * The ViewModels (for example `LandingPageViewModel`, `MapViewModel`) act as intermediaries and handle application logic while requesting data from repositories.
  > Example: `LandingPageScreen` does not directly access weather APIs or database queries. Instead it collects state from `LandingPageViewModel` which retrieves weather data through `MetWeatherRepository`.

### Unidirectional Data Flow (UDF)
The application strictly implements **Unidirectional Data Flow (UDF)** to manage the relationship between the UI and logic. In this pattern, state flows in only one direction:
* **State flows down:** ViewModels expose application state via `StateFlow` (e.g., `selectedLocation`). Every Composable reads this state to render the interface but never mutates it directly.
* **Events flow up:** When a user interacts with the UI, such as selecting a location or saving a plant, the Composable triggers an "intent" function within the ViewModel.
* **Single Source of Truth:** The ViewModel processes the intent (often updating a Repository or Database) and updates the `StateFlow`. This change automatically triggers a recomposition in Jetpack Compose. This ensures that the UI always mirrors the current data without manual synchronization, significantly reducing bugs related to inconsistent state across different screens.

## API-Levels
* **Minimum SDK: 33
* **Target SDK: 36
Initially, the project aimed for a lower minimum SDK such as 26, but we made the decision to raise it to 33. While a lower SDK would technically support more devices, this decision was made for the implementation of our reverse geocoding logic in the `getAdress` function. Viktoria skriver mer på dette.


## Architecture drawing
```mermaid
  graph TD
  
    MA[MainActivity]
    %% UI LAYER
    subgraph UI ["1. UI & Navigation (Presentation)"]
        direction TB
        Nav[AppNavHost]
        
        subgraph VMs ["ViewModels (State Management)"]
            MPVM[MyPlantsViewModel]
            LVM[LandingPageViewModel]
            MVM[MapViewModel]
            LocVM[LocationsViewModel]
            FVM[FindPlantsViewModel]
            PDVM[PlantDetailViewModel]
        end
        subgraph Screens ["Screens (UI)"]
            LandingS[LandingPageScreen]
            MapS[MapScreen]
            MyS[MyPlantsScreen]
            FindS[FindPlantScreen]
            DetailS[PlantDetailScreen]
        end
        subgraph Components ["UI Components"]
            PIC[PlantInfoCard]
            DTC[DailyTasksCard]
            SLD[SwitchLocationDialog]
            PC[PlantCard]
            SPC[SimplePlantCard]
            WC[WeatherCard]
        end
    end
    %% DOMAIN LAYER
    subgraph Domain ["2. Domain Layer "]
        direction TB
        LocRepo[LocationRepository]
        AiRepo[HybridAiRepository]
        PlantRepo[PlantRepository]
        MetRepo[MetWeatherRepository]
        FrostRepo[FrostRepository]
    end
    %% DATA LAYER
    subgraph Data ["3. Data Layer (Infrastructure)"]
        direction TB
        subgraph Remote ["Remote Data Sources"]
            FrostDS[FrostDataSource]
            PlantDS[PlantDataSource]
            MetClient[MetMcpClient]
            ClaudeApi[ClaudeAPI]
        end
        
        subgraph Local ["Local Data (Room)"]
            DB[Room Database]
            SDao["&laquo;interface&raquo;<br/>SavedLocationDao"]
            PDao["&laquo;interface&raquo;<br/>SavedPlantDao"]
            ConnObs[ConnectivityObserver]
        end
    end
    %% MODELS
    subgraph Models ["4. Models & Data Classes"]
        direction TB
        subgraph Entities ["Room Entities"]
            SL[SavedLocation]
            SP[SavedPlant]
        end
        subgraph AiModels ["AI & Response Models"]
            CReq[ClaudeRequest]
            CRes[ClaudeResponse]
            AR[AiRecommendationResponse]
            PA[PlantAdvice]
            PD[PropertyDefinition]
        end
        subgraph ClientModels ["Client Models (MCP/API)"]
            McpModels[MetMcpDataModels]
            WF[WeatherForecast]
            WA[WeatherAlert]
        end
    end
    %% RELATIONSHIPS
    %% MainActivity Setup
    MA --> Nav
    MA --> LocVM
    MA --> MPVM
    MA --> LVM
    MA --> PDVM
    MA --> ConnObs
    MA --> PlantDS
    %% Navigation Flow
    Nav --> LandingS
    Nav --> MapS
    Nav --> MyS
    Nav --> FindS
    Nav --> DetailS
    Nav --> MPVM
    Nav --> LVM
    %% Screen logic
    LandingS -.-> LVM
    LandingS -.-> MPVM
    LandingS -.-> DTC
    LandingS -.-> PIC
    LandingS -.-> WC
    
    MyS -.-> MPVM
    MyS -.-> PC
    MyS -.-> PIC
    
    MapS -.-> MVM
    
    FindS -.-> FVM
    FindS -.-> SPC
    
    DetailS -.-> PDVM
    %% ViewModel to Domain access
    VMs -.-> Domain
    AiRepo -.-> LocRepo
    AiRepo -.-> MetRepo
    %% Repository to Data Source flow
    PlantRepo --> PlantDS
    LocRepo --> SDao
    LocRepo --> PDao
    FrostRepo --> FrostDS
    MetRepo --> MetClient
    AiRepo --> ClaudeApi
    
    %% Database Interaction
    SDao --> SL
    PDao --> SP
    DB --> SDao
    DB --> PDao
    %% Model & Entity usage
    SL -.-> Domain
    SL -.-> VMs
    SL -.-> PIC
    SL -.-> DTC
    
    McpModels -.-> MetClient
    McpModels -.-> MetRepo
    
    %% Weather Model Flow
    WF -.-> MetRepo
    WA -.-> MetRepo
    WF -.-> LVM
    WA -.-> LVM
    WA -.-> MVM
    
    AiModels -.-> VMs
    ClientModels -.-> VMs
```
## Maintenance and Further Development





