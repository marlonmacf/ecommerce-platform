# Full‑Stack Event‑Driven Microservices
## Spring • Feign • PostgreSQL • AWS SQS • AWS CloudWatch Logs • Angular • Real‑Time Notifications • SSE • Websockets

Overall, this project demonstrates robust system design, cloud-native patterns, and full-stack development skills—all implemented with simple code focused on functionality. The goal is to showcase practical knowledge of microservices, AWS integration, real-time streaming, centralized logging and monitoring, and a deep understanding of real-world cloud-native system design and principles by using a hybrid approach combining Feign Client (synchronous) and AWS SQS (asynchronous).

---

## Technologies & Patterns Used (What I Learned)

1. **Spring Boot:** Leveraged Spring Boot’s auto-configuration and embedded Tomcat to rapidly build and structure each microservice, improving modularity and making testing and deployment straightforward.

2. **Feign Client:** Implemented declarative REST communication with timeout, retry, logging, and error decoding support. This allowed synchronous interactions (like stock checks and decrements) while maintaining an overall asynchronous workflow for resilience and clarity.

3. **AWS CloudWatch Logs & Insights:** Centralized logging across all services using CloudWatch. I configured log groups, streams, and retention policies, then used CloudWatch Insights to query data, build dashboards, tail live logs, and set alerts—significantly enhancing monitoring and operational visibility.

4. **Spring Cloud AWS SQS (@SqsListener + SqsTemplate):** Adopted Spring Cloud AWS SQS starter to create a fully asynchronous, event-driven architecture. Producers push events with SqsTemplate, and services now use @SqsListener to process background jobs. This decoupling improved fault tolerance, achieved independent scaling, and aligned with cloud-native best practices.

5. **Server-Sent Events (SSE):** Used SseEmitter to push real-time notifications from backend to Angular, selecting SSE for its simplicity and suitability in server→client streaming, without the complexity overhead of WebSockets.

6. **Layered Architecture:** Maintained clear separation between Controllers, Services, Repositories, DTOs, Messaging handlers, and Feign clients. This improved testability, readability, and allowed features to scale with minimal interdependency.

7. **Error Handling & Retry Patterns:** Built robust handling for downstream failures (e.g., 404s or out-of-stock), routing problem messages to dead-letter queues or generating alerts. This ensured service stability and graceful recovery.

8. **Observability & Debugging:** By aligning log correlation across services and combining it with queue visibility and metrics, I drastically improved traceability. Now I can trace an order’s lifecycle and rapidly identify issues, lowering MTTR.

9. **End-to-End Workflow:** Designed a practical pipeline: Angular UI → Feign-based stock check → order-created event → background processing → order-processed event → user notification. This showcases a full-stack, microservices architecture demonstrating synchronization, decoupling, and real-time updates.

10. **Decentralized Data Stores:** Ensured each service owns its own PostgreSQL database, reinforcing bounded contexts and service autonomy. This approach supports clean data ownership and adaptive scaling strategies.

11. **Event‑Driven Architecture:** Expanded event-driven capabilities with SQS to orchestrate communication flows, improving scalability, fault tolerance, and alignment with modern cloud patterns.

12. **Hands‑On AWS Integration:** Configured AWS components—SQS queues, IAM permissions, CloudWatch setup—through Spring Cloud AWS and AWS SDK. These integrations reinforced practical skills in cloud-based architecture and system observability.

---

## Architecture Overview

### 1. Project Structure

#### Order Service

**Purpose:**
* Core order management system handling order creation, validation, and lifecycle.
* Demonstrates synchronous validation (Feign) + event-driven decoupling (SQS)
* Showcases transactional boundaries (order creation vs. inventory check)

**Key Responsibilities:**

* REST API
    * POST /orders: Creates new orders (validates via InventoryService)
    * GET /orders/{id}: Retrieves order status

* Synchronous Communication
    * Uses Feign Client to call InventoryService for item stock checks

* Asynchronous Events
    * Publishes "order-created" events to AWS SQS (processing-queue) and (notification-queue)

* Data
    * PostgreSQL: Stores "orders" and "order_items" tables

* Logging/Monitoring
    * Sends logs to AWS CloudWatch (order-service-logs)

````text
order-service/
├── src/
│   ├── main/
│   │   ├── java/com/example/orderservice/
│   │   │   ├── config/
│   │   │   │   └── CloudWatchAppender.java (CloudWatch for logs)
│   │   │   ├── client/
│   │   │   │   └── InventoryClient.java (Feign Client for synch REST calls)
│   │   │   ├── messaging/
│   │   │   │   └── OrderEventPublisher.java (SQS publishes events in AWS)
│   │   │   ├── controller/
│   │   │   │   ├── OrderApiController.java (API to close orders - Feign)
│   │   │   │   └── OrderController.java (REST Crud)
│   │   │   ├── model/...
│   │   │   ├── service/...
│   │   │   ├── repository/...
│   │   │   └── OrderServiceApplication.java
│   │   └── resources/
│   │       ├── application.yml 
│   │       └── logback.xml
│   └── test/
│       └── java/com/order_service/...
└── pom.xml
````

#### Inventory Service

**Purpose:**
* Real-time product stock management.
* Optimistic locking (handles concurrent stock updates).
* Bounded context (owns product stock data exclusively).

**Key Responsibilities:**

* REST API
    * GET /inventory/{productId}: Checks stock (called by OrderService via Feign)
    * POST /inventory/decrement: Reduces stock (called by ProcessingService via Feign)

* Data
    * PostgreSQL: Stores "inventory" table (product_id, stock)

* Logging
    * Sends logs to AWS CloudWatch (inventory-service-logs)


````text
inventory-service/
├── src/
│   ├── main/
│   │   ├── java/com/inventory_service/inventory_service/
│   │   │   ├── config/
│   │   │   │   └── CloudWatchAppender.java (CloudWatch for logs)
│   │   │   ├── controller/
│   │   │   │   ├── InventoryApiControler.java (API for stock check - Feign)
│   │   │   │   └── InventoryController.java (REST Crud)
│   │   │   ├── model/...
│   │   │   ├── service/...
│   │   │   ├── repository/...
│   │   │   └── InventoryServiceApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── data.sql (PostgreSQL seeding)
│   │       └── logback.xml
│   └── test/
│       └── java/com/inventory_service/...
└── pom.xml
````

#### Processing Service

**Purpose:**
* Orchestrates post-order fulfillment (async workflow).
* Saga pattern (distributed transaction management).
* Retry/failure handling for inventory updates.

**Key Responsibilities:**
* Asynchronous Event Processing
    * Listens to processing-queue (SQS)
    * Executes workflow:
        * Calls InventoryService to decrement stock
        * Calls OrderService to mark order as completed
        * Publishes "order-processed" event to notification-queue
* Synchronous Communication
    * Uses Feign to call OrderService and InventoryService
* Logging
    * CloudWatch (processing-service-logs)

````text
processing-service/
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/com/processing_service/processing_service/
│   │   │   ├── config/
│   │   │   │   ├── WebSocketConfig.java (SSE: Server-Sent Events)
│   │   │   │   └── CloudWatchAppender.java (CloudWatch for logs)
│   │   │   ├── client/
│   │   │   │   ├── OrderClient.java (Feign Client for synch REST calls)
│   │   │   │   └── InventoryClient.java (Feign Client for synch REST calls)
│   │   │   ├── messaging/
│   │   │   │   └── ProcessingEventListener.java (SQS asynch calls to AWS and log events)
│   │   │   │   └── ProcessingEventPublisher.java (SQS publishes events in AWS)
│   │   │   ├── model/...
│   │   │   ├── service/...
│   │   │   └── ProcessingServiceApplication.java
│   │   └── resources/
│   │       ├── application.yml 
│   │       └── logback.xml
│   └── test/
│       └── java/com/notification_service/...
└── pom.xml
````
---

#### Notification Service

**Purpose:**
* Handles real-time user notifications via email/SMS (simulated).
* Event-driven patterns (vs. polling)
* SSE vs. WebSockets tradeoffs

**Key Responsibilities:**
* Asynchronous Event Processing
    * Listens to notification-queue (SQS) via @SqsListener
    * Triggers notifications for:
        * Order creation (Notification CREATED)
        * Order completion (Notification CLOSED)
    * Real-Time Updates
        * Server-Sent Events (SSE): Pushes notifications to Angular frontend
    * Logging
        * Sends logs to AWS CloudWatch (notification-service-logs)

````text
notification-service/
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/com/notification_service/notification_service/
│   │   │   ├── config/
│   │   │   │   ├── WebSocketConfig.java (SSE: Server-Sent Events with Dual Protocol Support)
│   │   │   │   └── CloudWatchAppender.java (CloudWatch for logs)
│   │   │   ├── messaging/
│   │   │   │   └── NotificationEventListener.java (SQS asynch calls to AWS and log events)
│   │   │   ├── controller/
│   │   │   │   └── NotificationController.java (WebSocket SSE emitter Stream)
│   │   │   ├── service/
│   │   │   │   └── SseEmitterService.java (SSE emitter logic)
│   │   │   ├── model/...
│   │   │   └── OrderServiceApplication.java
│   │   └── resources/
│   │       ├── application.yml 
│   │       └── logback.xml
│   └── test/
│       └── java/com/notification_service/...
└── pom.xml
````
---

### 2. Database Models

```mermaid
erDiagram
    ORDER ||--o{ ORDER_ITEM : has
    ORDER {
        bigint id PK
        varchar status
        timestamp created_at
    }
    ORDER_ITEM {
        bigint id PK
        bigint order_id FK
        varchar product_id
        int quantity
    }

    INVENTORY {
        varchar product_id PK
        int stock
        timestamp last_update
    }
```

**Key data models:**

* **Order Service** maintains orders and items across two tables.
* **Inventory Service** tracks product stock and updates.

---

### 3. System Flow (Use Cases and Testing)
```mermaid
sequenceDiagram
    autonumber
    participant Angular
    participant NotificationService
    participant SQS
    participant ProcessingService
    participant InventoryService
    participant OrderService
    participant CloudWatch
    Angular->>OrderService: POST /orders (productId=123)
    activate OrderService
    OrderService->>InventoryService: GET /inventory/123 - REST call (Feign)
    deactivate OrderService
    activate InventoryService
    InventoryService-->>OrderService: success/failure (in stock) (synch)
    deactivate InventoryService
    activate OrderService
    CloudWatch->>CloudWatch: Logs "Live Trail"
    InventoryService--)CloudWatch: CloudWatch Logs record (info/error)
    Note left of CloudWatch: inventory-service-logs
    OrderService-xSQS: Publish "{order-created: Notification CREATED for order 123}" (asynch)
    deactivate OrderService
    Note right of SQS: processing / notification-queues
    OrderService--)CloudWatch: CloudWatch Logs record (info/error)
    Note left of CloudWatch: order-service-logs
    NotificationService-->SQS: @SqsListener trigger
    Note left of SQS: notification-queue
    SQS--)NotificationService: Consume "Notification CREATED for order 123" (asynch)
    activate NotificationService
    NotificationService->>NotificationService: Log "Email/Notification sent"
    NotificationService--)Angular: SSE: "Notification CREATED for order 123"
    deactivate NotificationService
    NotificationService--)CloudWatch: CloudWatch Logs record (info/error)
    Note left of CloudWatch: notification-service-logs
    ProcessingService-->SQS: @SqsListener trigger
    Note right of SQS: processing-queue
    SQS--)ProcessingService: Consume "Notification CREATED for order 123" (asynch)
    activate ProcessingService
    ProcessingService->>InventoryService: POST /inventory/decrement/123 - REST call (Feign)
    deactivate ProcessingService
    activate InventoryService
    InventoryService-->>ProcessingService: success/failure (-stock) (synch)
    deactivate InventoryService
    activate ProcessingService
    ProcessingService->>OrderService: POST /order/close/123 - REST call (Feign)
    deactivate ProcessingService
    activate OrderService
    OrderService-->>ProcessingService: success/failure (closed) (synch)
    deactivate OrderService
    activate ProcessingService
    ProcessingService-xSQS: Publish "order-processed" or "order-failed" (asynch)
    deactivate ProcessingService
    Note right of SQS: notification-queue
    ProcessingService--)CloudWatch: CloudWatch Logs record (info/error)
    Note left of CloudWatch: processing-service-logs
    NotificationService-->SQS: @SqsListener trigger
    Note left of SQS: notification-queue
    SQS--)NotificationService: Consume "Notification CLOSED for order 123" (asynch)
    activate NotificationService
    NotificationService->>NotificationService: Log "Email/Notification sent"
    NotificationService--)Angular: SSE: "Notification CLOSED for order 123"
    deactivate NotificationService
    NotificationService--)CloudWatch: CloudWatch Logs record (info/error)
    Note left of CloudWatch: notification-service-logs
```

---

**Use Cases - What’s happening:**

1. **POST /orders/{productId=123}** – Angular/Postman calls OrderService to create a new order

2. **GET /inventory/123 REST call (Feign)** – OrderService calls InventoryService to check items out of stock

3. **success/failure (in stock) (synch)** – InventoryService returns inventory value so OrderService can continue

4. **Logs "Live Trail"** – Meanwhile CloudWatch is configured to receive logs from any microservice

5. **CloudWatch Logs record (info/error)** – InventoryService sends any logs to **"inventory-service-logs"** in AWS CloudWatch

6. **Publish "{order-created: Notification CREATED for order 123}" (asynch)** – OrderService writes an order to its database and sends a message to **"notification-queue"** AWS SQS

7. **CloudWatch Logs record (info/error)** – OrderService sends any logs to **"order-service-logs"** in AWS CloudWatch

8. **@SqsListener trigger** – NotificationService is also active polling any SQS messages from **"notification-queue"** in AWS SQS

9. **Consume "Notification CREATED for order 123" (asynch)** – NotificationService identifies a new order creation

10. **Log "Email/Notification"** – NotificationService simulate message broadcast by logging it to its terminal which shows in **"notification-service-logs"** at AWS CloudWatch

11. **SSE: "Notification CREATED for order 123"** – NotificationService can also provice REST port SSE allowing Dual Protocol Support

12. **CloudWatch Logs record (info/error)** – NotificationService also sends any logs to **"notification-service-logs"** in AWS CloudWatch

13. **@SqsListener trigger** – ProcessingService is also active polling any SQS messages from **"notification-queue"** in AWS SQS

14. **Consume "Notification CREATED for order 123" (asynch)** – ProcessingService identifies a new order creation

15. **POST /inventory/decrement/123 - REST call (Feign)** – ProcessingService calls InventoryService to handle items stock changes

16. **success/failure (in stock) (synch)** – InventoryService returns new inventory value so ProcessingService can continue

17. **POST /order/close/123 - REST call (Feign)** – ProcessingService calls OrderService to handle order finalization

18. **success/failure (close) (synch)** – OrderService returns updated order id so ProcessingService can continue

19. **Publish "order-processed or order-failed" (asynch)** – ProcessingService updates an order to its database and sends a message to **"processing-queue"** in AWS SQS

20. **CloudWatch Logs record (info/error)** – ProcessingService also sends any logs to **"processing-service-logs"** in AWS CloudWatch

21. **@SqsListener trigger** – NotificationService is also active polling any SQS messages from **"processing-queue"** in AWS SQS

22. **Consume "Notification CLOSED for order 123" (asynch)** – NotificationService identifies the order finalization

23. **Log "Email/Notification"** – NotificationService simulate message broadcast by logging it to its terminal which shows in **"notification-service-logs"** at AWS CloudWatch

24. **SSE: "Notification CLOSED for order 123"** – NotificationService can also provice REST port SSE allowing Dual Protocol Support

25. **CloudWatch Logs record (info/error)** – NotificationService also sends any logs to **"notification-service-logs"** in AWS CloudWatch

---

**Test Cases - Success & Fail:**

* **DB checking: Already created some order items:**
![1.get-order-items](images-use-cases/1.get-order-items.png)

* **DB checking: Create Order with some items:**
![2.create-order](images-use-cases/2.create-order.png)

* **Publish create-order event to AWS SQS (notification-queue):**
![3.publish-create-order-event-to-notification-queue](images-use-cases/3.publish-create-order-event-to-notification-queue.png)

* **Publish create-order event to AWS SQS (processing-queue):**
![4.publish-create-order-event-to-processing-queue](images-use-cases/4.publish-create-order-event-to-processing-queue.png)

* **Feign Api calls in CloudWatch (inventory-service-logs):**
![5.feign-api-calls-in-inventory-service-logs](images-use-cases/5.feign-api-calls-in-inventory-service-logs.png)

* **Publish to processing-queue event in CloudWatch (order-service-logs):**
![6.publish-to-processing-queue-in-order-service-logs](images-use-cases/6.publish-to-processing-queue-in-order-service-logs.png)

* **publish to notification-queue event in CloudWatch (order-service-logs):**
![7.publish-to-notification-queue-in-order-service-logs](images-use-cases/7.publish-to-notification-queue-in-order-service-logs.png)

* **Consuming create-order event from processing-queue in CloudWatch (processing-service-logs):**
![8.consuming-create-event-from-processing-queue-in-processing-service-logs](images-use-cases/8.consuming-create-event-from-processing-queue-in-processing-service-logs.png)

* **Publish to notification-queue in CloudWatch (processing-service-logs):**
![9.publish-to-notification-queue-in-processing-service-logs](images-use-cases/9.publish-to-notification-queue-in-processing-service-logs.png)

* **Consuming create-order event from notification-queue in CloudWatch (notification-service-logs):**
![10.consuming-create-event-from-notification-queue-in-notification-service-logs](images-use-cases/10.consuming-create-event-from-notification-queue-in-notification-service-logs.png)

* **Consuming processing-order event from notification-queue in CloudWatch (notification-service-logs):**
![11.consuming-processing-event-from-notification-queue-in-notification-service-logs](images-use-cases/11.consuming-processing-event-from-notification-queue-in-notification-service-logs.png)

* **DB checking: Get order by id is updated:**
![12.get-order-by-id-updated](images-use-cases/12.get-order-by-id-updated.png)

* **DB checking: Get order-items stock is updated:**
![13.get-order-items-updated](images-use-cases/13.get-order-items-updated.png)

* **Bonus: (SSE) Server-Sent Events stream emitter endpoint in notification-service project:**
![14.sse-stream-emitter-endpoint-in-notification-service](images-use-cases/14.sse-stream-emitter-endpoint-in-notification-service.png)

---