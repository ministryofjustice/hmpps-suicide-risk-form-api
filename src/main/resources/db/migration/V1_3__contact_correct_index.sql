ALTER TABLE public.contact
DROP CONSTRAINT xfk4_contact_locaation_address;

ALTER TABLE public.contact
    ADD CONSTRAINT xfk4_contact_locaation_address
        FOREIGN KEY (contact_location_id) REFERENCES public.address(id) ON DELETE No Action ON UPDATE No Action;