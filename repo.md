# Patient Service - Comprehensive Documentation

## 🏥 Overview

The Patient Service is a comprehensive microservice that manages all patient-related information in the Tiny Steps healthcare platform. It handles patient profiles, medical history, allergies, medications, emergency contacts, insurance, and provides advanced search and health summary capabilities. This service is built using Spring Boot 3.5.4 with Java 21 and PostgreSQL as the database.

## 🚀 Quick Start

### Prerequisites
- Java 21
- PostgreSQL 15+
- Maven 3.8+
- Running Eureka Service Registry (port 8761)
- Running Auth Service (port 8081)

### Running the Service
```bash
cd patient-service
mvn spring-boot:run
```
The service will start on port **8085**.

## 🏗️ Architecture & Dependencies

### Core Dependencies

| Dependency | Purpose | Real-World Example |
|------------|---------|-------------------|
| **Spring Boot Starter Web** | REST API framework | Handles HTTP requests like "GET /api/v1/patients" |
| **Spring Boot Starter Data JPA** | Database operations | Saves patient data to PostgreSQL automatically |
| **Spring Boot Starter Security** | Authentication & authorization | Ensures only doctors can view patient medical records |
| **Spring Boot Starter OAuth2 Resource Server** | JWT token validation | Validates tokens from Auth Service for secure access |
| **Spring Cloud Netflix Eureka Client** | Service discovery | Allows other services to find this service automatically |
| **Flyway** | Database migrations | Manages database schema changes safely |
| **MapStruct** | Object mapping | Converts between DTOs and entities efficiently |
| **Resilience4j** | Circuit breaker, retry, timeout | Handles failures when calling other services gracefully |
| **PostgreSQL Driver** | Database connectivity | Connects to PostgreSQL database |
| **Lombok** | Code generation | Reduces boilerplate code for getters/setters |

### Integration Dependencies

The service integrates with three external services:

1. **User Service** - Validates patient user accounts and retrieves user information
2. **Address Service** - Manages patient addresses and location data
3. **Schedule Service** - Handles appointment scheduling and session management

## 🔐 Security Configuration

### Authentication & Authorization
- **JWT-based authentication**: All requests require valid JWT tokens from Auth Service
- **Role-based access control**: Different permissions for ADMIN, DOCTOR, and PATIENT roles
- **Resource ownership validation**: Patients can only access their own data

### Security Rules Examples:
- **Patients** can only view/edit their own medical records
- **Doctors** can view patient records they're treating
- **Admins** have full access to all patient data and statistics
- **Public endpoints**: None (all endpoints require authentication)

### Real-World Security Scenario:
When a patient tries to view their medical history:
1. System validates JWT token from login
2. Checks if user has PATIENT role
3. Verifies the patient is accessing their own data (not someone else's)
4. Only then allows access to medical records

## 📊 Database Schema

### Core Tables and Their Purpose

#### 1. `patients` - Main Patient Profile
**Purpose**: Stores basic patient demographic and medical information
**Real-World Example**: When John Doe registers as a patient, his basic info is stored here

| Column | Purpose | Example |
|--------|---------|---------|
| `id` | Unique patient identifier | `550e8400-e29b-41d4-a716-446655440000` |
| `user_id` | Links to User Service | References user account in User Service |
| `date_of_birth` | Patient's birth date | `1990-05-15` |
| `gender` | Patient's gender | `MALE`, `FEMALE`, `OTHER` |
| `blood_group` | Blood type | `O+`, `A-`, `B+`, `AB-` |
| `height_cm` | Height in centimeters | `175` |
| `weight_kg` | Weight in kilograms | `70.5` |
| `created_at` | When record was created | `2024-01-15 10:30:00` |
| `updated_at` | Last modification time | `2024-01-20 14:45:00` |

#### 2. `patient_medical_history` - Medical Conditions
**Purpose**: Tracks all medical conditions and diagnoses
**Real-World Example**: Records that John has diabetes and hypertension

| Column | Purpose | Example |
|--------|---------|---------|
| `id` | Unique record identifier | Auto-generated UUID |
| `patient_id` | Links to patient | References patients table |
| `condition` | Medical condition name | `Type 2 Diabetes`, `Hypertension` |
| `notes` | Additional details | `Diagnosed in 2020, well controlled` |
| `recorded_at` | When condition was recorded | `2024-01-15 09:00:00` |

#### 3. `patient_allergies` - Known Allergies
**Purpose**: Stores patient allergies and reactions for safety
**Real-World Example**: Records that Sarah is allergic to penicillin

| Column | Purpose | Example |
|--------|---------|---------|
| `id` | Unique allergy record | Auto-generated UUID |
| `patient_id` | Links to patient | References patients table |
| `allergen` | What causes the allergy | `Penicillin`, `Peanuts`, `Latex` |
| `reaction` | How patient reacts | `Severe rash`, `Anaphylaxis`, `Breathing difficulty` |
| `recorded_at` | When allergy was recorded | `2024-01-10 11:30:00` |

#### 4. `patient_medications` - Current Medications
**Purpose**: Tracks all medications patient is taking
**Real-World Example**: Records that Mike takes Metformin for diabetes

| Column | Purpose | Example |
|--------|---------|---------|
| `id` | Unique medication record | Auto-generated UUID |
| `patient_id` | Links to patient | References patients table |
| `medication_name` | Name of medication | `Metformin`, `Lisinopril`, `Aspirin` |
| `dosage` | How much to take | `500mg twice daily`, `10mg once daily` |
| `start_date` | When medication started | `2024-01-01` |
| `end_date` | When medication ends | `2024-12-31` (null if ongoing) |

#### 5. `patient_emergency_contacts` - Emergency Contacts
**Purpose**: Stores emergency contact information for urgent situations
**Real-World Example**: Records that John's wife Mary is his emergency contact

| Column | Purpose | Example |
|--------|---------|---------|
| `id` | Unique contact record | Auto-generated UUID |
| `patient_id` | Links to patient | References patients table |
| `name` | Contact person's name | `Mary Doe`, `Dr. Smith` |
| `relationship` | How they're related | `Spouse`, `Parent`, `Doctor`, `Friend` |
| `phone` | Contact phone number | `+1-555-123-4567` |

#### 6. `patient_insurance` - Insurance Information
**Purpose**: Stores patient insurance details for billing
**Real-World Example**: Records that Lisa has Blue Cross Blue Shield insurance

| Column | Purpose | Example |
|--------|---------|---------|
| `id` | Unique insurance record | Auto-generated UUID |
| `patient_id` | Links to patient | References patients table |
| `provider` | Insurance company | `Blue Cross Blue Shield`, `Aetna`, `Cigna` |
| `policy_number` | Insurance policy number | `BC123456789` |
| `coverage_details` | What's covered | `Full coverage with $20 copay` |

#### 7. `patient_addresses` - Patient Addresses
**Purpose**: Links patients to their addresses (stored in Address Service)
**Real-World Example**: Links John to his home address in Address Service

| Column | Purpose | Example |
|--------|---------|---------|
| `id` | Unique address link | Auto-generated UUID |
| `patient_id` | Links to patient | References patients table |
| `address_id` | Links to Address Service | References address in Address Service |

#### 8. `patient_appointments` - Appointment Links
**Purpose**: Links patients to their appointments (stored in Schedule Service)
**Real-World Example**: Links Sarah to her cardiology appointment

| Column | Purpose | Example |
|--------|---------|---------|
| `id` | Unique appointment link | Auto-generated UUID |
| `patient_id` | Links to patient | References patients table |
| `appointment_id` | Links to Schedule Service | References appointment in Schedule Service |

## 🎯 API Endpoints - Complete Guide

### 1. Patient Management Controller (`/api/v1/patients`)

#### Basic CRUD Operations

**Create Patient**
- **Endpoint**: `POST /api/v1/patients`
- **Who can use**: ADMIN, PATIENT
- **Purpose**: Register a new patient in the system
- **Real-world example**: When John Doe signs up as a new patient
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "bloodGroup": "O+",
  "heightCm": 175,
  "weightKg": 70.5
}
```

**Get Patient by ID**
- **Endpoint**: `GET /api/v1/patients/{id}`
- **Who can use**: ADMIN, DOCTOR, or the patient themselves
- **Purpose**: Retrieve complete patient information
- **Real-world example**: Doctor viewing patient details before appointment

**Get All Patients (Paginated)**
- **Endpoint**: `GET /api/v1/patients?page=0&size=10`
- **Who can use**: ADMIN, DOCTOR
- **Purpose**: List all patients with pagination
- **Real-world example**: Hospital admin viewing patient registry

**Update Patient**
- **Endpoint**: `PUT /api/v1/patients/{id}`
- **Who can use**: ADMIN or the patient themselves
- **Purpose**: Update complete patient information
- **Real-world example**: Patient updating their profile after moving

**Partial Update Patient**
- **Endpoint**: `PATCH /api/v1/patients/{id}`
- **Who can use**: ADMIN or the patient themselves
- **Purpose**: Update specific fields only
- **Real-world example**: Patient updating just their weight after a visit

**Delete Patient**
- **Endpoint**: `DELETE /api/v1/patients/{id}`
- **Who can use**: ADMIN only
- **Purpose**: Remove patient from system (rare, usually for data cleanup)

#### Search Operations

**Find by User ID**
- **Endpoint**: `GET /api/v1/patients/user/{userId}`
- **Purpose**: Find patient record using their user account ID
- **Real-world example**: System linking user login to patient profile

**Search by Gender**
- **Endpoint**: `GET /api/v1/patients/search/gender?gender=FEMALE&page=0&size=10`
- **Who can use**: ADMIN, DOCTOR
- **Purpose**: Find patients by gender for research or statistics
- **Real-world example**: Finding all female patients for breast cancer screening

**Search by Blood Group**
- **Endpoint**: `GET /api/v1/patients/search/blood-group?bloodGroup=O+&page=0&size=10`
- **Who can use**: ADMIN, DOCTOR
- **Purpose**: Find patients by blood type
- **Real-world example**: Emergency blood donor matching

**Search by Age Range**
- **Endpoint**: `GET /api/v1/patients/search/age-range?minAge=18&maxAge=65&page=0&size=10`
- **Who can use**: ADMIN, DOCTOR
- **Purpose**: Find patients within specific age range
- **Real-world example**: Finding adults for vaccination campaign

#### Business Operations

**Update Medical Information**
- **Endpoint**: `PATCH /api/v1/patients/{id}/medical-info?heightCm=180&weightKg=75&bloodGroup=A+`
- **Who can use**: ADMIN, DOCTOR, or the patient themselves
- **Purpose**: Update height, weight, and blood group
- **Real-world example**: Nurse updating vitals after patient visit

**Update Personal Information**
- **Endpoint**: `PATCH /api/v1/patients/{id}/personal-info?dateOfBirth=1990-05-15&gender=MALE`
- **Who can use**: ADMIN or the patient themselves
- **Purpose**: Update birth date and gender
- **Real-world example**: Correcting incorrect birth date in records

**Calculate Age**
- **Endpoint**: `GET /api/v1/patients/{id}/age`
- **Who can use**: ADMIN, DOCTOR, or the patient themselves
- **Purpose**: Get current age of patient
- **Real-world example**: Doctor checking patient age for age-appropriate treatment
- **Response**: `{"status": "OK", "data": 34, "message": "Age calculated successfully"}`

**Calculate BMI**
- **Endpoint**: `GET /api/v1/patients/{id}/bmi`
- **Who can use**: ADMIN, DOCTOR, or the patient themselves
- **Purpose**: Calculate Body Mass Index
- **Real-world example**: Nutritionist assessing patient's weight status
- **Response**: `{"status": "OK", "data": 22.86, "message": "BMI calculated successfully"}`

#### Profile Completeness

**Check Profile Completeness**
- **Endpoint**: `GET /api/v1/patients/{id}/profile-completeness`
- **Purpose**: Get percentage of completed profile fields
- **Real-world example**: System prompting patient to complete missing information
- **Response**: `{"status": "OK", "data": 85, "message": "Profile completeness calculated"}`

**Get Missing Profile Fields**
- **Endpoint**: `GET /api/v1/patients/{id}/missing-fields`
- **Purpose**: List which profile fields are missing
- **Real-world example**: Registration wizard showing what info is still needed
- **Response**: `{"status": "OK", "data": ["bloodGroup", "height"], "message": "Missing fields retrieved"}`

#### Statistics (Admin Only)

**Get Patient Statistics**
- **Endpoint**: `GET /api/v1/patients/statistics`
- **Who can use**: ADMIN only
- **Purpose**: Get comprehensive patient statistics
- **Real-world example**: Hospital administrator generating monthly reports
```json
{
  "totalPatients": 1250,
  "averageAge": 42.5,
  "averageHeight": 170.2,
  "averageWeight": 68.7,
  "genderStats": [["MALE", 620], ["FEMALE", 630]],
  "bloodGroupStats": [["O+", 450], ["A+", 320], ["B+", 280], ["AB+", 200]],
  "distinctBloodGroups": ["O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"]
}
```

### 2. Medical History Controller (`/api/v1/patient-medical-history`)

**Add Medical History**
- **Endpoint**: `POST /api/v1/patient-medical-history`
- **Who can use**: ADMIN, DOCTOR, or the patient themselves
- **Purpose**: Record a new medical condition
- **Real-world example**: Doctor adding diabetes diagnosis after blood test
```json
{
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "condition": "Type 2 Diabetes",
  "notes": "Diagnosed after HbA1c test showed 7.2%. Patient advised on diet changes."
}
```

**Get Medical History by Patient**
- **Endpoint**: `GET /api/v1/patient-medical-history/patient/{patientId}`
- **Purpose**: Get all medical conditions for a patient
- **Real-world example**: Doctor reviewing patient's complete medical history before surgery

**Add Medical History (Simplified)**
- **Endpoint**: `POST /api/v1/patient-medical-history/patient/{patientId}/add?condition=Hypertension&notes=Stage 1, controlled with medication`
- **Purpose**: Quick way to add medical condition
- **Real-world example**: Nurse quickly adding new diagnosis during patient visit

**Get Recent Medical History**
- **Endpoint**: `GET /api/v1/patient-medical-history/patient/{patientId}/recent?daysBack=90`
- **Purpose**: Get medical history from last N days
- **Real-world example**: Doctor reviewing recent developments before follow-up appointment

**Get Chronic Conditions**
- **Endpoint**: `GET /api/v1/patient-medical-history/patient/{patientId}/chronic`
- **Purpose**: Get long-term medical conditions
- **Real-world example**: Insurance company assessing pre-existing conditions

**Medical History Statistics (Admin Only)**
- **Endpoint**: `GET /api/v1/patient-medical-history/statistics`
- **Purpose**: Get statistics about medical conditions
- **Real-world example**: Public health officials tracking disease prevalence
```json
{
  "totalHistory": 5420,
  "conditionStats": [["Diabetes", 450], ["Hypertension", 380], ["Asthma", 220]],
  "distinctConditions": ["Diabetes", "Hypertension", "Asthma", "Heart Disease"],
  "patientsWithMultipleRecords": ["uuid1", "uuid2"],
  "patientsWithoutHistory": ["uuid3", "uuid4"]
}
```

### 3. Allergy Management Controller (`/api/v1/patient-allergies`)

**Add Allergy**
- **Endpoint**: `POST /api/v1/patient-allergies`
- **Purpose**: Record a new allergy
- **Real-world example**: Patient reporting penicillin allergy during admission
```json
{
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "allergen": "Penicillin",
  "reaction": "Severe skin rash and difficulty breathing"
}
```

**Get Patient Allergies**
- **Endpoint**: `GET /api/v1/patient-allergies/patient/{patientId}`
- **Purpose**: Get all allergies for a patient
- **Real-world example**: Pharmacist checking allergies before dispensing medication

**Get Critical Allergies**
- **Endpoint**: `GET /api/v1/patient-allergies/patient/{patientId}/critical`
- **Purpose**: Get life-threatening allergies
- **Real-world example**: Emergency room staff checking for critical allergies
- **Response**: `["Penicillin", "Shellfish", "Latex"]`

**Search by Allergen**
- **Endpoint**: `GET /api/v1/patient-allergies/search/allergen?allergen=Penicillin&page=0&size=10`
- **Purpose**: Find all patients allergic to specific substance
- **Real-world example**: Hospital avoiding penicillin for all allergic patients

**Allergy Statistics (Admin Only)**
- **Endpoint**: `GET /api/v1/patient-allergies/statistics`
- **Purpose**: Get allergy statistics
- **Real-world example**: Hospital tracking most common allergies for staff training

### 4. Medication Management Controller (`/api/v1/patient-medications`)

**Add Medication**
- **Endpoint**: `POST /api/v1/patient-medications`
- **Purpose**: Record a new medication
- **Real-world example**: Doctor prescribing blood pressure medication
```json
{
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "medicationName": "Lisinopril",
  "dosage": "10mg once daily",
  "startDate": "2024-01-15",
  "endDate": null
}
```

**Get Current Medications**
- **Endpoint**: `GET /api/v1/patient-medications/patient/{patientId}/current`
- **Purpose**: Get medications patient is currently taking
- **Real-world example**: Pharmacist checking for drug interactions

**Get Expiring Medications**
- **Endpoint**: `GET /api/v1/patient-medications/patient/{patientId}/expiring?days=30`
- **Purpose**: Find medications expiring soon
- **Real-world example**: System alerting patient to renew prescriptions

**Start Medication**
- **Endpoint**: `POST /api/v1/patient-medications/patient/{patientId}/start?medicationName=Aspirin&dosage=81mg daily`
- **Purpose**: Start a new medication
- **Real-world example**: Doctor starting blood thinner after heart attack

**Stop Medication**
- **Endpoint**: `POST /api/v1/patient-medications/patient/{patientId}/stop?medicationName=Aspirin&endDate=2024-02-01`
- **Purpose**: Stop a medication
- **Real-world example**: Doctor stopping medication due to side effects

**Medication Statistics (Admin Only)**
- **Endpoint**: `GET /api/v1/patient-medications/statistics`
- **Purpose**: Get medication usage statistics
- **Real-world example**: Pharmacy tracking most prescribed medications

### 5. Emergency Contacts Controller (`/api/v1/patient-emergency-contacts`)

**Add Emergency Contact**
- **Endpoint**: `POST /api/v1/patient-emergency-contacts`
- **Purpose**: Add emergency contact for patient
- **Real-world example**: Patient adding spouse as emergency contact during registration
```json
{
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Mary Doe",
  "relationship": "Spouse",
  "phone": "+1-555-123-4567"
}
```

**Get Emergency Contacts**
- **Endpoint**: `GET /api/v1/patient-emergency-contacts/patient/{patientId}`
- **Purpose**: Get all emergency contacts for patient
- **Real-world example**: Hospital calling family during emergency surgery

**Add Emergency Contact (Simplified)**
- **Endpoint**: `POST /api/v1/patient-emergency-contacts/patient/{patientId}/add?name=John Smith&relationship=Brother&phone=555-987-6543`
- **Purpose**: Quick way to add emergency contact
- **Real-world example**: Nurse quickly adding contact during admission

**Emergency Contact Statistics (Admin Only)**
- **Endpoint**: `GET /api/v1/patient-emergency-contacts/statistics`
- **Purpose**: Get emergency contact statistics
- **Real-world example**: Hospital ensuring all patients have emergency contacts

### 6. Insurance Management Controller (`/api/v1/patient-insurance`)

**Add Insurance**
- **Endpoint**: `POST /api/v1/patient-insurance`
- **Purpose**: Add insurance information
- **Real-world example**: Patient providing insurance details for billing
```json
{
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "provider": "Blue Cross Blue Shield",
  "policyNumber": "BC123456789",
  "coverageDetails": "Full coverage with $20 copay for primary care"
}
```

**Get Patient Insurance**
- **Endpoint**: `GET /api/v1/patient-insurance/patient/{patientId}`
- **Purpose**: Get insurance information for patient
- **Real-world example**: Billing department verifying insurance before procedure

**Insurance Statistics (Admin Only)**
- **Endpoint**: `GET /api/v1/patient-insurance/statistics`
- **Purpose**: Get insurance statistics
- **Real-world example**: Hospital tracking insurance provider distribution

### 7. Advanced Search Controller (`/api/v1/patient-advanced-search`)

**Search by Medical Condition**
- **Endpoint**: `GET /api/v1/patient-advanced-search/medical-condition?condition=Diabetes&page=0&size=10`
- **Who can use**: ADMIN, DOCTOR
- **Purpose**: Find patients with specific medical condition
- **Real-world example**: Endocrinologist finding all diabetic patients for study

**Search by Medication**
- **Endpoint**: `GET /api/v1/patient-advanced-search/medication?medication=Insulin&page=0&size=10`
- **Purpose**: Find patients taking specific medication
- **Real-world example**: Pharmacy tracking insulin users for supply planning

**Search by Allergen**
- **Endpoint**: `GET /api/v1/patient-advanced-search/allergen?allergen=Penicillin&page=0&size=10`
- **Purpose**: Find patients allergic to specific substance
- **Real-world example**: Hospital avoiding penicillin for allergic patients

**Find Patients with Critical Allergies**
- **Endpoint**: `GET /api/v1/patient-advanced-search/critical-allergies`
- **Purpose**: Find all patients with life-threatening allergies
- **Real-world example**: Emergency department preparing allergy alert list

**Find Patients without Emergency Contacts**
- **Endpoint**: `GET /api/v1/patient-advanced-search/without-emergency-contacts`
- **Purpose**: Find patients missing emergency contacts
- **Real-world example**: Registration staff following up on incomplete profiles

**Find Patients with Multiple Medications**
- **Endpoint**: `GET /api/v1/patient-advanced-search/multiple-medications?minimumMedications=5`
- **Purpose**: Find patients taking many medications
- **Real-world example**: Pharmacist identifying patients at risk for drug interactions

**Find High-Risk Patients**
- **Endpoint**: `GET /api/v1/patient-advanced-search/high-risk`
- **Purpose**: Find patients with multiple risk factors
- **Real-world example**: Care coordinator identifying patients needing extra attention

**Multi-Criteria Search**
- **Endpoint**: `GET /api/v1/patient-advanced-search/multi-criteria?medicalCondition=Diabetes&medication=Insulin&gender=FEMALE&minAge=40&maxAge=70`
- **Purpose**: Complex search with multiple filters
- **Real-world example**: Researcher finding specific patient population for clinical trial

### 8. Health Summary Controller (`/api/v1/patient-health-summary`)

**Get Complete Health Summary**
- **Endpoint**: `GET /api/v1/patient-health-summary/{patientId}`
- **Purpose**: Get comprehensive patient health overview
- **Real-world example**: Doctor getting complete picture before appointment
```json
{
  "patient": {...},
  "allergies": [...],
  "currentMedications": [...],
  "emergencyContacts": [...],
  "insurance": [...],
  "medicalHistory": [...],
  "addresses": [...],
  "appointments": [...]
}
```

**Get Patient Dashboard**
- **Endpoint**: `GET /api/v1/patient-health-summary/{patientId}/dashboard`
- **Purpose**: Get key health metrics for patient dashboard
- **Real-world example**: Patient portal showing health overview
```json
{
  "patient": {...},
  "profileCompleteness": 85,
  "criticalAllergies": ["Penicillin"],
  "activeMedications": ["Lisinopril", "Metformin"],
  "expiringMedications": [...],
  "emergencyContacts": [...],
  "recentMedicalHistory": [...],
  "hasInsurance": true,
  "hasEmergencyContacts": true,
  "hasCriticalAllergies": true
}
```

**Get Safety Alerts**
- **Endpoint**: `GET /api/v1/patient-health-summary/{patientId}/safety-alerts`
- **Purpose**: Get critical safety information
- **Real-world example**: Emergency room getting immediate safety alerts
```json
{
  "criticalAllergies": ["Penicillin", "Shellfish"],
  "activeMedications": ["Warfarin", "Aspirin"],
  "expiringMedications": [...],
  "emergencyContacts": [...],
  "hasMultipleMedications": true,
  "hasCriticalAllergies": true,
  "hasEmergencyContacts": true
}
```

**Get Care Plan**
- **Endpoint**: `GET /api/v1/patient-health-summary/{patientId}/care-plan`
- **Purpose**: Get comprehensive care plan
- **Real-world example**: Care coordinator planning patient treatment
```json
{
  "patient": {...},
  "currentMedications": [...],
  "chronicConditions": ["Type 2 Diabetes", "Hypertension"],
  "criticalAllergies": ["Penicillin"],
  "upcomingAppointments": [...]
}
```

## 🔧 Service Layer Architecture

The service follows a layered architecture pattern:

### Service Interfaces
Each domain has a service interface defining business operations:
- `PatientService` - Core patient operations
- `PatientAllergyService` - Allergy management
- `PatientMedicationService` - Medication tracking
- `PatientMedicalHistoryService` - Medical history
- `PatientEmergencyContactService` - Emergency contacts
- `PatientInsuranceService` - Insurance management

### Service Implementation
All services use `@Service` and `@Transactional` annotations:
- `@Transactional(readOnly=true)` for read operations
- `@Transactional` for data modifications
- Comprehensive error handling with custom exceptions
- Business logic validation

### Repository Layer
JPA repositories with custom query methods:
- Standard CRUD operations
- Custom finder methods
- Statistical queries
- Pagination support

## 🌐 Integration Services

### 1. User Service Integration
**Purpose**: Validates patient user accounts and retrieves user information
**Circuit Breaker**: Configured with 50% failure threshold
**Retry**: 3 attempts with 1-second delay
**Timeout**: 10 seconds

**Methods**:
- `getUserById(UUID userId)` - Get user details
- `validateUser(UUID userId)` - Check if user exists

**Real-world example**: When creating a patient profile, system validates the user account exists in User Service

### 2. Address Service Integration
**Purpose**: Manages patient addresses and location data
**Circuit Breaker**: Same configuration as User Service

**Real-world example**: When patient updates their address, system stores the address in Address Service and links it to patient

### 3. Schedule Service Integration
**Purpose**: Handles appointment scheduling and session management

**Real-world example**: When viewing patient health summary, system retrieves upcoming appointments from Schedule Service

## 🛡️ Error Handling & Resilience

### Circuit Breaker Pattern
- **Closed State**: Normal operation
- **Open State**: Fails fast when service is down
- **Half-Open State**: Tests if service is back up

### Retry Mechanism
- Automatic retry on transient failures
- Exponential backoff to avoid overwhelming services
- Maximum 3 attempts per request

### Fallback Methods
- Graceful degradation when external services fail
- Default responses to maintain functionality
- Logging for monitoring and debugging

## 🧪 Testing & Development

### API Testing
- **Swagger UI**: Available at `http://localhost:8085/swagger-ui.html` (when implemented)
- **Postman Collection**: Create collection with all endpoints
- **Integration Tests**: Run with `mvn test`

### Database Testing
- **H2 In-Memory Database**: For unit tests
- **PostgreSQL**: For integration tests
- **Flyway Migrations**: Ensure consistent schema

### Security Testing
- Test role-based access control
- Verify JWT token validation
- Check resource ownership validation

## 📈 Monitoring & Statistics

### Available Statistics Endpoints
1. **Patient Statistics**: Demographics, averages, distributions
2. **Medical History Statistics**: Condition prevalence
3. **Allergy Statistics**: Common allergens and reactions
4. **Medication Statistics**: Most prescribed medications
5. **Emergency Contact Statistics**: Contact relationship patterns
6. **Insurance Statistics**: Provider distributions

### Real-World Usage
- **Hospital Administration**: Monthly patient reports
- **Public Health**: Disease surveillance
- **Quality Assurance**: Profile completeness tracking
- **Research**: Patient population analysis

## 🚀 Development Setup

### Local Development
1. Clone repository
2. Install Java 21 and PostgreSQL
3. Create database: `createdb patients`
4. Update `application.yml` with your database credentials
5. Run `mvn clean install`
6. Start dependent services (Eureka, Auth Service)
7. Run `mvn spring-boot:run`

### Environment Configuration
- **Development**: `application.yml`
- **Testing**: `application-test.yml`
- **Production**: Environment variables override

---

## 📞 Support

For technical support or questions about the Patient Service:
- **Documentation**: This README and API documentation
- **Database Schema**: Check Flyway migration files in `src/main/resources/db/migration/`
- **Integration Testing**: Use provided examples and test cases

---

*This documentation covers all aspects of the Patient Service. Each endpoint includes real-world examples to help understand practical usage scenarios.*
