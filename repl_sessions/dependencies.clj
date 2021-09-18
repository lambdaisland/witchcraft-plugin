(ns repl-sessions.dependencies
  (:require [lambdaisland.classpath :as licp]))

(licp/git-pull-lib 'com.lambdaisland/classpath)
(licp/git-pull-lib 'com.lambdaisland/witchcraft)
(licp/git-pull-lib 'io.github.clojure/tools.build)

(licp/classpath-chain)
