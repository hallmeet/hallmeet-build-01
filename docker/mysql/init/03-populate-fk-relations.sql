-- Migration: Populate FK columns from string references (Requirement #6)
-- Run after Hibernate has created the new columns (college_id, student_id, exam_id).
-- Safe to run multiple times: only updates rows where FK is null and string match exists.

USE hall_ticket;

-- Populate student.college_id from CollegeName (match college_model.CollegeName)
UPDATE student_model s
INNER JOIN college_model c ON TRIM(s.college_name) = TRIM(c.college_name) AND c.deleted = 0
SET s.college_id = c.id
WHERE s.college_id IS NULL AND s.deleted = 0;

-- Populate hall_ticket_model.student_id from email (match student_model.email)
UPDATE hall_ticket_model h
INNER JOIN student_model s ON TRIM(h.email) = TRIM(s.email) AND s.deleted = 0
SET h.student_id = s.id
WHERE h.student_id IS NULL AND h.deleted = 0;

-- Populate hall_ticket_model.exam_id from ExamName (match exam_model.exam_name)
UPDATE hall_ticket_model h
INNER JOIN exam_model e ON TRIM(h.exam_name) = TRIM(e.exam_name) AND e.deleted = 0
SET h.exam_id = e.id
WHERE h.exam_id IS NULL AND h.deleted = 0;
