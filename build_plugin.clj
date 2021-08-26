(ns build-plugin
  (:require [clojure.tools.build.api :as b]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(def lib 'com.lambdaisland/witchcraft-plugin)
(def version (format "0.0.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def jar-file )

(defn clean [params]
  (b/delete {:path "target"})
  params)

;; clj-yaml misbehaves with Glowstone on the classpath, because Glowstone
;; vendors an old version of SnakeYaml. For our needs this is good enough.
(defn yaml-str [m]
  (apply str
         (map (fn [[k v]]
                (str (name k) ": " (pr-str v) "\n")) m)))

(defn plugin-yml [{:keys [target-dir api-version]}]
  (spit (io/file target-dir "plugin.yml")
        (yaml-str
         {:main "com.lambdaisland.witchcraft.ClojurePlugin"
          :name "Witchcraft"
          :version version
          :author "lambdaisland"
          :description "Bootstrap Clojure/nREPL/Witchcraft"
          :api-version api-version})))

(defn build [{:keys [env api-version server] :as params
              :or {api-version "1.17"
                   server 'paper}}]
  (let [basis (b/create-basis {:project "deps.edn"
                               :aliases [(case server
                                           paper
                                           :mc/paper-api
                                           glowstone
                                           :mc/glowstone)]})
        jar-file (format "target/%s-%s-for-%s-%s.jar"
                         (name lib)
                         version
                         server
                         api-version)]
    (b/write-pom {:class-dir class-dir
                  :lib lib
                  :version version
                  :basis basis
                  :src-dirs ["src"]})
    (b/javac {:src-dirs ["java"]
              :class-dir class-dir
              :basis basis})
    (b/copy-dir {:src-dirs ["src"]
                 :target-dir class-dir})
    (plugin-yml {:target-dir class-dir :api-version api-version})
    (b/uber {:class-dir class-dir
             :uber-file jar-file
             :basis (b/create-basis {:project "deps.edn"
                                     :aliases [:licp :nrepl]})}))
  params)

(defn build-all [& _]
  (build '{:server glowstone :api-version 1.12})
  (build '{:server paper :api-version 1.17}))
