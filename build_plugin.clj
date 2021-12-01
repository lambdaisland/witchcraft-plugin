(ns build-plugin
  (:require [clojure.tools.build.api :as b]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [shade.core :as shade]
            [lambdaisland.classpath :as licp])
  (:import java.nio.file.FileSystems
           java.nio.file.Path
           java.nio.file.Paths
           java.nio.file.Files
           java.nio.file.FileVisitOption
           java.nio.file.StandardOpenOption
           java.nio.charset.StandardCharsets
           java.net.URI))

(def lib 'com.lambdaisland/witchcraft-plugin)
(def version (str (slurp ".VERSION_PREFIX") "." (b/git-count-revs nil)))
(def class-dir "target/classes")

(def shadings
  {"org.eclipse.aether" "com.lambdaisland.shaded.org.eclipse.aether"
   "org.apache.maven" "com.lambdaisland.shaded.org.apache.maven"
   "org.codehaus.plexus" "com.lambdaisland.shaded.org.codehaus.plexus"})

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

(defn shade-jar [in out]
  ;; java
  (shade/shade in out shadings)

  ;; clojure
  (with-open [zipfs (FileSystems/newFileSystem ^Path (Paths/get out (into-array String [])))] ;; Requires JDK 12
    (let [paths (iterator-seq (.iterator (Files/walk (first (.getRootDirectories zipfs)) (make-array FileVisitOption 0))))]
      (doseq [path paths
              :when (re-find #"\.cljc?$" (str path))]
        (let [txt (slurp (.toUri path))
              shaded (reduce
                      (fn [txt [from to]]
                        (str/replace txt from to))
                      txt
                      shadings)]
          (when (not= shaded txt)
            (Files/write path
                         (.getBytes shaded StandardCharsets/UTF_8)
                         (into-array StandardOpenOption
                                     [StandardOpenOption/TRUNCATE_EXISTING]))))))))

(defn witchcraft-coords []
  #_  (licp/git-pull-lib 'com.lambdaisland/witchcraft)
  (get-in (read-string (slurp "deps.edn"))
          [:aliases :witchcraft :extra-deps 'com.lambdaisland/witchcraft]))

(defn build [{:keys [env api-version server] :as params
              :or {api-version "1.17"
                   server 'paper}}]
  (let [basis (b/create-basis {:project "deps.edn"
                               :aliases [(keyword "mc" (str server "-" api-version))]})
        jar-file (format "target/%s-%s-for-%s-%s.jar"
                         (name lib)
                         version
                         server
                         api-version)]
    (.mkdirs (io/file class-dir "witchcraft_plugin"))
    (spit (io/file class-dir "witchcraft_plugin" "default_config.edn")
          (slurp (io/resource "witchcraft_plugin/default_config.edn.tmpl")))
    (spit (io/file class-dir "witchcraft_plugin" "default_deps.edn")
          (str/replace
           (slurp (io/resource "witchcraft_plugin/default_deps.edn.tmpl"))
           "{{witchcraft-coords}}"
           (binding [*print-namespace-maps* false]
             (pr-str (witchcraft-coords)))))
    (b/write-pom {:class-dir class-dir
                  :lib lib
                  :version version
                  :basis basis
                  :src-dirs ["src"]})
    (b/javac {:src-dirs ["java"]
              :class-dir class-dir
              :basis basis
              :javac-opts ["-source" "11" "-target" "11"]})
    (b/copy-dir {:src-dirs ["src"]
                 :target-dir class-dir})
    (plugin-yml {:target-dir class-dir :api-version api-version})
    (b/uber {:class-dir class-dir
             :uber-file jar-file
             :basis (b/create-basis {:project "deps.edn"
                                     :aliases [:licp :nrepl]})})
    (shade-jar jar-file (str/replace jar-file ".jar" "-shaded.jar")))
  params)

(defn build-all [& _]
  (build '{:server glowstone :api-version 1.12})
  (build '{:server paper :api-version 1.17})
  (build '{:server paper :api-version 1.18})
  (build '{:server spigot :api-version 1.17}))
