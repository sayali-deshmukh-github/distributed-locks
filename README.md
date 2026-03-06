# Distributed Inventory Management System with Distributed Locks

A Spring Boot project demonstrating concurrency control in a distributed inventory management system. Progresses through multiple locking strategies — from no lock to Redis distributed locks — to prevent overselling under high concurrent access.

---

## 📋 Table of Contents

1. [Project Goal](#project-goal)
2. [Tech Stack](#tech-stack)
3. [Architecture](#architecture)
4. [Implementation Steps](#implementation-steps)
5. [Learnings](#learnings)
6. [Testing](#testing)
7. [References](#references)

---

## 🎯 Project Goal

- Understand concurrency and overselling problems in inventory management
- Implement and compare solutions using no lock, DB locks, and Redis distributed locks
- Learn transaction management, isolation levels, and distributed locking patterns
- Build a robust system suitable for multi-instance distributed environments

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java 17+ | Core language |
| Spring Boot | Application framework |
| Spring Data JPA | Database access and repository layer |
| H2 Database | In-memory DB for local testing |
| Redis | Distributed lock backend |
| Redisson | Redis client with distributed lock support |
| Maven | Build tool |
| JUnit 5 | Testing framework |

---

## 🏗️ Architecture

```
Client Requests
       |
       v
[Multiple Service Instances] ---> Redis Distributed Lock ---> Database
```

- **Client Requests** — Multiple concurrent purchase attempts hitting the system simultaneously
- **Service Instances** — Spring Boot apps running on different ports
- **Redis Distributed Lock** — Ensures single access to an inventory item across all instances
- **Database** — H2 (for development) stores and persists inventory state

---

## 🔄 Implementation Steps

### 1. No Lock
- Implemented inventory purchase without any locking mechanism
- Observed overselling in concurrent tests — multiple threads read the same inventory value before any update is committed

### 2. Database Pessimistic Lock
- Added `@Lock(LockModeType.PESSIMISTIC_WRITE)` in the repository layer
- Wrapped purchase logic in `@Transactional`
- Successfully prevented overselling on a single database instance

### 3. Redis Distributed Lock with Redisson
- Configured Redis and Redisson client
- Implemented `RLock` for distributed locking across service instances
- Added retry logic with exponential backoff
- Leveraged Redisson's watchdog for automatic lock expiration/renewal
- Logged failed lock acquisition attempts
- Gracefully handled scenarios where retries are exhausted

### 4. Multi-Instance Simulation
- Deployed multiple service instances concurrently
- Verified inventory consistency is maintained across all instances under load

---

## 🧠 Learnings

**Transactions vs. Locks**
- `@Transactional` ensures atomic operations, but isolation alone does not prevent race conditions
- Locks explicitly control concurrent access to shared resources

**Race Conditions**
- Without locks, multiple threads can read the same inventory count simultaneously, all proceed to decrement, causing overselling

**Pessimistic vs. Optimistic Locking**
- Pessimistic locks block other transactions — safe but introduces latency
- Redis distributed locks are required for coordination across multiple JVM instances

**Distributed Systems Considerations**
- Multi-node systems need coordination outside the database boundary
- Redisson handles automatic lock renewal and safe unlock on failure
- Retry with exponential backoff reduces thundering herd problems

---

## 🧪 Testing

**Multithreaded Test**
- Simulated 50+ concurrent purchase requests
- Verified inventory consistency after all requests completed

**H2 Database Console**
- Inspected the inventory table post-test
- Confirmed quantity never dropped below 0

**Redis Lock Logging**
- Monitored retry and failed lock acquisition logs
- Verified correct locking behavior under high concurrency

---

## 📚 References

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [H2 Database](https://www.h2database.com/html/main.html)
- [Redis](https://redis.io/docs/)
- [Redisson Documentation](https://redisson.org/documentation.html)
- [ACID Transactions](https://en.wikipedia.org/wiki/ACID)
- [Distributed Locking Patterns](https://redis.io/docs/manual/patterns/distributed-locks/)

---

> This project demonstrates a progressive approach to locking strategies and distributed system design, reflecting real-world patterns for high-concurrency inventory management.
