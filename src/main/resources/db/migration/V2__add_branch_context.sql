-- Add branch context to patients table
ALTER TABLE patients ADD COLUMN branch_id UUID;

-- Add foreign key constraint (assuming branches table exists in a central location)
-- Note: In a microservices architecture, this might be a logical foreign key
-- ALTER TABLE patients ADD CONSTRAINT fk_patients_branch_id FOREIGN KEY (branch_id) REFERENCES branches(id);

-- Create indexes for efficient branch-based queries
CREATE INDEX idx_patients_branch_id ON patients(branch_id);
CREATE INDEX idx_patients_user_id_branch_id ON patients(user_id, branch_id);
CREATE INDEX idx_patients_branch_id_created_at ON patients(branch_id, created_at);
CREATE INDEX idx_patients_branch_id_gender ON patients(branch_id, gender);
CREATE INDEX idx_patients_branch_id_blood_group ON patients(branch_id, blood_group);

-- Add branch context to patient_addresses table
ALTER TABLE patient_addresses ADD COLUMN branch_id UUID;
CREATE INDEX idx_patient_addresses_branch_id ON patient_addresses(branch_id);
CREATE INDEX idx_patient_addresses_patient_id_branch_id ON patient_addresses(patient_id, branch_id);

-- Add branch context to patient_allergies table
ALTER TABLE patient_allergies ADD COLUMN branch_id UUID;
CREATE INDEX idx_patient_allergies_branch_id ON patient_allergies(branch_id);
CREATE INDEX idx_patient_allergies_patient_id_branch_id ON patient_allergies(patient_id, branch_id);

-- Add branch context to patient_appointments table
ALTER TABLE patient_appointments ADD COLUMN branch_id UUID;
CREATE INDEX idx_patient_appointments_branch_id ON patient_appointments(branch_id);
CREATE INDEX idx_patient_appointments_patient_id_branch_id ON patient_appointments(patient_id, branch_id);

-- Add branch context to patient_emergency_contacts table
ALTER TABLE patient_emergency_contacts ADD COLUMN branch_id UUID;
CREATE INDEX idx_patient_emergency_contacts_branch_id ON patient_emergency_contacts(branch_id);
CREATE INDEX idx_patient_emergency_contacts_patient_id_branch_id ON patient_emergency_contacts(patient_id, branch_id);

-- Add branch context to patient_insurance table
ALTER TABLE patient_insurance ADD COLUMN branch_id UUID;
CREATE INDEX idx_patient_insurance_branch_id ON patient_insurance(branch_id);
CREATE INDEX idx_patient_insurance_patient_id_branch_id ON patient_insurance(patient_id, branch_id);

-- Add branch context to patient_medical_history table
ALTER TABLE patient_medical_history ADD COLUMN branch_id UUID;
CREATE INDEX idx_patient_medical_history_branch_id ON patient_medical_history(branch_id);
CREATE INDEX idx_patient_medical_history_patient_id_branch_id ON patient_medical_history(patient_id, branch_id);

-- Add branch context to patient_medications table
ALTER TABLE patient_medications ADD COLUMN branch_id UUID;
CREATE INDEX idx_patient_medications_branch_id ON patient_medications(branch_id);
CREATE INDEX idx_patient_medications_patient_id_branch_id ON patient_medications(patient_id, branch_id);

-- Update existing records to use a default branch (if needed)
-- This should be done carefully in production
-- UPDATE patients SET branch_id = 'default-branch-uuid' WHERE branch_id IS NULL;
-- UPDATE patient_addresses SET branch_id = (SELECT branch_id FROM patients WHERE patients.id = patient_addresses.patient_id) WHERE branch_id IS NULL;
-- UPDATE patient_allergies SET branch_id = (SELECT branch_id FROM patients WHERE patients.id = patient_allergies.patient_id) WHERE branch_id IS NULL;
-- UPDATE patient_appointments SET branch_id = (SELECT branch_id FROM patients WHERE patients.id = patient_appointments.patient_id) WHERE branch_id IS NULL;
-- UPDATE patient_emergency_contacts SET branch_id = (SELECT branch_id FROM patients WHERE patients.id = patient_emergency_contacts.patient_id) WHERE branch_id IS NULL;
-- UPDATE patient_insurance SET branch_id = (SELECT branch_id FROM patients WHERE patients.id = patient_insurance.patient_id) WHERE branch_id IS NULL;
-- UPDATE patient_medical_history SET branch_id = (SELECT branch_id FROM patients WHERE patients.id = patient_medical_history.patient_id) WHERE branch_id IS NULL;
-- UPDATE patient_medications SET branch_id = (SELECT branch_id FROM patients WHERE patients.id = patient_medications.patient_id) WHERE branch_id IS NULL;