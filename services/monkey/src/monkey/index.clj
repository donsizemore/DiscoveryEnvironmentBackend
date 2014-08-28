(ns monkey.index
  "This namespace implements the Indexes protocol where elastisch library is used to interface with
   the search index."
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.elastisch.query :as query]
            [clojurewerkz.elastisch.rest :as es]
            [clojurewerkz.elastisch.rest.bulk :as bulk]
            [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.response :as resp]
            [monkey.props :as props])
  (:import [clojure.lang ISeq PersistentArrayMap]))


(defn- init-tag-seq
  [props es]
  (let [res (doc/search es (props/es-index props) (props/es-tag-type props)
              :query       (query/match-all)
              :fields      ["_id"]
              :search_type "scan"
              :scroll      (props/es-scroll-timeout props)
              :size        (props/es-scroll-size props))]
    (if (resp/any-hits? res)
      (doc/scroll es (:_scroll_id res) :scroll (props/es-scroll-timeout props))
      res)))


(defn- log-failures
  [res op log-failure]
  (let [fails (->> (:items res)
                (map op)
                (remove #(and (>= 200 (:status %)) (< 300 (:status %)))))]
    (doseq [fail fails]
      (log-failure (:id fail)))))


(defn- log-index-failure
  [id]
  (log/warn "Unable to index the document for the tag" id))


(defn- log-remove-failure
  [id]
  (log/warn "Unable to remove the indexed document for the tag" id))


(defprotocol Indexes
  "This protocol defines the operations needed to interact with the data search index."

  (^ISeq all-tags [_]
    "returns a sequence of all of the ids for the tag documents in the search index")

  (index-tags [_ ^ISeq tags]
    "adds the provided tag documents to the search index")

  (remove-tags [_ ^ISeq ids]
    "Removes the tags with the provided ids from the search index"))


(deftype ^{:private true} Index [props es]
  Indexes

  (all-tags [_]
    (map :_id (doc/scroll-seq es (init-tag-seq props es))))

  (index-tags [_ tags]
    (try
      (let [resp (bulk/bulk-with-index-and-type es
                                                (props/es-index props)
                                                (props/es-tag-type props)
                                                (bulk/bulk-index tags))]
        (log-failures resp :index log-index-failure))
      (catch Throwable t
        (log/debug t "failed to index tags")
        (doseq [tag tags]
          (log-index-failure (:id tag))))))

  (remove-tags [_ ids]
    (try
      (let [resp (bulk/bulk-with-index-and-type es
                                                (props/es-index props)
                                                (props/es-tag-type props)
                                                (bulk/bulk-delete ids))]
        (log-failures resp :delete log-remove-failure))
      (catch Throwable t
        (log/debug t "failed to remove tags")
        (doseq [id ids]
          (log-remove-failure id))))))


(defn ^Indexes mk-index
  "creates the object used to interact with the search index

   Parameters:
     props - The configuration properties map

   Returns:
     It returns the object."
  [^PersistentArrayMap props]
  (->Index props
           (es/connect (str (props/es-url props)))))