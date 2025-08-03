CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Core patient profile (links to User Service)
CREATE TABLE patients (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          user_id UUID NOT NULL,                -- FK to User Service user.id
                          date_of_birth DATE,
                          gender VARCHAR(10),                   -- MALE, FEMALE, OTHER
                          blood_group VARCHAR(5),
                          height_cm INT,
                          weight_kg DECIMAL(5,2),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Patient contact addresses (links to Address Service)
CREATE TABLE patient_addresses (
                                   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   patient_id UUID REFERENCES patients(id) ON DELETE CASCADE,
                                   address_id UUID NOT NULL               -- FK to Address Service addresses.id
);

-- Emergency contacts
CREATE TABLE patient_emergency_contacts (
                                            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                            patient_id UUID REFERENCES patients(id) ON DELETE CASCADE,
                                            name VARCHAR(100) NOT NULL,
                                            relationship VARCHAR(50),
                                            phone VARCHAR(20) NOT NULL
);

-- Medical history entries
CREATE TABLE patient_medical_history (
                                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                         patient_id UUID REFERENCES patients(id) ON DELETE CASCADE,
                                         condition VARCHAR(255) NOT NULL,
                                         notes TEXT,
                                         recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Known allergies
CREATE TABLE patient_allergies (
                                   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   patient_id UUID REFERENCES patients(id) ON DELETE CASCADE,
                                   allergen VARCHAR(100) NOT NULL,
                                   reaction VARCHAR(255),
                                   recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Current medications
CREATE TABLE patient_medications (
                                     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                     patient_id UUID REFERENCES patients(id) ON DELETE CASCADE,
                                     medication_name VARCHAR(100) NOT NULL,
                                     dosage VARCHAR(50),
                                     start_date DATE,
                                     end_date DATE
);

-- Insurance details
CREATE TABLE patient_insurance (
                                   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   patient_id UUID REFERENCES patients(id) ON DELETE CASCADE,
                                   provider VARCHAR(100),
                                   policy_number VARCHAR(50),
                                   coverage_details TEXT
);

-- Appointment references (Schedule Service)
CREATE TABLE patient_appointments (
                                      id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                      patient_id UUID REFERENCES patients(id) ON DELETE CASCADE,
                                      appointment_id UUID NOT NULL          -- FK to Schedule Service appointments.id
);
