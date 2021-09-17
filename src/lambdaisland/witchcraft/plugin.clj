(ns lambdaisland.witchcraft.plugin
  (:require [nrepl.cmdline :as nrepl]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [lambdaisland.classpath :as cp]))

(def instance (atom nil))

(def config-file (io/file "plugins/witchcraft.edn"))

(defn read-config []
  (read-string (slurp config-file)))

(defn pprint-str [o]
  (with-out-str (pprint/pprint o)))

(defn on-enable [plugin]
  (reset! instance plugin)
  (when-not (.exists config-file)
    (.info (.getLogger plugin) (str "No " config-file " found, creating default."))
    (.mkdirs (io/file (.getParent config-file)))
    (spit config-file (slurp (io/resource "witchcraft_plugin/default_config.edn.tmpl"))))

  (let [{:keys [nrepl init deps]} (read-config)]
    (when (and deps (not (.exists (io/file "deps.edn"))))
      (.info (.getLogger plugin) (str "No deps.edn found, creating default."))
      (spit "deps.edn" (slurp (io/resource "witchcraft_plugin/default_deps.edn.tmpl"))))

    (when deps
      (.info (.getLogger plugin) (str "Loading deps.edn" (when (map? deps) (str " with " (pr-str deps)))))
      @(cp/update-classpath! (if (map? deps) deps {})))

    (.config (.getLogger plugin) (str "Classpath:\n" (pprint-str (cp/classpath-chain))))

    (future
      (nrepl/dispatch-commands nrepl))

    (doseq [form init]
      (.info (.getLogger plugin) (str "init: " (pr-str form)))
      (try
        (eval form)
        (catch Throwable e
          (.log (.getLogger plugin)
                java.util.logging.Level/WARNING
                (str "Init form failed to evaluate: " (pr-str form))
                e))))))

(defn on-disable [plugin]
  (reset! instance nil))
