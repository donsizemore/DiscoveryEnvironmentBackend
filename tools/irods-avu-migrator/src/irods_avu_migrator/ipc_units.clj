(ns irods-avu-migrator.ipc-units
  (:use [korma.core])
  (:require [irods-avu-migrator.db :as db]
            [kameleon.uuids :as uuids]
            [korma.db :refer [with-db]]))

(defn- remove-irods-ipc-units
  []
  (with-db db/icat
    (update :r_meta_main
            (set-fields {:meta_attr_unit nil})
            (where {:meta_attr_unit "ipc_user_unit_tag"}))))

(defn convert-ipc-units
  [options]
  (println "Setting AVU units ipc_user_unit_tag to NULL")
  (db/connect-icat options)
  (remove-irods-ipc-units)
  (println "Done setting AVU units ipc_user_unit_tag to NULL"))
