# 🔍 Specification Deduction Guide — From Prompt to SPEC

> **Objective:** Learn how to translate a business idea, an academic prompt, or a real problem into precise specifications (`SPEC_[Feature].md`) ready to feed to the AI.

---

## 🧠 Key Mindset

> **"Don't read the prompt as a story. Read it like an architect looking for puzzle pieces."**

The skill you will master here is **requirements engineering**: translating a narrative need into logical components and verifiable rules.

The guiding question is always:

> *"What is the minimum that must happen for this operation to be considered **successful and safe**?"*

The answer to that question is your business specifications.

---

## 📋 The 5-Step Process

```
┌─────────────────────────────────────────────────────────────────┐
│                  DEDUCTION WORKFLOW                             │
│                                                                 │
│  📖 Prompt                                                      │
│    ↓                                                            │
│  1️⃣  Identify Entities (main nouns)                             │
│    ↓                                                            │
│  2️⃣  Extract Business Rules (constraints and validations)      │
│    ↓                                                            │
│  3️⃣  Detect Transactional Flows (atomic operations)             │
│    ↓                                                            │
│  4️⃣  Separate Business vs. Infrastructure (what vs. how)       │
│    ↓                                                            │
│  5️⃣  Define Acceptance Criteria (BDD scenarios)                 │
│    ↓                                                            │
│  📋 SPEC_[Feature].md ready for the AI                          │
└─────────────────────────────────────────────────────────────────┘
```

---

## Step 1: Entity Identification (Logical Grouping)

### What to do?

Don't see the prompt as an endless list. Group it by **entities** — the main nouns in the text.

### Technique

Underline the **key nouns** in the prompt:

| What you read | Identified Entity |
|-------------|------------------------|
| *"The restaurant registers **dishes** on its menu"* | `Dish` |
| *"**Clients** make reservations"* | `Client` |
| *"Each **reservation** has a date and number of people"* | `Reservation` |
| *"**Tables** have a maximum capacity"* | `Table` |

### The Golden Rule

> If an entity has **its own validation rules** and likely **a table in the database**, it deserves its own `SPEC_[Entity].md`.

### Practical Example

**Prompt:**
> *"A system is needed for a restaurant to manage menu dishes, clients, and reservations. Dishes have a name, price, and category. Clients have a name, phone, and email. Reservations are assigned to an available table."*

**Result:**

```
📋 SPEC_Dish.md         → Entity with name, price, category
📋 SPEC_Client.md       → Entity with name, phone, email
📋 SPEC_Reservation.md  → Transactional flow between Client and Table
```

---

## Step 2: Extraction of Business Rules (BR)

### What to do?

Separate what the system **does** from what the system **prohibits or requires**.

### Technique: Search for Keywords

| Keyword in prompt | Rule Type | BR Example |
|-------------------------------|---------------|---------------|
| *"unique"*, *"not duplicated"* | Uniqueness | `BR-001: Patient ID must be unique` |
| *"cannot"*, *"prohibited"* | Restriction | `BR-002: Cancelled order cannot be modified` |
| *"mandatory"*, *"required"* | Null validation | `BR-003: Product name must not be null or empty` |
| *"only if"*, *"whenever"* | Precondition | `BR-004: Appointment requires doctor to be available` |
| *"validate"*, *"verify"* | Format | `BR-005: Phone must match format` |
| *"greater than"*, *"minimum"* | Range | `BR-006: Quantity must be >= 1` |

### Practical Example

**Prompt:**
> *"The dish name is mandatory and cannot be repeated on the menu. The price must be greater than zero. Each dish belongs to a category."*

**Result in the SPEC:**

```markdown
## 2. Hard Business Rules

| Rule ID | Rule Description                         | Error Behavior                                 |
|---------|------------------------------------------|------------------------------------------------|
| BR-001  | Dish name must not be null or empty      | Throw InvalidDishException("Name required")    |
| BR-002  | Dish name must be unique in the menu     | Throw DuplicateDishException("Dish exists")    |
| BR-003  | Price must be greater than zero          | Throw InvalidDishException("Invalid price")    |
```

> **💡 Tip:** Every business rule implies **a custom exception** and at least **one test**. If you can't write an `assertThrows` for the rule, it's likely not a real business rule.

---

## Step 3: Detection of Transactional Flows

### What to do?

Identify operations that **affect more than one table or entity** — these require transactions.

### The Warning Signal

> When an action touches **two or more tables**, you need `setAutoCommit(false)` → `commit()` → `rollback()` in the Service.
> (See `CONSTITUTION.md §2.2`)

### Practical Example

**Prompt:**
> *"When a client places an order in the online store, the order is registered and the inventory is discounted for each product."*

**Analysis:**

```
Action "Create Order":
  1. INSERT INTO order (...)                     ← Order Table
  2. INSERT INTO order_details (...)             ← OrderDetails Table
  3. UPDATE product SET stock = stock - qty      ← Product Table
  
  → Are there 2+ tables? YES → Mandatory transactional operation
```

**Result in the SPEC:**

```markdown
| BR-005 | Order creation must be atomic (insert order + insert details + update stock) | Rollback all on failure |
```

### Common Errors

| ❌ Error | ✅ Correct |
|----------|------------|
| Treating an order as a simple INSERT | Defining it as a multi-step atomic operation |
| Putting the `commit()` in the DAO | The `commit()` ONLY goes in the Service (CONSTITUTION §2.2) |
| Ignoring the rollback if stock discount fails | Defining an explicit BR for atomicity |

---

## Step 4: Separation of Business vs. Infrastructure

### What to do?

The SPEC should describe the **WHAT** (the rule) and the **HOW** (the technical detail) separately, so the developer knows what goes in each layer.

### Technique: The Two-Column Table

| Business Aspect (WHAT) | Infrastructure Detail (HOW) |
|---------------------------|----------------------------------|
| *"The medical appointment lasts 30 minutes"* | Read the value from `app.properties` |
| *"The patient's email must be valid"* | Validate with regex in Service |
| *"Appointments cannot be scheduled outside business hours"* | Validate `hour >= 08:00 && hour <= 18:00` |
| *"Each doctor has a specialty"* | ENUM or String field in the Model |

### Where each thing goes in the SPEC

- **Business Rules (Section 2)** → The WHAT
- **Technical Notes (Section 4)** → The HOW

### Practical Example

**Prompt:**
> *"The clinic's system must calculate the consultation cost. If the patient has insurance, a 20% discount is applied."*

**In the SPEC:**

```markdown
## 2. Hard Business Rules
| CALC-001 | Consultation cost with insurance | basePrice * 0.80 |
| CALC-002 | Consultation cost without insurance | basePrice (no discount) |

## 4. Technical Notes
### 4.3 Configuration
- Insurance discount rate should be configurable via `app.properties`
- Key: `clinic.insurance.discount.rate=0.20`
```

---

## Step 5: Definition of Acceptance Criteria (BDD)

### What to do?

For **each business rule (BR-XXX)**, imagine the best and worst scenarios.

### Technique: The 3 Minimum Scenarios

| Type | Question you ask yourself | Result |
|------|----------------------|-----------|
| **Happy Path** 🟢 | *What if the user does everything right?* | The system completes the operation |
| **Validation** 🔴 | *What if a field is invalid?* | A specific exception is thrown |
| **Edge Case** 🟡 | *What happens at the boundary? (stock = 0, full table, closing time)* | Defined behavior at the boundary |

### Practical Example

**BR-004:** *"An appointment can only be scheduled if the doctor has availability at that time"*

```gherkin
### Scenario — Happy Path: Schedule appointment successfully
Given a Doctor with id = 1 and no appointments at 10:00
  And a Patient with id = 5
When  the service method scheduleAppointment() is called for 10:00
Then  the appointment is persisted successfully
  And the doctor's schedule is updated

### Scenario — Validation: Doctor not available
Given a Doctor with id = 1 who already has an appointment at 10:00
  And a Patient with id = 5
When  the service method scheduleAppointment() is called for 10:00
Then  a DoctorNotAvailableException is thrown with message "Doctor already has an appointment at this time"
  And no changes are persisted (rollback)

### Scenario — Edge Case: Outside business hours
Given a Doctor with id = 1
  And a Patient with id = 5
When  the service method scheduleAppointment() is called for 21:00
Then  an InvalidScheduleException is thrown with message "Appointments only between 08:00 and 18:00"
```

> 💡 **Result:** Each scenario becomes a `@Test` method in your code.

---

## 🎯 Summary of the Deduction Flow (Cheat Sheet)

```
┌──────────────────────────────────────────────────────────────────┐
│              DEDUCTION CHECKLIST                                 │
│                                                                  │
│  ✅  1. Underline Entities     → Dish, Client, Reservation      │
│  ✅  2. Isolate Constraints   → Unique name, Price > 0         │
│  ✅  3. Detect Calculations    → Discounts, totals, taxes       │
│  ✅  4. Detect Transactions   → Touches 2+ tables? → Atomic    │
│  ✅  5. Separate Business/Infra → WHAT (rule) vs. HOW (config)  │
│  ✅  6. Define Error Layers    → What exception will I throw?   │
│  ✅  7. Write Scenarios        → Happy + Validation + Edge Case │
│                                                                  │
│  📋 Result: SPEC_[Feature].md ready for the AI                   │
└──────────────────────────────────────────────────────────────────┘
```

---

## 🏋️ Guided Practical Exercise

Let's look at the full process with a real prompt.

### Prompt

> *"Develop an employee management module for an HR company. Employees have a name, ID, position, and salary. The ID is mandatory and cannot be repeated. The salary must be greater than or equal to the current minimum wage. When registering an employee, if the position does not exist in the catalog, it must be created automatically."*

### Step 1 — Entities

```
📋 SPEC_Employee.md   → name, ID, position, salary
📋 SPEC_Position.md   → name (might just be an auxiliary table)
```

> ⚠️ **Decision:** Is the position complex enough for its own SPEC? If it's just an `id + name`, it can be part of `SPEC_Employee.md`. If it has its own rules (unique name, hierarchical levels, salary ranges per position), it deserves its own SPEC.

### Step 2 — Business Rules

| Rule ID | Rule Description | Error Behavior |
|---------|-----------------|----------------|
| `BR-001` | Employee name must not be null or empty | Throw `InvalidEmployeeException("Name required")` |
| `BR-002` | ID must be unique | Throw `DuplicateEmployeeException("ID already registered")` |
| `BR-003` | Salary must be >= minimum wage | Throw `InvalidEmployeeException("Salary below minimum")` |
| `BR-004` | If position does not exist, create it automatically | Auto-insert position |

### Step 3 — Transactional Flows

```
BR-004 implies:
  1. SELECT position WHERE name = ?  (check if it exists)
  2. INSERT INTO position (...)      (create if it doesn't)
  3. INSERT INTO employee (...)      (create employee)

  → 2 tables → Mandatory transaction
```

### Step 4 — Business vs. Infrastructure

| Business | Infrastructure |
|---------|----------------|
| ID cannot be repeated | `SELECT COUNT(*) FROM employee WHERE id_number = ?` |
| Salary >= minimum | Read minimum value from `app.properties` |
| Auto-created position | Logic in Service: `findByName()` → if null → `save()` |

### Step 5 — BDD Scenarios

```gherkin
Scenario 1 — Happy Path
Given a valid Employee with name "Ana García", ID "1234567890",
      position "Developer", salary 3500000
  And position "Developer" exists in the database
When  the service method save() is called
Then  the employee is persisted successfully

Scenario 2 — Null name
Given an Employee with name = null
When  save() is called
Then  InvalidEmployeeException is thrown

Scenario 3 — Duplicate ID
Given an Employee with ID "1234567890" that already exists
When  save() is called
Then  DuplicateEmployeeException is thrown

Scenario 4 — Salary below minimum wage
Given an Employee with salary = 500000 (below minimum wage of 1300000)
When  save() is called
Then  InvalidEmployeeException is thrown with message "Salary below minimum"

Scenario 5 — Auto-create position
Given an Employee with position "Data Analyst" that does NOT exist
When  save() is called
Then  the position is created automatically
  And the employee is persisted with the new position
```

---

## 🚫 Common Errors in Specification Deduction

| ❌ Error | ✅ Solution |
|----------|------------|
| Mixing rules from several entities in one SPEC | One SPEC per entity/logical domain |
| Writing ambiguous rules: *"The name must be valid"* | Be specific: *"Name must not be null, empty, or exceed 100 chars"* |
| Forgetting edge cases | Always ask: *What happens if the value is 0? Or null? Or negative?* |
| Putting UI details in the SPEC | The SPEC is about business logic, not about buttons or screens |
| Not defining what exception is thrown | Each BR must have an explicit `Error Behavior` |
| Assuming a flow is simple CRUD | If it touches 2 tables → transaction. If it has rules → it's not CRUD |

---

## 💡 Senior's Advice

> Whenever you read a requirement, ask yourself these 4 questions in order:
>
> 1. **Who are the entities?** → Nouns from the prompt
> 2. **What can go wrong?** → Each answer is a business rule
> 3. **Is it a simple or compound operation?** → If compound → transaction
> 4. **How do I know it works?** → Each answer is a BDD scenario
>
> With these 4 questions, you can deduce a full SPEC from any prompt.

---

> **Next step:** Take your prompt, apply the 5 steps in this guide, and fill out a `SPEC_[Feature].md` using `SPEC_TEMPLATE.md`. Then follow the flow in the `USER_GUIDE.md` to feed the AI.
