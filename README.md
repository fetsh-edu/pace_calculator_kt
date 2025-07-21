# Pace Calculator 2

![Android CI](https://github.com/fetsh-edu/pace_calculator_kt/actions/workflows/ci.yml/badge.svg)

A simple pace/speed/time/distance calculator Android application, rebuilt from the ground up as an educational exercise in building an application using the latest recommended tools and architectures.

*(Screenshot will go here later)*

---

## Project Goals

- **Modern Android Development:** Utilize the latest stack recommended by Google.
- **Clean Architecture & MVI:** Implement a strict Model-View-Intent architecture based on The Elm Architecture (TEA), promoting a unidirectional data flow and pure business logic.
- **Test-Driven Development (TDD):** Adhere to a strict TDD workflow for all logic.
- **CI/CD:** Maintain a continuous integration pipeline to ensure code quality and stability.
- **Exemplary Codebase:** Serve as a clear, well-documented example for other developers.

## Tech Stack & Architecture

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Architecture:** MVI (Model-View-Intent) / TEA (The Elm Architecture)
- **State Management:** Kotlin Coroutines & `StateFlow`
- **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) (with KSP)
- **Testing:**
    - JUnit5
    - [MockK](https://mockk.io/)
    - [Turbine](https://github.com/cashapp/turbine)
    - Truth
- **Linting:** [ktlint](https://ktlint.github.io/) with custom Compose ruleset
- **CI/CD:** [GitHub Actions](https://github.com/features/actions)

## How to Build

1. Clone the repository.
2. Open the project in the latest version of Android Studio.
3. The project should build automatically using the included Gradle wrapper, which will auto-provision the correct JDK via the Foojay plugin.

---

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
