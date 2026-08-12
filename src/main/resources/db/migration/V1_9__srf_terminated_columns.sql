ALTER TABLE public.suicide_risk
    ADD COLUMN terminated boolean NULL;

ALTER TABLE public.suicide_risk
    ADD COLUMN terminated_unterminated_date timestamp NULL;
