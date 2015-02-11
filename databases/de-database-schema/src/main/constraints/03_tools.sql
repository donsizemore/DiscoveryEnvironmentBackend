SET search_path = public, pg_catalog;

--
-- Other constraints for this table are located in the 99_constraints.sql file.
--

--
-- Foreign key into the container_settings table from the tools table.
--
ALTER TABLE ONLY tools
    ADD CONSTRAINT tools_container_settings_fkey
    FOREIGN KEY(container_settings_id)
    REFERENCES container_settings(id)
