ALTER TABLE public.contact
    ADD COLUMN send_form_manually boolean NULL,
    ADD COLUMN send_form_via_email boolean NULL;
