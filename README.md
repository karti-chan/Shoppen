# Shoppen (Radnom-App) 🛒

Projekt to nowoczesna aplikacja e-commerce typu SPA (Single Page Application) stworzona w architekturze klient-serwer. Składa się z reaktywnego frontendu napisanego w **React + Vite** oraz bezpiecznego, wydajnego backendu opartego na **Spring Boot**.

## 🚀 Technologie

### Backend
* **Java 17**
* **Spring Boot 3.2.0**
  * **Spring Web** – obsługa API REST.
  * **Spring Security & JWT (JSON Web Tokens)** – autoryzacja i uwierzytelnianie użytkowników (w tym Refresh Tokens).
  * **Spring Data JPA** – komunikacja z bazą danych.
  * **Spring Boot Mail** – wysyłka powiadomień e-mail (np. resetowanie hasła).
  * **Thymeleaf** – szablony wiadomości e-mail.
* **Baza danych:** Wsparcie dla H2 (podczas developmentu) oraz MySQL (produkcyjnie).
* **Lombok** – generowanie boilerplate-u (gettery/settery itp.).

### Frontend
* **React 19**
* **Vite** – szybkie środowisko budowania aplikacji.
* **React Router DOM** – nawigacja po podstronach.
* **Axios** – komunikacja HTTP z backendem, wyposażony w interceptory obsługujące automatyczne odświeżanie tokenów JWT.
* **JS Cookie** – bezpieczne zarządzanie tokenami w przeglądarce.

---

## 🌟 Główne Funkcjonalności

1. **Kompletny system uwierzytelniania i autoryzacji:**
   - Rejestracja i logowanie użytkowników.
   - Zabezpieczenie endpointów za pomocą bezstanowego mechanizmu tokenów **JWT**.
   - Mechanizm **Refresh Token** zapewniający dłuższą sesję bez konieczności ponownego logowania.
   - Odzyskiwanie hasła za pomocą jednorazowych tokenów wysyłanych na adres e-mail.
2. **Sklep i Katalog Produktów:**
   - Przeglądanie listy produktów oraz szczegółów każdego z nich.
   - Dynamiczna wyszukiwarka produktów.
3. **Koszyk zakupowy:**
   - Dodawanie, modyfikowanie liczby sztuk oraz usuwanie produktów z koszyka.
   - Synchronizacja zawartości koszyka w bazie danych dla zalogowanych użytkowników.
4. **Wieloprofilowa konfiguracja e-mail:**
   - Dostosowanie zachowania usług pocztowych w zależności od profilu uruchomieniowego (`Dev`, `Sandbox`, `Prod`).

---

## 🛠️ Jak uruchomić projekt lokalnie

### Wymagania wstępne
* Zainstalowane **JDK 17** lub nowsze.
* Zainstalowany **Node.js** (rekomendowany LTS).
* **Maven** (lub użycie dołączonego wrappera `mvnw`).

### Krok 1: Klonowanie repozytorium
```bash
git clone https://github.com/karti-chan/Shoppen.git
cd Shoppen
```

### Krok 2: Uruchomienie Backend (Spring Boot)
1. Utwórz plik konfiguracji środowiskowej `application.properties` (lub `application-dev.properties`) w folderze `src/main/resources/`, jeśli nie istnieje. Przykładowa konfiguracja bazowa dla H2:
   ```properties
   spring.datasource.url=jdbc:h2:file:./data/radnom_db
   spring.datasource.driverClassName=org.h2.Driver
   spring.datasource.username=sa
   spring.datasource.password=
   spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
   spring.h2.console.enabled=true
   ```
2. Uruchom serwer aplikacji za pomocą Maven:
   ```bash
   # Windows:
   mvnw.cmd spring-boot:run
   
   # Linux/macOS:
   ./mvnw spring-boot:run
   ```
Backend domyślnie nasłuchuje na porcie `8080`.

### Krok 3: Uruchomienie Frontend (React)
1. Przejdź do katalogu frontendu:
   ```bash
   cd src/main/frontend
   ```
2. Zainstaluj wymagane zależności:
   ```bash
   npm install
   ```
3. Uruchom aplikację w trybie deweloperskim:
   ```bash
   npm run dev
   ```
Aplikacja frontendowa zostanie uruchomiona domyślnie pod adresem `http://localhost:5173/`.

---

## 📁 Struktura Projektu

```text
├── .gitignore
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/radnom/
│   │   │   ├── config/       # Konfiguracja Security, JWT, CORS
│   │   │   ├── controller/   # Endpointy API REST
│   │   │   ├── entity/       # Model danych (JPA Entities)
│   │   │   ├── exception/    # Obsługa wyjątków i błędów
│   │   │   ├── repository/   # Interfejsy JPA Repository
│   │   │   └── service/      # Logika biznesowa i integracje
│   │   ├── resources/
│   │   │   ├── templates/    # Szablony Thymeleaf
│   │   │   └── data.sql      # Dane inicjalizacyjne
│   │   └── frontend/         # Aplikacja React + Vite (SPA)
│   └── test/                 # Testy jednostkowe i integracyjne backendu
```
