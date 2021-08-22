(ns lambdaisland.witchcraft.plugin
  (:require [nrepl.cmdline :as nrepl]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [lambdaisland.witchcraft :as wc]))

(def config-file (io/file "plugins/witchcraft.edn"))

(def default-config
  '{:nrepl {:port 25555
            :middleware
            [refactor-nrepl.middleware/wrap-refactor
             cider.nrepl/cider-middleware]}
    :init [(println " --  🪄 W I T C H C R A F T 🪄   -- ")
           (/ 0 0)]})

(defn read-config []
  (read-string (slurp config-file)))

(defn pprint-str [o]
  (with-out-str (pprint/pprint o)))

(defn on-enable [plugin]
  (when-not (.exists config-file)
    (.info (.getLogger plugin) (str "No " config-file " found, creating default."))
    (.mkdirs (io/file (.getParent config-file)))
    (spit config-file (pprint-str default-config)))

  (wc/init-xmaterial!)

  (let [{:keys [nrepl init]} (read-config)]
    (future
      (nrepl/dispatch-commands
       (update nrepl :middleware (partial map symbol))))
    (doseq [form init]
      (.info (.getLogger plugin) (str "init: " (pr-str form)))
      (try
        (eval form)
        (catch Throwable e
          (.log (.getLogger plugin)
                java.util.logging.Level/WARNING
                (str "Init form failed to evaluate: " (pr-str form))
                e))))))

(defn on-disable [plugin])
