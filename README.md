# Exam Attendance Management System

A system that records student attendance at examinations by scanning an encrypted
QR code printed on the convocation each student already receives.

End-of-Year Project (PFA) — Zakariya SABRI
Cybersecurity, First Year Engineering Cycle — UM6P College of Computing
Carried out at the Faculté Polydisciplinaire de Taroudant, Université Ibn Zohr
20 June – 20 August 2026

---

## The problem

Attendance at the faculty is recorded on a single printed sheet per examination
room. Because only one copy exists, only one person can record attendance
however many proctors are present. When students do not sit in their assigned
seats, each name has to be searched for in a list of several dozen entries.

This system removes that constraint. A server holds the data and enforces every
rule, so several telephones can record the same examination at the same time,
and the database itself guarantees that a student cannot be recorded twice.

---

## Repository layout

| Folder | Contents |
|---|---|
| `Backend/` | Spring Boot server — REST interface, business rules, persistence, security |
| `Frontend/` | Angular administration interface used by the faculty |
| `Mobile/` | Native Android application used by proctors in the examination room |

The two clients communicate only with the server, never with each other.

---

## Architecture

```
   Android app                Server                 Admin web
  (Kotlin/Compose)  ──HTTP──▶  (Spring Boot)  ◀──HTTP──  (Angular)
   ML Kit camera                   │
                                   ▼
                            Database (H2)
```

All data and all rules live on the server. Neither client decides whether a scan
is valid, whether a student is already recorded, or whether an examination is in
progress. The server's clock is the only one that counts.

---

## The three rules

A scan is accepted only if all three are true:

1. **The code was issued by the system** — the payload is encrypted with
   AES-GCM, so a code produced by an ordinary QR generator fails to decrypt.
2. **The examination is in progress** — `start ≤ now < start + duration`,
   measured by the server.
3. **The student is not already recorded** — enforced by a uniqueness
   constraint on the database, not by a check in the application.

---

## Technologies

**Server** — Java, Spring Boot, Spring Data JPA, Spring Security, H2, ZXing
(QR generation), AES-GCM, JJWT, BCrypt

**Administration interface** — Angular, Bootstrap

**Mobile application** — Kotlin, Jetpack Compose, ML Kit Barcode Scanning,
Retrofit, Gson

---

## Running the system

### 1. Configuration

The server needs three values that are **not in this repository**. Copy the
example file and fill it in:

```
cd Backend
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Then set:

| Property | What it is |
|---|---|
| `app.encryption.key` | A base64 AES key used to encrypt the printed codes |
| `jwt.secret` | The signing secret for the administrator's token |
| `app.admin.password-hash` | A BCrypt hash of the administrator's password |

Never commit real values for any of these.

### 2. Server

```
cd Backend
./mvnw spring-boot:run
```

Runs on `http://localhost:8080`. The database is held in memory, so all data is
lost when the server stops. Programmes, modules and representative student
records are loaded at startup.

### 3. Administration interface

```
cd Frontend
npm install
ng serve
```

Open `http://localhost:4200` and log in.

### 4. Mobile application

Open the `Mobile` folder in Android Studio and run it on a device.
The application reaches the server through the USB connection:

```
adb reverse tcp:8080 tcp:8080
```

---

## Interface

Ten operations are exposed. Three are public — listing the exam blocks, logging
in, and recording a scan. Everything that changes data or exposes personal data
requires an authenticated administrator.

| Method | Path | Access |
|---|---|---|
| POST | `/api/auth/login` | public |
| GET | `/api/exam-blocks` | public |
| POST | `/api/exam-blocks` | protected |
| PUT | `/api/exam-blocks/{id}` | protected |
| DELETE | `/api/exam-blocks/{id}` | protected |
| GET | `/api/exam-blocks/{id}/attendance/export` | protected |
| GET | `/api/majors` | protected |
| GET | `/api/modules` | protected |
| GET | `/api/students/{codeApogee}/qr-code` | protected |
| POST | `/api/attendance/scan` | public |

The full contract — request bodies, error codes, authorisation rules in
evaluation order — is in Appendix A of the report.

---

## Security

- The printed code carries the registration number encrypted with **AES-GCM**,
  with a fresh random initialisation vector for every code. Authenticated
  encryption gives confidentiality and integrity in one operation: a code that
  was altered, or never issued by the system, fails to decrypt.
- Administrative operations require a **JWT** signed with HS256, valid for eight
  hours. The token is signed, not encrypted, so nothing confidential is placed
  in it.
- The administrator's password is stored only as a **BCrypt** hash.
- **The mobile application has no account, by design.** Proctors are often
  master's or doctoral students helping for a single session. The scan operation
  is protected by the three rules above instead.
- No key, secret or password is present in this repository.

---

## Known limitations

**Deliberate** — the system does not verify identity; the proctor still compares
the face with the photograph. The mobile application has no account and no
offline mode: a scan that cannot reach the server fails and says so.

**From the conditions of the internship** — the database is in memory; access to
real student data was not authorised, so representative data is used; the system
runs over HTTP rather than HTTPS; there is a single administrator account; and
it has never been used during a real examination.

The report states these in full, along with the defects found during testing.

---

## Report

The full report — context, preliminary study, design, implementation, results
and the interface contract — is `Rapport_PFA.pdf`.
