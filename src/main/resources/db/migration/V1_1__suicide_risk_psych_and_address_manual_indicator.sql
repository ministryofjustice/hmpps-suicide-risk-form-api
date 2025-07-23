ALTER TABLE public.suicide_risk
ALTER COLUMN current_psych_treatment TYPE text;

ALTER TABLE public.address
ALTER COLUMN address_id DROP NOT NULL;

ALTER TABLE public.address
ADD COLUMN manually_added boolean NULL;