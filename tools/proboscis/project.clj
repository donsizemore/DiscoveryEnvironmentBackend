(use '[clojure.java.shell :only (sh)])
(require '[clojure.string :as string])

(defn git-ref
  []
  (or (System/getenv "GIT_COMMIT")
      (string/trim (:out (sh "git" "rev-parse" "HEAD")))
      ""))

(defproject org.iplantc/proboscis "3.2.0"
  :description "A utility for creating an ElasticSearch index and its mappings for Infosquito."
  :url "http://www.iplantcollaborative.org"
  :license {:name "BSD License"
            :url "http://iplantcollaborative.org/sites/default/files/iPLANT-LICENSE.txt"}
  :scm {:connection "scm:git:git@github.com:iPlantCollaborativeOpenSource/proboscis.git"
        :developerConnection "scm:git:git@github.com:iPlantCollaborativeOpenSource/proboscis.git"
        :url "git@github.com:iPlantCollaborativeOpenSource/proboscis.git"}
  :manifest {"Git-Ref" ~(git-ref)}
  :pom-addition [:developers
                 [:developer
                  [:url "https://github.com/orgs/iPlantCollaborativeOpenSource/teams/iplant-devs"]]]
  :classifiers [["javadoc" :javadoc]
                ["sources" :sources]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [cheshire "5.3.1"]
                 [clojurewerkz/elastisch "2.0.0"]
                 [slingshot "0.10.3"]]
  :resource-paths ["config"]
  :plugins [[org.iplantc/lein-iplant-cmdtar "3.2.0"]]
  :repositories [["sonatype-nexus-snapshots"
                  {:url "https://oss.sonatype.org/content/repositories/snapshots"}]]
  :deploy-repositories [["sonatype-nexus-staging"
                         {:url "https://oss.sonatype.org/service/local/staging/deploy/maven2/"}]]
  :aot [proboscis.core]
  :main proboscis.core)