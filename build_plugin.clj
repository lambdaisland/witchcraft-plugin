(ns build-plugin
  (:require [clojure.tools.build.api :as b]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(def lib 'com.lambdaisland/witchcraft-plugin)
(def version (format "0.0.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"
                            :aliases [:yaml :mc/paper-api :cider/nrepl]}))
(def jar-file (format "target/%s-%s.jar" (name lib) version))

(defn clean [params]
  (b/delete {:path "target"})
  params)

;; clj-yaml misbehaves with Glowstone on the classpath, because Glowstone
;; vendors an old version of SnakeYaml. For our needs this is good enough.
(defn yaml-str [m]
  (apply str
         (map (fn [[k v]]
                (str (name k) ": " (pr-str v) "\n")) m)))

(defn plugin-yml [{:keys [target-dir]}]
  (spit (io/file target-dir "plugin.yml")
        (yaml-str
         {:main "com.lambdaisland.witchcraft.ClojurePlugin"
          :name "Witchcraft"
          :version "0.1"
          :author "lambdaisland"
          :description "Bootstrap Clojure/nREPL/Witchcraft"
          :api-version "1.17"})))

(defn build [{:keys [env] :as params}]
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
  (plugin-yml {:target-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file jar-file
           :basis (b/create-basis {:project "deps.edn"
                                   :aliases [:witchcraft  :cider/nrepl]})})
  params)
