# bai

## Konfiguracja środowiska
Utwórzony plik `.env` w katalogu głównym projektu, on jest ignorowany przez Git

## Migracje bazy danych
Migracje Flyway znajdują się w:
src/main/resources/db/migration

## Packeges & classes
#Config - Konfiguracja bezpieczeństwa aplikacji,
określająca podstawowe zasady dostępu do endpointów (SecurityConfig)

#Controller odpowiada za obsługę żądań HTTP/
(HelloController- prosty endpoint GET do testowania działania aplikacji , UserController-endpointy rejestracji i logowania użytkownika)

#Model
model domenowy
model.dto - obiekty DTO wykorzystywane do komunikacji z API (CreateUserRequest, LoginRequest)

#Repository - dostęp do danych
UserRepository – interfejs Spring Data JPA do operacji na tabeli users

#Service - warstwa logiki biznesowej aplikacji
UserService – zawiera logike tworzenia użytkownika
