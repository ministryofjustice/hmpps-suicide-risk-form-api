ALTER TABLE public.suicide_risk
    ALTER COLUMN review_required_date TYPE timestamp with time zone USING review_required_date AT TIME ZONE 'Europe/London';

ALTER TABLE public.suicide_risk
    ALTER COLUMN terminated_unterminated_date TYPE timestamp with time zone USING terminated_unterminated_date AT TIME ZONE 'Europe/London';
