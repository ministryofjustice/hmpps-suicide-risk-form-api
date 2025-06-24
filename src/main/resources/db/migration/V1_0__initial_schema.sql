CREATE TABLE public.suicide_risk(id uuid not null primary key,
                           crn char(7) not null,
                           title_and_full_name varchar(200) NULL,
                           date_of_letter date NULL,
                           sheet_sent_by varchar(200),
                           telephone_number varchar(35) NULL,
                           signature varchar(200) NULL,
                           completed_date timestamp with time zone NULL,
                           nature_of_risk text NULL,
                           risk_is_greatest_when text NULL,
                           risk_increases_when text NULL,
                           risk_decreases_when text NULL,
                           additional_info text NULL,
                           current_psych_treatment varchar(4000),
                           postal_address_id uuid NULL,
                           date_of_birth date NULL,
                           prison_number varchar(50),
                           work_address_id uuid NULL,
                           basic_details_saved boolean NULL,
                           information_saved boolean NULL,
                           treatment_saved boolean NULL,
                           sign_and_send_saved boolean NULL,
                           contact_saved boolean NULL,
                           review_required_date timestamp without time zone NULL,
                           review_event varchar(100) NULL,
                           last_updated_datetime timestamp without time zone not NULL,
                           last_updated_user varchar(100) not NULL,
                           created_by_user varchar(100) not NULL,
                           created_datetime timestamp without time zone not NULL);

CREATE TABLE public.address(id uuid not null primary Key,
                     address_id bigint not null,
                     status varchar(100) NULL,
                     office_description varchar(50) NULL,
                     building_name varchar(35) NULL,
                     address_number varchar(35) NULL,
                     street_name varchar(35) NULL,
                     district varchar(35) NULL,
                     town_city varchar(35) NULL,
                     county varchar(35) NULL,
                     postcode varchar(8) NULL,
                     created_by_user varchar(100) not null,
                     created_datetime timestamp without time zone not null,
                     last_updated_user varchar(100) not null,
                     last_updated_datetime timestamp without time zone not null);

CREATE TABLE public.contact(id uuid not null primary key,
                            suicide_risk_id uuid not null,
                            contact_type_description varchar(200) NULL,
                            contact_person varchar(200),
                            contact_location_id uuid NULL,
                            form_sent boolean NULL,
                            created_by_user varchar(100) not null,
                            created_datetime timestamp without time zone NULL,
                            last_updated_user varchar(100) not null,
                            last_updated_datetime timestamp without time zone NULL);

ALTER TABLE public.suicide_risk ADD CONSTRAINT xfk1_suicide_risk_postal_address
    FOREIGN KEY (postal_address_id) REFERENCES public.address(id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE public.suicide_risk ADD CONSTRAINT xfk2_suicide_risk_work_address
    FOREIGN KEY (work_address_id) REFERENCES public.address(id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE public.contact ADD CONSTRAINT xfk3_contact_suicide_risk
    FOREIGN KEY (suicide_risk_id) REFERENCES public.suicide_risk(id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE public.contact ADD CONSTRAINT xfk4_contact_locaation_address
    FOREIGN KEY (contact_location_id) REFERENCES public.suicide_risk(id) ON DELETE No Action ON UPDATE No Action;