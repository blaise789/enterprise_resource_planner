-- PostgreSQL Trigger to generate email messages when payslips are approved

-- Function to generate the message content
CREATE OR REPLACE FUNCTION generate_salary_message()
RETURNS TRIGGER AS $$
DECLARE
    employee_firstname VARCHAR;
    employee_code VARCHAR;
    month_name VARCHAR;
    institution VARCHAR := 'Rwanda Government';  -- This could be parameterized or stored in a settings table
BEGIN
    -- Get employee details from the employee_code foreign key
    SELECT e.first_name, e.code INTO employee_firstname, employee_code
    FROM employees e
    JOIN payslips p ON e.code = p.employee_code
    WHERE p.id = NEW.id;

    -- Convert month number to month name
    SELECT TO_CHAR(TO_DATE(NEW.month::text, 'MM'), 'Month') INTO month_name;

    -- Create message record
    INSERT INTO messages (
        employee_code,
        message_content,
        sent_at,
        month,
        year,
        email_sent_status
    ) VALUES (
        employee_code,
        'Dear ' || employee_firstname || ', your salary for ' || month_name || '/' || NEW.year || 
        ' from ' || institution || ' amounting to ' || NEW.net_salary || 
        ' has been credited to your account ' || employee_code || ' successfully.',
        NOW(),
        NEW.month,
        NEW.year,
        'UNSENT'
    );

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create the trigger
-- First, check if the trigger exists and drop it if it does
DO $$
BEGIN
    -- Check if the trigger exists
    IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'payslip_approval_trigger') THEN
        -- Drop the trigger if it exists
        DROP TRIGGER payslip_approval_trigger ON payslips;
    END IF;
END $$;

-- Create the trigger
CREATE TRIGGER payslip_approval_trigger
AFTER UPDATE ON payslips
FOR EACH ROW
WHEN (OLD.status = 'PENDING' AND NEW.status = 'PAID')
EXECUTE FUNCTION generate_salary_message();

-- Instructions for applying this trigger:
-- 1. Connect to your PostgreSQL database
-- 2. Run this script to create the trigger
-- 3. The trigger will automatically generate messages when payslips are approved
--
-- Example command to run this script:
-- psql -U postgres -d erp -f create_email_trigger.sql
