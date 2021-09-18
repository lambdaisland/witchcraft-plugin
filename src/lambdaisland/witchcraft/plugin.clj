(ns lambdaisland.witchcraft.plugin
  (:require [nrepl.cmdline :as nrepl]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [lambdaisland.classpath :as cp]))

(def instance (atom nil))

(def config-file (io/file "plugins/witchcraft.edn"))

(defn read-config []
  (read-string (slurp config-file)))

(defn pprint-str [o]
  (with-out-str (pprint/pprint o)))

(defn plugin ^org.bukkit.plugin.java.JavaPlugin []
  @instance)

(defn logger ^java.util.logging.Logger []
  (.getLogger (plugin)))

(defn log-info [& args] (.info (logger) (str/join " " args)))
(defn log-config [& args] (.config (logger) (str/join " " args)))
(defn log-severe [& args] (.severe (logger) (str/join " " args)))
(defn log-warn [& args] (.warning (logger) (str/join " " args)))

(defn log-error [^Throwable t & args]
  (log-severe (str/join " " args))
  (log-severe (.getName (.getClass t)) ":" (.getMessage t))
  (run! log-severe (str/split
                    (with-out-str
                      (.printStackTrace t))
                    #"\R")))

(defn on-enable [plugin]
  (reset! instance plugin)
  (when-not (.exists config-file)
    (log-info "No" config-file "found, creating default.")
    (.mkdirs (io/file (.getParent config-file)))
    (spit config-file (slurp (io/resource "witchcraft_plugin/default_config.edn"))))

  (let [{:keys [nrepl init deps] :as config} (read-config)]
    (when (and deps (not (.exists (io/file "deps.edn"))))
      (log-info "No deps.edn found, creating default.")
      (spit "deps.edn" (slurp (io/resource "witchcraft_plugin/default_deps.edn"))))

    (when deps
      (log-info "Loading deps.edn" (when (map? deps) (str "with " (pr-str deps))))
      @(cp/update-classpath! (if (map? deps) deps {})))

    (log-config "Classpath:\n" (pprint-str (cp/classpath-chain)))

    (future
      (try
        (nrepl/dispatch-commands nrepl)
        (log-info "nREPL exited.")
        (catch Throwable t
          (log-error t "nREPL failed to start ar exited abnormally"))))

    (doseq [ns-name (:require config)]
      (log-info "require:" ns-name)
      (try
        (require ns-name)
        (catch Throwable e
          (log-error e "Require namespace failed:" ns-name))))

    (doseq [form init]
      (log-info "init:" (pr-str form))
      (try
        (eval form)
        (catch Throwable e
          (log-error e "Init form failed to evaluate:" (pr-str form)))))))

(defn on-disable [plugin]
  (reset! instance nil))
