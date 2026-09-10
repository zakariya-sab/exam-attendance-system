# Exam Attendance Management System

Records student attendance at examinations by scanning an encrypted QR code
printed on the convocation each student already receives.

End-of-Year Project (PFA) — Zakariya SABRI
Cybersecurity, First Year Engineering Cycle — UM6P College of Computing
Faculté Polydisciplinaire de Taroudant, Université Ibn Zohr — 2025–2026

---

## The problem

Attendance is recorded on a single printed sheet per examination room. Because
only one copy exists, only one person can record attendance however many
proctors are present. When students do not sit in their assigned seats, each
name has to be searched for in a list of several dozen entries.

A server now holds the data and enforces every rule, so several telephones can
record the same examination at the same time, and the database itself guarantees
that a student cannot be recorded twice.

---

## Contents

| Folder | |
|---|---|
| `Backend/` | Spring Boot server — REST interface, rules, persistence, security |
| `Frontend/` | Angular administration interface |
| `Mobile/` | Android application used by proctors |

The two clients talk only to the server, never to each other.

---

## The three rules

A scan is accepted only if all three are true:

1. **The code was issued by the system** — the payload is encrypted with
   AES-GCM, so a code made with an ordinary QR generator fails to decrypt.
2. **The examination is in progress** — decided by the server's clock, never
   the telephone's.
3. **The student is not already recorded** — enforced by a uniqueness
   constraint in the database.

---

## Security

- The printed code carries the registration number encrypted with **AES-GCM**,
  with a fresh random IV for every code.
- Administrative operations require a **JWT** signed with HS256, valid eight
  hours. The administrator's password is stored only as a **BCrypt** hash.
- The mobile application has **no account, by design** — proctors are often
  master's or doctoral students helping for one session. The three rules above
  protect the scan instead.
- **No key, secret or password is in this repository.**

---

## Running it

**Server**

```
cd Backend/qrattendance
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Fill in `qr.encryption.key`, `jwt.secret`, `admin.username` and
`admin.password-hash`, then:

```
./mvnw spring-boot:run
```

Runs on `http://localhost:8080`. The database is in memory, so data is lost when
the server stops.

**Administration interface**

```
cd Frontend
npm install
ng serve
```

Open `http://localhost:4201`.

**Mobile application**

Open `Mobile` in Android Studio, run it on a device, then:

```
adb reverse tcp:8080 tcp:8080
```

---

## Interface

| Method | Path | Access |
|---|---|---|
| POST | `/api/auth/login` | public |
| GET | `/api/exam-blocks` | public |
| POST · PUT · DELETE | `/api/exam-blocks` | protected |
| GET | `/api/exam-blocks/{id}/attendance/export` | protected |
| GET | `/api/majors` · `/api/modules` | protected |
| GET | `/api/students/{codeApogee}/qr-code` | protected |
| POST | `/api/attendance/scan` | public |

The full contract is in Appendix A of the report.

---

## Limitations

The system has **never been used during a real examination**. The database is in
memory, communication is over HTTP rather than HTTPS, student data is
representative rather than real, and there is one administrator account. The
proctor still verifies identity by comparing the face with the photograph — the
system replaces the search through a list, not the check.