# Architecture
Clean Architecture with MVVM pattern in the presentation layer

# Tech Stack
- Modularization.
- 100% [Kotlin](https://kotlinlang.org/)
  based + [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) + [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/)
  for asynchronous.
- [Hilt](https://dagger.dev/hilt/): for dependency injection.
- MVVM with management state
- [Jetpack Compose](https://developer.android.com/jetpack/compose/)
- Room Persistence - construct database to cache data.
- Worker Manager to invalidate cache periodically.
- [Material-Components](https://github.com/material-components/material-components-android) -
  Material design components.

# Testing Libraries
- Junit4
- Mockito
