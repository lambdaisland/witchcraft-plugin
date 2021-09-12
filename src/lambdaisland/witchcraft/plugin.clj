(ns lambdaisland.witchcraft.plugin
  (:require [nrepl.cmdline :as nrepl]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [lambdaisland.classpath :as cp]))

(def instance (atom nil))

(def config-file (io/file "plugins/witchcraft.edn"))

(def default-config
  '{:nrepl {:port 25555
            :middleware [refactor-nrepl.middleware/wrap-refactor
                         cider.nrepl/cider-middleware
                         lambdaisland.witchcraft.nrepl.task-eval/wrap-eval]}
    :init [(require 'lambdaisland.witchcraft 'lambdaisland.witchcraft.paper)
           (lambdaisland.witchcraft/init-xmaterial!)
           (alter-var-root #'lambdaisland.witchcraft/server-type (constantly :paper))]
    :deps true})

(def default-deps-edn
  '{:deps
    {com.lambdaisland/witchcraft {:git/url "https://github.com/lambdaisland/witchcraft"
                                  :git/sha "4216141c738c54f1e6c622db23e787228aba1959"}
     refactor-nrepl/refactor-nrepl {:mvn/version "2.5.1"}
     cider/cider-nrepl             {:mvn/version "0.26.0"}}})

(defn read-config []
  (read-string (slurp config-file)))

(defn pprint-str [o]
  (with-out-str (pprint/pprint o)))

(defn on-enable [plugin]
  (reset! instance plugin)
  (when-not (.exists config-file)
    (.info (.getLogger plugin) (str "No " config-file " found, creating default."))
    (.mkdirs (io/file (.getParent config-file)))
    (spit config-file (pprint-str default-config)))

  (let [{:keys [nrepl init deps]} (read-config)]
    (when (and deps (not (.exists (io/file "deps.edn"))))
      (.info (.getLogger plugin) (str "No deps.edn found, creating default."))
      (spit "deps.edn" (pprint-str default-deps-edn)))

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
